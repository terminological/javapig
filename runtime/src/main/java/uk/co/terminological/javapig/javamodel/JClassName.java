package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Optional;

/**
 * The class name is a holder for identifying information about a class or type.
 * It does not contain information about generics which are out of scope of javapig.
 * @author terminological
 *
 */
public class JClassName implements Serializable, JName {

	//String fullyQualifiedName;
	String packageNameOrNull;
	String className;
	
	/**
	 * Create a class name from a fully qualified name
	 * 
	 * @param s
	 * @return the class name
	 */
	public static JClassName from(String s) {
		if (null == s || s.isEmpty()) return null;
		return new JClassName(s);
	}
	
	private JClassName() {}
	
	/*
	 * Strips generics if present.
	 * Splits in first capital letter.
	 * If no default package name it is assumed to be java.lang
	 * This is needed at java.lang.String for example is often not fully qualified
	 * Leaves array brackets (? should it)
	 */
	private JClassName(String s) {
		if (s.contains("<")) s = s.substring(0,s.indexOf("<"));
		
		String tmp = s.replaceFirst("\\.([A-Z])", "!$1");
		if (tmp.contains("!")) {
			//this.fullyQualifiedName = s;
			packageNameOrNull = tmp.split("!")[0];
			className = tmp.split("!")[1];
		} else {
			packageNameOrNull = null;
			className = tmp;
			//this.fullyQualifiedName = null;
		}
	}
	
	/**
	 * Get the package name for this class <br> <br>
	 * e.g. is the FQN is "org.example.test.Class.SubClass" <br>
	 * then the simple name is "Class.Subclass". If this is a primitive the 
	 * package is returned as "java.lang"
	 * @return The FQN of this class's package as a String
	 */
	public String getPackageName() {
		return packageNameOrNull == null ? "java.lang" : packageNameOrNull;
	}
	
	/**
	 * The simple name is the class name you use to refer to a type  <br>
	 * This assumes the model is using standard Java conventions with a classname in CamelCase with 
	 * first letter capital <br> <br>
	 * e.g. is the FQN is "org.example.test.Class.SubClass" <br>
	 * then the simple name is "Class.Subclass"
	 * 
	 * If this represents a primitive type then it will be the "int" part of the name, or int[] in the case
	 * of an array
	 * 
	 * @return String
	 */
	public String getSimpleName() {
		return className;
	}
	
	public String toString() {return getCanonicalName();}
	
	/**
	 * The canonical name of the class  <br> <br>
	 * e.g. is the FQN is "org.example.test.Class.SubClass"  <br>
	 * then the canonical name is "org.example.test.Class.SubClass"
	 *
	 * if this represents a primitive type then it will be the fqn of the boxed class. e.g. java.lang.Integer[]
	 
	 * 
	 * @return
	 */
	public String getCanonicalName() {return box();}
	
	/**
	 * The part of the name used for import statements <br>
	 * e.g. is the FQN is "org.example.test.Class.SubClass" <br>
	 * then the import name is "org.example.test.Class" <br>
	 * 	 
	 * If this represents a primitive type then it will be the fqn of the boxed class. e.g. java.lang.Integer
	 * 
	 * @return the component of the name for an import statement as a String
	 */
	public String importName() {
		if (className.contains(".")) {
			return getPackageName()+"."+className.substring(0,className.indexOf("."));
		} else {
			return getCanonicalName().replace("[]", "");
		}
	}

	
	/**
	 * Determines if a class name is exactly equal to another class (as a String) <br>
	 * @param fqn - the String of the fqn (including [] for arrays)
	 * @return true if fqn is the same as the class name
	 */
	public boolean equivalent(String fqn) {
		return getCanonicalName().equals(box(fqn));
	}
	
	/**
	 * During code creation some classes are compiled already - typically those that are
	 * imported from another package. 
	 * @return true if the code is already known to the ClassLoader
	 */
	public boolean isCompiled() {
		return convert().isPresent();
	}
	
	/*
	 * internal function check for type equivalence assuming compiled classes only
	 * This is used by JInterface
	 */
	protected Optional<Boolean> typeOf(String fqn) {
		try {
			return typeOf(Class.forName(fqn));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	/*
	 * internal function check for type equivalence assuming compiled classes only
	 * This is used by JInterface
	 */
	protected Optional<Boolean> typeOf(Class<?> clazz) {
		if (convert().isPresent())
			return Optional.of(clazz.isAssignableFrom(convert().get()));
		else 
			return Optional.empty();
	}

	/*
	 * internal function get a Class<?> for a JClassName is it is compiled
	 */
	protected Optional<Class<?>> convert() {
		Class<?> tmp;
		try {
			tmp = Class.forName(importName());
		if (isArray()) return Optional.of(Array.newInstance(tmp, 0).getClass());
		return Optional.of(tmp);
		} catch (ClassNotFoundException e) {
			return Optional.empty();
		}
	}
	
	/**
	 * Array classes are denoted by trailing square brackets
	 * 
	 * e.g. org.example.SomeClass[]
	 * @return true if this is an Array class
	 */
	public boolean isArray() {
		return className.endsWith("[]");
	}
	
	
	@Override
	public int hashCode() {
		return getCanonicalName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof JClassName)) {
			return false;
		}
		return this.getCanonicalName().equals(((JClassName) obj).getCanonicalName());
	}

	/**
	 * This returns an alphanumeric code that is unique for this class name and consists only of letters & numbers
	 * @return e.g. IDM123122223
	 */
	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}

	@Override
	public String getClassName() {
		return getCanonicalName();
	}
	
	public String box() {
		return box(packageNameOrNull == null ? className :packageNameOrNull+"."+className);
	}
	
	public static String box(String className) {
		String value = className.replace("[]", "");
		if ("void".equals(value) || "Void".equals(value)) 
			value = Void.class.getCanonicalName();
		if ("boolean".equals(value) || "Boolean".equals(value)) 
			value = Boolean.class.getCanonicalName();
		if ("byte".equals(value) || "Byte".equals(value)) 
			value = Byte.class.getCanonicalName();
		if ("char".equals(value) || "Char".equals(value)) 
			value = Character.class.getCanonicalName();
		if ("short".equals(value) || "Short".equals(value))
			value = Short.class.getCanonicalName();
		if ("int".equals(value) || "Integer".equals(value)) 
			value = Integer.class.getCanonicalName();
		if ("long".equals(value) || "Long".equals(value)) 
			value = Long.class.getCanonicalName();
		if ("float".equals(value) || "Float".equals(value)) 
			value = Float.class.getCanonicalName();
		if ("double".equals(value) || "Double".equals(value)) 
			value = Double.class.getCanonicalName();
		if ("String".equals(value)) 
			value = String.class.getCanonicalName();
		return value + (className.endsWith("[]") ? "[]" : "");
	}
}
