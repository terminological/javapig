package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Arrays;

import uk.co.terminological.javapig.annotations.Inverse;

public class JGetMethod extends JElement implements JTemplateInput  { 

	public JGetMethod(JProject model, String javaDoc, List<JAnnotation> annotations, JClassName declaringClass,
			JMethodName name, String returnTypeDefinition, JClassName returnType, JClassName underlyingType,
			boolean isDefault) {
		super(model, javaDoc, annotations);
		this.declaringClass = declaringClass;
		this.name = name;
		this.returnTypeDefinition = returnTypeDefinition;
		this.returnType = returnType;
		this.underlyingType = underlyingType;
		this.isDefault = isDefault;
	}
	
	public JGetMethod(JGetMethod copy) {
		this(
				copy.getModel(),
				copy.getJavaDoc(),
				new ArrayList<>(),
				copy.declaringClass,
				copy.getName(),
				copy.getReturnTypeDefinition(),
				copy.getReturnType(),
				copy.getUnderlyingType(),
				copy.isDefault()
		);
	}

	private JClassName declaringClass;
	private JMethodName name;
	private String returnTypeDefinition;
	private JClassName returnType;
	private JClassName underlyingType;
	private boolean isDefault;
	
	/*public boolean returnsComplexType() {
		return getUnderlyingClass() != null;
	}*/
	
	/**
	 * @return the JInterface that this method is defined in, if known to the current model. Otherwise null.
	 * 
	 */
	public JInterface getDeclaringClass() {
		JInterface tmp = getModel().findClass(declaringClass);
		return tmp;
	}
	
	/**
	 * returns true if this method is defined inthe supplied class
	 * @param test
	 * @return
	 */
	public boolean declaredBy(JInterface iface) {
		return getDeclaringClass() != null && getDeclaringClass().equals(iface);
	}
	
	/**
	 * 
	 * @return the JInterface that this method is defined in.
	 *
	@Override
	public boolean isInPackage(JName pkg) {
		return this.getDeclaringClass().isInPackage(pkg);
	}*/
	
	/*
	 * The underlying return type for a method is 
	 * @return the JInterface that represents an underlying .
	 *
	public JInterface getUnderlyingClass() {
		return getModel().findClass(underlyingType);
	}*/
	
	/**
	 * If this method is annotated with @Inverse and the value points to a method that is known
	 * to the compiler this will return a representation of that method. This can be used to generate
	 *  
	 * 
	 * If no annotation or the method is 
	 * unknown to the compiler (e.g. hasn't been generated yet) this will return null. The return value of
	 * this method will change over the various runs of the processor. T
	 * @return a JGetMethod for the inverse
	 */
	public JGetMethod getInverseMethod() {
		Inverse inv = this.getAnnotation(Inverse.class);
		if (inv ==null) return null;
		String methodName = inv.value();
		JMethodName tmp;
		tmp = JMethodName.from(methodName);
		return getModel().findMethod(tmp);
	}

	public JMethodName getName() {
		return name;
	}

	/**
	 * gives us a for of the return variable that we would plan to use in an interface specification
	 * i.e. uses primitives where they were present in the original specification, and is generics returns 
	 * the form that was specified in the original domain model spec.
	 * @return
	 */
	public String getInterfaceType() {
		if (isPrimitive()) return getReturnTypeDefinition(); 
		if (getUnderlyingType() == null) return getReturnType().getSimpleName();
		else {
			return getReturnType().getSimpleName()+"<"+getUnderlyingType().getSimpleName()+">";
		}
	}
	
	/**
	 * gives us either the primitive name (e.g. int) or the FQN of a non primitive class
	 * e.g. java.lang.String or if generic the simple Collection class name with the FQN of the
	 * type parameter e.g. List<java.lang.Integer>. These names can be used where it is complicated
	 * to get the imports right or there is a risk of name collision, and you want to define the abstract
	 * type rather than a specific concrete implementation.
	 * @return
	 */
	public String getInterfaceTypeFQN() {
		if (isPrimitive()) return getReturnTypeDefinition();
		if (getUnderlyingType() == null) return getReturnType().getCanonicalName();
		else {
			return getReturnType().getSimpleName()+"<"+getUnderlyingType().getCanonicalName()+">";
		}
	}
	
	/**
	 * gives us either the FQN of the return type. Boxed if needs be.
	 * e.g. java.lang.String or if generic the simple Collection class name with the FQN of the
	 * type parameter e.g. List<java.lang.Integer>. These names can be used within parameterised types
	 * @return
	 */
	public String getBoxedTypeFQN() {
		if (getUnderlyingType() == null) return getReturnType().getCanonicalName();
		else {
			return getReturnType().getSimpleName()+"<"+getUnderlyingType().getCanonicalName()+">";
		}
	}
	
	/**
	 * Gives us the short form of a return type in a format that is suitable for instantiating
	 * using default implementations of collection classes.
	 * @return
	 */
	public String getImplementationType() {
		if (getUnderlyingType() == null) return getReturnType().getSimpleName(); 
		try {
			Class<?> clazz = Class.forName(getReturnType().toString());
			if (List.class.isAssignableFrom(clazz)) {
				return "ArrayList<"+getUnderlyingType().getSimpleName()+">";
			} else if (Set.class.isAssignableFrom(clazz)) {
				return "HashSet<"+getUnderlyingType().getSimpleName()+">";
			} else if (Collection.class.isAssignableFrom(clazz)) {
				return "HashSet<"+getUnderlyingType().getSimpleName()+">";
			} else if (Optional.class.isAssignableFrom(clazz)) {
				return "Optional<"+getUnderlyingType().getSimpleName()+">";
			}
			return getReturnType().getSimpleName();
		} catch (ClassNotFoundException e) {
			return getReturnType().getSimpleName();
		}
	}
	
