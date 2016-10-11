package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import java.util.Collections;
import uk.co.terminological.javapig.javamodel.tools.JModelComponent;

public class JAnnotationValue<T> extends JModelComponent {

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
	
	public boolean isAnnotation() {return false;}
	public boolean isPrimitive() {return false;}
	public boolean isClass() {return false;}
	
	public static class Primitive extends JAnnotationValue<java.lang.Object> {
		public boolean isPrimitive() {return true;}
	}
	public static class Annotation extends JAnnotationValue<JAnnotation> {
		public boolean isAnnotation() {return true;}
	}
	public static class Class extends JAnnotationValue<JClassName> {
		public boolean isClass() {return true;}
	}
		
	@SuppressWarnings("unchecked")
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
					new Annotation().with((JAnnotation) value));
			else if (value instanceof JClassName) out.add( 
					new Class().with((JClassName) value));
			else out.add(
					new Primitive().with(value));
		}
		return out;
	}
	
}

