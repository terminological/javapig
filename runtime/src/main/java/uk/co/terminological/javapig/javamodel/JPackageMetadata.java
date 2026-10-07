package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.annotations.Model;

/**
 * Configuration metadata for a package annotated with {@code @Model}.
 *
 * <p>Carries the values from the {@code @Model} annotation, including which
 * built-in generators to enable and which custom plugins to load.</p>
 *
 * @see Model
 * @see BuiltIn
 * @see JPackage#getMetadata()
 */
public class JPackageMetadata implements Serializable {
	
	/**
	 * Constructs package metadata from built-in generator enums and plugin class names.
	 *
	 * @param builtIns the built-in generators to enable for this package
	 * @param plugins fully-qualified class names of custom plugin adaptors
	 */
	public JPackageMetadata(
			List<BuiltIn> builtIns,
			List<String> plugins) {
		//this.templates = templates;
		this.builtIns = builtIns;
		this.setPlugins(plugins);
		//this.templateDirectory = templateDirectory;
	}

	/** The list of enabled built-in generators (e.g. IMPL, FLUENT, FACTORY). */
	private List<BuiltIn> builtIns = new ArrayList<>();
	
	/** Fully-qualified class names of custom plugin adaptors. */
	private List<String> plugins = new ArrayList<>();

	/**
	 * Returns the list of enabled built-in generators.
	 *
	 * @return the built-in generator list
	 */
	public List<BuiltIn> getBuiltIns() {
		return builtIns;
	}
	
	/**
	 * Checks whether the specified built-in generator is enabled for this package.
	 *
	 * @param builtInName the name of the built-in (e.g. {@code "IMPL"}, {@code "FLUENT"})
	 * @return {@code true} if the built-in is enabled
	 */
	public boolean isEnabled(String builtInName) {
		return builtIns.stream().anyMatch(b -> b.toString().equals(builtInName));
	}

	/**
	 * Creates a deep clone of this metadata by copying the built-ins and plugins lists.
	 *
	 * @return a new JPackageMetadata with the same configuration
	 */
	public JPackageMetadata clone() {
		return new JPackageMetadata(
				this.getBuiltIns(),
				this.getPlugIns()
				);
	}

	/**
	 * Returns this instance (identity copy).
	 *
	 * @return this metadata
	 */
	public JPackageMetadata copy() {
		return this;
	}
	
	/**
	 * Constructs package metadata from a {@code @Model} annotation element.
	 *
	 * @param ann the annotation element extracted by the scanner
	 * @return a new JPackageMetadata
	 */
	public static JPackageMetadata from (Model ann) {
		return new JPackageMetadata(
				Arrays.asList(ann.builtins()),
				Arrays.asList(ann.plugins()));
	}

	/**
	 * Returns the list of plugin class names.
	 *
	 * @return the plugin class name list
	 */
	public List<String> getPlugIns() {
		return plugins;
	}

	/**
	 * Sets the list of plugin class names.
	 *
	 * @param plugins the fully-qualified class names
	 */
	public void setPlugins(List<String> plugins) {
		this.plugins = plugins;
	}
}
