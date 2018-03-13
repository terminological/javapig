package uk.co.terminological.javapig.csvloader;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.persistence.Id;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.datatypes.FluentSet;
import uk.co.terminological.datatypes.Triple;
import uk.co.terminological.javapig.JModelWriter;
import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.scanner.ReflectionUtils;
import uk.co.terminological.mappers.ProxyMapWrapper;
import uk.co.terminological.mappers.StringCaster;
import uk.co.terminological.parser.ParserException;
import uk.co.terminological.parser.StateMachineExecutor.ErrorHandler;
import uk.co.terminological.tabular.Delimited;
import uk.co.terminological.tabular.Delimited.Content;

@Mojo( name = "javaFromCsv", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class JavaFromCsvMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		JProject proj = new JProject();
		ArrayList<JavaFromCsvExecution> executions = new ArrayList<>();
		if (baseDirectory != null && filenameFilter != null && defaultTargetPackage != null) {
			FileFilter filter = new WildcardFileFilter(this.filenameFilter);
			File[] inputs = this.baseDirectory.listFiles(filter);
			
			for (File input: inputs) {
				JavaFromCsvExecution execution = new JavaFromCsvExecution();
				execution.setFile(input);
				execution.setType(defaultType);
				execution.setTargetFQN(ProxyMapWrapper.className(defaultTargetPackage,input.getName()));
				executions.add(execution);
			}
	
			if (javaFromCsvExecutions != null) {
				executions.addAll(Arrays.asList(javaFromCsvExecutions));
			}
		}
		for (JavaFromCsvExecution f: executions) {
			try {

				System.out.println("JavaFromCSV Execution: "+ f.getFile().getCanonicalPath() + ": as "+ f.getTargetFQN());
				BufferedReader in = Files.newBufferedReader(f.getFile().toPath());
				
				// next section needed as large files not streamed correctly by Delimited - takes first 50 lines
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				PrintWriter out = new PrintWriter(baos);
				int i = 50;
				String line;
				while (i>0 && (line = in.readLine())!=null ) {
					out.println(line);
					//this will convert line ending to platform default.
					i--;
				}
				in.close();
				out.close();
				Reader head = new StringReader(new String(baos.toByteArray()));
				Delimited.Format d = Delimited.fromReader(head);
				Delimited.Content c = null;
				switch (f.getType()) {
				case CSV:
					c = d.csv();
					break;
				case MYSQL:
					c = d.separatedByEnclosedWith(",", "\"").parse(ErrorHandler.DEBUG);
					break;
				case PIPE_DELIM:
					c = d.pipe();
					break;
				case TSV:
					c = d.tsv();
					break;
				case CUSTOM:
				default:
					c = d.separatedByEnclosedWith(f.getSeperator(),f.getSeperator()
							).parse(ErrorHandler.DEBUG);
					break;
				}
				Content content = c.headerLabels().noIdentifiers();
				Delimited parser = content.begin();
				
				List<String> headers = parser.getHeaders();
				
				Map<String,Class<?>> methodSignatures = new LinkedHashMap<>();
				headers.stream().forEach(k -> methodSignatures.put(k, null));
				
				Iterator<Triple<Long,Integer,String>> tmp = parser.streamContentsByRow().iterator();
				
				boolean allDone = false;
				while (tmp.hasNext() && !allDone) {
					Triple<Long,Integer,String> tmpValue = tmp.next();
					if (tmpValue.value() != null && !tmpValue.value().isEmpty()) {
						Class<?> guess = StringCaster.guessType(tmpValue.value());
						String methodName = headers.get(tmpValue.attribute());
						Class<?> current = methodSignatures.get(methodName); 
						if (current != null) {
							if (!current.equals(guess)) {
								methodSignatures.put(methodName, String.class);
							} else {
								//no need to update.
							}
						} else {
							methodSignatures.put(methodName, guess);
						}
					}
					allDone = !methodSignatures.containsValue(null);
				}
				
				for (String key: methodSignatures.keySet()) {
					if (methodSignatures.get(key) == null) methodSignatures.put(key, String.class);
				}
				
				parser.close();
				
				String packageFQN = JClassName.from(f.getTargetFQN()).getPackageName();
				if (!proj.packageIsDefined(packageFQN)) {
					proj.addPackage(new JPackage(proj, "",
							FluentList.empty(), 
							packageFQN, 
							Optional.of(new JPackageMetadata(
									FluentList.create(BuiltIn.CORE),
									FluentList.empty()
									))));
				}
				proj.addInterface(new JInterface(
						proj,"",
						FluentList.create( 
								new JAnnotation(
										Optional.of(Csv.class.getCanonicalName()),
										"Csv",
										FluentList.create(
												new JAnnotationEntry(
														JMethodName.from(Csv.class.getCanonicalName()+"#value"),
														JAnnotationValue.of(f.getType()))
												))),
						JClassName.from(f.getTargetFQN()),
						FluentSet.empty()));
				int j=0;
				for (Entry<String,Class<?>> methodSignature : methodSignatures.entrySet()) {
					proj.addMethod(new JGetMethod(
							proj,"",
							FluentList.create( 
									new JAnnotation(
											Optional.of(ByField.class.getCanonicalName()),
											"ByField",
											FluentList.create(
													new JAnnotationEntry(
															JMethodName.from(ByField.class.getCanonicalName()+"#value"),
															JAnnotationValue.of(j))
													))),
							JClassName.from(f.getTargetFQN()),
							JMethodName.from(f.getTargetFQN()+"#"+ProxyMapWrapper.methodName(methodSignature.getKey())),
							methodSignature.getValue().getCanonicalName(),
							JClassName.from(ReflectionUtils.box(methodSignature.getValue()).getCanonicalName()),
							null,
							false
							));
					j++;
				}
				proj.addMethod(new JGetMethod(
						proj,"",
						FluentList.create(new JAnnotation(Optional.of(Id.class.getCanonicalName()),"Id",FluentList.empty())),
						JClassName.from(f.getTargetFQN()),
						JMethodName.from(f.getTargetFQN()+"#getId"),
						"java.lang.String",
						JClassName.from("java.lang.String"),
						null,false
						));
			} catch (ParserException e) {
				e.printStackTrace();
				throw new MojoFailureException(e.getLocalizedMessage());
			} catch (IOException e) {
				e.printStackTrace();
				throw new MojoFailureException(e.getLocalizedMessage());
			}
		}
		// Pass whole model to JModelWriter
		JModelWriter writer = new JModelWriter();
		writer.setTargetDirectory(targetDirectory);
		writer.setModel(proj);
		writer.writeSafe(); //TODO: THIS DOESN'T WORK but is ignored
	}

	@Parameter(required=false)
	File baseDirectory;

	@Parameter(required=false)
	String filenameFilter = "*.csv";

	@Parameter(required=false)
	String defaultTargetPackage;



	@Parameter(required=false)
	Type defaultType;

	@Parameter(required=false)
	JavaFromCsvExecution[] javaFromCsvExecutions;

	public JavaFromCsvExecution[] getJavaFromCsvExecutions() {
		return javaFromCsvExecutions;
	}

	public void setJavaFromCsvExecutions(JavaFromCsvExecution[] javaFromCsvExecutions) {
		this.javaFromCsvExecutions = javaFromCsvExecutions;
	}

	@Parameter(required=true)
	File targetDirectory;

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}




}
