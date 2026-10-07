package uk.co.terminological.javapig.mirrorapi;

import uk.co.terminological.javapig.javamodel.JMethodName;

/**
 * Represents a field-level mirror in the generated mirror API.
 *
 * <p>Implementations provide read/write access to a single field via {@link #get()}
 * and {@link #set(Object)}, along with the field name. Used by the MIRROR
 * built-in generator.</p>
 *
 * @param <T> the field value type
 * @see #get()
 * @see #set(Object)
 * @see #getName()
 */
public interface MirrorField<T extends Object> {
	/**
	 * Sets the field value.
	 *
	 * @param value the new value
	 * @throws UnsupportedOperationException if the field is read-only
	 */
	public void set(T value) throws UnsupportedOperationException;
	
	/**
	 * Returns the current field value.
	 *
	 * @return the field value
	 */
	public T get();
	
	/**
	 * Returns the field name.
	 *
	 * @return the JMethodName
	 */
	public JMethodName getName();
}
