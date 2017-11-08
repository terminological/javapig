package uk.co.terminological.javapig.index;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;
import javax.persistence.OneToMany;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;

public class IndexableInterface extends JInterface {

	public IndexableInterface(JInterface in) {
		super(in);
	}

	/**
	 * Determines whether a method in this interface has been annotated with a {@link javax.persistance.Id} annotation
	 * @return true if present
	 */
	public boolean hasIdentifier() {
		return getIdentifier() != null;
	}
	
	/** 
	 * If a method in this interface exists that is annotated with {@link javax.persistance.Id} then this returns that method
	 * otherwise returns null.
	 * @return
	 */
	public JGetMethod getIdentifier() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Id.class))
			.findFirst()
			.orElse(null);
	}
	
	/** 
	 * If a method in this interface exists that is annotated with {@link uk.co.terminological.javapig.index.Secondary} then this returns that method
	 * otherwise returns null.
	 * @return
	 */
	public Set<JGetMethod> getPrimaryIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Id.class) || m.isAnnotationPresent(Primary.class) )
			.collect(Collectors.toSet());
	}
	
	/** 
	 * If a method in this interface exists that is annotated with {@link uk.co.terminological.javapig.index.Secondary} then this returns that method
	 * otherwise returns an empty set.
	 * @return
	 */
	public Set<JGetMethod> getSecondaryIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Secondary.class) )
			.collect(Collectors.toSet());
	}
	
	/** 
	 * If a method in this interface exists that is annotated with {@link uk.co.terminological.javapig.index.Secondary} then this returns that method
	 * otherwise returns null.
	 * @return
	 */
	public Set<JGetMethod> getForeignIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.getUnderlyingType().equals(this.getName()))
			.filter(m -> m.isAnnotationPresent(OneToMany.class) )
			.collect(Collectors.toSet());
	}
	
	/** 
	 * If a method in this interface exists that is annotated with {@link javax.persistance.Id} then this returns that method
	 * otherwise returns null.
	 * @return
	 */
	public Set<JGetMethod> getSearchIndexes() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Searchable.class) )
			.collect(Collectors.toSet());
	}
	
	public boolean isIndexed() {
		return !getPrimaryIndexes().isEmpty() || !getSecondaryIndexes().isEmpty() || !getSearchIndexes().isEmpty(); 
	}
}
