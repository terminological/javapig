package uk.co.terminological.index.memory;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import uk.co.terminological.index.api.Facet;
import uk.co.terminological.index.api.Index;
import uk.co.terminological.index.api.Repository;

public class InMemoryRepository implements Repository {

	HashMap<Class<?>, InMemoryIndex<?>> stores = new HashMap<>();

	@SuppressWarnings("unchecked")
	private <V> Class<V> classFor(V value) {
		if (Proxy.isProxyClass(value.getClass())) {
			return (Class<V>) value.getClass().getInterfaces()[0];
		} else {
			return (Class<V>) value.getClass();
		}
	}
	
	@SuppressWarnings("unchecked")
	public <V> void put(V value) {
		Class<V> cls = classFor(value);
		if (!stores.containsKey(cls)) stores.put(cls, new InMemoryIndex<V>(cls));
		InMemoryIndex<V> il = (InMemoryIndex<V>) stores.get(cls);
		il.put(value);
	}

	@SuppressWarnings("unchecked")
	public <V> Set<V> getAllByType(Class<V> type) {
		if (!stores.containsKey(type)) return new HashSet<>();
		return ((InMemoryIndex<V>) stores.get(type)).retrieve().all().get();
	}
	
	@SuppressWarnings("unchecked")
	public <V> Set<V> getAllByExample(V value) {
		if (!stores.containsKey(value.getClass())) return new HashSet<>();
		return ((InMemoryIndex<V>) 
				stores.get(value.getClass()))
					.retrieve()
					.withPrototype(value)
					.get();	
	}
	
	@SuppressWarnings("unchecked")
	public <V> Optional<V> getTypeByPrimaryKey(Class<V> type, Object key) {
		Optional<Facet.Unique<?,V>> tmp = getIndex(type)
				.getPrimaryIndex();
		return ((InMemoryUniqueFacet<Object,V>) tmp.get()).byKey(key);
	}

	@Override
	public Set<Index<?>> getIndexes() {
		return new HashSet<Index<?>>(stores.values());
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V> Index<V> getIndex(Class<V> type) {
		if (!stores.containsKey(type)) stores.put(type, new InMemoryIndex<V>(type));
		return (InMemoryIndex<V>) stores.get(type);
	}

}