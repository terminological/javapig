package uk.co.terminological.javapig.index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a getter method as a primary index key for the index plugin.
 *
 * <p>Methods annotated with {@code @Primary} are treated identically to those
 * annotated with {@code @Id} — they define the primary lookup key for generated
 * index operations. Unlike {@code @Id}, this annotation does not imply database
 * persistence semantics.</p>
 *
 * @see Secondary
 * @see Searchable
 * @see IndexableInterface
 * @see IndexablePlugin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Primary {

}