	/**
	 * This return a list of one or two items based on the return type of the
	 * get method. If this is a generic it will include the type of the generic 
	 * (e.g. java.util.Set) and the underlying type of the first type parameter 
	 * @return the fqn of the return type or types if a generic
	 */
	public Set<String> getImports() {
		Set<String> tmp = new HashSet<>();
		tmp.add(getReturnType().importName());
		if (getUnderlyingType() == null) return tmp;
		// This is a generic or an array
		if (!getReturnType().toString().equals(getUnderlyingType().importName())) {
			//This is a generic 
			tmp.add(getUnderlyingType().importName());
			Class<?> clazz;
			try {
				//The implementation type.
				clazz = Class.forName(getReturnType().toString());
				if (List.class.isAssignableFrom(clazz))
					tmp.add(ArrayList.class.getCanonicalName());
				if (Set.class.isAssignableFrom(clazz)) 
					tmp.add(HashSet.class.getCanonicalName());
				if (Optional.class.isAssignableFrom(clazz)) 
					tmp.add(HashSet.class.getCanonicalName());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			
		}	
		return tmp;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JGetMethod other = (JGetMethod) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	public boolean isParameterised() {
		return getUnderlyingType() != null;
	}
	
	public boolean isArray() {
		return returnType.isArray();
	}
	
	public boolean isInModel() {
		return getModel().interfaceIsDefined(returnType) || (underlyingType != null && getModel().interfaceIsDefined(underlyingType));
	}
	
	public boolean isTypeOf(String fqn) {
		if(getModel().interfaceIsDefined(returnType)) {
			JInterface iface = getModel().findClass(returnType);
			return iface.isTypeOf(fqn);
		}
		return returnType.typeOf(fqn).orElse(Boolean.FALSE);
	}
	
	public boolean isEnum() {
		if (isParameterised()) return false;
		return returnType.typeOf(Enum.class).orElse(Boolean.FALSE);
	}
	
	public boolean isCollection() {
		if (!isParameterised()) return false;
		return returnType.typeOf(Collection.class).orElse(Boolean.FALSE) ||
			returnType.typeOf(Iterable.class).orElse(Boolean.FALSE);
	}
	
	public boolean isList(boolean exact) {
		if (!isParameterised()) return false;
		if (exact) return returnType.equivalent(List.class.getCanonicalName());
		return returnType.typeOf(List.class).orElse(Boolean.FALSE);
	}
	
	public boolean isSet() {
		return isSet(false);
	}
	
	public boolean isList() {
		return isList(false);
	}
	
	public boolean isPrimitive() {
		return Arrays.asList(new String[] {"int","float","char","double","long","void","boolean","byte","short"}).contains(this.getReturnTypeDefinition());
	}
	
	public boolean isSet(boolean exact) {
		if (!isParameterised()) return false;
		if (exact) return returnType.equivalent(Set.class.getCanonicalName());
		return returnType.typeOf(Set.class).orElse(Boolean.FALSE);
	}
	
	public boolean isOptional() {
		if (!isParameterised()) return false;
		return returnType.typeOf(Optional.class).orElse(Boolean.FALSE);
	}
	
	public boolean hasInverseMethod() {
		return this.getInverseMethod() != null; //This may be null because the annotation contains things the compiler doesn't yet know about. 
	}
	
	public void setName(JMethodName methodName) {
		this.name = methodName;
	}

	public JClassName getReturnType() {
		return returnType;
	}

	public void setReturnType(JClassName returnType) {
		this.returnType = returnType;
	}

	/**
	 * If this method returns a paramerterised type (e.g. a list) this returns the underlying type of that list. 
	 * If it is not parameterised this will return null
	 * 
	 * @return a type as a JClassName
	 */
	public JClassName getUnderlyingType() {
		return underlyingType;
	}

	public void setUnderlyingType(JClassName underlyingType) {
		this.underlyingType = underlyingType;
	}

	public void setDeclaringClass(JClassName declaringClass) {
		this.declaringClass = declaringClass;
	}

	public String getReturnTypeDefinition() {
		return returnTypeDefinition;
	}

	public void setReturnTypeDefinition(String returnTypeDefinition) {
		this.returnTypeDefinition = returnTypeDefinition;
	}

	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public JGetMethod clone() {
		JGetMethod out = copy();
		out.setAnnotations(
			this.getAnnotations().stream().map(a -> a.clone()).collect(Collectors.toList())	
		);
		return out;
	}

	@Override
	public JGetMethod copy() {
		return new JGetMethod(this);
	}

	@Override
	public JPackageMetadata getMetadata() {
		return this.getDeclaringClass().getMetadata();
	}
	
	public String cardinality() {
		if (this.isCollection()) return "0..*";
		if (this.isOptional()) return "0..1";
		else return "1..1";
	}
	
}
