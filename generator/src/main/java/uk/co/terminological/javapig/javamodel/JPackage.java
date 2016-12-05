package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class JPackage extends JElement implements JTemplateInput {
	
	private String name;
	private JPackageMetadata metadata;
	
	public Set<JInterface> getClasses() {
		Set<JInterface> tmp = getModel().getClasses().stream().filter(i -> i.getName().getPackageName().equals(name)).collect(Collectors.toSet());
		return tmp;
	}

	public Set<JPackage> getPackages() {
		Set<JPackage> tmp = getModel().getPackages().stream()
				.filter(p -> p.isInPackage(this.getName()))
				.collect(Collectors.toSet());
		return tmp;
	}
	
	public Set<JGetMethod> getMethods() {
		return getModel().getMethods().stream()
				.filter(i -> i.isInPackage(getName()))
				.collect(Collectors.toSet());
	}
	
	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}
	
	public boolean isEnabled(String builtInName) {
		return getMetadata().isEnabled(builtInName);
	}
	
	//JavaBean
	
	public JPackage(JProject model, String javaDoc, List<JAnnotation> annotations, String name, Optional<JPackageMetadata> metdata) {
		super(model,javaDoc,annotations);
		this.name = name;
		this.metadata = metdata.orElse(null);
	}
	
	public JPackage(JPackage copy) {
		this(
			copy.getModel(),
			copy.getJavaDoc(),
			new ArrayList<>(),
			copy.getName().toString(),
			Optional.ofNullable(copy.metadata)
		);
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
		JPackage other = (JPackage) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	// Getters & setters
	
	public JName getName() {
		return new JName() {
			@Override
			public String getClassName() {
				return name;
			}

			@Override
			public String getCanonicalName() {
				return name;
			}

			@Override
			public String getSimpleName() {
				return name;
			}
			
			public String toString() {
				return name;
			}
			
		};
	}

	public void setName(String packageName) {
		this.name = packageName;
	}

	public JPackageMetadata getMetadata() {
		if (metadata != null) return metadata;
		return this.getParent().map(p -> p.getMetadata()).orElse(null);
	}

	public Set<String> getImports() {
		Set<String> tmp = new HashSet<>();
		for (JInterface iface: getClasses()) {
			tmp.add(iface.getName().toString());
			tmp.addAll(iface.getImports());
		}
		return tmp;
	}
	
	
	public void setMetadata(JPackageMetadata metadata) {
		this.metadata = metadata;
	}

	@Override
	public JPackage clone() {
		JPackage out = copy();
		out.setAnnotations(
			this.getAnnotations().stream().map(a -> a.clone()).collect(Collectors.toList())	
		);
		out.setMetadata(metadata.clone());
		return out;
	}

	@Override
	public JPackage copy() {
		return new JPackage(this);
	}
	
	public Optional<JPackage> getParent() {
		String pkgname = this.getName().getCanonicalName().toString();
		if (!pkgname.contains(".")) return null; 
		String parentPkgname = pkgname.substring(0, pkgname.lastIndexOf("."));
		return Optional.ofNullable(this.getModel().findPackage(parentPkgname));
	}

	public boolean isRootPackage() {
		return metadata != null;
	}
}
