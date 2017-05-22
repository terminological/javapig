package uk.co.terminological.index.api;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

/**
 * The index provides all the ways of organising the content of a specific
 * class and class level accessors. This is not a 1:1 match to the database concept
 * of an index - it is more like an object repository. 
 * 
 * @author terminological
 *
 */
public interface Index<V> {

	public Class<V> valueType();
	public Set<Facet<?,V>> getFacets();
	public Optional<Facet<?,V>> getFacetByName(String facetName);
	public <K extends Object> Optional<Facet<K,V>> getFacet(Class<K> keyType,String facetName);
	
	public Optional<Facet.Unique<?,V>> getPrimaryIndex();
	public Set<Facet.Unique<?,V>> getUniqueFacets();
	
	public <K extends Object> void addFacet(Facet<K,V> facet);
	public <K extends Object> void addPrimaryIndex(Facet.Unique<K,V> facet);
	
	public void create(V value);
	public Finder<V> retrieve();
	public void update(V value);
	public void delete(V proto);
	
	public Integer count();
	
	public V index(V value);
	public Collection<V> index(Collection<V> values);
	public Iterable<V> index(Iterable<V> values);
	
	
	
}
