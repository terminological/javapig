package uk.co.terminological.javapig.csvloader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Csv {
	Type value();
	String lineTerminator() default "\r\n";
	String seperator() default ",";
	String enclosedBy() default "\"";
	String escapedBy() default "\"";
	int headerLines() default 1;
	boolean alwaysEnclosed() default false;
}
