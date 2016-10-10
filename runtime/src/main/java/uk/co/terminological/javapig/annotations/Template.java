package uk.co.terminological.javapig.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The template annotation is expecteed to be within a @Model annotation and 
 * gives us the filename of a freemarker template, a context within which 
 * to use it (Scope.PACKAGE, Scope.CLASS or Scope.INTERFACE and a 
 * template for the naming of the generated class.
 * 
 * This template performs the following substitutions (only) 
 * 1) ${package} for the package fully qualified name
 * 2) ${class} for the class or interface FQN
 * 3) ${className} for the class or interface short name
 * The result should be the FQN of the new class 
 */
@Retention(RetentionPolicy.SOURCE)
public @interface Template {
	String filename();
	Scope[] appliesTo();
	String classnameTemplate();
	String extension() default "java";
}
