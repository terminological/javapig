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
import uk.co.terminological.javapig.javamodel.JModel;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;
import uk.co.terminological.javapig.javamodel.JTemplateMetadata;

public abstract class JModelVisitor<OUT> {

	public Stream<? extends OUT> visit(JModelComponent in) {
		
		if (in instanceof JModel) {
			
			JModel tmp = (JModel) in;
			return visitModel(
					tmp,
					tmp.getPackages().stream().flatMap(a -> this.visit(a))
			);
			
			
		} else if (in instanceof JPackage) {
			
			JPackage tmp = (JPackage) in;
			return visitPackage(
					tmp,
					Stream.of(tmp.getMetadata()).flatMap(a -> this.visit(a)),
					tmp.getAnnotations().stream().flatMap(a -> this.visit(a)),
					tmp.getClasses().stream().flatMap(a -> this.visit(a))
			);
			
		} else if (in instanceof JPackageMetadata) {
			
			JPackageMetadata tmp = (JPackageMetadata) in;
			return visitPackageMetadata(
					tmp,
					tmp.getTemplates().stream().flatMap(a -> this.visit(a))
			);
		
		} else if (in instanceof JTemplateMetadata) {
			
			JTemplateMetadata tmp = (JTemplateMetadata) in;
			return visitTemplateMetadata(
					tmp);
			
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
					tmp.getValues().stream().flatMap(a -> this.visit(a))
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
		
	
	
	public abstract Stream<? extends OUT> visitModel(JModel in, Stream<OUT> packages);
	public abstract Stream<? extends OUT> visitPackage(JPackage in, Stream<OUT> metadata, Stream<OUT> annotations, Stream<OUT> classes);
	public abstract Stream<? extends OUT> visitPackageMetadata(JPackageMetadata in, Stream<OUT> templates);
	public abstract Stream<? extends OUT> visitTemplateMetadata(JTemplateMetadata in);
	public abstract Stream<? extends OUT> visitInterface(JInterface in, Stream<OUT> annotations, Stream<OUT> methods);
	public abstract Stream<? extends OUT> visitGetMethod(JGetMethod in, Stream<OUT> annotations);
	public abstract Stream<? extends OUT> visitAnnotation(JAnnotation in, Stream<OUT> annotationEntries);
	public abstract Stream<? extends OUT> visitAnnotationEntry(JAnnotationEntry in, Stream<OUT> annotationValues);
	
	public abstract OUT visitAnnotationValue(JAnnotationValue.Primitive in);
	public abstract OUT visitClassName(JClassName className);
	public abstract OUT visitMethodName(JMethodName className);
	
	
	@SafeVarargs
	public static <X extends Object> Stream<X> merge(Stream<X>... streams) {
		List<X> tmp = new ArrayList<>();
		for (Stream<X> stream : streams) {
			tmp.addAll(stream.collect(Collectors.toList()));
		}
		return tmp.stream();
	};

	public static Stream<String> indent(Stream<String> in) {
		return in.map(i -> "\t"+i);
	}
	
	public FluentStreamBuilder<OUT> start() {return new FluentStreamBuilder<OUT>();}
	
	public static class FluentStreamBuilder<X> {
		ArrayList<X> tmp = new ArrayList<>();
		
		
		public FluentStreamBuilder<X> with(@SuppressWarnings("unchecked") final X... items) {
			for (X item: items)
				tmp.add(item);
			return this;
		}
		
		public FluentStreamBuilder<X> with(Stream<X> items) {
			items.forEach(i -> tmp.add(i));
			return this;
		}
		
		public Stream<X> end() {return tmp.stream();}
	}
	
	
}
