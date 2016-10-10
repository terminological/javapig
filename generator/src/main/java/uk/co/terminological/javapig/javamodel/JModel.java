package uk.co.terminological.javapig.javamodel;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.terminological.javapig.javamodel.tools.JModelComponent;

public class JModel extends JModelComponent {

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
				.anyMatch(p -> p.getName().equals(fqn));
	}
	
	public void addInterface(JInterface iface) {
		elements.remove(iface);
		elements.add(iface);
	}
	
	public void addMethod(JGetMethod method) {
		elements.remove(method);
		elements.add(method);
	}
	
	public void addPackage(JPackage pkg) {
		elements.remove(pkg);
		elements.add(pkg);
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
	
	public Set<String> getImports() {
		Set<String> tmp = new HashSet<>();
		for (JInterface iface: getClasses()) {
			tmp.add(iface.getName().toString());
			tmp.addAll(iface.getImports());
		}
		return tmp;
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
				.filter(p -> p.getName().equals(fqn))
				.findFirst().orElse(null);
	}

	public String getRootPackage() {
		return elements.stream()
				.filter(e -> e instanceof JPackage)
				.map(p -> ((JPackage) p).getName())
				.reduce((a,b) -> {return (a.length()<b.length()) ? a : b;}).orElse("");
	}
	
	public boolean isEnabled(String builtInName) {
		return getPackages().stream().allMatch(p -> p.getMetadata().isEnabled(builtInName));
	}
}
