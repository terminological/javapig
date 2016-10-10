package uk.co.terminological.javapig.javamodel.tools;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JAnnotationValue.Primitive;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JModel;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;
import uk.co.terminological.javapig.javamodel.JTemplateMetadata;

public class JModelToString extends JModelVisitor<String> {

	@Override
	public Stream<? extends String> visitModel(JModel in, Stream<String> packages) {
		return Stream.of("Jmodel:",packages.collect(Collectors.joining("\n")));
	}

	@Override
	public Stream<? extends String> visitPackage(JPackage in, Stream<String> metadata, Stream<String> annotations, Stream<String> classes) {
		return start()
				.with(
						"{ "+in.getName()+":",
						"\tmetadata: [",
						"\t\t"+metadata.collect(Collectors.joining("; ")),
						"\t]",
						"\tannotations: [")
				.with(
						annotations.map(m -> "\t\t"+m))
				.with(
						"\t]",
						"\tclasses: [")
				.with(
						classes.map(m -> "\t\t"+m))
				.with(
						"\t]",
						"}")
				.end();
	}

	@Override
	public Stream<? extends String> visitPackageMetadata(JPackageMetadata in, Stream<String> templates) {
		
		return Stream.of(
				"directory="+in.getDirectory().getAbsolutePath(),
				"["+templates.collect(Collectors.joining(", "))+"]"
				);
	}

	@Override
	public Stream<? extends String> visitTemplateMetadata(JTemplateMetadata in) {
		return Stream.of(
				in.getTemplateFilename(),
				in.getClassNameTemplate(),
				Arrays.toString(in.getScope())
				);
	}

	@Override
	public Stream<? extends String> visitInterface(JInterface in, Stream<String> annotations, Stream<String> methods) {
		return start()
				.with(
						"{ "+visitClassName(in.getName())+":",
						"\timports: [")
				.with(
						in.getImports().stream().map(s -> "\t\t"+s))
				.with(
						"\tsupertypes: [")
				.with(
						in.getSupertypes().stream().map(m -> "\t\t"+m.getCanonicalName()))
				.with(
						"\t]",
						"\tannotations: [")
				.with(
						annotations.map(m -> "\t\t"+m))
				.with(
						"\t]",
						"\tmethods: [")
				.with(
						methods.map(m -> "\t\t"+m))
				.with(
						"\t]",
						"}")
				.end();
				
	}

	@Override
	public Stream<? extends String> visitGetMethod(JGetMethod in, Stream<String> annotations) {
		return start()
				.with(
						"{ "+visitMethodName(in.getName())+":",
						"\tannotations: [")
				.with(
						annotations.map(m -> "\t\t"+m))
				.with(
						"\t]",
						"\ttypes: [",
						"\t\treturnTypeDefinition: "+in.getReturnTypeDefinition(),
						"\t\treturnType: "+visitClassName(in.getReturnType()),
						"\t\tunderlyingType: "+visitClassName(in.getUnderlyingType()),
						"\t\tinterfaceType: "+in.getInterfaceType(),
						"\t\timplementationType: "+in.getImplementationType(),
						"\t\tinverse: "+(in.hasInverseMethod() ? visitMethodName(in.getInverseMethod().getName()) : "<none>"),
						"\t]",
						"}")
				.end();
	}

	@Override
	public Stream<? extends String> visitAnnotation(JAnnotation in, Stream<String> annotationEntries) {
		return start()
				.with(
						"@"+in.getName()+" [")
				.with(
						annotationEntries.map(m -> "\t"+m))
				.with(
						"]")
				.end();
	}

	@Override
	public Stream<? extends String> visitAnnotationEntry(JAnnotationEntry in, Stream<String> annotationValues) {
		return start()
				.with(
						"{ "+visitMethodName(in.getMethod())+"=")
				.with(
						annotationValues.map(m -> "\t"+m))
				.with(
						"}")
				.end();
	}

	@Override
	public String visitAnnotationValue(Primitive in) {
		if (in == null) return "null";
		return "["+in.getValue().getClass().getName()+"] "+in.getValue().toString();
	}

	@Override
	public String visitClassName(JClassName in) {
		if (in == null) return "null";
		return in.getCanonicalName();
	}

	@Override
	public String visitMethodName(JMethodName in) {
		if (in == null) return "null";
		return in.getter();
	}

	

}
