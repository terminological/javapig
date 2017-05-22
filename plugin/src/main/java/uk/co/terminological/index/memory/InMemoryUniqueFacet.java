package uk.co.terminological.index.memory;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import uk.co.terminological.index.api.Facet;

public class InMemoryUniqueFacet<K, V> extends InMemoryFacet<K, V> implements Facet.Unique<K,V> {

	public InMemoryUniqueFacet(
			String name, 
			Class<K> keyType, 
			Function<V, K> function) {
		super(name, keyType, function);
	}
	
	Map<K,V> index = new HashMap<>();

	public Optional<V> byKey(K value) {
		Optional<V> out = Optional.ofNullable(index.get(value));
		return out;
	}

	public void put(V value) {
		if (value == null) return;
		K indexKey = generateKey(value);
		if (indexKey == null) return;
		index.put(indexKey,value);
	}

	public Integer count() {
		return index.entrySet().size();
	}

}