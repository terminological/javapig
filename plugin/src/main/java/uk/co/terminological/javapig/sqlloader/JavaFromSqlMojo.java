package uk.co.terminological.javapig.sqlloader;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;

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

@Mojo( name = "javaFromSql", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class JavaFromSqlMojo extends AbstractMojo {

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {

		JProject proj = new JProject();
		ArrayList<JavaFromSqlExecution> executions = new ArrayList<>();

		Properties prop = null;
		Connection conn = null;

		try {

			prop =  new Properties();
			System.out.println("Properties file: "+this.connectionProperties.getAbsolutePath());
			prop.load(new FileInputStream(this.connectionProperties));

			System.out.println("Driver: "+prop.getProperty("driver"));
			System.out.println("Url: "+prop.getProperty("url"));
			
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop);

		} catch (Exception e) {
			throw new MojoExecutionException("exception setting up database connection: "+e.getLocalizedMessage(), e);
		}

		if (baseDirectory != null && filenameFilter != null && defaultTargetPackage != null) {
			FileFilter filter = new WildcardFileFilter(this.filenameFilter);
			File[] inputs = this.baseDirectory.listFiles(filter);

			for (File input: inputs) {
				try {
					JavaFromSqlExecution execution = new JavaFromSqlExecution();
					String sql = new String(Files.readAllBytes(input.toPath()));
					execution.setSql(sql);
					execution.setTargetFQN(ProxyMapWrapper.className(defaultTargetPackage,input.getName()));
					executions.add(execution);
				} catch (IOException e) {
					e.printStackTrace();
					throw new MojoFailureException(e.getLocalizedMessage());
				}
			}
		}

		if (javaFromSqlExecutions != null) {
			executions.addAll(Arrays.asList(javaFromSqlExecutions));
		}
		
		for (JavaFromSqlExecution f: executions) {


			System.out.println("JavaFromSQL Execution: "+ f.getTargetFQN() + ": from "+ f.getSql());

			String sql = f.getSql();
			Statement st = null;
			ResultSet rs = null;
			try {
				st = conn.createStatement();
				st.setMaxRows(1);
				rs = st.executeQuery(sql);

				Map<String,Class<?>> methodSignatures = SqlUtils.methodsFromResultSet(rs);

				//TODO methodSignatures from resultSetMetadata

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
										Optional.of(Sql.class.getCanonicalName()),
										"Sql",
										FluentList.create(
												new JAnnotationEntry(
														JMethodName.from(Sql.class.getCanonicalName()+"#value"),
														JAnnotationValue.of(f.getSql()))
												))),
						JClassName.from(f.getTargetFQN()),
						FluentSet.empty()));
				int j=0;
				for (Entry<String,Class<?>> methodSignature : methodSignatures.entrySet()) {
					proj.addMethod(new JGetMethod(
							proj,"",
							FluentList.create( 
									new JAnnotation(
											Optional.of(Column.class.getCanonicalName()),
											"Column",
											FluentList.create(
													new JAnnotationEntry(
															JMethodName.from(Column.class.getCanonicalName()+"#value"),
															JAnnotationValue.of(methodSignature.getKey()))
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
						JMethodName.from(f.getTargetFQN()+"#getRowNumber"),
						"java.lang.String",
						JClassName.from("java.lang.String"),
						null,false
						));
			} catch (SQLException e) {
				System.out.println("The SQL generated an error - skipping...");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				throw new MojoExecutionException("SQL datatypes are not found on the class path");
			} finally {
				if (rs != null) try { rs.close(); } catch(Exception e) {}  
				if (st != null) try { st.close(); } catch(Exception e) {}
			}
		}
		
		if (conn != null) try { conn.close(); } catch(Exception e) {} 
		
		// Pass whole model to JModelWriter
		JModelWriter writer = new JModelWriter();
		writer.setTargetDirectory(targetDirectory);
		writer.setModel(proj);
		writer.writeSafe(); //TODO: THIS DOESN'T WORK but is ignored


	}

	@Parameter(required=false)
	File baseDirectory;

	@Parameter(required=false)
	String filenameFilter = "*.sql";

	@Parameter(required=false)
	String defaultTargetPackage;

	@Parameter(required=true)
	File connectionProperties;

	@Parameter(required=false)
	JavaFromSqlExecution[] javaFromSqlExecutions;

	@Parameter(required=true)
	File targetDirectory;

	public File getBaseDirectory() {
		return baseDirectory;
	}

	public void setBaseDirectory(File baseDirectory) {
		this.baseDirectory = baseDirectory;
	}

	public File getConnectionProperties() {
		return connectionProperties;
	}

	public void setConnectionProperties(File connectionProperties) {
		this.connectionProperties = connectionProperties;
	}

	public String getDefaultTargetPackage() {
		return defaultTargetPackage;
	}

	public void setDefaultTargetPackage(String defaultTargetPackage) {
		this.defaultTargetPackage = defaultTargetPackage;
	}


	public JavaFromSqlExecution[] getJavaFromSqlExecutions() {
		return javaFromSqlExecutions;
	}

	public void setJavaFromSqlExecutions(JavaFromSqlExecution[] JavaFromSqlExecutions) {
		this.javaFromSqlExecutions = JavaFromSqlExecutions;
	}

	public File getTargetDirectory() {
		return targetDirectory;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.targetDirectory = targetDirectory;
	}




}
