package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class JAnnotationValue<T> extends JProjectComponent {

	private T value;
	public T getValue() {
		return value;
	}
	public void setValue(T value) {
		this.value = value;
	}
	public JAnnotationValue<T> with(T value) {
		this.value = value;
		return this;
	}
	
	public JAnnotationValue(T value) {
		this.value = value;
	}
	
	public boolean isAnnotation() {return false;}
	public boolean isPrimitive() {return false;}
	public boolean isClass() {return false;}
	public Set<String> getImports() {return Collections.emptySet();}
	
	public static class Primitive extends JAnnotationValue<java.lang.Object> {
	
		public Primitive(Object value) {
			super(value);
		}
		
		public Primitive(Primitive copy) {
			super(copy.getValue());
		}

		public boolean isPrimitive() {return true;}
		
		//TODO: if this is to be used in source code we need a toString method that gives
		//a source code friendly representation for each possible type e.g. "A String", 0.2F, etc..
	}
	
	public static class Annotation extends JAnnotationValue<JAnnotation> {
		
		public Annotation(JAnnotation value) {
			super(value);
		}

		public Annotation(Annotation copy) {
			super(copy.getValue());
		}
		
		public boolean isAnnotation() {return true;}
		
		public Set<String> getImports() { 
			return getValue().getImports();
		}
	}
	
	public static class Class extends JAnnotationValue<JClassName> {
		
		public Class(JClassName value) {
			super(value);
		}

		public Class(Class copy) {
			super(copy.getValue());
		}
		
		public boolean isClass() {return true;}
		
		public Set<String> getImports() { 
			return Collections.singleton(getValue().importName());
		}
	}

	
	public static JAnnotationValue<?> from(Object value) {
		if (value == null) throw new RuntimeException("Null annotation value");
		if (value instanceof JClassName) return new Class((JClassName) value);
		if (value instanceof JAnnotation) return new Annotation((JAnnotation) value);
		return new Primitive(value);
	}
	
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
	@Override
	public JAnnotationValue<?> clone() {
		if (value instanceof JAnnotation) return JAnnotationValue.from(((JAnnotation) this.getValue()).clone());
		return copy();
	}
	
	@Override
	public JAnnotationValue<?> copy() {
		if (this instanceof Primitive) return new Primitive((Primitive) this);
		if (this instanceof Annotation) return new Annotation((Annotation) this);
		if (this instanceof Class) return new Class((Class) this);
		throw new RuntimeException("inconsistent state");
	}
	
}

