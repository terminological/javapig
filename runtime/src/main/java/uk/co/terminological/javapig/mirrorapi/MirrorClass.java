package uk.co.terminological.javapig.mirrorapi;

import uk.co.terminological.javapig.javamodel.JClassName;

/**
 * Represents a class-level mirror in the generated mirror API.
 *
 * <p>Implementations reflect the class name and can produce a typed instance
 * via {@link #reflect()}. Used by the MIRROR built-in generator.</p>
 *
 * @param <T> the reflected type
 * @see #reflect()
 * @see #getName()
 */
public interface MirrorClass<T extends Object> {
	/**
	 * Returns a typed instance of the reflected class.
	 *
	 * @return the reflected object
	 */
	public T reflect();
	
	/**
	 * Returns the class name of this mirror.
	 *
	 * @return the JClassName
	 */
	public JClassName getName();
}
