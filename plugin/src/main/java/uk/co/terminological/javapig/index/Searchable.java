package uk.co.terminological.javapig.index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a getter method as a searchable field for the index plugin.
 *
 * <p>Methods annotated with {@code @Searchable} indicate that the returned value
 * should be indexed for text-based search operations. This annotation is typically
 * applied to String-returning methods.</p>
 *
 * @see Primary
 * @see Secondary
 * @see IndexableInterface
 * @see IndexablePlugin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Searchable {

}
