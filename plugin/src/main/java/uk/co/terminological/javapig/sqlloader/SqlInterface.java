package uk.co.terminological.javapig.sqlloader;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.emory.mathcs.backport.java.util.Collections;
import uk.co.terminological.datatypes.FluentList;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;

public class SqlInterface extends JInterface {

	public SqlInterface(JInterface in) {
		super(in);
	}

	/** 
	 * If a method in this interface exists that is annotated with {@link uk.co.terminological.javapig.sqlloader.Column} then this returns that method
	 * otherwise returns null.
	 * @return
	 */
	public List<JGetMethod> getColumns() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Column.class))
			.collect(Collectors.toList());
	}
	
	/** 
	 * If a method in this interface exists that is annotated with {@link uk.co.terminological.javapig.sqlloader.Column} and that are
	 * not marked as autoincrement.
	 * @return
	 */
	public List<JGetMethod> getWriteColumns() {
		return this.getMethods().stream()
			.filter(m -> m.isAnnotationPresent(Column.class) && !m.getAnnotation(Column.class).isAutoIncrement())
			.collect(Collectors.toList());
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
		
		public List<JClassName> getParameterTypes() {
			if (this.getAnnotation(Query.class).parameterTypes() == null) return FluentList.empty();
			return Stream.of(this.getAnnotation(Query.class).parameterTypes())
					.map(c -> JClassName.from(c.getCanonicalName()))
					.collect(Collectors.toList());
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
