package uk.co.terminological.javapig.csvloader;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.index.IndexablePlugin;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JTemplateInput;

/**
 * Base class for the CSV loader plugin, which generates CSV reading/writing code
 * from annotated interfaces.
 *
 * <p>The plugin extends the index plugin's metamodel and provides adaptor implementations
 * for generating a CSV factory ({@link Factory}) and POJO implementations ({@link POJO}).</p>
 *
 * <p>Only interfaces annotated with {@code @Csv} are included in code generation.</p>
 *
 * @see Csv
 * @see CsvProject
 * @see CsvInterface
 * @see CsvGetMethod
 * @see Factory
 * @see POJO
 */
public abstract class CsvPlugin extends IndexablePlugin implements JModelAdaptor {

	@Override
	public CsvProject map(JProject in) {
		return new CsvProject(in);
	}

	@Override
	public CsvInterface map(JInterface in) {
		return new CsvInterface(in);
	}

	@Override
	public CsvGetMethod map(JGetMethod in) {
		return new CsvGetMethod(in);
	}

	/**
	 * Filters interfaces: only those annotated with {@code @Csv} pass through.
	 *
	 * @param input the template input
	 * @return {@code true} if the input should be processed
	 */
	@Override
	public boolean filter(JTemplateInput input) {
		if (input instanceof CsvInterface) {
			return ((CsvInterface) input).isCsvBound();
		} else {
			return true;
		}
	}

	/**
	 * Factory-level adaptor: generates a single {@code CsvFactory} for the root package.
	 */
	public static class Factory extends CsvPlugin {
		@Override
		public Scope[] getScope() {
			return new Scope[] { Scope.MODEL };
		}

		@Override
		public String getTemplateFilename() {
			return "csv-factory-model.ftl";
		}

		@Override
		public String getClassNameTemplate() {
			return "${rootPackage}.CsvFactory";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}

	/**
	 * Interface-level adaptor: generates a {@code XxxCsv} POJO implementation for each CSV interface.
	 */
	public static class POJO extends CsvPlugin {
		@Override
		public Scope[] getScope() {
			return new Scope[] { Scope.INTERFACE };
		}

		@Override
		public String getTemplateFilename() {
			return "csv-implementation-class.ftl";
		}

		@Override
		public String getClassNameTemplate() {
			return "${classFQN}Csv";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}
}
