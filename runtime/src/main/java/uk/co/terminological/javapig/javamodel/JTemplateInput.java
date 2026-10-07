package uk.co.terminological.javapig.javamodel;

/**
 * Represents an element in the javapig metamodel that can be used as a template
 * input for code generation.
 *
 * <p>Interfaces ({@link JInterface}), packages ({@link JPackage}), methods ({@link JGetMethod}),
 * and the project itself ({@link JProject}) all implement this interface. Each element
 * provides a name and optional package metadata for scope resolution.</p>
 *
 * <p>The default {@link #isInPackage(JName)} method uses exact package-name matching
 * (not prefix matching) to determine whether a class belongs to a given package.</p>
 *
 * @see JInterface
 * @see JPackage
 * @see JGetMethod
 * @see JProject
 */
public interface JTemplateInput {
	
	/**
	 * Returns the name of this element.
	 *
	 * @return the JName for this element
	 */
	public JName getName();
	
	/**
	 * Returns the package metadata associated with the root package containing this element,
	 * or {@code null} if no metadata is available.
	 *
	 * @return the JPackageMetadata, or null
	 */
	public JPackageMetadata getMetadata();
	
	/**
	 * Checks whether this element belongs to the specified package using exact
	 * package-name matching (extracts the package from the element's canonical name
	 * by splitting at the last dot).
	 *
	 * <p>For example, {@code "com.example.terminological.MyClass"} belongs to package
	 * {@code "com.example.terminological"} but NOT to {@code "com.example.terminologi"}.</p>
	 *
	 * @param pkg the package name to check against
	 * @return {@code true} if this element's package exactly matches the given package name
	 */
	default boolean isInPackage(JName pkg) {
		String className = this.getName().getCanonicalName();
		String pkgName = pkg.getCanonicalName();
		int dotIndex = className.lastIndexOf('.');
		String classPackage = (dotIndex >= 0) ? className.substring(0, dotIndex) : "";
		return classPackage.equals(pkgName);
	}
}
