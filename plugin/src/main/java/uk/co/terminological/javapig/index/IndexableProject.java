package uk.co.terminological.javapig.index;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JProject;

/**
 * An extended {@link JProject} that provides project-wide indexing metadata for the index plugin.
 *
 * <p>Aggregates all indexed interfaces and methods across the entire project,
 * enabling generation of index containers that span multiple packages.</p>
 *
 * @see IndexablePlugin
 * @see IndexableInterface
 */
public class IndexableProject extends JProject {

	/**
	 * Constructs an indexable project by wrapping the given project.
	 *
	 * @param in the project to wrap
	 */
	public IndexableProject(JProject in) {
		super(in);
	}
	
	/**
	 * Returns all interfaces in the project that have at least one indexed method
	 * (annotated with {@code @Id}, {@link Primary}, {@link Secondary}, or {@link Searchable}).
	 *
	 * @return the set of indexed interfaces
	 */
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
	
	/**
	 * Returns all methods annotated with {@code @Id} or {@link Primary} across the project.
	 *
	 * @return the set of primary index methods
	 */
	public Set<JGetMethod> getPrimaryIndexes() {
		return this.getMethods().stream()
			.filter(m -> (m.isAnnotationPresent(Id.class) || m.isAnnotationPresent(Primary.class)))
			.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all methods annotated with {@link Secondary} across the project.
	 *
	 * @return the set of secondary index methods
	 */
	public Set<JGetMethod> getSecondaryIndexes() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Secondary.class))
			.collect(Collectors.toSet());
	}

	/**
	 * Returns all methods annotated with {@link Searchable} across the project.
	 *
	 * @return the set of searchable methods
	 */
	public Set<JGetMethod> getSearchIndexes() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Searchable.class))
			.collect(Collectors.toSet());
	}
	
}
