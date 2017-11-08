package uk.co.terminological.javapig.index;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JProject;

public class IndexableProject extends JProject {

	public IndexableProject(JProject in) {
		super(in);
	}
	
	public Set<IndexableInterface> getIndexedClasses() {
		return this.getMethods().stream()
				.filter(
					m -> (m.isAnnotationPresent(Id.class) || 
					m.isAnnotationPresent(Primary.class) ||
					m.isAnnotationPresent(Secondary.class) ||
					m.isAnnotationPresent(Searchable.class)
					)
				)
				.map(m -> m.getDeclaringClass())
				.map(c -> (IndexableInterface) c)
				.collect(Collectors.toSet());
	}
	
	public Set<JGetMethod> getPrimaryIndexes() {
		return this.getMethods().stream()
			.filter(m -> (m.isAnnotationPresent(Id.class) || m.isAnnotationPresent(Primary.class)))
			.collect(Collectors.toSet());
	}
	
	public Set<JGetMethod> getSecondaryIndexes() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Secondary.class))
			.collect(Collectors.toSet());
	}

	public Set<JGetMethod> getSearchIndexes() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Searchable.class))
			.collect(Collectors.toSet());
	}
	
}
