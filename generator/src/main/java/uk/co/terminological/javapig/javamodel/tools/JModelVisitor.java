package uk.co.terminological.javapig.javamodel.tools;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JProjectComponent;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JPackage;

/**
 * Abstract visitor that traverses the javapig metamodel tree and produces typed results.
 *
 * <p>Implement this class to process the model in a specific way—for example, generating
 * string representations ({@link JModelToString}), cloning the model
 * ({@link JModelCopier}), or extracting information for code generators.</p>
 *
 * <p>Each {@code visitXxx} method receives the current element and a stream of
 * results from recursively visiting its children. Override the abstract methods
 * to define custom processing logic.</p>
 *
 * @param <OUT> the type of result produced by each visit method
 * @see JProjectComponent#accept(JModelVisitor)
 * @see JModelToString
 * @see JModelCopier
 */
public abstract class JModelVisitor<OUT> {

	/**
	 * Dispatches the visit to the appropriate method based on the runtime type of the element.
	 *
	 * @param in the metamodel element to visit
	 * @return a stream of results from visiting this element and its children
	 * @throws RuntimeException if the element type is unexpected
	 */
	public Stream<? extends OUT> visit(JProjectComponent in) {
		
		if (in instanceof JProject) {
			
			JProject tmp = (JProject) in;
			return visitModel(
					tmp,
					tmp.getPackages().stream().flatMap(a -> this.visit(a))
			);
			
			
		} else if (in instanceof JPackage) {
			
			JPackage tmp = (JPackage) in;
			return visitPackage(
					tmp,
					tmp.getAnnotations().stream().flatMap(a -> this.visit(a)),
					tmp.getClasses().stream().flatMap(a -> this.visit(a))
			);
			
		} else if (in instanceof JInterface) {
			
			JInterface tmp = (JInterface) in;
			return visitInterface(
					tmp,
					tmp.getAnnotations().stream().flatMap(a -> this.visit(a)),
					tmp.getExtendedMethods().stream().flatMap(a -> this.visit(a))
			);
			
		} else if (in instanceof JGetMethod) {
			
			JGetMethod tmp = (JGetMethod) in;
			return visitGetMethod(
					tmp,
					tmp.getAnnotations().stream().flatMap(a -> this.visit(a))
			);
				
		} else if (in instanceof JAnnotation) {
			
			JAnnotation tmp = (JAnnotation) in;
			return visitAnnotation(
					tmp,
					tmp.getEntries().stream().flatMap(a -> this.visit(a))
			);
			
		} else if (in instanceof JAnnotationEntry) {
			
			JAnnotationEntry tmp = (JAnnotationEntry) in;
			return visitAnnotationEntry(
					tmp,
					tmp.getValues().stream().flatMap(a -> this.visit(a))
			);
			
		} else if (in instanceof JAnnotationValue.Annotation) {
			
			JAnnotation tmp = ((JAnnotationValue.Annotation) in).getValue();
			return visit(tmp);
			
		} else if (in instanceof JAnnotationValue.Class) {
			
			return Stream.of(visitClassName(((JAnnotationValue.Class) in).getValue()));
		
		} else if (in instanceof JAnnotationValue.Primitive) {
			
			return Stream.of(visitAnnotationValue((JAnnotationValue.Primitive) in));
		
		}
		
		throw new RuntimeException("ERROR in model, unexpected: "+in.getClass()+" = "+in.toString());
		
	}
	
	/**
	 * Visits a {@link JProject} root element with results from visiting all its packages.
	 *
	 * @param in the project to visit
	 * @param packages results from visiting each package
	 * @return the visit result for the project
	 */
	public abstract Stream<? extends OUT> visitModel(JProject in, Stream<OUT> packages);
	
	/**
	 * Visits a {@link JPackage} element with results from visiting its annotations and classes.
	 *
	 * @param in the package to visit
	 * @param annotations results from visiting each annotation
	 * @param classes results from visiting each class/interface
	 * @return the visit result for the package
	 */
	public abstract Stream<? extends OUT> visitPackage(JPackage in, Stream<OUT> annotations, Stream<OUT> classes);
	
