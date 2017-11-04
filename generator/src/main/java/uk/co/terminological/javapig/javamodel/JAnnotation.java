package uk.co.terminological.javapig.javamodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.terminological.javapig.StringCaster;
import uk.co.terminological.javapig.javamodel.tools.JNameBuilder;
import uk.co.terminological.javapig.scanner.ReflectionUtils;

/**
 * represents an annotation in the source code and contains the detail of the 
 * annotation in a way that can be accessed in template and plugins.
 * @author terminological
 *
 */
public class JAnnotation extends JProjectComponent { 
	
	public JAnnotation(Optional<String> annotationClassFQN, String annotationName, List<JAnnotationEntry> entries) {
		this.annotationName = annotationName;
		this.entries = entries;
		this.fqnOrNull = annotationClassFQN.orElse(null);
	}
	
	public JAnnotation(JAnnotation copy) {
		this(
				Optional.ofNullable(copy.fqnOrNull),
				copy.getName(),
				new ArrayList<>()
		);
	}
	
	/**
	 * Method to access all the imports that are needed to make this annotation
	 * This includes the fqn of the annotation itself but also the 
	 * fqn of any classes referenced in the annotations<br/>
	 * 
	 * There is a scenario where the FQN of the annotation cannot be determined
	 * by QDox, when dealing with package level annotations, and this cannot be
	 * used reliably on package level annotations if using it as a Maven plugin
	 * @return a set of strings representing the FQN of imports for this annotation
	 */
	public Set<String> getImports() {
		Set<String> refl = new HashSet<>();
		try {
			if (fqnOrNull == null) throw new ClassNotFoundException();
			Class<?> cls = Class.forName(fqnOrNull);
			@SuppressWarnings("unchecked")
			Annotation a = convert((Class<Annotation>) cls);
			refl.addAll(ReflectionUtils.AnnotationImports.scan(a));
		} catch (Exception e) {
			//Can occur for many reasons. 
		}
		refl.addAll(entries.stream()
		.flatMap(ae -> ae.getValues().stream())
		.flatMap(av -> av.getImports().stream())
		.collect(Collectors.toSet()));
		if (this.fqnOrNull != null) refl.add(this.fqnOrNull);
		return refl;
	}
	
	private String fqnOrNull;
	private String annotationName;
	private List<JAnnotationEntry> entries = new ArrayList<>();
	//private Class<? extends Annotation> type;
	
	
	public List<JAnnotationEntry> getEntries() {
		return entries;
	}
	
	public void setEntries(List<JAnnotationEntry> values) {
		this.entries = values;
	}
	
	public List<JAnnotationValue<?>> getValues(String keyName) {
		return entries.stream()
			.filter(a -> a.getMethod().getter().equals(keyName))
			.flatMap(a -> a.getValues().stream())
			.collect(Collectors.toList());
	}
	
