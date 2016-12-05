package uk.co.terminological.javapig.csvloader;


import java.util.Iterator;
import java.util.function.Function;

public class IterableMapper<IN,OUT> implements Iterable<OUT> {

	public static <X,Y> IterableMapper<X,Y> create(Iterable<X> in, Function<X,Y> function) {
		return new IterableMapper<X,Y>(in, function);
	}
	
	Iterable<IN> in;
	Function<IN,OUT> mapper;
	
	public IterableMapper(Iterable<IN> in, Function<IN,OUT> mapper)  {
		this.in = in;
		this.mapper = mapper;
	}

	@Override
	public Iterator<OUT> iterator() {
		Iterator<IN> inIterator = in.iterator();
		return new Iterator<OUT>() {
			@Override
			public boolean hasNext() {
				return inIterator.hasNext();
			}
			@Override
			public OUT next() {
				return mapper.apply(inIterator.next());
			}
			
		};
	}

}
