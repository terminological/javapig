package uk.co.terminological.javapig.javamodel;

import java.util.Set;
import java.util.stream.Collectors;

public class JPackage extends JElement {
	
	private String name;
	private JPackageMetadata metadata = new JPackageMetadata();
	
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

	public String getName() {
		return name;
	}

	public void setName(String packageName) {
		this.name = packageName;
	}

	public Set<JInterface> getClasses() {
		return getModel().getClasses().stream().filter(i -> i.getName().getPackageName().equals(name)).collect(Collectors.toSet());
	}

	/*public void setClasses(Set<JInterface> classes) {
		this.classes = classes;
	}*/

	public JPackageMetadata getMetadata() {
		return metadata;
	}

	public void setMetadata(JPackageMetadata metadata) {
		this.metadata = metadata;
	}
	
	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}

}
