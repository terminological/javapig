/**
 * 
 */
package uk.co.terminological.javapig.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringEscapeUtils;

/**
 * @see #ReflectionJModelBuilder
 * @author rchallen
 *
 */
public class ReflectionUtils {

	public static <T> T instance(Class<T> clazz) {
		try {
			return clazz.newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean isCollectionType(Class<?> returnType) {
		return 	Iterable.class.isAssignableFrom(returnType) || 
				Iterator.class.isAssignableFrom(returnType) ||
				Array.class.isAssignableFrom(returnType) ||
				Optional.class.isAssignableFrom(returnType);
	}

	public static interface Converter<R,T> { public R convert(T input); }  

	public static <X> Iterable<X> iterable(Object returnType, final Class<X> desiredType) {
		return iterable(returnType, new Converter<X,Object>() {
			@Override
			public X convert(Object input) {
				return desiredType.cast(input);
			}});
	}

	public static Iterable<Object> iterable(Object collectionOrSingleton) {
		return iterable(collectionOrSingleton, new Converter<Object,Object>() {
			@Override
			public Object convert(Object input) {
				return input;
			}});
	}

	public static Iterable<String> iterableToString(Object returnType) {
		return iterable(returnType, new Converter<String,Object>() {
			@Override
			public String convert(Object input) {
				return input.toString();
			}});
	}

	@SuppressWarnings("unchecked")
	public static <X,Y> Iterable<X> iterable(Object returnType, Converter<X,Y> converter) throws ClassCastException {
		if (returnType == null) return Collections.emptyList();
		List<X> out = new ArrayList<X>();
		if (Iterable.class.isAssignableFrom(returnType.getClass())) {
			for (Object tmp: (Iterable<?>) returnType) {
				out.add(converter.convert((Y) tmp));
			}
			return out;
		}
		if (Iterator.class.isAssignableFrom(returnType.getClass())) {
			while (((Iterator<?>) returnType).hasNext()) {
				out.add(converter.convert(((Iterator<Y>) returnType).next()));
			}
			return out;
		}
		if (Array.class.isAssignableFrom(returnType.getClass())) {
			for (Object tmp: (Object[]) returnType) {
				out.add(converter.convert((Y)tmp));
			}
			return out;
		}
		return Collections.singletonList(converter.convert((Y) returnType));
	}

	public static boolean returnsSingleton(Method m) {
		return !isCollectionType(m.getReturnType());
	}

	public static Class<?> underlyingReturnType(Method instanceOrCollectionOrArray) {
		if (!isCollectionType(instanceOrCollectionOrArray.getReturnType())) return instanceOrCollectionOrArray.getReturnType();
		Class<?> returnType = instanceOrCollectionOrArray.getReturnType();
		if (Array.class.isAssignableFrom(returnType)) {
			return returnType.getComponentType();
		} else if (Iterable.class.isAssignableFrom(returnType) || Iterator.class.isAssignableFrom(returnType)) {
			Type r = instanceOrCollectionOrArray.getGenericReturnType();
			Type[] types = ((ParameterizedType) r).getActualTypeArguments();
			if (types.length > 0) return getClass(types[0]);
			return Object.class;
		} else {
			return Object.class;
		}
	}




	/**
	 * Get the underlying class for a type, or null if the type is a variable type.
	 * @param type the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class<?>) {
			return (Class<?>) type;
		}
		else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		}
		else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null ) {
				return Array.newInstance(componentClass, 0).getClass();
			}
			else {
				return Object.class;
			}
		}
		else {
			//log.warn(type.toString());
			return Object.class;
		}
	}

	/**
	 * Get the actual type arguments a child class has used to extend a generic base class.
	 *
	 * @param baseClass the base class
	 * @param childClass the child class
	 * @return a list of the raw classes for the actual type arguments.
	 */
	public static <T> List<Class<?>> getTypeArguments(
			Class<T> baseClass, Class<? extends T> childClass) {
		Map<Type, Type> resolvedTypes = new HashMap<Type, Type>();
		Type type = childClass;
		// start walking up the inheritance hierarchy until we hit baseClass
		while (! getClass(type).equals(baseClass)) {
			if (type instanceof Class<?>) {
				// there is no useful information for us in raw types, so just keep going.
				type = ((Class<?>) type).getGenericSuperclass();
			}
			else {
				ParameterizedType parameterizedType = (ParameterizedType) type;
				Class<?> rawType = (Class<?>) parameterizedType.getRawType();

				Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
				TypeVariable<?>[] typeParameters = rawType.getTypeParameters();
				for (int i = 0; i < actualTypeArguments.length; i++) {
					resolvedTypes.put(typeParameters[i], actualTypeArguments[i]);
				}

				if (!rawType.equals(baseClass)) {
					type = rawType.getGenericSuperclass();
				}
			}
		}

		// finally, for each actual type argument provided to baseClass, determine (if possible)
		// the raw class for that type argument.
		Type[] actualTypeArguments;
		if (type instanceof Class<?>) {
			actualTypeArguments = ((Class<?>) type).getTypeParameters();
		}
		else {
			actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
		}
		List<Class<?>> typeArgumentsAsClasses = new ArrayList<Class<?>>();
		// resolve types by chasing down type variables.
		for (Type baseType: actualTypeArguments) {
			while (resolvedTypes.containsKey(baseType)) {
				baseType = resolvedTypes.get(baseType);
			}
			typeArgumentsAsClasses.add(getClass(baseType));
		}
		return typeArgumentsAsClasses;
	}

	public static <T extends Annotation> List<T> getAllInheritedAnnotations(Method method, Class<T> annotation) {
		ArrayList<T> out = new ArrayList<T>();
		if (method.isAnnotationPresent(annotation)) out.add(method.getAnnotation(annotation));
		Class<?>[] clazzes = getAllSuperclassesAndInterfaces(method.getDeclaringClass());

		//For each of these classes, see if there is a method that looks exactly like this one and
		//is annotated with the given annotation
		for(Class<?> clazz : clazzes) {
			try {
				//Throws an exception if method not found.
				Method m = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());

				if(m.isAnnotationPresent(annotation)) {
					out.add(m.getAnnotation(annotation));
				}
			}
			catch(Exception e) {
				/*Do nothing*/
			}
		}
		return out;
	}

	/*
	 * Check to see if a given method is annotated with the given annotation.
	 * This is the method you call in your client code.
	 */
	public static boolean isImplementedMethodAnnotatedWith(Method method, Class<? extends Annotation> annotation) {

		//Find all interfaces/classes that are extended/implemented **explicitly** by the class
		//the given method belongs to. "Explicitly" in this context means the classes/interfaces
		//that are listed in an extends/implements clause. I.e. don't expect Object to show up.
		if (method.isAnnotationPresent(annotation)) return true;
		Class<?>[] clazzes = getAllSuperclassesAndInterfaces(method.getDeclaringClass());

		//For each of these classes, see if there is a method that looks exactly like this one and
		//is annotated with the given annotation
		for(Class<?> clazz : clazzes) {
			try {
				//Throws an exception if method not found.
				Method m = clazz.getDeclaredMethod(method.getName(), method.getParameterTypes());

				if(m.isAnnotationPresent(annotation)) {
					return true;
				}
			}
			catch(Exception e) {
				/*Do nothing*/
			}
		}

		return false;
	}

	public static Class<?>[] getAllSuperclassesAndInterfaces(Class<?> clazz) {
		return getAllInterfaces(Collections.singleton(clazz)).toArray(new Class<?>[] {});
	}

	//This method walks up the inheritance hierarchy to make sure we get every class/interface that could
	//possibly contain the declaration of the annotated method we're looking for.
	private static Set<Class<?>> getAllInterfaces(Set<Class<?>> classes) {
		if(0 == classes.size() ) {
			return classes;
		}
		else {
			Set<Class<?>> extendedClasses = new HashSet<Class<?>>();
			for (Class<?> clazz: classes) {
				extendedClasses.addAll(Arrays.asList( clazz.getInterfaces() ) );
				if (clazz.getSuperclass() != null) extendedClasses.add(clazz.getSuperclass());
			}
			//Class::getInterfaces() gets only interfaces/classes implemented/extended directly by a given class.
			//We need to walk the whole way up the tree.
			classes.addAll(getAllInterfaces(extendedClasses));
			return classes;
		}
	}

	public static Class<?> box(Type primitiveType) {
		if (primitiveType.equals(Byte.TYPE)) {return Byte.class;}
		if (primitiveType.equals(Short.TYPE)) {return Short.class;}
		if (primitiveType.equals(Integer.TYPE)) {return Integer.class;}
		if (primitiveType.equals(Long.TYPE)) {return Long.class;}
		if (primitiveType.equals(Float.TYPE)) {return Float.class;}
		if (primitiveType.equals(Double.TYPE)) {return Double.class;}
		if (primitiveType.equals(Boolean.TYPE)) {return Boolean.class;}
		if (primitiveType.equals(Character.TYPE)) {return Character.class;}
		if (primitiveType.equals(Void.TYPE)) {return Void.class;}
		else return (Class<?>) primitiveType;
	}

	public static class AnnotationToSource {

		Annotation annotation;
		StringBuilder code = new StringBuilder();

		private AnnotationToSource(Annotation annotation) {this.annotation = annotation;}

		public static String convert(Annotation annotation) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			AnnotationToSource out = new AnnotationToSource(annotation);
			out.code.append("@"+annotation.annotationType().getSimpleName());
			StringBuilder tmp = new StringBuilder();
			boolean first = true;
			for (Method m: annotation.annotationType().getMethods()) {
				if (!Arrays.asList("hashCode","equals","toString","annotationType").contains(m.getName())) {
					if (!first) tmp.append(", ");
					tmp.append(out.convertMethod(m));
					first = false;
				}
			}

			if (tmp.length() > 0) {
				out.code.append("("+tmp.toString()+")");
			}
			return out.code.toString();
		}

		private String convertMethod(Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			StringBuilder out = new StringBuilder();
			out.append(method.getName()+"=");
			if (method.getReturnType().isArray()) {
				Object[] tmp = (Object[]) method.invoke(annotation);
				out.append("{");
				boolean first = false;
				for (Object v: tmp) {
					if (!first) out.append(",");
					out.append(this.convertValue(v));
					first = false;
				}
				out.append("}");
			} else {
				out.append(convertValue(method.invoke(annotation)));
			}
			return out.toString();
		};

		private String convertValue(Object o) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (o instanceof Class) {
				return ((Class<?>) o).getName()+".class";
			} else if (o instanceof Enum) {
				return o.getClass().getSimpleName()+"."+((Enum<?>) o).name();
			} else if (o instanceof Annotation) {
				return AnnotationToSource.convert((Annotation) o);
			} else { 
				return primitiveToSource(o);
			}
		}
	}

	public static class AnnotationImports {

		Annotation annotation;
		Set<String> imports = new HashSet<>();

		private AnnotationImports(Annotation annotation) {this.annotation = annotation;}

		public static Set<String> scan(Annotation annotation) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			AnnotationImports out = new AnnotationImports(annotation);
			for (Method m: annotation.annotationType().getMethods()) {
				if (!Arrays.asList("hashCode","equals","toString","annotationType").contains(m.getName())) {
					out.scanMethod(m);
				}
			}
			return out.imports;
		}

		private void scanMethod(Method method) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (method.getReturnType().isArray()) {
				Object[] tmp = (Object[]) method.invoke(annotation);
				for (Object v: tmp) {
					this.scanValue(v);
				}
			} else {
				scanValue(method.invoke(annotation));
			}
		};

		private void scanValue(Object o) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			if (o instanceof Class) {
				this.imports.add(((Class<?>) o).getCanonicalName());
			} else if (o instanceof Enum) {
				this.imports.add(((Enum<?>) o).getClass().getCanonicalName());
			} else if (o instanceof Annotation) {
				this.imports.addAll(AnnotationImports.scan((Annotation) o));
			} else { 
				return;
			}
		}
	}

	private static String primitiveToSource(Object o) {
		Class<?> primitiveType = o.getClass();
		if (o instanceof Byte || primitiveType.equals(Byte.TYPE)) {return Byte.toString((byte) o);}
		if (o instanceof Short || primitiveType.equals(Short.TYPE)) {return Short.toString((short) o);}
		if (o instanceof Long || primitiveType.equals(Long.TYPE)) {return Long.toString((long) o)+"L";}
		if (o instanceof Integer || primitiveType.equals(Integer.TYPE)) {return Integer.toString((int) o);}
		if (o instanceof Float || primitiveType.equals(Float.TYPE)) {return Float.toString((float) o)+"F";}
		if (o instanceof Double || primitiveType.equals(Double.TYPE)) {return Double.toString((double) o)+"D";}
		if (o instanceof Boolean || primitiveType.equals(Boolean.TYPE)) {return ((boolean) o) ? "true" : "false";}
		if (o instanceof Character || primitiveType.equals(Character.TYPE)) {return "'"+o.toString()+"'";}
		if (o instanceof Void || primitiveType.equals(Void.TYPE)) {return "void";}
		if (o instanceof String) return "\""+StringEscapeUtils.escapeJava(o.toString())+"\"";
		else throw new ClassCastException("Not a primitive type "+primitiveType.getCanonicalName());
	}

}
