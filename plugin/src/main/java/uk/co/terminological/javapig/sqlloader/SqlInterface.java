package uk.co.terminological.javapig.sqlloader;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;

public class SqlInterface extends JInterface {

	public SqlInterface(JInterface in) {
		super(in);
	}

	/** 
	 * If a method in this interface exists that is annotated with {@link uk.co.terminological.javapig.sqlloader.Query} then this returns that method
	 * otherwise returns null.
	 * @return
	 */
	public Set<JGetMethod> getColumns() {
		return this.getModel().getMethods().stream().filter(m -> m.declaredBy(this))
			.filter(m -> m.isAnnotationPresent(Column.class))
			.collect(Collectors.toSet());
	}
	
	public boolean isQueryBound() {
		return this.isAnnotationPresent(Query.class);
	}
	
	public boolean isTableBound() {
		return this.isAnnotationPresent(Table.class);
	}
	
	public static class QueryBound extends SqlInterface {

		public QueryBound(JInterface in) {
			super(in);
		}
		
		public String getSql() {
			return this.getAnnotation(Query.class).sql();
		}
		
		public List<Class<?>> getParameterTypes() {
			return Arrays.asList(this.getAnnotation(Query.class).parameterTypes());
		}
		
	}
	
	public static class TableBound extends SqlInterface {

		public TableBound(JInterface in) {
			super(in);
		}
		
		public String getTableName() {
			return this.getAnnotation(Table.class).name();
		}
		
	}
	
	
	
	
	
}
