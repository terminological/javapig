package uk.co.terminological.javapig.javamodel;

/**
 * Represents a named entity in the javapig metamodel, such as a class, package,
 * or method.
 *
 * <p>All name-bearing elements implement this interface and provide three accessors:</p>
 * <ul>
 *   <li>{@link #getClassName()} — the full class/package name (equivalent to getCanonicalName).</li>
 *   <li>{@link #getCanonicalName()} — the fully-qualified name.</li>
 *   <li>{@link #getSimpleName()} — the simple (non-qualified) name.</li>
 * </ul>
 *
 * <p>Common implementations: {@link JClassName} for classes, anonymous {@code JName}
 * instances in {@link JPackage} for packages, and {@link JMethodName} for methods.</p>
 *
 * @see JClassName
 * @see JMethodName
 * @see JTemplateInput#getName()
 */
public interface JName {

	/**
	 * Returns the full name of this entity.
	 * For classes this is the fully-qualified name; for packages it is the package name.
	 *
	 * @return the class/package name
	 */
	public String getClassName();
	
	/**
	 * Returns the fully-qualified canonical name.
	 * For simple class names without a package, this defaults to the boxed type name.
	 *
	 * @return the canonical name
	 */
	public String getCanonicalName();
	
	/**
	 * Returns the simple (unqualified) name — the last segment after the final dot.
	 *
	 * @return the simple name (e.g. {@code "List"} for {@code "java.util.List"})
	 */
	public String getSimpleName();
	
	/**
	 * Returns a string representation of this name.
	 *
	 * @return the name as a string
	 */
	public String toString();
	
}
