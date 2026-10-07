package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a Java package within the scanned metamodel.
 *
 * <p>A {@code JPackage} holds a reference to its containing {@link JProject},
 * declares its fully-qualified name, and exposes the interfaces, methods, and
 * sub-packages it contains. Packages are identified by the {@code @Model} annotation
 * placed in their {@code package-info.java}; only annotated packages enter the
 * scanning scope.</p>
 *
 * <p>Key responsibilities:</p>
 * <ul>
 *   <li>Filter interfaces and methods that belong to this package (exact package-name match).</li>
 *   <li>Provide access to the {@link JPackageMetadata} that controls which built-in generators
 *       are enabled for this package.</li>
 *   <li>Collect all imports required for code generation by aggregating imports from all
 *       interfaces in the package.</li>
 * </ul>
 *
 * @see JProject
 * @see JInterface
 * @see JGetMethod
 * @see JPackageMetadata
 */
public class JPackage extends JElement implements JTemplateInput {
	
	private String name;
	private JPackageMetadata metadata;
	
	/**
	 * Returns all interfaces declared in this package by querying the parent project
	 * and matching each interface's package name to this package's name.
	 *
	 * @return a set of all interfaces belonging to this package
	 * @see JProject#getClasses()
	 */
	public Set<JInterface> getClasses() {
		Set<JInterface> tmp = getModel().getClasses().stream().filter(i -> i.getName().getPackageName().equals(name)).collect(Collectors.toSet());
		return tmp;
	}

	/**
	 * Returns all sub-packages of this package by filtering the project's packages
	 * against this package's name using {@link JTemplateInput#isInPackage(JName)}.
	 *
	 * @return a set of direct child packages
	 * @see JTemplateInput#isInPackage(JName)
	 */
	public Set<JPackage> getPackages() {
		Set<JPackage> tmp = getModel().getPackages().stream()
				.filter(p -> p.isInPackage(this.getName()))
				.collect(Collectors.toSet());
		return tmp;
	}
	
	/**
	 * Returns all methods declared on interfaces in this package by filtering the
	 * project's methods against this package's name.
	 *
	 * @return a set of all methods belonging to interfaces in this package
	 */
	public Set<JGetMethod> getMethods() {
		return getModel().getMethods().stream()
				.filter(i -> i.isInPackage(getName()))
				.collect(Collectors.toSet());
	}
	
	/**
	 * Returns a unique string identifier for this package derived from its {@code hashCode}.
	 * The format is {@code "ID"} followed by the hash code with all {@code -} characters
	 * replaced by {@code M}.
	 *
	 * @return a package-specific identifier string
	 */
	public String code() {
		return ("ID"+this.hashCode()).replace("-", "M");
	}
	
	/**
	 * Checks whether a specific built-in generator is enabled for this package
	 * by delegating to the package metadata.
	 *
	 * @param builtInName the name of the built-in (e.g. {@code "IMPL"}, {@code "FLUENT"})
	 * @return {@code true} if the built-in is enabled, {@code false} otherwise
	 * @throws NullPointerException if this package has no metadata
	 * @see JPackageMetadata#isEnabled(String)
	 */
	public boolean isEnabled(String builtInName) {
		return getMetadata().isEnabled(builtInName);
	}
	
	/**
	 * Constructs a new package in the given project.
	 *
	 * @param model the containing project
	 * @param javaDoc the package-level JavaDoc comment
	 * @param annotations the annotations attached to this package
	 * @param name the fully-qualified package name (e.g. {@code "com.example.models"})
	 * @param metdata optional package metadata controlling built-in generators
	 */
	//JavaBean
	
	/**
	 * Constructs a new package in the given project.
	 */
	public JPackage(JProject model, String javaDoc, List<JAnnotation> annotations, String name, Optional<JPackageMetadata> metdata) {
		super(model,javaDoc,annotations);
		this.name = name;
		this.metadata = metdata.orElse(null);
	}
	
	/**
	 * Copy constructor that creates a shallow copy of the given package,
	 * preserving the model reference, name, and metadata.
	 *
	 * @param copy the package to copy
	 */
	public JPackage(JPackage copy) {
		this(
			copy.getModel(),
			copy.getJavaDoc(),
			new ArrayList<>(),
			copy.getName().toString(),
			Optional.ofNullable(copy.metadata)
		);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JPackage other = (JPackage) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	/**
	 * Returns the package name as a {@link JName} instance.
	 *
	 * @return the package name with identical values for getClassName(), getCanonicalName(), and getSimpleName()
	 */
	// Getters & setters
	
	public JName getName() {
		return new JName() {
			@Override
			public String getClassName() {
				return name;
			}

			@Override
			public String getCanonicalName() {
				return name;
			}

			@Override
			public String getSimpleName() {
				return name;
			}
			
			public String toString() {
				return name;
			}
			
		};
	}

	/**
	 * Sets the fully-qualified package name.
	 *
	 * @param packageName the new package name (e.g. {@code "com.example.newpkg"})
	 */
	public void setName(String packageName) {
		this.name = packageName;
	}

	/**
	 * Returns the metadata for this package, falling back to parent package metadata
	 * if this package has none.
	 *
	 * @return the package metadata, or the parent package's metadata if this package lacks one
	 * @see JPackage#getParent()
	 */
	public JPackageMetadata getMetadata() {
		if (metadata != null) return metadata;
		return this.getParent().map(p -> p.getMetadata()).orElse(null);
	}

	/**
	 * Collects all import names required for code generation within this package
	 * by aggregating imports from all interfaces contained in the package.
	 *
	 * @return a set of fully-qualified class names that should be imported
	 */
	public Set<String> getImports() {
		Set<String> tmp = new HashSet<>();
		for (JInterface iface: getClasses()) {
			tmp.add(iface.getName().toString());
			tmp.addAll(iface.getImports());
		}
		return tmp;
	}
	
	/**
	 * Sets the package metadata directly.
	 *
	 * @param metadata the metadata controlling built-in generators
	 */
	public void setMetadata(JPackageMetadata metadata) {
		this.metadata = metadata;
	}

	/**
	 * Creates a deep clone of this package including all annotations, metadata, and
	 * inherited elements from the model.
	 *
	 * @return a new JPackage instance with the same content
	 */
	@Override
	public JPackage clone() {
		JPackage out = copy();
		out.setAnnotations(
			this.getAnnotations().stream().map(a -> a.clone()).collect(Collectors.toList())	
		);
		out.setMetadata(metadata.clone());
		return out;
	}

	/**
	 * Creates a shallow copy of this package using the copy constructor.
	 *
	 * @return a new JPackage instance sharing the same model reference
	 */
	@Override
	public JPackage copy() {
		return new JPackage(this);
	}
	
	/**
	 * Returns the parent package of this package by looking up the parent name
	 * in the project, or {@code Optional.empty()} if no parent exists or the
	 * package is a root package (no dots in the name).
	 *
	 * @return the containing parent package, or empty if none
	 */
	public Optional<JPackage> getParent() {
		String pkgname = this.getName().getCanonicalName().toString();
		if (!pkgname.contains(".")) return null; 
		String parentPkgname = pkgname.substring(0, pkgname.lastIndexOf("."));
		return Optional.ofNullable(this.getModel().findPackage(parentPkgname));
	}

	/**
	 * Checks whether this package is a root package by determining if it has
	 * associated metadata (which indicates it was annotated with {@code @Model}).
	 *
	 * @return {@code true} if this package has metadata (is a root), {@code false} otherwise
	 */
	public boolean isRootPackage() {
		return metadata != null;
	}
}
