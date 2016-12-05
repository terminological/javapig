package uk.co.terminological.javapig.javamodel.tools;

import java.util.stream.Collectors;

import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.Project;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;

public abstract class JModelCopier {

	JProject out;
	
	public JProject adapt(Project model) {
		return convert((JProject) model);
	}
	
	public JProject convert(JProject in) {
		JProject out = map(in);
		in.getPackages().stream().forEach(p -> out.addPackage(convert(p)));
		in.getClasses().stream().forEach(c -> out.addInterface(convert(c)));
		in.getMethods().stream().forEach(m -> out.addMethod(convert(m)));
		return out;
	}

	public JPackage convert(JPackage in) {
		JPackage out = map(in);
		out.setMetadata(map(in.getMetadata()));
		out.setAnnotations(in.getAnnotations().stream().map(a -> convert(a)).collect(Collectors.toList()));
		return out;
	}

	public JPackageMetadata convert(JPackageMetadata in) {
		JPackageMetadata out = map(in);
		return out;
	}
	
	public JInterface convert(JInterface in) {
		JInterface out = map(in);
		out.setAnnotations(in.getAnnotations().stream().map(a -> convert(a)).collect(Collectors.toList()));
		return out;
	}
	
	public JGetMethod convert(JGetMethod in) {
		JGetMethod out = map(in);
		out.setAnnotations(in.getAnnotations().stream().map(a -> convert(a)).collect(Collectors.toList()));
		return out;
	}
	
	public JAnnotation convert(JAnnotation in) {
		JAnnotation out = map(in);
		out.setEntries(in.getEntries().stream().map(c -> convert(c)).collect(Collectors.toList()));
		return out;
	}
	
	public JAnnotationEntry convert(JAnnotationEntry in) {
		JAnnotationEntry out = map(in);
		out.setValues(in.getValues().stream().map(c -> map(c)).collect(Collectors.toList()));
		return out;
	}
	
	public JProject map(JProject in) {return in.copy();}
	public JPackage map(JPackage in) {return in.copy();}
	public JPackageMetadata map(JPackageMetadata in) {return in.copy();}
	public JInterface map(JInterface in) {return in.copy();}
	public JGetMethod map(JGetMethod in) {return in.copy();}
	public JAnnotation map(JAnnotation in) {return in.copy();}
	public JAnnotationEntry map(JAnnotationEntry in) {return in.copy();}
	public JAnnotationValue<?> map(JAnnotationValue<?> in) {return in.copy();}
	
	/*public static class Status<X extends JModelComponent> {
		X value;
		boolean carryOn;
	}*/

	
		
}
