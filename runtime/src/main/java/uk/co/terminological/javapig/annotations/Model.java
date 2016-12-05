package uk.co.terminological.javapig.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The Model annotation is used to select packages that are going to be processed. 
 * The annotation must be in a package-info.java file and all classes and interfaces
 * in that package. 
 * Packages underneath are not processed unless they have their own Model annotation
 * and package-info.java file.
 * The mandatory directory defines where the freemarker templates for generating
 * class files are to be found e.g. "src/main/resources"
 * @author robchallen
 *
 */
@Retention(RetentionPolicy.SOURCE)
@Target(value = { ElementType.PACKAGE })
public @interface Model {
	BuiltIn[] builtins() default {};
	String[] plugins() default {};
}
