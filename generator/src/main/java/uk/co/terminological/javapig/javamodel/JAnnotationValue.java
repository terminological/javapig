package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Holds a single value within a {@link JAnnotationEntry}, with three subtypes
 * for primitive values, class references, and nested annotations.
 *
 * <p>Subtypes:</p>
 * <ul>
 *   <li>{@link Primitive} — primitive types, strings, enums, and other literal values.</li>
 *   <li>{@link Annotation} — nested annotation instances ({@link JAnnotation}).</li>
 *   <li>{@link Class} — class references ({@link JClassName}).</li>
 * </ul>
 *
 * <p>Factory methods {@link #from(Object)} and {@link #of(Object)} automatically
 * infer the appropriate subtype from the runtime type of the value.</p>
 *
 * @see JAnnotationEntry
 * @see JAnnotation
 * @param <T> the value type (Object for Primitive, JAnnotation for Annotation, JClassName for Class)
 */
public class JAnnotationValue<T> extends JProjectComponent {

	private T value;
	
	/**
	 * Returns the annotation value.
	 *
	 * @return the value, which is one of: Object (primitive), JAnnotation, or JClassName
	 */
	public T getValue() {
		return value;
	}

	/**
	 * Sets the annotation value.
	 *
	 * @param value the new value
	 */
	public void setValue(T value) {
		this.value = value;
	}

	/**
	 * Fluent setter that assigns the value and returns this instance.
	 *
	 * @param value the value to set
	 * @return this instance for chaining
	 */
	public JAnnotationValue<T> with(T value) {
		this.value = value;
		return this;
	}
	
	/**
	 * Constructs a new annotation value with the given value.
	 *
	 * @param value the value to wrap
	 */
	public JAnnotationValue(T value) {
		this.value = value;
	}
	
	/**
	 * Returns {@code true} if this value represents a nested annotation.
	 * Always {@code false} for the base class; overridden by {@link Annotation}.
	 *
	 * @return {@code true} for annotation values
	 */
	public boolean isAnnotation() {return false;}
	
	/**
	 * Returns {@code true} if this value represents a primitive or literal value.
	 * Always {@code false} for the base class; overridden by {@link Primitive}.
	 *
	 * @return {@code true} for primitive values
	 */
	public boolean isPrimitive() {return false;}
	
	/**
	 * Returns {@code true} if this value represents a class reference.
	 * Always {@code false} for the base class; overridden by {@link Class}.
	 *
	 * @return {@code true} for class values
	 */
	public boolean isClass() {return false;}
	
	/**
	 * Returns the set of imports required for this value's type.
	 * Base implementation returns an empty set; subclasses provide meaningful imports.
	 *
	 * @return a set of fully-qualified import class names
	 */
	public Set<String> getImports() {return Collections.emptySet();}
	
	/**
	 * A {@link JAnnotationValue} subtype for primitive and literal annotation values
	 * (strings, integers, booleans, enums, etc.).
	 */
	public static class Primitive extends JAnnotationValue<java.lang.Object> {
	
		/**
		 * Constructs a primitive annotation value.
		 *
		 * @param value the primitive or literal value
		 */
		public Primitive(Object value) {
			super(value);
		}
		
		/**
		 * Copy constructor for a primitive annotation value.
		 *
		 * @param copy the value to copy
		 */
		public Primitive(Primitive copy) {
			super(copy.getValue());
		}

		@Override
		public boolean isPrimitive() {return true;}
		
		//TODO: if this is to be used in source code we need a toString method that gives
		//a source code friendly representation for each possible type e.g. "A String", 0.2F, etc..
		
		//TODO: enums end up here, for complex reasons, depending on the builder that created them
		//we may or may not know their full type.
		
		//in the template environment we want to compare them to their string representation anyway 
		//so that is an advantage. Outside of the template environment then we can use the 
		//JAnnotation 
	}
	
	/**
	 * A {@link JAnnotationValue} subtype for nested annotation values.
	 *
	 * @see #getValue() returns a JAnnotation
	 */
	public static class Annotation extends JAnnotationValue<JAnnotation> {
		
		/**
		 * Constructs a nested annotation value.
		 *
		 * @param value the nested JAnnotation
		 */
		public Annotation(JAnnotation value) {
			super(value);
		}

		/**
		 * Copy constructor for a nested annotation value.
		 *
		 * @param copy the value to copy
		 */
		public Annotation(Annotation copy) {
			super(copy.getValue());
		}
		
		@Override
		public boolean isAnnotation() {return true;}
		
		@Override
		public Set<String> getImports() { 
			return getValue().getImports();
		}
	}
	
	/**
	 * A {@link JAnnotationValue} subtype for class reference values.
	 *
	 * @see #getValue() returns a JClassName
	 */
	public static class Class extends JAnnotationValue<JClassName> {
		
		/**
		 * Constructs a class reference value.
		 *
		 * @param value the class name reference
		 */
		public Class(JClassName value) {
			super(value);
		}

		/**
		 * Copy constructor for a class reference value.
		 *
		 * @param copy the value to copy
		 */
		public Class(Class copy) {
			super(copy.getValue());
		}
		
		@Override
		public boolean isClass() {return true;}
		
		@Override
		public Set<String> getImports() { 
			return Collections.singleton(getValue().importName());
		}
	}

	/**
	 * Infers the appropriate {@link JAnnotationValue} subtype from the given object's runtime type.
	 *
	 * @param value the annotation attribute value
	 * @return a Primitive, Class, or Annotation subtype
	 * @throws RuntimeException if the value is {@code null}
	 */
	public static JAnnotationValue<?> from(Object value) {
		if (value == null) throw new RuntimeException("Null annotation value");
		if (value instanceof JClassName) return new Class((JClassName) value);
		if (value instanceof JAnnotation) return new Annotation((JAnnotation) value);
		return new Primitive(value);
	}
	
	/**
	 * Converts a single value or collection of values into a list of {@link JAnnotationValue} instances.
	 *
	 * <p>Handles the following input types:</p>
	 * <ul>
	 *   <li>{@code null} → empty list</li>
	 *   <li>Arrays → each element becomes a value</li>
	 *   <li>{@link Collection} → each element becomes a value</li>
	 *   <li>Other → a single-element list</li>
	 * </ul>
	 *
	 * @param values the raw annotation values
	 * @return a list of annotation value objects
	 */
	public static List<JAnnotationValue<?>> of(Object values) {
		if (values == null) return Collections.emptyList();
		
		List<Object> tmp = new ArrayList<>();
		if (!Collection.class.isAssignableFrom(values.getClass())) {
			if (values.getClass().isArray()) {
				for (Object o: (Object[]) values) {
					tmp.add(o);
				}
			} else {
				tmp.add(values);
			}
		} else {
			tmp.addAll((Collection<?>) values);
		}
		ArrayList<JAnnotationValue<?>> out = new ArrayList<>();
		for (Object value: tmp) {
			if (value instanceof JAnnotation) out.add( 
					new Annotation((JAnnotation) value));
			else if (value instanceof JClassName) out.add( 
					new Class((JClassName) value));
			else out.add(
					new Primitive(value));
		}
		return out;
	}
	
	/**
	 * Creates a deep clone of this annotation value, handling nested annotation cloning.
	 *
	 * @return a cloned JAnnotationValue
	 */
	@Override
	public JAnnotationValue<?> clone() {
		if (value instanceof JAnnotation) return JAnnotationValue.from(((JAnnotation) this.getValue()).clone());
		return copy();
	}
	
	/**
	 * Creates a shallow copy of this annotation value using the appropriate copy constructor.
	 *
	 * @return a copied JAnnotationValue
	 * @throws RuntimeException if the subtype is unrecognized
	 */
	@Override
	public JAnnotationValue<?> copy() {
		if (this instanceof Primitive) return new Primitive((Primitive) this);
		if (this instanceof Annotation) return new Annotation((Annotation) this);
		if (this instanceof Class) return new Class((Class) this);
		throw new RuntimeException("inconsistent state");
	}
	
}
