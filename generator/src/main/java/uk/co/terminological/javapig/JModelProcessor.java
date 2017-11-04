package uk.co.terminological.javapig;

import static javax.lang.model.SourceVersion.RELEASE_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.scanner.AptJModelBuilder;

@SupportedAnnotationTypes(value={"uk.co.terminological.javapig.annotations.Model"})
@SupportedSourceVersion(RELEASE_8)
public class JModelProcessor extends AbstractProcessor {

	private ProcessingEnvironment penv;
	private Filer filer;
	private FileObject model;
	private File modelFile;
	private JProject packages;

	/**
	 * The processing environment is initialised.
	 * This involves loading processing runs in previous compilation phases from file
	 * N.b. logging is done to System.out as there is no obvious way to make compiler 
	 * logging visible to the user in any other way.
	 */
	@Override
	public void init(ProcessingEnvironment penv) {
		this.penv = penv;
		this.filer = penv.getFiler();

		try {

			// The destination file of the model will be something like /generated-sources/annotation/working
			model = filer.getResource(StandardLocation.SOURCE_OUTPUT, "working", "model.ser");
			System.out.println("Loading meta model from: "+model.toUri());
			modelFile = new File(model.toUri());
			if (!modelFile.exists()) throw new IOException();

			try {

				FileInputStream fileIn = new FileInputStream(modelFile);
				ObjectInputStream in = new ObjectInputStream(fileIn);
				packages = (JProject) in.readObject();
				in.close();
				fileIn.close();



			} catch (ClassNotFoundException e) {

				throw new RuntimeException("Could not understand jmodel: "+modelFile.getAbsolutePath(),e);

			} catch (IOException e) {

				System.out.println("Meta-model file was found but seems to be corrupt. Replacing");			
				packages = new JProject();

			} 

		} catch (IOException e) {

			System.out.println("No model file found. Probably mvn clean has been called");
			packages = new JProject();	

		}

		System.out.println("Processor initialised and ready to start working");
	}

	/**
	 * Uses the AptJModelBuilder
	 * @see javax.annotation.processing.AbstractProcessor#process(java.util.Set, javax.annotation.processing.RoundEnvironment)
	 */
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

		System.out.println("processing...");

		AptJModelBuilder ms = new AptJModelBuilder(packages, penv);

		// the model scanner will update the PackagesList model.
		// the ECJ compiler seems to sometimes only process TypeElements
		// that have changed during an incremental compile.

		Set<? extends Element> scans = env.getRootElements();
		
		scans.stream()
			.filter(s -> s.getKind().equals(ElementKind.PACKAGE))
			.forEach(p -> {
				System.out.println("inspecting package: "+p.getSimpleName());
				ms.scan(p);
			});
	
		
		scans.stream()
			.filter(s -> s.getKind().equals(ElementKind.INTERFACE) || s.getKind().equals(ElementKind.CLASS))
			.flatMap(s -> 
				s.getEnclosingElement()
				.getEnclosedElements().stream())
			.distinct()
			.forEach(element -> {
				System.out.println("inspecting class/interface: "+element.getSimpleName());
				ms.scan(element);
		});

		// save the updated PackagesList model

		try {
			modelFile.delete();
			modelFile.getParentFile().mkdirs();

			FileOutputStream fos = new FileOutputStream(modelFile);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(packages);
			out.close();
			fos.close();
			System.out.println("writing class model to: "+modelFile);

		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("failed to write freemarker class model");
		}


		// Use the updated PackagesList model to generate code based on 
		// the freemarker templates. This is done both on a package by package 
		// basis using the PackageTemplates, and on a class by class basis
		// using ClassTemplates. This is done based on the annotations held in 
		// the @Model(@Template) annotation
		
		JModelWriter writer = new JModelWriter();
		writer.setTargetDirectory(modelFile.getParentFile().getParentFile());
		writer.setModel(ms.getModel());
		writer.setFiler(filer);
		writer.write();
		return false;

	}

}
