package uk.co.terminological.index.api;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A class specific index focussed around a specific value
 * 
 * If a class is a table, and an object is a row, this is the closest 
 * conceptually to a single database index. It lets
 * the user directly query for a key, or create key constraints, page and sort
 * statements for multiple index searches.
 * 
 * @author terminological
 *
 * @param <K>
 * @param <V>
 */
public interface Facet<K,V> {

	public String getName();
	public Class<K> keyType();
	public Function<V,K> keyGenerator();
	
	public default Optional<Secondary<K,V>> secondary() {return Optional.empty();}
	public default Optional<Unique<K,V>> unique() {throw new ClassCastException();}
	public default K generateKey(V value) {
		if (value == null) return null;
		return keyGenerator().apply(value);
	}
	
	public static interface Secondary<K,V> extends Facet<K,V> {
		
		public Set<V> byKey(K value);
		public Set<V> byKeyConstraint(Predicate<K> test);
		
		public Stream<V> streamByKey(K value);
		public Stream<V> streamByKeyConstraint(Predicate<K> test);
		
		public default Optional<Secondary<K,V>> secondary() {return Optional.of(this);}
		
		public Integer countsByKey(K value);
		public Map<K,Integer> counts();
		public Integer countsByKeyConstraint(Predicate<K> test);
	
		public Constraint<K,V> createConstraint(Predicate<K> tester);
	}
	
	public static interface Unique<K,V> extends Facet<K,V> {
		
		public Optional<V> byKey(K value);
		public default Optional<Unique<K,V>> unique() {return Optional.of(this);}
	
	}
	
	
		
	
	// public Sorter<K> createSorter(Comparator<K> sort);
	// public Pager<V> createPager(Long start, Long items);

	/*public interface Sorter<K> {
		public Comparator<K> getComparator();
	}
	
	public interface Pager<V> {

	}*/
	
}