	public <T extends Annotation> String toSourceCode() throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (fqnOrNull == null) throw new ClassNotFoundException();
		Class<?> cls = Class.forName(fqnOrNull);
		@SuppressWarnings("unchecked")
		Annotation a = convert((Class<T>) cls);
		// do something with tmp.getImports() if issues from getImports() method above
		return ReflectionUtils.AnnotationToSource.convert(a);
	}
	
	/**
	 * Get this annotation as a instance of the given annotation type.
	 * If the wrong annotation type is given there will be many downstream errors, The
	 * type must be specified for 2 reasons. Firstly it ensures that the annotation type
	 * is compiled and known to the runtime, secondly in some circumstances the FQN of the
	 * annotation is unknowable. <br>
	 *  
	 * Typically this is going to be used in plugins rather than in templates.
	 *  
	 * @see {@link uk.co.terminological.javapig.javamodel.JElement} - which exposes this to the template engine
	 * @param type - The type of this annotation. This must match the defined type.
	 * @return an instance of that annotation class.
	 */
	public <T extends Annotation> T convert(Class<T> type) {
		return convertToJavaLang(this, type);
	}
	
	/*
	 * Creates a proxy - wrapping an annotation parsed from the source code into an
	 * instance of the annotation class itself 
	 */
	@SuppressWarnings("unchecked")
	private static <X> X convertToJavaLang(final JAnnotation annotation, Class<X> annotationClass) {
	    
		X proxy = (X) Proxy.newProxyInstance(
        		annotationClass.getClassLoader(), 
        		new Class[]{annotationClass},
                new InvocationHandler() {

                    public Object invoke(Object instance, Method method,
                                         Object[] args) throws Throwable {
                        if (method.getName().equals("toString")) {
                            return "Proxied annotation of type " + annotationClass;
                        } else if (method.getName().equals("getClass")) {
                            return annotationClass;
                        } else if (method.getName().equals("annotationType")) {
                        	return annotationClass;
                        }
                        
                        JMethodName mname = JNameBuilder.from(method);
                        List<JAnnotationValue<?>>  value = annotation
                        		.getEntries().stream()
                        		.filter(e -> e.getMethod().equals(mname))
                        		.findFirst()
                        		.map(ae -> ae.getValues()).orElse(null)
                        				;
                        
                        if (value == null || value.isEmpty()) {
                            return method.getDefaultValue();
                        }
                        
                        if (method.getReturnType().isArray()) {
                        	
                        	//TODO: this should probably be fixed in JAnnotationValue
                        	
                        	Object[] out = (Object[]) Array.newInstance(method.getReturnType().getComponentType(), value.size());
                        	int i=0;
                        	for (JAnnotationValue<?> component: value) {
                        		out[i] = unwrap(component,method.getReturnType().getComponentType());
                        		i++;
                        	}
                        	return out;
                        }

                        JAnnotationValue<?> singleValue = value.get(0);
                        return unwrap(singleValue, method.getReturnType());
                        
                    }
                });

        return proxy;
	}
	
	/*
	 * Utility to unwrap the values into 
	 * N.B Enums are held as Strings in the JAnnotation model. This is to allow them to 
	 * be easily manipulated in the template world. They are cast back to Enums here.
	 */
	@SuppressWarnings("unchecked")
	private static <X extends Object> X unwrap(JAnnotationValue<?> singleValue, Class<X> expectedType) throws ClassNotFoundException {
		if (singleValue instanceof JAnnotationValue.Annotation) {
        	JAnnotation component = ((JAnnotationValue.Annotation) singleValue).getValue();
        	return JAnnotation.convertToJavaLang(component, expectedType);
        } else if (singleValue instanceof JAnnotationValue.Class) {
    		return (X) ((JAnnotationValue.Class) singleValue).getValue().convert();
        } else {
        	Object primitive = ((JAnnotationValue.Primitive) singleValue).getValue();
        	if (expectedType.isAssignableFrom(primitive.getClass())) {
            	return expectedType.cast(primitive);
            } else {
            	return StringCaster.cast(expectedType,primitive.toString());
            }
        }
	}
	
	/**
	 * The simple name of the annotation. This is guaranteed to be defined. It does not
	 * include a "@"
	 * @return e.g. "Deprecated" if annotation was "@Deprecated"
	 */
	public String getName() {
		return annotationName;
	}
	
	/**
	 * The fully qualified name of the annotation. 
	 * Sometimes this is not known particularly in the example of a package-info level annotation 
	 * @return a fqn or "&ltunknown&gt"
	 */
	public String getCanonicalName() {
		return (fqnOrNull == null ? "<unknown>" : fqnOrNull);
	}
	
	
	/**
	 * Allows us to swap the name of an annotation for another. Best to use FQN here if known.
	 * The effect of this is somewhat undefined, and possibly shoudl not be generally part of the API.
	 * @param annotationName
	 */
	public void setName(String annotationName) {
		if (annotationName.contains(".")) { 
			this.annotationName = annotationName.substring(annotationName.lastIndexOf(".")+1);
			this.fqnOrNull = annotationName;
		} else {
			this.annotationName = annotationName;
			this.fqnOrNull = null;
		}
	}
	
	@Override
	public JAnnotation clone() {
		JAnnotation out = copy();
		out.setEntries(this.getEntries().stream().map(a -> a.clone()).collect(Collectors.toList()));
		return out;
	}
	@Override
	public JAnnotation copy() {
		return new JAnnotation(this);	
	}

}
