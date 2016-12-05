package uk.co.terminological.javapig.index;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JTemplateInput;
import uk.co.terminological.javapig.javamodel.tools.JModelCopier;

public abstract class IndexablePlugin extends JModelCopier implements JModelAdaptor {

	@Override
	public IndexablePackage map(JPackage in) {
		return new IndexablePackage(in);
	}
	
	@Override
	public IndexableInterface map(JInterface in) {
		return new IndexableInterface(in);
	}
	
	@Override
	public IndexableGetMethod map(JGetMethod in) {
		return new IndexableGetMethod(in);
	}

	@Override
	public boolean filter(JTemplateInput input) {
		if (input instanceof IndexableInterface) {
			return ((IndexableInterface) input).hasIdentifier();
		} else {
			return true;
		}
	}

	public static class Model extends IndexablePlugin {

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

	public static class Interface extends IndexablePlugin {

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
