package uk.co.terminological.javapig.index;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a getter method as a secondary index for the index plugin.
 *
 * <p>Methods annotated with {@code @Secondary} generate alternative lookup keys
 * that supplement the primary index. Unlike primary indexes, secondary indexes
 * are not used for the main entity lookup but enable additional query patterns.</p>
 *
 * @see Primary
 * @see Searchable
 * @see IndexableInterface
 * @see IndexablePlugin
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface Secondary {

}
