package uk.co.terminological.index.api;

import java.util.Set;
import java.util.stream.Stream;

/**
 * created in the context of a type specific index the finder is logically 
 * equivalent to a query specification and runner.
 * @author terminological
 *
 * @param <V>
 */
public interface Finder<V> {

	public Finder<V> all();
	public Finder<V> withPrototype(V proto);
	
	@SuppressWarnings("unchecked")
	public Finder<V> withConstraints(Constraint<?,V>... constraints);
	
	public Set<V> get();
	
	/*
	@SuppressWarnings("unchecked")
	public List<V> get(Sorter<?,V>... sorters);
	
	@SuppressWarnings("unchecked")
	public List<V> get(Pager<V> pager, Sorter<?,V>... sorters);
	*/
	
	public Stream<V> stream();
	
	/*
	@SuppressWarnings("unchecked")
	public Stream<V> stream(Sorter<?,V>... sorters);
	
	@SuppressWarnings("unchecked")
	public Stream<V> stream(Pager<V> pager, Sorter<?,V>... sorters);
	*/
}
