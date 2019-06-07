package uk.co.terminological.javapig.sqlloader;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JTemplateInput;
import uk.co.terminological.javapig.javamodel.tools.JModelCopier;

public abstract class SqlPlugin extends JModelCopier implements JModelAdaptor {

	@Override
	public SqlProject map(JProject in) {
		return new SqlProject(in);
	}
	
	@Override
	public SqlInterface map(JInterface in) {
		if (in.isAnnotationPresent(Query.class)) return new SqlInterface.QueryBound(in);
		if (in.isAnnotationPresent(Table.class)) return new SqlInterface.TableBound(in);
		throw new RuntimeException();
	}
	
	@Override
	public SqlGetMethod map(JGetMethod in) {
		return new SqlGetMethod(in);
	}

	@Override
	public boolean filter(JTemplateInput input) {
		/*if (input instanceof IndexableInterface) {
			return ((IndexableInterface) input).hasIdentifier();
		} else {*/
			return true;
		//}
	}

	public static class Model extends SqlPlugin {

		@Override
		public Scope[] getScope() {
			return new Scope[] { Scope.MODEL };
		}

		@Override
		public String getTemplateFilename() {
			return "sql-factory-model.ftl";
		}

		@Override
		public String getClassNameTemplate() {
			return "${rootPackage}.SqlReader";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}

	public static class Interface extends SqlPlugin {

		@Override
		public Scope[] getScope() {
			return new Scope[] { Scope.INTERFACE };
		}

		@Override
		public String getTemplateFilename() {
			return "sql-implementation-class.ftl";
		}

		@Override
		public String getClassNameTemplate() {
			return "${classFQN}Sql";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}
	
}
