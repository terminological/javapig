package uk.co.terminological.index.memory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.co.terminological.index.api.Facet;
import uk.co.terminological.index.api.Facet.Unique;
import uk.co.terminological.index.api.Finder;
import uk.co.terminological.index.api.Index;

class InMemoryIndex<V> implements Index<V> {

	static Logger log = LoggerFactory.getLogger(InMemoryIndex.class);
	
	public InMemoryIndex(Class<V> type) {
		this.valueType = type;
	}
	
	Class<V> valueType;
	InMemoryUniqueFacet<?,V> primaryIndex;
	Set<InMemoryUniqueFacet<?,V>> uniqueFacets = new HashSet<>();
	Set<InMemorySecondaryFacet<?,V>> secondaryFacets = new HashSet<>();
	Set<V> objects = new HashSet<>();
	
	@Override
	public <K> void addFacet(Facet<K, V> facet) {
		log.error("Should be picked up by more specific methods");
		throw new RuntimeException("Can only add a InMemoryUniqueFacet or InMemorySecondaryFacet");	
	}
	
	public <K> void addFacet(InMemoryUniqueFacet<K,V> key) {
		if (!getFacetByName(key.getName()).isPresent()) {
			uniqueFacets.add(key);
			for (V v:objects) {
				key.put(v);
			}
		} else {
			log.warn("Duplicate index creation with name: "+key.getName());
		}
	}
	
	public <K> void addFacet(InMemorySecondaryFacet<K,V> key) {
		if (!getFacetByName(key.getName()).isPresent()) {
			secondaryFacets.add(key);
			for (V v:objects) {
				key.put(v);
			}
		} else {
			log.warn("Duplicate index creation with name: "+key.getName());
		}
	}
	
	public <K> void addPrimaryIndex(Facet.Unique<K,V> facet) {
		primaryIndex = (InMemoryUniqueFacet<?, V>) facet;
		this.addFacet(facet);
	}
	
	protected void put(V value) {
		objects.add(value);
		Stream.concat(
				uniqueFacets.stream(),
				secondaryFacets.stream()
		).forEach(i -> i.put(value));
	}

	
	public static <K,V> Facet.Secondary<K,V> secondary(String name, 
			Class<K> keyType, 
			Function<V, K> function) {
		return new InMemorySecondaryFacet<K,V>(
				name, keyType, function);
	}
	
	public static <K,V> InMemoryUniqueFacet<K,V> unique(String name, 
			Class<K> keyType, 
			Function<V, K> function) {
		return new InMemoryUniqueFacet<K,V>(
				name, keyType, function);
	}

	@Override
	public Class<V> valueType() {
		return valueType;
	}

	@Override
	public Set<Facet<?, V>> getFacets() {
		Set<Facet<?, V>> out = new HashSet<>(this.uniqueFacets);
		out.addAll(this.secondaryFacets);
		return out;
	}

	@Override
	public Optional<Facet<?, V>> getFacetByName(String facetName) {
		return Stream.concat(
			this.uniqueFacets.stream(), 
			this.secondaryFacets.stream()	
		).filter(f -> f.getName().equals(facetName))
		.findAny().map(f -> (Facet<?,V>) f);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <K> Optional<Facet<K, V>> getFacet(Class<K> keyType, String facetName) {
		return getFacets().stream()
				.filter(f -> f.keyType().equals(keyType))
				.filter(f -> f.getName().equals(facetName))
				.findAny()
				.map(f -> (Facet<K, V>) f);
	}

	@Override
	public Optional<Unique<?, V>> getPrimaryIndex() {
		return Optional.ofNullable(this.primaryIndex);
	}

	@Override
	public Set<Unique<?, V>> getUniqueFacets() {
		return new HashSet<Unique<?, V>>(this.uniqueFacets);
	}

	@Override
	public void create(V value) { //shoudl throw exception?
		put(value);
	}

	@Override
	public Finder<V> retrieve() {
		return new InMemoryFinder<V>(this);
	}

	@Override
	public void update(V value) {
		put(value);
		
	}

	@Override
	public void delete(V proto) {
		//Not yet implemented
		throw new RuntimeException("Not implemented");
	}

	@Override
	public Integer count() {
		return this.objects.size();
	}

	@Override
	public V index(V value) {
		put(value);
		return value;
	}

	@Override
	public Collection<V> index(Collection<V> values) {
		values.stream().forEach(v -> this.index(v));
		return values;
	}

	@Override
	public Iterable<V> index(Iterable<V> values) {
		for (V value: values) {
			this.index(value);
		}
		return values;
	}
	
	public Set<V> getAll() {
		return objects;
	}
}