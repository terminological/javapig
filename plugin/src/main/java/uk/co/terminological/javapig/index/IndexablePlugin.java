package uk.co.terminological.javapig.index;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JTemplateInput;
import uk.co.terminological.javapig.javamodel.tools.JModelCopier;

/**
 * Base class for the index plugin, which extends the metamodel with indexing support
 * and provides adaptor implementations for generating indexed search code.
 *
 * <p>The plugin transforms the standard JProject into an {@link IndexableProject} that
 * identifies interfaces with {@code @Id} annotations on getter methods, then renders
 * FreeMarker templates to generate index implementations.</p>
 *
 * <p>Two built-in variants are provided:</p>
 * <ul>
 *   <li>{@link Model} — generates a single {@code Indexes} class for the root package.</li>
 *   <li>{@link Interface} — generates a {@code XxxIndexable} class for each indexed interface.</li>
 * </ul>
 *
 * @see IndexableProject
 * @see IndexableInterface
 * @see IndexableGetMethod
 * @see Primary
 * @see Secondary
 * @see Searchable
 */
public abstract class IndexablePlugin extends JModelCopier implements JModelAdaptor {

	@Override
	public IndexableProject map(JProject in) {
		return new IndexableProject(in);
	}
	
	@Override
	public IndexableInterface map(JInterface in) {
		return new IndexableInterface(in);
	}
	
	@Override
	public IndexableGetMethod map(JGetMethod in) {
		return new IndexableGetMethod(in);
	}

	/**
	 * Filters interfaces: only interfaces with an identifier (annotated with {@code @Id})
	 * pass through the MODEL scope filter.
	 *
	 * @param input the template input
	 * @return {@code true} if the input should be processed
	 */
	@Override
	public boolean filter(JTemplateInput input) {
		if (input instanceof IndexableInterface) {
			return ((IndexableInterface) input).hasIdentifier();
		} else {
			return true;
		}
	}

	/**
	 * Model-level adaptor: generates a single {@code Indexes} class for the entire root package.
	 */
	public static class Model extends IndexablePlugin {

		/** Generates the entire model as a single index container. */
		@Override
		public Scope[] getScope() {
			return new Scope[] { Scope.MODEL };
		}

		@Override
		public String getTemplateFilename() {
			return "index-model.ftl";
		}

		@Override
		public String getClassNameTemplate() {
			return "${rootPackage}.Indexes";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}

	/**
	 * Interface-level adaptor: generates an {@code XxxIndexable} class for each indexed interface.
	 */
	public static class Interface extends IndexablePlugin {

		/** Generates indexable wrappers for individual interfaces. */
		@Override
		public Scope[] getScope() {
			return new Scope[] { Scope.INTERFACE };
		}

		@Override
		public String getTemplateFilename() {
			return "indexable-class.ftl";
		}

		@Override
		public String getClassNameTemplate() {
			return "${classFQN}Indexable";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}
	
}
