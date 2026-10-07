package uk.co.terminological.javapig.javamodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Abstract base class for all elements in the javapig metamodel.
 *
 * <p>Each {@code JElement} holds a reference to its containing {@link JProject},
 * a list of {@link JAnnotation}s, and an optional JavaDoc comment. Concrete
 * subclasses include {@link JInterface}, {@link JGetMethod}, and {@link JPackage}.</p>
 *
 * <p>Annotation lookup methods support both class-based and string-based queries.</p>
 *
 * @see JProject
 * @see JInterface
 * @see JGetMethod
 * @see JAnnotation
 */
public abstract class JElement extends JProjectComponent {

	private JProject model;
	private List<JAnnotation> annotations = new ArrayList<>();
	private String javaDoc;
	
	/**
	 * Checks whether an annotation of the given type is present on this element.
	 *
	 * @param annotation the annotation class to look for
	 * @return {@code true} if the annotation is present
	 * @see #getAnnotation(Class)
	 */
	public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotation) {
		return getAnnotation(annotation) != null;
	}
	
	/**
	 * Checks whether an annotation whose name ends with the given string is present.
	 * This is a loose match useful for checking annotations where only the simple
	 * name is known.
	 *
	 * @param annotation the suffix of the annotation name to look for
	 * @return {@code true} if a matching annotation is found
	 */
	public <T extends Annotation> boolean isAnnotationPresent(String annotation) {
		return annotations.stream()
				.filter(a -> annotation.endsWith(a.getName()))
				.findAny().isPresent();
	}
	
	/**
	 * Returns the first annotation matching the given class type, or {@code null}
	 * if no matching annotation is present.
	 *
	 * @param annotation the annotation class to look for
	 * @return the annotation value, or {@code null}
	 */
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		Optional<JAnnotation> ann = annotations.stream()
				.filter(a -> annotation.getCanonicalName().endsWith(a.getName()))
				.findFirst().map(a -> (JAnnotation) a);
		if (!ann.isPresent()) return null;
		else {
			JAnnotation tmp = ann.get();
			return tmp.convert(annotation);
		}
	}
	
	/**
	 * Returns an annotation by its fully-qualified class name.
	 *
	 * @param annotation the fully-qualified annotation class name
	 * @return the annotation value
	 * @throws ClassNotFoundException if the annotation class cannot be loaded
	 */
	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(String annotation) throws ClassNotFoundException {
		return getAnnotation((Class<T>) Class.forName(annotation));
	}
	
	
	/**
	 * Constructs a new element in the given project with the specified annotations
	 * and JavaDoc.
	 *
	 * @param model the containing project
	 * @param javaDoc the JavaDoc comment
	 * @param annotations the list of annotations
	 */
	//JavaBean
	
	public JElement(JProject model, String javaDoc, List<JAnnotation> annotations) {
		this.model = model;
		this.javaDoc = javaDoc;
		this.annotations = annotations;
	}
	
	// getters and setters
	
	/**
	 * Returns the list of annotations attached to this element.
	 *
	 * @return the annotation list
	 */
	public List<JAnnotation> getAnnotations() {
		return annotations;
	}

	/**
	 * Sets the list of annotations on this element.
	 *
	 * @param annotations the new annotation list
	 */
	public void setAnnotations(List<JAnnotation> annotations) {
		this.annotations = annotations;
	}

	/**
	 * Returns the containing project metamodel.
	 *
	 * @return the JProject
	 */
	public JProject getModel() {
		return model;
	}

	/**
	 * Sets the containing project metamodel.
	 *
	 * @param model the project reference
	 */
	public void setModel(JProject model) {
		this.model = model;
	}

	/**
	 * Returns the JavaDoc comment for this element.
	 *
	 * @return the JavaDoc string, or {@code null}
	 */
	public String getJavaDoc() {
		return javaDoc;
	}

	/**
	 * Sets the JavaDoc comment for this element.
	 *
	 * @param javaDoc the JavaDoc string
	 */
	public void setJavaDoc(String javaDoc) {
		this.javaDoc = javaDoc;
	}
	
}
