package uk.co.terminological.javapig.csvloader;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks an interface as a CSV data binding for the CSV loader plugin.
 *
 * <p>The plugin generates CSV reading/writing code based on the interface's getter
 * methods. Each getter corresponds to a CSV column, with the column position
 * determined by the {@link Type} value.</p>
 *
 * @see CsvPlugin
 * @see CsvInterface
 * @see Type
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Csv {
	/** The CSV column type for each field. */
	Type value();
	/** The line terminator character. Default is CRLF. */
	String lineTerminator() default "\r\n";
	/** The field separator. Default is comma. */
	String seperator() default ",";
	/** The enclosing character for fields. Default is double-quote. */
	String enclosedBy() default "\"";
	/** The escape character. Default is double-quote. */
	String escapedBy() default "\"";
	/** Number of header lines to skip. Default is 1. */
	int headerLines() default 1;
	/** Whether to always enclose fields. Default is false. */
	boolean alwaysEnclosed() default false;
}
