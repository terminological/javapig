package uk.co.terminological.javapig.sqlloader;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JTemplateInput;
import uk.co.terminological.javapig.javamodel.tools.JModelCopier;

/**
 * Base class for the SQL loader plugin, which generates JDBC reading/writing code
 * from interfaces annotated with {@code @Query} or {@code @Table}.
 *
 * <p>The plugin transforms the metamodel into {@link SqlProject} and provides adaptor
 * implementations for generating a database factory ({@link Model}) and SQL implementations
 * ({@link Interface}).</p>
 *
 * @see Query
 * @see Table
 * @see Column
 * @see SqlProject
 * @see SqlInterface
 * @see SqlGetMethod
 * @see Model
 * @see Interface
 */
public abstract class SqlPlugin extends JModelCopier implements JModelAdaptor {

	@Override
	public SqlProject map(JProject in) {
		return new SqlProject(in);
	}
	
	@Override
	public SqlInterface map(JInterface in) {
		if (in.isAnnotationPresent(Query.class)) return new SqlInterface.QueryBound(in);
		if (in.isAnnotationPresent(Table.class)) return new SqlInterface.TableBound(in);
		return new SqlInterface(in);
	}
	
	@Override
	public SqlGetMethod map(JGetMethod in) {
		return new SqlGetMethod(in);
	}

	/**
	 * Filters interfaces: only those annotated with {@code @Query} or {@code @Table} pass through.
	 *
	 * @param input the template input
	 * @return {@code true} if the input should be processed
	 */
	@Override
	public boolean filter(JTemplateInput input) {
		if (input instanceof SqlInterface) {
			return 
					((SqlInterface) input).isQueryBound() ||
					((SqlInterface) input).isTableBound()
					;
		} else {
			return true;
		}
	}

	/**
	 * Model-level adaptor: generates a {@code Database} factory for the root package.
	 */
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
			return "${rootPackage}.Database";
		}

		@Override
		public String getExtension() {
			return "java";
		}
	}

	/**
	 * Interface-level adaptor: generates an {@code XxxSql} implementation for each SQL interface.
	 */
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
