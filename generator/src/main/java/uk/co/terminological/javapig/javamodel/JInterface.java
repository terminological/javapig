package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The interface describes the basic class structure of the overall model. It provides functions to access the methods of the model
 * and several other utility functions for generating code templates. <br>
 * The interface instances are constructed during scanning of the source code
 * @author terminological
 *
 */
public class JInterface extends JElement implements JTemplateInput  { 

	public JInterface(JProject model, String javaDoc, List<JAnnotation> annotations, JClassName name,
			Set<JClassName> supertypes) {
		super(model, javaDoc, annotations);
		this.name = name;
		this.supertypes = supertypes;
	}
	
	public JInterface(JInterface copy) {
		this(
				copy.getModel(),
				copy.getJavaDoc(),
				new ArrayList<>(),
				copy.getName(),
				copy.getSupertypes()
		);
	}

	private JClassName name;
	private Set<JClassName> supertypes = new LinkedHashSet<>();
	
	/*
	 * Lists the directly declared supertypes of this interface. 
	 * @return A list of {@link JClassName} describing the interfaces. These may include interfaces that are declared outside of the overall model.
	 *
	public Set<JClassName> supertypeNames() {
		return getSupertypes();
	}*/
	
	/**
	 * Designed to return a list of imports this interface depends on plus any additional imports that are specified when the method is called
	 * @param additional
	 * @return a list of fully qualified names suitable for an "import ....;" statement
	 */
	public List<String> getImports(String... additional) {
		Set<String> tmp = new HashSet<>();
		for (JGetMethod method: getMethods()) {
			for (String imp: method.getImports()) { 
				if (!imp.startsWith("java.lang")) {
					if (imp.startsWith("java.util")) tmp.add("java.util.*");
					else tmp.add(imp);
				}
			}
		}
		for (String imp: additional) { 
			if (!imp.startsWith("java.lang")) {
				if (imp.startsWith("java.util")) tmp.add("java.util.*");
				else tmp.add(imp);
			}
		}
		return tmp.stream().sorted().collect(Collectors.toList());
	}

	/**
	 * The name of this class 
	 * @return a {@link JClassName}
	 */
	public JClassName getName() {
		return name;
	}

	/**
	 * Accesses the defined getter methods of the interface, and those defined in supertypes. This does not include methods that are Java 8 
	 * default implementations, and only includes definitions that require to be implemented. This does not guarantee order (but seems to 
	 * follow the order they are declared in) 
	 * @return a list of {@link JGetMethod}
	 */
	public List<JGetMethod> getMethods() {
		List<JGetMethod> out = new ArrayList<>();
		out.addAll(
				getModel().getMethods().stream()
				.filter(m -> 
					m.declaredBy(this) &&
					!m.isDefault()
					)
				.collect(Collectors.toList()));
		out.addAll(
				this.getSupertypes().stream()
					.filter(cn -> getModel().interfaceIsDefined(cn))
					.map(cn -> getModel().findClass(cn))
					.flatMap(i -> i.getMethods().stream())
					.collect(Collectors.toList())
			);
		return out;
	}

	/**
	 * This provides a list of all the methods defined in the interface in question and all of  its supertypes. This includes methods that have 
	 * default implementations. 
	 * @return a list of {@link JGetMethod}s
	 */
	public List<JGetMethod> getExtendedMethods() {
		List<JGetMethod> out = new ArrayList<>();
		out.addAll(
				getModel().getMethods().stream()
				.filter(m -> 
					m.declaredBy(this)
					)
				.collect(Collectors.toList()));
		out.addAll(
				this.getSupertypes().stream()
					.filter(cn -> getModel().interfaceIsDefined(cn))
					.map(cn -> getModel().findClass(cn))
					.flatMap(i -> i.getMethods().stream())
					.collect(Collectors.toList())
			);
		return out;
	}
	
	/**
	 * Lists the directly declared supertypes of this interface. 
	 * @return A list of {@link JClassName} describing the interfaces. These may include interfaces that are declared outside of the overall model.
	 */
	public Set<JClassName> getSupertypes() {
		return supertypes;
	}

	protected void setSupertypes(Set<JClassName> supertypes) {
		this.supertypes = supertypes;
	}

	protected void setName(JClassName name) {
		this.name = name;
	}

	/**
	 * Provides access to the immediate package of this class
	 * @return a {@link JPackage}
	 */
	public JPackage getPkg() {
		return getModel().findPackage(this.getName().getPackageName());
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
		JInterface other = (JInterface) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * This provides a naive implementation of a typeOf function that handles the simple cases. It won't
	 * necessarily work in all cases but is good enough to determine if something is a Collection, or similar
	 * @param fqn - the fully qualified name of a Class
	 * @return true if the interface represented by the FQN is a supertype of the interface.
	 */
	public boolean isTypeOf(String fqn) {
		if (this.supertypes.stream().anyMatch(s -> s.equivalent(fqn))) return true;
		if (this.supertypes.stream()
			.filter(t -> getModel().interfaceIsDefined(t))
			.map(t -> getModel().findClass(t))
			.anyMatch(i -> i.isTypeOf(fqn))) return true;
		return this.supertypes.stream()
			.filter(t -> t.isCompiled())
			.map(t -> t.typeOf(fqn))
			.anyMatch(b -> Boolean.TRUE.equals(b));
		
	}

	@Override
	public JInterface clone() {
		JInterface out = copy();
		out.setAnnotations(
				this.getAnnotations().stream().map(a -> a.clone()).collect(Collectors.toList())	
		);
		return out;
	}

	@Override
	public JInterface copy() {
		return new JInterface(this);
	}

	@Override
	public JPackageMetadata getMetadata() {
		return this.getPkg().getMetadata();
	}

}
