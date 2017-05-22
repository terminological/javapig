package uk.co.terminological.index.api;

import java.util.function.Predicate;

public interface Constraint<K,V> {

	public Facet.Secondary<K,V> getFacet();
	public Predicate<K> getTest();
	
}
