package uk.co.terminological.javapig.scanner;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Vector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.builder.impl.EvaluatingVisitor;
import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaParameterizedType;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.expression.FieldRef;
import com.thoughtworks.qdox.model.expression.TypeRef;
import com.thoughtworks.qdox.model.impl.DefaultJavaType;

import java.util.Arrays;
import javassist.Modifier;
import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.annotations.Scope;

public class QDoxUtils {

	static Logger log = LoggerFactory.getLogger(QDoxUtils.class); 
	static Scope ensureImported = Scope.INTERFACE;

	public static boolean returnsSingleton(JavaMethod m) {
		return !isCollectionType(m.getReturns());
	}

	public static boolean isResolvableURI(JavaClass cls) {
		return (cls.isA(URI.class.getCanonicalName()) ||
				cls.isA(URL.class.getCanonicalName()) ||
				cls.isA(java.net.URI.class.getCanonicalName())
				);
	}

	/*@SuppressWarnings("unchecked")
	public static Collection<String> getIriValue(JavaAnnotatedElement elem) {
		JavaClass c = null;
		if (elem instanceof JavaClass) {
			c = (JavaClass) elem;
		} else 	if (elem instanceof JavaMethod) {
			c = ((JavaMethod) elem).getDeclaringClass();
		}
		try {
			if (elem instanceof JavaClass) {
				return Arrays.asList(Class.forName(c.getCanonicalName()).getAnnotation(IRI.class).value());
			}
			if (elem instanceof JavaMethod) {
				return Arrays.asList(Class.forName(c.getCanonicalName()).getMethod(
						((JavaMethod) elem).getName()).getAnnotation(IRI.class).value());
			}
		} catch (ClassNotFoundException | NoSuchMethodException e) {
			for (JavaAnnotation ann: elem.getAnnotations()) {
				if (ann.getType().getFullyQualifiedName().equals(IRI.class.getCanonicalName())) {
					Object tmp = ann.getProperty("value").accept(new WorkaroundVisitor(c));
					if (tmp instanceof Collection) return (Collection<String>) tmp;
					if (tmp.getClass().isArray()) return Arrays.asList((String[]) tmp);
					return Collections.singleton((String) tmp);
				}
			}
		}
		log.error("Couldn't find IRI annotation on "+c.getCanonicalName()+" or method");
		return Collections.emptySet();

	}*/

	public static boolean hasAnnotation(Class<? extends java.lang.annotation.Annotation> ann, JavaAnnotatedElement elem) {
		try {
			if (elem instanceof JavaClass) {
				return Class.forName(((JavaClass) elem).getCanonicalName()).isAnnotationPresent(ann);
			}
			if (elem instanceof JavaMethod) {
				JavaClass cls = ((JavaMethod) elem).getDeclaringClass();
				return Class.forName(cls.getCanonicalName()).getMethod(
						((JavaMethod) elem).getName()).isAnnotationPresent(ann);
			}
		} catch (ClassNotFoundException | NoSuchMethodException e) {

		}
		for (JavaAnnotation a: elem.getAnnotations()) {
			String aname = a.getType().getValue();
			String jname = ann.getCanonicalName();
			if (jname.endsWith(aname)) return true;
		}
		return false;
	}

	/*@SuppressWarnings("unchecked")
	public static Collection<Object> getValue(JavaAnnotation ann, JavaAnnotatedElement cls, String prop) {
		Object tmp = ann.getProperty(prop).accept(new WorkaroundVisitor(cls));
		if (tmp instanceof Collection) return (Collection<Object>) tmp;
		if (tmp.getClass().isArray()) return Arrays.asList((Object[]) tmp);
		return Collections.singleton(tmp);
	}*/

