package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Represents a single annotation entry (name-value pair) within a {@link JAnnotation}.
 *
 * <p>An annotation entry pairs a {@link JMethodName} (identifying the annotation
 * attribute, e.g. {@code MyAnnotation#value}) with a list of {@link JAnnotationValue}
 * objects that hold the attribute's value(s).</p>
 *
 * @see JAnnotation
 * @see JAnnotationValue
 * @see JMethodName
 */
public class JAnnotationEntry extends JProjectComponent {

	/**
	 * Constructs a new annotation entry with the given attribute name and values.
	 *
	 * @param method the method/attribute name within the annotation (e.g. {@code MyAnnotation#value})
	 * @param values the values for this attribute
	 */
	public JAnnotationEntry(JMethodName method, List<JAnnotationValue<?>> values) {
		this.method = method;
		this.values = values;
	}
	
	/**
	 * Copy constructor that creates a shallow copy of the given entry.
	 * Note: the values list is replaced with a new empty list; callers must
	 * populate it manually or use {@link #clone()} for deep copy.
	 *
	 * @param copy the entry to copy
	 */
	public JAnnotationEntry(JAnnotationEntry copy) {
		this(
				copy.getMethod(),
				new ArrayList<>()
		);
	}

	private JMethodName method;
	private List<JAnnotationValue<?>> values = new ArrayList<>();
	
	/**
	 * Returns the annotation attribute name (e.g. {@code MyAnnotation#value}).
	 *
	 * @return the JMethodName representing the attribute
	 */
	public JMethodName getMethod() {
		return method;
	}

	/**
	 * Sets the annotation attribute name.
	 *
	 * @param method the JMethodName for the attribute
	 */
	public void setMethod(JMethodName method) {
		this.method = method;
	}

	/**
	 * Returns the list of values for this annotation attribute.
	 *
	 * @return the values list
	 */
	public List<JAnnotationValue<?>> getValues() {
		return values;
	}

	/**
	 * Sets the list of values for this annotation attribute.
	 *
	 * @param values the values list
	 */
	public void setValues(List<JAnnotationValue<?>> values) {
		this.values = values;
	}

	/**
	 * Creates a deep clone of this annotation entry by copying the values list.
	 *
	 * @return a new JAnnotationEntry with cloned values
	 */
	@Override
	public JAnnotationEntry clone() {
		JAnnotationEntry out = copy();
		out.setValues(this.getValues().stream().map(ae -> ae.clone()).collect(Collectors.toList()));
		return out;
	}

	/**
	 * Creates a shallow copy of this annotation entry.
	 *
	 * @return a new JAnnotationEntry sharing the same model reference
	 */
	@Override
	public JAnnotationEntry copy() {
		return new JAnnotationEntry(this);
	}
		
}
