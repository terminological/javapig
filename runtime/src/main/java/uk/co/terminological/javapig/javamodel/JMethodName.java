package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;

/**
 * A method name is the main
 * @author terminological
 *
 */
public class JMethodName implements Serializable, JName {

	String name;
	String classSimpleName;
	String classFQN;
	
	/**
	 * Creating a method name requires a fqn of the method. This is not directly obtained from the class
	 * as the method may be inherited  <br>
	 * 
	 * e.g. the fqn might be org.example.Class#getThing <br>
	 * or sometimes it might be Integer#toString  <br>
	 * but not getThing()
	 * 
	 * @param fqn - the FQN
	 * @return a method name object
	 */
	public static JMethodName from(String fqn) {
		if (fqn == null || fqn.isEmpty()) return null;
		return new JMethodName(fqn);
	}
	
	private JMethodName() {}
	
	private JMethodName(String fqn) {
		String tmp = fqn.split("#")[0];
		this.classFQN = tmp;
		if (tmp.contains(".")) {
			this.classSimpleName = tmp.substring(tmp.lastIndexOf(".")+1);
		} else {
			this.classSimpleName = tmp;
		}
		if (fqn.contains("#")) {
			this.name = fqn.split("#")[1];
		} else {
			throw new RuntimeException("FQN: "+fqn);
		}
	}
	
	/**
	 * a utility method for identifying a constant that related to a method <br>
	 * e.g. if the method was org.example.Class#getSomeConstant  <br>
	 * then the constant would be:  SOME_CONSTANT
	 * 
	 * @return an upper case underscore seperated string for a constant name for this method
	 */
	public String constant() {
		String tmp = methodBase().replaceAll("([a-z])([A-Z])","$1_$2");
		return tmp.toUpperCase();
	}

	/**
	 * a utility method for identifying a class that relates to a method <br>
	 * e.g. if the method was org.example.Class#getSomeClass  <br>
	 * then the class would be:  SomeClass
	 *  
	 * @return a CamelCase String with initial capital for use as a class name
	 */
	public String getClassName() {
		String tmp = methodBase();
		return tmp.substring(0, 1).toUpperCase()+tmp.substring(1);
	}
	
	/**
	 * a utility method for identifying a setter that relates to a method <br>
	 * e.g. if the method was org.example.Class#getSomeThing  <br>
	 * then the setter would be:  setSomeThing
	 *  
	 * @return a CamelCase String starting with "set" for use as a setter method name
	 */
	public String setter() {
		return prefix("set");
	}
	
	/**
	 * a utility method for identifying a field that relates to a method <br>
	 * e.g. if the method was org.example.Class#getSomeThing  <br>
	 * then the setter would be:  _someThing
	 *  
	 * @return a CamelCase String starting with "_" and lower case for use as a field name
	 */
	public String field() {
		return "_"+methodBase();
	}
	
	/**
	 * a utility method for identifying a getter that relates to a method <br>
	 * e.g. if the method was org.example.Class#getSomeThing  <br>
	 * then the setter would be:  getSomeThing <br>
	 * e.g. if the method was org.example.Class#someThing  <br>
	 * then the getter would be:  someThing
	 *  
	 * @return a CamelCase String possibly starting with get and lower case for use as a getter
	 */
	public String getter() {
		return name;
	}
	
	/**
	 * Allows for a new arbitrary method name related to the base getter method 
	 * @param s - the prefix
	 * @return e.g. if prefix is "test" and method was "org.example.Class#getSomething" then will return "testSomething"
	 */
	public String prefix(String s) {
		String tmp = methodBase();
		return s+tmp.substring(0,1).toUpperCase()+tmp.substring(1);
	}
	
	/**
	 * Allows for a fluent setter method based on the base getter method
	 * @return e.g. if method was "org.example.Class#getSomething" then will return "withSomething"
	 */
	public String with() {
		return prefix("with").replaceAll("ies$", "y").replaceAll("s$", "");
	}
	
	/**
	 * Allows for an adder method based on the base getter method
	 * @return e.g. if method was "org.example.Class#getListOfObjects" then will return "addListOfObject"
	 */
	public String adder() {
		return prefix("add").replaceAll("ies$", "y").replaceAll("s$", "");
	}
	
	/**
	 * Allows for an adder method based on the base getter method
	 * @return e.g. if method was "org.example.Class#getListOfObjects" then will return "addAllListOfObjects"
	 */
	public String addAll() {
		return prefix("addAll");
	}
	
	/**
	 * Gets the unadorned method in the singular form with no "get" or "is" prefix 
	 * @return e.g. if method was "org.example.Class#getListOfThings" then will return "listOfThing"
	 */
	public String methodBase() {
		String tmp = name;
		tmp = tmp.replaceFirst("^get", "");
		tmp = tmp.replaceFirst("^is", "");
		return tmp.substring(0,1).toLowerCase()+tmp.substring(1);
	}
	
	/**
	 * returns the FQN of the method with hash separating the method name
	 * @return e.g. org.example.Class#getSomeThing
	 */
	public String toString() {return classSimpleName+"#"+name;}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classSimpleName == null) ? 0 : classSimpleName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof JMethodName)) {
			return false;
		}
		JMethodName other = (JMethodName) obj;
		if (classSimpleName == null) {
			if (other.classSimpleName != null) {
				return false;
			}
		} else if (!classSimpleName.equals(other.classSimpleName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	/**
	 * This returns an alphanumeric code that is unique for this method name and consists only of letters & numbers
	 * @return e.g. IDM123122223
	 */	
	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}

	@Override
	public String getCanonicalName() {
		return classFQN+"#"+name;
	}

	@Override
	public String getSimpleName() {
		return name;
	}
}