	/*public static Iterable<URI> getURIs(final JavaClass annotatedClass) {
		return new Iterable<URI>() {
			@Override
			public Iterator<URI> iterator() {
				final Iterator<String> uris = getIriValue(annotatedClass).iterator();
				return new Iterator<URI>() {
					@Override
					public boolean hasNext() {
						return uris.hasNext();//return i<uris.length;
					}
					@Override
					public URI next() {
						return URI.create(uris.next());
					}
					@Override
					public void remove() {
						throw new RuntimeException("not implemented");
					}
				};
			}
		};
	}

	public static Set<JavaClass> nearestDescendents(JavaClass start, JavaProjectBuilder jdb) {
		Set<JavaClass> out = new HashSet<>();
		if (hasAnnotation(IRI.class, start)) return Collections.singleton(start);
		for (JavaClass clazz: jdb.getClasses()) {
			if (hasAnnotation(IRI.class, clazz)) {
				if (clazz.isA(start)) {
					Set<JavaClass> clone = new HashSet<>();
					boolean add = true;
					for (JavaClass tmp: out) {
						if (!tmp.isA(clazz)) clone.add(tmp);
						if (clazz.isA(tmp)) add=false;
					}
					if (add) clone.add(clazz);
					out = clone;
				}
			}
		}
		return out;
	}

	public static Set<JavaClass> nearestAnnotated(JavaClass start) {
		if (hasAnnotation(IRI.class, start)) return Collections.singleton(start);
		Set<JavaClass> tmp = new HashSet<>();
		for (JavaClass iface: start.getInterfaces()) {
			tmp.addAll(nearestAnnotated(iface));
		}
		if (start.getSuperClass() != null) {
			tmp.addAll(nearestAnnotated(start.getSuperJavaClass()));
		}
		return tmp;
	}

	public static Set<JavaClass> nearestAncestor(JavaClass start) {
		Set<JavaClass> tmp = new HashSet<>();
		for (JavaClass iface: start.getInterfaces()) {
			if (hasAnnotation(IRI.class, iface)) tmp.add(iface);
			else tmp.addAll(nearestAncestor(iface));
		}
		if (start.getSuperJavaClass() != null) {
			if (hasAnnotation(IRI.class, start.getSuperJavaClass())) tmp.add(start.getSuperJavaClass());
			else tmp.addAll(nearestAncestor(start.getSuperJavaClass()));
		}
		return tmp;
	}


	//@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Set<JavaClass> allAnnotated(JavaClass start) {
		Set<JavaClass> tmp = new HashSet<>();
		if (hasAnnotation(IRI.class,start)) tmp.add(start);
		for (JavaClass iface: start.getInterfaces()) {
			tmp.addAll(allAnnotated(iface));
		}
		if (start.getSuperClass() != null) {
			tmp.addAll(allAnnotated(start.getSuperJavaClass()));
		}
		return tmp;
	}*/



	public static JavaAnnotation getAnnotation(Class<? extends java.lang.annotation.Annotation> ann, JavaAnnotatedElement elem) {
		for (JavaAnnotation a: elem.getAnnotations()) {
			String aname = a.getType().getFullyQualifiedName();
			String jname = ann.getSimpleName(); //.getCanonicalName();
			if (jname.equals(aname)) return a;
		}
		return null;

		/*for (JavaAnnotation a: clazz.getAnnotations()) {
			if (a.getType().getFullyQualifiedName().equals(ann.getSimpleName())) return a;
		}
		return null;*/
	}

