package uk.co.terminological.javapig.csvloader;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.index.IndexablePlugin;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JTemplateInput;

public abstract class CsvPlugin extends IndexablePlugin implements JModelAdaptor {

	@Override
	public CsvPackage map(JPackage in) {
		return new CsvPackage(in);
	}

	@Override
	public CsvInterface map(JInterface in) {
		return new CsvInterface(in);
	}

	@Override
	public CsvGetMethod map(JGetMethod in) {
		return new CsvGetMethod(in);
	}

	@Override
	public boolean filter(JTemplateInput input) {
		if (input instanceof CsvInterface) {
			return ((CsvInterface) input).isCsvBound();
		} else {
			return true;
		}
	}

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
