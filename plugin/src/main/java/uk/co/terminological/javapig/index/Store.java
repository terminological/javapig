package uk.co.terminological.javapig.index;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Store {

	HashMap<Class<?>, IndexedList<?>> stores = new HashMap<>();

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
		if (!stores.containsKey(cls)) stores.put(cls, new IndexedList<V>());
		IndexedList<V> il = (IndexedList<V>) stores.get(cls);
		for (Index<?,V> index: il.indexes) {
			index.put(value);
		}
		il.objects.add(value);
	}

	@SuppressWarnings("unchecked")
	public <K,V> Index<K,V> addIndex(final Class<V> cls, final Class<K> keyCls, final Function<V,K> wrapper) {
		Index<K,V> ik = new Index<K,V>() {
			public Function<V,K> function() {return wrapper;}
			public Class<V> inputType() {return cls;}
			public Class<K> keyType() {return keyCls;} 
		};
		if (!stores.containsKey(cls)) stores.put(cls, new IndexedList<V>());
		IndexedList<V> il = (IndexedList<V>) stores.get(cls);
		il.addIndex(ik);
		return ik;
	}
	
	@SuppressWarnings("unchecked")
	public <V> Set<V> getAllByType(Class<V> type) {
		if (!stores.containsKey(type)) return new HashSet<>();
		return ((IndexedList<V>) stores.get(type)).objects;
	}
	
	/* Not sure this does anything useful
	@SuppressWarnings("unchecked")
	public <V,K> Set<V> getAllByKey(Class<K> type, K key, Index<?,V>... indexes) {
		if (!stores.containsKey(type)) return new HashSet<>();
		Set<V> out = null;
		for (Index<?,V> index: indexes) {
			if (index.keyType().isAssignableFrom(key.getClass())) {
				if (out == null) {
					out = ((Index<K,V>) index).getByKey(key);
				} else {
					out.retainAll(((Index<K,V>) index).getByKey(key));
					if (out.isEmpty()) return out;
				}
			}
		}
		return out;
	}*/

	@SuppressWarnings("unchecked")
	public <V> Set<V> getAllByExample(Class<V> type, V value, Index<?,V>... indexes) {
		if (!stores.containsKey(type)) return new HashSet<>();
		Set<V> out = null;
		for (Index<?,V> index: indexes) {
			if (out == null) {
				out=index.getByExample(value);
			} else {
				out.retainAll(index.getByExample(value));
				if (out.isEmpty()) return out;
			}
		}
		return out;
	}
	
	private static class IndexedList<V> {

		Set<Index<?,V>> indexes = new HashSet<>();
		Set<V> objects = new HashSet<>();

		protected <K> void addIndex(Index<K,V> key) {
			if (!indexes.contains(key)) {
				indexes.add(key);
				for (V v:objects) {
					key.put(v);
				}
			}
		}

	}
	
	public static abstract class Index<K, V> {

		public abstract Class<V> inputType();
		public abstract Class<K> keyType();
		public abstract Function<V,K> function();

		public static <K2,V2> Index<K2,V2> from(Class<K2> kClass, Class<V2> vClass, Function<V2,K2> lambda) {
			return new Index<K2,V2>() {
				public Function<V2,K2> function() {return lambda;}
				public Class<V2> inputType() {return vClass;}
				public Class<K2> keyType() {return kClass;} 
			};
		}

		public K keyFor(V x) { 
			return function().apply(x) ; 
		}

		Map<K,Set<V>> index = new HashMap<>();

		public Set<V> getByKey(K value) {
			//if (value.getClass().isAssignableFrom(keyType())) {
			Set<V> out = index.get(value);
			if (out == null) return Collections.emptySet();
			return out;
			//} else { return Collections.emptySet(); }
		}

		public Set<V> getByExample(V value) {
			Set<V> out = index.get(keyFor(value));
			if (out == null) return Collections.emptySet();
			return out;
		}

		public void put(V value) {
			K indexKey = keyFor(value);
			if (!index.containsKey(indexKey)) {
				index.put(indexKey, new HashSet<>());
			}
			index.get(indexKey).add(value);
		}

		public Set<K> getKeys() {
			return index.keySet();
		}

		public Map<K,Integer> counts() {
			Map<K,Integer> out = new HashMap<>();
			for (Map.Entry<K, Set<V>> entries: index.entrySet()) {
				out.put(entries.getKey(), entries.getValue().size());
			}
			return out;
		}

		public String toString() {
			return counts().entrySet().stream().map(e -> e.getKey()+"="+e.getValue()).collect(Collectors.joining("\n"));
		}

	}

}