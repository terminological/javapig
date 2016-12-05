package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JProject extends JProjectComponent implements Project {//, JTemplateInput {

	public JProject() {
		
	}
	
	public JProject(JProject copy) {
		this();
	}
	
	private Set<JElement> elements = new LinkedHashSet<>();
	
	public boolean interfaceIsDefined(JClassName fqn) {
		return elements.stream()
				.filter(e -> e instanceof JInterface)
				.map(e -> (JInterface) e)
				.anyMatch(i -> i.getName().equals(fqn));
	}
	
	public boolean methodIsDefined(JMethodName fqn) {
		return elements.stream()
				.filter(e -> e instanceof JGetMethod)
				.map(e -> (JGetMethod) e)
				.anyMatch(m -> m.getName().equals(fqn));
	}
	
	public boolean packageIsDefined(String fqn) {
		return elements.stream()
				.filter(e -> e instanceof JPackage)
				.map(e -> (JPackage) e)
				.anyMatch(p -> p.getName().toString().equals(fqn));
	}
	
	public void addInterface(JInterface iface) {
		elements.remove(iface);
		elements.add(iface);
		iface.setModel(this);
	}
	
	public void addMethod(JGetMethod method) {
		elements.remove(method);
		elements.add(method);
		method.setModel(this);
	}
	
	public void addPackage(JPackage pkg) {
		elements.remove(pkg);
		elements.add(pkg);
		pkg.setModel(this);
	}
	
	public Collection<JInterface> getClasses() {
		return elements.stream()
				.filter(e -> e instanceof JInterface)
				.map(e -> (JInterface) e)
				.collect(Collectors.toList());
	}
	public Collection<JGetMethod> getMethods() {
		return elements.stream()
				.filter(e -> e instanceof JGetMethod)
				.map(e -> (JGetMethod) e)
				.collect(Collectors.toList());
	}
	public Collection<JPackage> getPackages() {
		return elements.stream()
				.filter(e -> e instanceof JPackage)
				.map(e -> (JPackage) e)
				.collect(Collectors.toList());
	}
	public Collection<JPackage> getRootPackages() {
		return elements.stream()
				.filter(e -> e instanceof JPackage)
				.map(e -> (JPackage) e)
				.filter(p -> p.isRootPackage())
				.collect(Collectors.toList());
	}
	
	
	public JInterface findClass(JClassName c) {
		return elements.stream()
				.filter(e -> e instanceof JInterface)
				.map(e -> (JInterface) e)
				.filter(i -> i.getName().equals(c))
				.findFirst().orElse(null);
	}
	
	public JGetMethod findMethod(JMethodName mn) {
		return elements.stream()
				.filter(e -> e instanceof JGetMethod)
				.map(e -> (JGetMethod) e)
				.filter(m -> m.getName().equals(mn))
				.findFirst().orElse(null);
	}
	
	public JPackage findPackage(String fqn) {
		return elements.stream()
				.filter(e -> e instanceof JPackage)
				.map(e -> (JPackage) e)
				.filter(p -> p.getName().toString().equals(fqn))
				.findFirst().orElse(null);
	}

	@Override
	public JProject clone() {
		JProject out = copy();
		this.getPackages().stream().forEach(p -> out.addPackage(p.clone()));
		this.getClasses().stream().forEach(c -> out.addInterface(c.clone()));
		this.getMethods().stream().forEach(m -> out.addMethod(m.clone()));
		return out;
	}

	@Override
	public JProject copy() {
		return new JProject(this);
	}

	/*@Override
	public JName getName() {
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public Collection<JInterface> getClasses(JTemplateInput pckge) {
		Set<JInterface> tmp = this.getClasses().stream()
				.filter(
						i -> i.getName().getPackageName().startsWith(pckge.getName().toString()))
				.collect(Collectors.toSet());
		return tmp;
	}

	@Override
	public Collection<JGetMethod> getMethods(JTemplateInput in) {
		JInterface iface = (JInterface) in;
		List<JGetMethod> out = new ArrayList<>();
		out.addAll(
				this.getMethods().stream()
				.filter(m -> 
					
							(
									m.getDeclaringClass().equals(iface) ||
									iface.getSupertypes().contains(m.getDeclaringClass().getName())
							)
							&& !m.isDefault())
				.collect(Collectors.toList()));
		return out;
	}

	/*@Override
	public JPackageMetadata getMetadata() {
		return this.findPackage(this.getRootPackage()).getMetadata();
	}*/
}
