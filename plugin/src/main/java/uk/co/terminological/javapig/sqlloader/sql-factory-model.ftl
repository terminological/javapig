package ${packagename};

import javax.annotation.Generated;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.*;
import java.util.function.Consumer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

<#list model.getSqlClasses() as class>
import ${class.getName().getCanonicalName()};
import ${class.getName().getCanonicalName()}Sql;
</#list>

/**
*	The SqlFactory
*
*	@author: javapig. oink, oink! 
*/
@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} {

	//Coordination and indexing of the output
	
	Connection conn;
	
	public ${classname}(Path config) {
		try {
			Properties prop =  new Properties();
			prop.load(Files.newInputStream(config));
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop);
		} catch (Exception e) {
			throw new RuntimeException("exception setting up database connection: "+e.getLocalizedMessage(), e);
		}
	}
	
	private static <T extends Object> Stream<T> streamOf(Iterator<T> av) {
		Iterable<T> iterable = () -> av;
		return StreamSupport.stream(iterable.spliterator(), false);
	}
	
	/*************** TABLE READERS **************************/
	
	public Reader read() throws SQLException {
		if (reader == null) reader = new Reader(conn);
		return reader;
	}
	
	Reader reader;
	
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class Reader {
	
<#list model.getTables() as class>
		PreparedStatement pst${class.getName().getSimpleName()};
</#list>

		Reader(Connection conn) throws SQLException {
<#list model.getTables() as class>
			pst${class.getName().getSimpleName()} = conn.prepareStatement("select * from ${class.getTableName()}");
</#list>
		}
	
<#list model.getTables() as class>
	<#assign sn=class.getName().getSimpleName()/>
	
		public Iterator<${sn}> from${sn}() throws SQLException {
			return from${sn}(0);
		}

		public Iterator<${sn}> from${sn}(int limit) throws SQLException {
			if (limit >= 0) pst${sn}.setMaxRows(limit);
    		ResultSet rs = pst${sn}.executeQuery();
    		return new Iterator<${sn}>() {
				@Override
				public boolean hasNext() {
					try {
						return !rs.isLast();
					} catch (SQLException e) {
						return false;
					}
				}

				@Override
				public ${sn} next() {
					if (!hasNext()) throw new NoSuchElementException();
					try {
						rs.next();
						return new ${sn}Sql(rs);
					} catch (SQLException e) {
						throw new NoSuchElementException(e.getMessage());
					}
				}
			};
    	}
	
	
		public Stream<${sn}> stream${sn}() throws SQLException {
			return streamOf(from${sn}());
		}
		
		public Stream<${sn}> stream${sn}(int limit) throws SQLException {
			return streamOf(from${sn}(limit));
		}
</#list>
	}
	
	/*************** TABLE WRITERS **************************/

	public Writer write() throws SQLException {
		if (writer == null) writer = new Writer(conn);
		return writer;
	}
	
	public Writer writer;
	
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class Writer{
	
<#list model.getTables() as class>
		PreparedStatement pst${class.getName().getSimpleName()};
</#list>

		Writer(Connection conn) throws SQLException {
<#list model.getTables() as class>
			pst${class.getName().getSimpleName()} = conn.prepareStatement("insert into ${class.getTableName()} "+
				"(<#list class.getColumns() as method>${method.getColumnName()}<#sep>,</#sep></#list>)"+
				" values (<#list class.getColumns() as method>?<#sep>,</#sep></#list>)"
			);
</#list>
		}

<#list model.getTables() as class>
	<#assign sn=class.getName().getSimpleName()/>
		public Consumer<${sn}> of${sn}(final boolean rethrow) {
    		return new Consumer<${sn}>() {
    			@Override
    			public void accept(${sn} input) {
    				try {
    					write${sn}(input);
    				} catch (SQLException e) {
    					if (rethrow) {
    						throw new RuntimeException(e);
    					}
    				}
				}
			};
    	}
	
		public int write${sn}(${sn} input) throws SQLException {
			pst${sn}.clearParameters();
	<#list class.getColumns() as method>
    		pst${sn}.setObject(${method?index+1}, input.${method.getName().getter()}());
    </#list>
    		return pst${sn}.executeUpdate();
    	}
	
		public int writeBatch${sn}(Collection<${sn}> inputs) throws SQLException {
			return writeBatch${sn}(inputs,0);
		}
	
		public int writeBatch${sn}(Collection<${sn}> inputs, int max) throws SQLException {
			int affected = 0;
			int current = 0;
			for (${sn} input: inputs) {
				pst${sn}.clearParameters();
	<#list class.getColumns() as method>
    			pst${sn}.setObject(${method?index+1}, input.${method.getName().getter()}());
    </#list>
    			pst${sn}.addBatch();
    			if (max > 0 && current >= max) {
    				affected += IntStream.of(pst${sn}.executeBatch()).sum();
    				current = 0;
    			}
    		} 
    		affected += IntStream.of(pst${sn}.executeBatch()).sum();
    		return affected;
		}
</#list>
	}
	
	/*************** SQL QUERIES **************************/

	public Query query() throws SQLException {
		if (query == null) query = new Query(conn);
		return query;
	}
	
	Query query;

	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public class Query {
	
<#list model.getQueries() as class>
		PreparedStatement pst${class.getName().getSimpleName()};
</#list>
		 
	
		Query(Connection conn) throws SQLException {
<#list model.getQueries() as class>
			pst${class.getName().getSimpleName()} = conn.prepareStatement("${class.getSql()}");
</#list>
		}
	
	
<#list model.getQueries() as class>
	<#assign sn=class.getName().getSimpleName()/>
		
		public Iterator<${sn}> from${sn}(<#list class.getParameterTypes() as param>${param.getSimpleName()} param${param?index+1}<#sep>, <#sep></#list>) throws SQLException  {
    <#list class.getParameterTypes() as param>
    		pst${sn}.setObject(${param?index+1}, param${param?index+1});
    </#list>		
    		ResultSet rs = pst${sn}.executeQuery();
			return new Iterator<${sn}>() {
				@Override
				public boolean hasNext() {
					try {
						return !rs.isLast();
					} catch (SQLException e) {
						return false;
					}
				}

				@Override
				public ${sn} next() {
					if (!hasNext()) throw new NoSuchElementException();
					try {
						rs.next();
						return new ${sn}Sql(rs);
					} catch (SQLException e) {
						throw new NoSuchElementException(e.getMessage());
					}
				}
			}; 
    	}
	
		public Stream<${sn}> stream${sn}(<#list class.getParameterTypes() as param>${param.getSimpleName()} param${param?index+1}<#sep>, <#sep></#list>) throws SQLException {
			return streamOf(from${sn}(<#list class.getParameterTypes() as param>param${param?index+1}<#sep>, <#sep></#list>));
		}
	
</#list>
	}
}
