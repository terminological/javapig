package uk.co.terminological.javapig.test.annnotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Tester {
	String string() default "a default value";
	int integer() default 5;
	float floater() default 1.2F;
}
