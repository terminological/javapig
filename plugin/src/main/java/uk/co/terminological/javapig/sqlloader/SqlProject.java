package uk.co.terminological.javapig.sqlloader;

import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.Id;

import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JProject;

public class SqlProject extends JProject {

	public SqlProject(JProject in) {
		super(in);
	}
	
	public Set<SqlInterface> getSqlClasses() {
		return this.getClasses().stream()
				.filter(
					m -> (m.isAnnotationPresent(Table.class) || 
					m.isAnnotationPresent(Query.class)
					)
				)
				.map(c -> (SqlInterface) c)
				.collect(Collectors.toSet());
	}
	
	public Set<SqlInterface.QueryBound> getQueries() {
		return this.getClasses().stream()
				.filter(
						m -> m.isAnnotationPresent(Query.class)
					)
					.map(c -> (SqlInterface.QueryBound) c)
					.collect(Collectors.toSet());
	}
	
	public Set<SqlInterface.TableBound> getTables() {
		return this.getClasses().stream()
				.filter(
						m -> m.isAnnotationPresent(Table.class)
					)
					.map(c -> (SqlInterface.TableBound) c)
					.collect(Collectors.toSet());
	}
	
		
}
