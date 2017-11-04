package uk.co.terminological.javapig.test.model;





import java.io.File;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import com.google.common.io.Files;

import uk.co.terminological.javapig.csvloader.JavaFromCsvExecution;
import uk.co.terminological.javapig.csvloader.JavaFromCsvMojo;
import uk.co.terminological.javapig.csvloader.Type;

public class TestMavenPlugin {

	public static void main(String[] args) throws MojoExecutionException, MojoFailureException {
		BasicConfigurator.configure();
		Logger.getRootLogger().setLevel(Level.ALL);
		JavaFromCsvMojo mojo = new JavaFromCsvMojo();
		mojo.setTargetDirectory(Files.createTempDir());
		JavaFromCsvExecution exec = new JavaFromCsvExecution();
		exec.setFile(new File("src/test/resources/LABEVENTS_50.csv"));
		exec.setType(Type.CSV);
		exec.setTargetFQN("uk.co.terminological.javapig.examples.mimic.LabEvents");
		mojo.setJavaFromCsvExecutions(new JavaFromCsvExecution[] {exec});
		mojo.execute();
	}

}
