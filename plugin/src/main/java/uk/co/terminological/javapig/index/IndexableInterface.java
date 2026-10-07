package uk.co.terminological.javapig.index;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;
import javax.persistence.OneToMany;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;

/**
 * An extended {@link JInterface} that provides indexing metadata for the index plugin.
 *
 * <p>Methods annotated with {@code @Id}, {@link Primary}, {@link Secondary}, or
 * {@link Searchable} are used to determine primary keys, secondary indexes, and
 * search fields. Methods returning this interface type with {@code @OneToMany}
 * are treated as foreign key relationships.</p>
 *
 * @see IndexablePlugin
 * @see IndexableProject
 * @see Primary
 * @see Secondary
 * @see Searchable
 */
public class IndexableInterface extends JInterface {

	/**
	 * Constructs an indexable interface by wrapping the given interface.
	 *
	 * @param in the interface to wrap
	 */
	public IndexableInterface(JInterface in) {
		super(in);
	}

	/**
	 * Checks whether this interface has an identifier method annotated with {@code @Id}.
	 *
	 * @return {@code true} if a method with {@code @Id} exists
	 */
	public boolean hasIdentifier() {
		return getIdentifier() != null;
	}
	
	/**
	 * Returns the method annotated with {@code @Id}, if present.
	 *
	 * @return the identifier method, or {@code null}
	 */
	public JGetMethod getIdentifier() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Id.class))
			.findFirst()
			.orElse(null);
	}
	
	/**
	 * Returns all methods annotated with {@code @Id} or {@link Primary} — these form
	 * the primary indexes for the interface.
	 *
	 * @return the set of primary index methods
	 */
	public Set<JGetMethod> getPrimaryIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Id.class) || m.isAnnotationPresent(Primary.class) )
			.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all methods annotated with {@link Secondary} — these form the
	 * secondary indexes for the interface.
	 *
	 * @return the set of secondary index methods
	 */
	public Set<JGetMethod> getSecondaryIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Secondary.class) )
			.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all methods annotated with {@code @OneToMany} that return this interface type —
	 * these represent foreign key relationships from other interfaces.
	 *
	 * @return the set of foreign key methods
	 */
	public Set<JGetMethod> getForeignIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.getUnderlyingType().equals(this.getName()))
			.filter(m -> m.isAnnotationPresent(OneToMany.class) )
			.collect(Collectors.toSet());
	}
	
	/**
	 * Returns all methods annotated with {@link Searchable} — these are fields that
	 * support text-based search.
	 *
	 * @return the set of searchable methods
	 */
	public Set<JGetMethod> getSearchIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Searchable.class) )
			.collect(Collectors.toSet());
	}
	
	/**
	 * Checks whether this interface has any indexable fields (primary, secondary, or search).
	 *
	 * @return {@code true} if at least one index is defined
	 */
	public boolean isIndexed() {
		return !getPrimaryIndexes().isEmpty() || !getSecondaryIndexes().isEmpty() || !getSearchIndexes().isEmpty(); 
	}
}
