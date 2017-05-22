package uk.co.terminological.index.memory;

import java.util.function.Function;

import uk.co.terminological.index.api.Facet;

public abstract class InMemoryFacet<K,V> implements Facet<K,V> {

	private String name;
	private Class<K> keyType;
	private Function<V,K> function;

	public Class<K> keyType() {return keyType;}
	public abstract void put(V value);
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public Function<V, K> keyGenerator() {
		return function;
	}

	public InMemoryFacet(
			String name,
			Class<K> keyType,
			Function<V,K> function
			) {
		this.name = name;
		this.keyType = keyType;
		this.function = function;
	}
	
}