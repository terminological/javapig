package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JInterface extends JElement { 

	private String pkg;
	private JClassName name;
	private Set<JClassName> supertypes = new LinkedHashSet<>();
	
	public Set<JClassName> supertypeNames() {
		return getSupertypes();
	}
	
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

	public JClassName getName() {
		return name;
	}

	public List<JGetMethod> getMethods() {
		List<JGetMethod> out = new ArrayList<>();
		out.addAll(
				getModel().getMethods().stream()
				.filter(m -> 
					m.getDeclaringClass().equals(this) &&
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

	public List<JGetMethod> getExtendedMethods() {
		List<JGetMethod> out = new ArrayList<>();
		out.addAll(
				getModel().getMethods().stream()
				.filter(m -> 
					m.getDeclaringClass().equals(this)
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
	
	public Set<JClassName> getSupertypes() {
		return supertypes;
	}

	public void setSupertypes(Set<JClassName> supertypes) {
		this.supertypes = supertypes;
	}

	public void setName(JClassName name) {
		this.name = name;
	}

	public JPackage getPkg() {
		return getModel().findPackage(pkg);
	}

	public void setPkg(String fqn) {
		this.pkg = fqn;
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

}
