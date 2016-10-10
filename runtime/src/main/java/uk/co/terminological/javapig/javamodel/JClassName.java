package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.Optional;

public class JClassName implements Serializable {

	String fullyQualifiedName;
	String packageName;
	String className;
	
	public static JClassName from(String s) {
		if (null == s || s.isEmpty()) return null;
		return new JClassName(s);
	}
	
	private JClassName() {}
	
	private JClassName(String s) {
		if (s.contains("<")) s = s.substring(0,s.indexOf("<"));
		
		String tmp = s.replaceFirst("\\.([A-Z])", "!$1");
		if (tmp.contains("!")) {
			this.fullyQualifiedName = s;
			packageName = tmp.split("!")[0];
			className = tmp.split("!")[1];
		} else {
			packageName = "java.lang";
			className = tmp;
			this.fullyQualifiedName = packageName+"."+className;
		}
	}
	
	public String getPackageName() {
		return packageName;
	}
	
	public String getSimpleName() {
		return className;
	}
	
	public String toString() {return fullyQualifiedName;}
	
	public String getCanonicalName() {return fullyQualifiedName;}
	
	public String importName() {
		return packageName+"."+(className.contains(".") ?
				className.substring(0,className.indexOf("."))
					: className.replace("[]", ""));
	}

	public boolean equivalent(String fqn) {
		return fullyQualifiedName.equals(fqn);
	}
	
	public boolean equivalent(Class<?> clazz) {
		return fullyQualifiedName.equals(clazz.getCanonicalName());
	}
	
	public boolean isCompiled() {
		return convert().isPresent();
	}
	
	protected Optional<Boolean> typeOf(String fqn) {
		try {
			return typeOf(Class.forName(fqn));
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected Optional<Boolean> typeOf(Class<?> clazz) {
		if (convert().isPresent())
			return Optional.of(clazz.isAssignableFrom(convert().get()));
		else 
			return Optional.empty();
	}

	/*protected Class<?> convert() throws ClassNotFoundException {
		Class<?> tmp = Class.forName(importName());
		if (isArray()) return Array.newInstance(tmp, 0).getClass();
		return tmp;
	}*/
	
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
	
	public boolean isArray() {
		return fullyQualifiedName.endsWith("[]");
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
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
		if (!(obj instanceof JClassName)) {
			return false;
		}
		JClassName other = (JClassName) obj;
		if (fullyQualifiedName == null) {
			if (other.fullyQualifiedName != null) {
				return false;
			}
		} else if (!fullyQualifiedName.equals(other.fullyQualifiedName)) {
			return false;
		}
		return true;
	}

	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}
}
