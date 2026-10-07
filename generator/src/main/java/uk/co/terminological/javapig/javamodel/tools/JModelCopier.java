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

/**
 * Base class for cloning a {@link JProject} metamodel into a new instance.
 *
 * <p>Concrete subclasses override the {@code map} methods to provide deep-copy
 * semantics for custom subclasses of model components. The default implementation
 * delegates to each component's {@code copy()} method, producing a shallow copy.</p>
 *
 * <p>This class is used by {@link uk.co.terminological.javapig.javamodel.JModelAdaptor#adapt(Project)}
 * to create adapted copies of the model before template rendering.</p>
 *
 * @see JModelVisitor
 * @see JModelAdaptor
 */
public abstract class JModelCopier {

	/** The cloned project, built during the copy operation. */
	JProject out;
	
	/**
	 * Converts a project (which may be a JProject or a Project interface) into a JProject.
	 *
	 * @param model the project to convert
	 * @return a cloned JProject
	 */
	public JProject adapt(Project model) {
		return convert((JProject) model);
	}
	
	/**
	 * Clones an entire project metamodel, recursively copying all packages,
	 * classes, and methods.
	 *
	 * @param in the project to clone
	 * @return a new JProject with all elements copied
	 */
	public JProject convert(JProject in) {
		JProject out = map(in);
		in.getPackages().stream().forEach(p -> out.addPackage(convert(p)));
		in.getClasses().stream().forEach(c -> out.addInterface(convert(c)));
		in.getMethods().stream().forEach(m -> out.addMethod(convert(m)));
		return out;
	}

	/**
	 * Clones a package, copying its metadata and annotations.
	 *
	 * @param in the package to clone
	 * @return a new JPackage
	 */
	public JPackage convert(JPackage in) {
		JPackage out = map(in);
		out.setMetadata(map(in.getMetadata()));
		out.setAnnotations(in.getAnnotations().stream().map(a -> convert(a)).collect(Collectors.toList()));
		return out;
	}

	/**
	 * Clones package metadata by delegating to the metadata's {@code copy()} method.
	 *
	 * @param in the metadata to clone
	 * @return a new JPackageMetadata
	 */
	public JPackageMetadata convert(JPackageMetadata in) {
		JPackageMetadata out = map(in);
		return out;
	}
	
	/**
	 * Clones an interface, copying its annotations.
	 *
	 * @param in the interface to clone
	 * @return a new JInterface
	 */
	public JInterface convert(JInterface in) {
		JInterface out = map(in);
		out.setAnnotations(in.getAnnotations().stream().map(a -> convert(a)).collect(Collectors.toList()));
		return out;
	}
	
	/**
	 * Clones a method, copying its annotations.
	 *
	 * @param in the method to clone
	 * @return a new JGetMethod
	 */
	public JGetMethod convert(JGetMethod in) {
		JGetMethod out = map(in);
		out.setAnnotations(in.getAnnotations().stream().map(a -> convert(a)).collect(Collectors.toList()));
		return out;
	}
	
	/**
	 * Clones an annotation, recursively copying its entries.
	 *
	 * @param in the annotation to clone
	 * @return a new JAnnotation
	 */
	public JAnnotation convert(JAnnotation in) {
		JAnnotation out = map(in);
		out.setEntries(in.getEntries().stream().map(c -> convert(c)).collect(Collectors.toList()));
		return out;
	}
	
	/**
	 * Clones an annotation entry, copying its values.
	 *
	 * @param in the entry to clone
	 * @return a new JAnnotationEntry
	 */
	public JAnnotationEntry convert(JAnnotationEntry in) {
		JAnnotationEntry out = map(in);
		out.setValues(in.getValues().stream().map(c -> map(c)).collect(Collectors.toList()));
		return out;
	}
	
	/**
	 * Maps a JProject to its copy. Override for deep-copy of custom subclasses.
	 *
	 * @param in the project to map
	 * @return a copy of the project
	 */
	public JProject map(JProject in) {return in.copy();}
	
	/**
	 * Maps a JPackage to its copy. Override for deep-copy of custom subclasses.
	 *
	 * @param in the package to map
	 * @return a copy of the package
	 */
	public JPackage map(JPackage in) {return in.copy();}
	
	/**
	 * Maps JPackageMetadata to its copy.
	 *
	 * @param in the metadata to map
	 * @return a copy of the metadata
	 */
	public JPackageMetadata map(JPackageMetadata in) {return in.copy();}
	
	/**
	 * Maps a JInterface to its copy. Override for deep-copy of custom subclasses.
	 *
	 * @param in the interface to map
	 * @return a copy of the interface
	 */
	public JInterface map(JInterface in) {return in.copy();}
	
	/**
	 * Maps a JGetMethod to its copy. Override for deep-copy of custom subclasses.
	 *
	 * @param in the method to map
	 * @return a copy of the method
	 */
	public JGetMethod map(JGetMethod in) {return in.copy();}
	
	/**
	 * Maps a JAnnotation to its copy. Override for deep-copy of custom subclasses.
	 *
	 * @param in the annotation to map
	 * @return a copy of the annotation
	 */
	public JAnnotation map(JAnnotation in) {return in.copy();}
	
	/**
	 * Maps a JAnnotationEntry to its copy. Override for deep-copy of custom subclasses.
	 *
	 * @param in the entry to map
	 * @return a copy of the entry
	 */
	public JAnnotationEntry map(JAnnotationEntry in) {return in.copy();}
	
	/**
	 * Maps a JAnnotationValue to its copy.
	 *
	 * @param in the annotation value to map
	 * @return a copy of the annotation value
	 */
	public JAnnotationValue<?> map(JAnnotationValue<?> in) {return in.copy();}
	
}
