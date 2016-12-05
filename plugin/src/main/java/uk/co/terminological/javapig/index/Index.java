package uk.co.terminological.javapig.index;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface Index<K,V> {
	
	Collection<V> getAll();
	void index(V input);
	Optional<K> keyFor(V value);
	
	
	public static interface Unique<K1,V1> extends Index<K1,V1> {
	
		Optional<V1> get(K1 key);
		
	}
	
	public static interface Secondary<K2,V2> extends Index<K2,V2> {
		
		Set<V2> get(K2 key);
		
	}
	
	public static interface Text<V> extends Secondary<String,V> {
		
		
		
	}
	
}