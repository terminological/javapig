package uk.co.terminological.javapig.javamodel.tools;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue.Primitive;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JPackage;

/**
 * Converts a {@link JProject} metamodel into a human-readable string representation.
 *
 * <p>The output is a structured, indented textual dump showing all model elements
 * (packages, interfaces, methods, annotations) and their properties. This is primarily
 * used for debugging and logging.</p>
 *
 * <p>Example output:</p>
 * <pre>
 * Jmodel:
 * { uk.co.example.models:
 *   annotations: []
 *   classes: [
 *     { MyInterface:
 *       imports: []
 *       supertypes: []
 *       annotations: [@MyAnnotation [
 *         { MyAnnotation#value=[java.lang.String] Hello}
 *       ]]
 *       methods: [
 *         { #getName:
 *           annotations: []
 *           types: [...]
 *         }
 *       ]
 *     }
 *   ]
 * }
 * </pre>
 *
 * @see JProjectComponent#toString()
 * @see JModelVisitor
 */
public class JModelToString extends JModelVisitor<String> {

	/**
	 * Formats the root model entry with all packages.
	 *
	 * @param in the project
	 * @param packages results from visiting packages
	 * @return a single-line summary with all packages joined
	 */
	@Override
	public Stream<? extends String> visitModel(JProject in, Stream<String> packages) {
		return Stream.of("Jmodel:",packages.collect(Collectors.joining("\n")));
	}

	/**
	 * Formats a package entry showing its name, annotations, and contained classes.
	 */
	@Override
	public Stream<? extends String> visitPackage(JPackage in, Stream<String> annotations, Stream<String> classes) {
		return start()
				.with(
						"{ "+in.getName()+":",
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

	/**
	 * Formats an interface entry showing its imports, supertypes, annotations, and methods.
	 */
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

	/**
	 * Formats a method entry showing its annotations and all type information
	 * (return type, underlying type, interface type, implementation type, inverse method).
	 */
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

	/**
	 * Formats an annotation entry showing its name and all entries.
	 */
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

	/**
	 * Formats an annotation entry showing its method name and values.
	 */
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

	/**
	 * Formats a primitive annotation value showing its runtime type and string value.
	 */
	@Override
	public String visitAnnotationValue(Primitive in) {
		if (in == null) return "null";
		return "["+in.getValue().getClass().getName()+"] "+in.getValue().toString();
	}

	/**
	 * Formats a class name by returning its canonical (fully-qualified) name.
	 */
	@Override
	public String visitClassName(JClassName in) {
		if (in == null) return "null";
		return in.getCanonicalName();
	}

	/**
	 * Formats a method name by returning its getter representation.
	 */
	@Override
	public String visitMethodName(JMethodName in) {
		if (in == null) return "null";
		return in.getter();
	}

	

}
