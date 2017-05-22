package uk.co.terminological.index.memory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import uk.co.terminological.index.api.Constraint;
import uk.co.terminological.index.api.Facet;
import uk.co.terminological.index.api.Finder;

public class InMemoryFinder<V> implements Finder<V> {

	private InMemoryIndex<V> index;
	private List<Constraint<?,V>> constraints = new ArrayList<>();
	private V prototype;

	public InMemoryFinder(InMemoryIndex<V> index) {
		this.index = index;
	}

	@Override
	public Finder<V> all() {
		return this;
	}

	@Override
	public Finder<V> withPrototype(V proto) {
		this.prototype = proto;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Finder<V> withConstraints(Constraint<?,V>... constraints) {
		this.constraints.addAll(Arrays.asList(constraints));
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<V> get() {
		Iterator<Constraint<?,V>> it = constraints.iterator();
		Set<V> out = new HashSet<>();
		if (prototype == null)
			if (it.hasNext()) {
				Constraint<Object,V> c = (Constraint<Object, V>) it.next();
				out = c.getFacet().byKeyConstraint((Predicate<Object>) c.getTest());
			} else {
				out = index.getAll();
			}
		else {
			for (Facet<?,V> f:index.getFacets()) {
				Object key = f.generateKey(prototype);
				if (key != null) {
					if (f.unique().isPresent()) {
						Optional<V> value = ((InMemoryUniqueFacet<Object, V>) f.unique().get()).byKey(key);
						if (value.isPresent()) {
							out = Collections.singleton(value.get());
						} else {
							out = Collections.emptySet();
							return out;
						}
					} else {
						Set<V> tmp = ((InMemorySecondaryFacet<Object,V>) f.secondary().get()).byKey(key);
						if (out.isEmpty()) {
							out = tmp;
						} else {
							out.retainAll(tmp);
						}
					}
				}
			}
		}
		while (it.hasNext()) {
			Constraint<Object,V> c = (Constraint<Object, V>) it.next();
			Set<V> tmp = new HashSet<V>();
			for (V v:out) {
				Predicate<Object> tester = c.getTest();
				if (tester.test(c.getFacet().generateKey(v))) {
					tmp.add(v);
				};
			}
			out = tmp;
		}
		return out;
	}

	@Override
	public Stream<V> stream() {
		return get().stream();
	}

}
