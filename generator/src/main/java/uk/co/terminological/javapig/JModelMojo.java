package uk.co.terminological.javapig;

import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;
import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
import static org.twdata.maven.mojoexecutor.MojoExecutor.version;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.scanner.QDoxJModelBuilder;

@Mojo( name = "javaGenerator", defaultPhase = LifecyclePhase.GENERATE_SOURCES )
public class JModelMojo extends AbstractMojo {

	static Logger log = LoggerFactory.getLogger(JModelMojo.class);
	//static String SEP = File.separator;

	@Parameter(required=true)
	File[] sources;

	@Parameter(required=true)
	File targetDirectory;
	
	@Parameter( defaultValue = "${project}", readonly = true )
	private MavenProject mavenProject;

	@Parameter( defaultValue = "${session}", readonly = true )
	private MavenSession mavenSession;

	@Component
	private BuildPluginManager pluginManager;


	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		try {

			{
				// try once to build a model and generate the source files.
				// This should work but any interdependencies between the source and the
				// generated code will not be identified.
				System.out.println("First pass - scanning source code");
				JProject model = QDoxJModelBuilder.scanModel(sources, targetDirectory);
				JModelWriter writer = new JModelWriter();
				writer.setTargetDirectory(targetDirectory);
				writer.setModel(model);
				writer.write();
			}
			
			executeMojo(plugin(
					groupId("org.codehaus.mojo"),
					artifactId("build-helper-maven-plugin"),
					version("1.9.1")
					),
					goal("add-source"),
					configuration(
							element(name("sources"), 
									element(name("source"),targetDirectory.getAbsolutePath())
									)
							),
							executionEnvironment(
									mavenProject,
									mavenSession,
									pluginManager)

					);
			
			{
				// the second pass should deal with dependencies between the source and the
				// generated code.	
				System.out.println("Second pass - scanning source code");
				JProject model = QDoxJModelBuilder.scanModel(sources, targetDirectory);
				JModelWriter writer = new JModelWriter();
				writer.setTargetDirectory(targetDirectory);
				writer.setModel(model);
				writer.write();
				
				File modelFileObject = new File(targetDirectory, "working/model.ser");
				modelFileObject.getParentFile().mkdirs();
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(modelFileObject));
				out.writeObject(model);
				out.close();
				System.out.println("writing class model to: "+modelFileObject.getAbsolutePath());
			
			}
			
			

		} catch (Exception e) {
			e.printStackTrace();
			throw new MojoFailureException(e.getMessage());
		}
	}


}
