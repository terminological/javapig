package uk.co.terminological.index.memory;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.index.api.Constraint;
import uk.co.terminological.index.api.Facet;

public class InMemorySecondaryFacet<K, V> extends InMemoryFacet<K,V> implements Facet.Secondary<K,V> {

	public InMemorySecondaryFacet(String name, 
			Class<K> keyType, 
			Function<V, K> function) {
		super(name, keyType, function);
	}

	Map<K,Set<V>> index = new HashMap<>();

	public Set<V> byKey(K value) {
		if (value == null) return Collections.emptySet();
		Set<V> out = index.get(value);
		if (out == null) return Collections.emptySet();
		return out;
	}

	public void put(V value) {
		K indexKey = generateKey(value);
		if (indexKey == null) return;
		if (!index.containsKey(indexKey)) {
			index.put(indexKey, new HashSet<>());
		}
		index.get(indexKey).add(value);
	}

	public String toString() {
		return counts().entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining("\n"));
	}
	
	@Override
	public Set<V> byKeyConstraint(Predicate<K> test) {
		return streamByKeyConstraint(test).collect(Collectors.toSet());
	}

	@Override
	public Integer countsByKey(K key) {
		if (key == null) return 0;
		if (!index.containsKey(key)) return 0;
		return index.get(key).size();
	}

	@Override
	public Map<K,Integer> counts() {
		Map<K, Integer> out = new HashMap<>();
		for (Entry<K,Set<V>> in: index.entrySet()) {
			out.put(in.getKey(), in.getValue().size());
		}
		return out;
	}

	@Override
	public Integer countsByKeyConstraint(Predicate<K> test) {
		return byKeyConstraint(test).size();
	}

	@Override
	public Stream<V> streamByKey(K key) {
		return byKey(key).stream();
	}

	@Override
	public Stream<V> streamByKeyConstraint(Predicate<K> tester) {
		return index.entrySet()
				.stream()
				.filter(e -> tester.test(e.getKey()))
				.flatMap(e -> e.getValue().stream())
		;
	}

	public Set<K> getKeys() {
		return index.keySet();
	}
	
	@Override
	public Constraint<K, V> createConstraint(Predicate<K> tester) {
		return new Constraint<K,V>() {
			@Override
			public Facet.Secondary<K,V> getFacet() {
				return InMemorySecondaryFacet.this;
			}
			@Override
			public Predicate<K> getTest() {
				return tester;
			}
		};
	}
	
}