	/**
	 * Visits a {@link JInterface} element with results from visiting its annotations and methods.
	 *
	 * @param in the interface to visit
	 * @param annotations results from visiting each annotation
	 * @param methods results from visiting each method
	 * @return the visit result for the interface
	 */
	public abstract Stream<? extends OUT> visitInterface(JInterface in, Stream<OUT> annotations, Stream<OUT> methods);
	
	/**
	 * Visits a {@link JGetMethod} element with results from visiting its annotations.
	 *
	 * @param in the method to visit
	 * @param annotations results from visiting each annotation
	 * @return the visit result for the method
	 */
	public abstract Stream<? extends OUT> visitGetMethod(JGetMethod in, Stream<OUT> annotations);
	
	/**
	 * Visits a {@link JAnnotation} element with results from visiting its entries.
	 *
	 * @param in the annotation to visit
	 * @param annotationEntries results from visiting each entry
	 * @return the visit result for the annotation
	 */
	public abstract Stream<? extends OUT> visitAnnotation(JAnnotation in, Stream<OUT> annotationEntries);
	
	/**
	 * Visits a {@link JAnnotationEntry} element with results from visiting its values.
	 *
	 * @param in the entry to visit
	 * @param annotationValues results from visiting each value
	 * @return the visit result for the entry
	 */
	public abstract Stream<? extends OUT> visitAnnotationEntry(JAnnotationEntry in, Stream<OUT> annotationValues);
	
	/**
	 * Visits a primitive annotation value (String, Integer, Boolean, etc.).
	 *
	 * @param in the primitive value wrapper
	 * @return the visit result for the primitive value
	 */
	public abstract OUT visitAnnotationValue(JAnnotationValue.Primitive in);
	
	/**
	 * Visits a class name reference ({@link JClassName}).
	 *
	 * @param className the class name to visit
	 * @return the visit result for the class name
	 */
	public abstract OUT visitClassName(JClassName className);
	
	/**
	 * Visits a method name ({@link JMethodName}).
	 *
	 * @param className the method name to visit
	 * @return the visit result for the method name
	 */
	public abstract OUT visitMethodName(JMethodName className);
	
	
	/**
	 * Merges multiple streams into a single stream.
	 *
	 * @param streams the streams to merge
	 * @return a stream containing all elements from the input streams
	 */
	@SafeVarargs
	public static <X extends Object> Stream<X> merge(Stream<X>... streams) {
		List<X> tmp = new ArrayList<>();
		for (Stream<X> stream : streams) {
			tmp.addAll(stream.collect(Collectors.toList()));
		}
		return tmp.stream();
	};

	/**
	 * Prepends a tab character to each line in the stream.
	 *
	 * @param in the stream of strings
	 * @return a stream with each string prefixed by a tab
	 */
	public static Stream<String> indent(Stream<String> in) {
		return in.map(i -> "\t"+i);
	}
	
	/**
	 * Creates a new fluent stream builder for collecting results incrementally.
	 *
	 * @return a new FluentStreamBuilder
	 */
	public FluentStreamBuilder<OUT> start() {return new FluentStreamBuilder<OUT>();}
	
	/**
	 * A fluent builder for collecting stream elements into a result list.
	 *
	 * <p>Usage: {@code builder.with(item1, item2).with(anotherStream).end()}</p>
	 *
	 * @param <X> the type of elements in the stream
	 */
	public static class FluentStreamBuilder<X> {
		ArrayList<X> tmp = new ArrayList<>();
		
		/**
		 * Adds individual items to the builder.
		 *
		 * @param items the items to add
		 * @return this builder for chaining
		 */
		public FluentStreamBuilder<X> with(@SuppressWarnings("unchecked") final X... items) {
			for (X item: items)
				tmp.add(item);
			return this;
		}
		
		/**
		 * Adds all elements from a stream to the builder.
		 *
		 * @param items the stream of items
		 * @return this builder for chaining
		 */
		public FluentStreamBuilder<X> with(Stream<X> items) {
			items.forEach(i -> tmp.add(i));
			return this;
		}
		
		/**
		 * Returns a stream of all collected elements.
		 *
		 * @return a stream of the collected items
		 */
		public Stream<X> end() {return tmp.stream();}
	}
	
	
}
