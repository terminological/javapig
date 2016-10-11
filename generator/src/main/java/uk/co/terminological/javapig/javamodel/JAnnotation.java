package uk.co.terminological.javapig.javamodel;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import uk.co.terminological.javapig.javamodel.tools.JModelComponent;
import uk.co.terminological.javapig.javamodel.tools.JNameBuilder;
import uk.co.terminological.javapig.scanner.StringCaster;

public class JAnnotation extends JModelComponent { 
	
	private String annotationName;
	private List<JAnnotationEntry> values = new ArrayList<>();
	//private Class<? extends Annotation> type;
		
	public List<JAnnotationEntry> getValues() {
		return values;
	}
	public void setValues(List<JAnnotationEntry> values) {
		this.values = values;
	}
	
	public List<JAnnotationValue<?>> getValues(String keyName) {
		return values.stream()
			.filter(a -> a.getMethod().getter().equals(keyName))
			.flatMap(a -> a.getValues().stream())
			.collect(Collectors.toList());
	}
	
	public <T extends Annotation> T convert(Class<T> type) {
		return convertToJavaLang(this, type);
	}
	
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
                        }
                        
                        JMethodName mname = JNameBuilder.from(method);
                        List<JAnnotationValue<?>>  value = annotation
                        		.getValues().stream()
                        		.filter(e -> e.getMethod().equals(mname))
                        		.findFirst()
                        		.map(ae -> ae.getValues()).orElse(null)
                        				;
                        
                        if (value == null || value.isEmpty()) {
                            return method.getDefaultValue();
                        }
                        
                        if (method.getReturnType().isArray()) {
                        	
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
	
	public String getName() {
		return annotationName;
	}
	public void setName(String annotationName) {
		if (annotationName.contains(".")) 
			annotationName = annotationName.substring(annotationName.lastIndexOf(">"));
		this.annotationName = annotationName;
	}
	/*public Class<? extends Annotation> getType() {
		return type;
	}
	public void setType(Class<? extends Annotation> type) {
		this.type = type;
	}*/

}
