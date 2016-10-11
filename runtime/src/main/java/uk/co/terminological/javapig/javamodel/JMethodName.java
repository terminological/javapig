package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;

public class JMethodName implements Serializable {

	String name;
	String classSimpleName;
	String classFQN;
	
	public static JMethodName from(String fqn) {
		if (fqn == null || fqn.isEmpty()) return null;
		return new JMethodName(fqn);
	}
	
	private JMethodName() {}
	
	private JMethodName(String fqn) {
		String tmp = fqn.split("#")[0];
		if (tmp.contains(".")) {
			this.classSimpleName = tmp.substring(tmp.lastIndexOf(".")+1);
			this.classFQN = tmp.substring(0,tmp.lastIndexOf("."));
		} else {
			this.classSimpleName = tmp;
		}
		if (fqn.contains("#")) {
			this.name = fqn.split("#")[1];
		} else {
			throw new RuntimeException("FQN: "+fqn);
		}
	}
	

	public String constant() {
		String tmp = methodBase().replaceAll("([a-z])([A-Z])","$1_$2");
		return tmp.toUpperCase();
	}

	public String className() {
		String tmp = methodBase();
		return tmp.substring(0, 1).toUpperCase()+tmp.substring(1);
	}
	
	public String setter() {
		return prefix("set");
	}
	
	public String field() {
		return "_"+methodBase();
	}
	
	public String getter() {
		return name;
	}
	
	public String prefix(String s) {
		String tmp = methodBase();
		return s+tmp.substring(0,1).toUpperCase()+tmp.substring(1);
	}
	
	public String with() {
		return prefix("with").replaceAll("ies$", "y").replaceAll("s$", "");
	}
	
	public String adder() {
		return prefix("add").replaceAll("ies$", "y").replaceAll("s$", "");
	}
	
	public String addAll() {
		return prefix("addAll");
	}
	
	public String methodBase() {
		String tmp = name;
		tmp = tmp.replaceFirst("^get", "");
		tmp = tmp.replaceFirst("^is", "");
		return tmp.substring(0,1).toLowerCase()+tmp.substring(1);
	}
	
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

	
	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}
}
