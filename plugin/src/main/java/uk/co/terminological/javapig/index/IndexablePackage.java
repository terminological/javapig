package uk.co.terminological.javapig.index;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Id;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JPackage;

public class IndexablePackage extends JPackage {

	public IndexablePackage(JPackage in) {
		super(in);
	}
	
	public List<IndexableInterface> getIndexedClasses() {
		return this.getClasses().stream()
				.filter(c -> c instanceof IndexableInterface)
				.map(c -> ((IndexableInterface) c))
				.filter(id -> id.isIndexed())
				.collect(Collectors.toList());
	}
	
	public List<JGetMethod> getPrimaryIndexes() {
		return this.getMethods().stream()
			.filter(m -> (m.isAnnotationPresent(Id.class) || m.isAnnotationPresent(Primary.class)))
			.collect(Collectors.toList());
	}
	
	public List<JGetMethod> getSecondaryIndexes() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Secondary.class))
			.collect(Collectors.toList());
	}

	public List<JGetMethod> getSearchIndexes() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Searchable.class))
			.collect(Collectors.toList());
	}
	
}