	public static JavaClass underlyingReturnType(JavaMethod instanceOrCollectionOrArray, JavaProjectBuilder jdb) {
		JavaClass returnType = jdb.getClassByName(instanceOrCollectionOrArray.getReturnType().getGenericValue());

		if (isParameterised(instanceOrCollectionOrArray.getReturnType())) {
			JavaType r = instanceOrCollectionOrArray.getReturnType();
			List<JavaType> types = ((JavaParameterizedType) r).getActualTypeArguments();
			try {
				return getClass(types.get(0), jdb);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else if (returnType.isArray()) {
			return returnType.getComponentType();
		} else {
			return null;
		} 
	}

	public static JavaClass getClass(JavaType javaType, JavaProjectBuilder jdb) throws ClassNotFoundException {
		return getClass(box(javaType,jdb).getFullyQualifiedName(),jdb);//.getCanonicalName());
	}

	public static JavaClass getClass(TypeRef javaType, JavaProjectBuilder jdb) throws ClassNotFoundException {
		return getClass(box(javaType.getType(),jdb).getFullyQualifiedName(),jdb);//.getCanonicalName());
	}

	public static JavaClass getClass(String fqn, JavaProjectBuilder jdb) throws ClassNotFoundException {
		Optional<JavaClass> out = jdb.getClasses().stream()
			.filter(n -> n.getCanonicalName().equals(fqn))
			.findFirst();
		if (out.isPresent()) return out.get(); //this is in the QDox library
		Class<?> cls = Class.forName(fqn); //The system class loader has heard of this
		return jdb.getClassByName(cls.getCanonicalName());
	}

	public static boolean containsClass(TypeRef javaType, JavaProjectBuilder jdb) {
		try {
			getClass(javaType,jdb);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean containsClass(JavaType javaType, JavaProjectBuilder jdb) {
		try {
			getClass(javaType,jdb);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean containsClass(String javaType, JavaProjectBuilder jdb) {
		try {
			getClass(javaType,jdb);
			return true;
		} catch (ClassNotFoundException e) {
			return false;
		}
	}

	public static boolean isCollectionType(JavaClass returnType) {
		return returnType.isArray() || 
				returnType.isA(Iterable.class.getCanonicalName()) ||
				returnType.isA(Iterator.class.getCanonicalName());
	}

	public static boolean isParameterised(JavaType type) {
		if (type instanceof JavaParameterizedType) {
			return !((JavaParameterizedType) type).getActualTypeArguments().isEmpty();
		}
		return !((DefaultJavaType) type).getTypeParameters().isEmpty();
	}

	/**
	 * @author Dmitry Baev charlie@yandex-team.ru
	 *         Date: 03.04.15
	 * work around for QDox handling of statics embedded in inner classes being used as annotation values. 
	 */
	public static class WorkaroundVisitor extends EvaluatingVisitor {

		//private JavaClass context;
		private List<String> imports = new ArrayList<>();
		private String packageName;
		protected JavaProjectBuilder jdb;
		protected JavaAnnotatedElement context;

		//protected JavaClass fieldClass;
		//protected JavaField field;

		public WorkaroundVisitor(JavaAnnotatedElement context, JavaProjectBuilder jdb) {
			this.jdb = jdb;
			this.context = context;
			if (context instanceof JavaClass) {
				this.imports = ((JavaClass) context).getSource().getImports();
				this.packageName = ((JavaClass) context).getSource().getPackage().getName();				
			}
			if (context instanceof JavaMethod) {
				this.imports = ((JavaMethod) context).getDeclaringClass().getSource().getImports();
				this.packageName = ((JavaMethod) context).getDeclaringClass().getSource().getPackage().getName();
			}
			if (context instanceof JavaPackage) {
				this.packageName = ((JavaPackage) context).getName();
				this.imports = jdb.getSources().stream().flatMap(s -> s.getImports().stream()).collect(Collectors.toList());
				this.imports.addAll(jdb.getClasses().stream().map(c -> c.getCanonicalName()).collect(Collectors.toList()));
				this.imports.add(BuiltIn.class.getCanonicalName());
			}

		}

		public JavaType visit( TypeRef typeRef )
		{
		
			try {
				String name = typeRef.getType().getValue();
				if (QDoxUtils.containsClass(typeRef,jdb)) 
					return QDoxUtils.getClass(typeRef,jdb);

				String[] fieldParts = name.split("\\.");
				String className = null;
				for (String imp: imports) {
					if (imp.endsWith(fieldParts[0])) {
						className = imp.substring(0, imp.lastIndexOf('.'))+"."+name;
					} 
				}
				if (className != null && QDoxUtils.containsClass(className,jdb)) {
					log.debug("Found: "+className);
					return QDoxUtils.getClass(className,jdb);

				}
				String tmp = packageName+"."+name;
				if (QDoxUtils.containsClass(tmp, jdb)) {
					log.debug("Found: "+tmp);
					return 
							QDoxUtils.getClass(packageName+"."+name,jdb);

				}
				return null;
				//throw new RuntimeException("Couldn't find: "+name);
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e); // all instances tested for already
			}
		}

		@Override
		public Object visit(FieldRef fieldRef) {
			try {
				Field fieldRefField = fieldRef.getClass().getDeclaredField("field");
				fieldRefField.setAccessible(true);

				Field fieldIndex = FieldRef.class.getDeclaredField("fieldIndex");
				fieldIndex.setAccessible(true);

				if (fieldRef.getField() == null) {
					String className = null;
					String[] fieldParts = fieldRef.getName().split("\\.");

					for (String imp: imports) {
						if (imp.endsWith(fieldParts[0])) {
							className = imp.substring(0, imp.lastIndexOf('.'));
						} 
					}
					if (className == null) className = packageName;
				
					
					String fqn = className+"."+fieldRef.getName();
					String containingClassFQN = fqn.substring(0,fqn.lastIndexOf("."));
					String fieldName = fqn.substring(fqn.lastIndexOf(".")+1);

					while (!containingClassFQN.isEmpty()) {

						try {
							Class<?> fieldClazz = Class.forName(containingClassFQN);
							Field reflectField = fieldClazz.getField(fieldName);
							if (reflectField.isEnumConstant()) { 
								return fieldName;
							} else {
								if (reflectField.isAccessible() && 
										Modifier.isStatic(reflectField.getModifiers())) {
									return reflectField.get(null);
								} else {
									throw new RuntimeException("Field must be static and public to be used here "+containingClassFQN+"."+fieldName);
								}
							}
						} catch (ClassNotFoundException | NoSuchFieldException e) {
							//The class / field is not known by reflection
						}
						
						if (QDoxUtils.containsClass(containingClassFQN,jdb)) {

							JavaClass fieldClass = QDoxUtils.getClass(containingClassFQN, jdb);
							fieldIndex.set(fieldRef, fieldParts.length - fieldName.split("\\.").length);
							JavaField field = fieldClass.getFieldByName(fieldName);

							if (field != null) {
								if (field.isEnumConstant()) {
									return fieldName;
								} else {
									fieldRefField.set(fieldRef, field);
									return super.visit(fieldRef);
								}
							} else {
								//Qdox doesn't know about this field
								//Maybe we can't know this thing yet because it is part of 
								//generated code that hasn't been built yet
								//lets ignore this but return a string...
								return fieldName;
							}
						} else {
							//QDox doesn't know about this class - it may not yet have been generated
						}

						//Shorten containingClassFQN and lengthen fieldName ansd try again
						fieldName = containingClassFQN.substring(containingClassFQN.lastIndexOf(".")+1)+"."+fieldName;
						if (containingClassFQN.contains(".")) {
							containingClassFQN = containingClassFQN.substring(0,containingClassFQN.lastIndexOf("."));
						} else {
							break;
						}
					}
				}

				
				if (context instanceof JavaPackage) {
					try {
						
						//This is a horrible hack from StackOverflow
						Field f = ClassLoader.class.getDeclaredField("classes");
						f.setAccessible(true);
						ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
						@SuppressWarnings("unchecked")
						Vector<Class<?>> tmp =  (Vector<Class<?>>) f.get(classLoader);
						ArrayList<Class<?>> classes = new ArrayList<>(tmp); 
						
						//classes.stream().forEach(System.out::println);
						
						String cls = fieldRef.getName().split("\\.")[0];
						String fld = fieldRef.getName().split("\\.")[1];
						
						//System.out.println(cls);
						
						List<Class<?>> matches = classes.stream()
							.filter(c -> c.getCanonicalName() != null && c.getCanonicalName().endsWith(cls))
							.collect(Collectors.toList());
						
						Field match = matches.stream()
								.flatMap(c -> Arrays.asList(c.getFields()).stream())
								.filter(fd -> fd.getName().equals(fld))
								.findFirst()
								.orElseThrow(() -> new Exception());
						
						if (match.isEnumConstant()) {
							return fld;
						} else {
							fieldRefField.set(fieldRef, match);
							return super.visit(fieldRef);
						}
						
					} catch (Exception e) {
						throw new RuntimeException("QDox can't handle package annotations "+
						"that are enums or constant fields, please try using the annotation processor "+
						"or simplifying your package level annotations", e);
					}
				} else {
					return null;
				}
				
			} catch (NoSuchFieldException| IllegalAccessException ignored) {
				throw new RuntimeException(ignored);
			} catch (ClassNotFoundException thrown) {
				throw new RuntimeException(thrown);
			}

			// basically we can't resolve the value of this field. Probably because it has not been
			// compiled yet. If we knew it was an enum we could return just the name and wait until 
			// later to map it back, but I don't think we can even figure that out.
			
			// there is however a gotcha. Even if we did everything perfectly QDox does not sort out package-info.java level
			// import statements properly so we may well have missed something that the compiler knows about
			
			
			/*throw new RuntimeException("QDox can't handle package annotations "+
					"that are enums or constant fields, please try using the annotation processor "+
					"or simplifying your package level annotations");*/
			
			/*String string = fieldRef.getName();
			if (string.contains(".")) string = string.substring(string.lastIndexOf(".")+1);
			if (string.equals(string.toUpperCase())) return string;*/
			
			
		}

		@Override
		protected Object getFieldReferenceValue(JavaField javaField) {

			//Check this is not known to the classloader already.
			try {
				Class<?> clz = Class.forName(javaField.getDeclaringClass().getFullyQualifiedName());
				try {
					Field fld = clz.getDeclaredField(javaField.getName());
					fld.setAccessible(true);
					return fld.get(clz.newInstance());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}

			} catch (ClassNotFoundException e) { 

				String tmp = javaField.getInitializationExpression();
				if (tmp == null) throw new RuntimeException("Could not find definition for: "+javaField.getDeclaringClass().getName()+"."+javaField.getName());
				if (tmp.startsWith("\"")) return tmp.substring(1, tmp.lastIndexOf('"'));
				if (tmp.startsWith("[")) return tmp.substring(1, tmp.lastIndexOf(']')).split(",");
				else return tmp;
			}


		}
	}


	public static JavaType box(JavaType returnType, JavaProjectBuilder jpb) {
		String value = returnType.getFullyQualifiedName();
		if ("void".equals(value)) return  jpb.getClassByName(Void.class.getCanonicalName());
		if ("boolean".equals(value)) return  jpb.getClassByName(Boolean.class.getCanonicalName());
		if ("byte".equals(value)) return  jpb.getClassByName(Byte.class.getCanonicalName());
		if ("char".equals(value)) return  jpb.getClassByName(Character.class.getCanonicalName());
		if ("short".equals(value)) return  jpb.getClassByName(Short.class.getCanonicalName());
		if ("int".equals(value)) return  jpb.getClassByName(Integer.class.getCanonicalName());
		if ("long".equals(value)) return  jpb.getClassByName(Long.class.getCanonicalName());
		if ("float".equals(value)) return  jpb.getClassByName(Float.class.getCanonicalName());
		if ("double".equals(value)) return  jpb.getClassByName(Double.class.getCanonicalName());
		if ("String".equals(value)) return  jpb.getClassByName(String.class.getCanonicalName());
		return returnType;
	}
}
