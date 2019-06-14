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
import java.sql.ResultSetMetaData;

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
	
	/*************** RESULT SET UTILITY FUNCTIONS **************************/
	
	private static interface FunctionWithException<T, R, E extends Exception> {
		R apply(T t) throws E;
	}
	
	private static <X, E extends Exception> Stream<X> streamResultSet(ResultSet rs, FunctionWithException<ResultSet,X, E> mapper) {
		Iterable<X> iterable = () -> iterateResultSet(rs,mapper);
		return StreamSupport.stream(iterable.spliterator(), false).onClose(() -> {
			try {
				rs.close();
			} catch (SQLException e) {
				//we tried;
			}
		});
	}
	
	private static <X, E extends Exception> Iterator<X> iterateResultSet(ResultSet rs, FunctionWithException<ResultSet,X,E> mapper) {
		return new Iterator<X>() {

			X out = null;
			@Override
			public boolean hasNext() {
				try {
					if (out == null) {
						boolean ready = rs.next();
						if (ready) out = mapper.apply(rs);
					}
				} catch (Exception e) {
					out = null;
				} 
				return out != null;
			}

			@Override
			public X next() {
				if (!hasNext()) throw new NoSuchElementException();
				X tmp = out;
				out = null;
				return tmp;
			}
			
		};
	}
	
	private static Map<String,Object> rowToMap(ResultSet rs) throws SQLException {
		Map<String, Object> out = new HashMap<>();
		ResultSetMetaData rsm = rs.getMetaData();
		for (int i=0; i<rsm.getColumnCount(); i++) {
			out.put(
				rsm.getColumnName(i+1), 
				rs.getObject(i+1)
			);
		}
		return out;
	}
	
	/*************** GENERAL DB FUNCTIONS **************************/
	
	public int apply(String preparedSql, Object... parameters) throws SQLException {
		int i = 1;
		PreparedStatement pst = conn.prepareStatement(preparedSql);
		for (Object parameter: parameters) {
			pst.setObject(i, parameter);
		}
    	return pst.executeUpdate();
	}
	
	public Stream<Map<String,Object>> retrieve(String preparedSql, Object... parameters) throws SQLException {
		int i = 1;
		PreparedStatement pst = conn.prepareStatement(preparedSql);
		for (Object parameter: parameters) {
			pst.setObject(i, parameter);
		}
    	ResultSet rs = pst.executeQuery();
    	return streamResultSet(rs,r -> rowToMap(r));
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
    		return iterateResultSet(rs,r -> new ${sn}Sql(rs));
    	}
	
	
		public Stream<${sn}> stream${sn}() throws SQLException {
			return stream${sn}(0);
		}
		
		public Stream<${sn}> stream${sn}(int limit) throws SQLException {
			if (limit >= 0) pst${sn}.setMaxRows(limit);
    		ResultSet rs = pst${sn}.executeQuery();
    		return streamResultSet(rs,r -> new ${sn}Sql(rs));
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
				"(<#list class.getWriteColumns() as method>${method.getColumnName()}<#sep>,</#sep></#list>)"+
				" values (<#list class.getWriteColumns() as method>?<#sep>,</#sep></#list>)"
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
	<#list class.getWriteColumns() as method>
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
	<#list class.getWriteColumns() as method>
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
    		return iterateResultSet(rs,r -> new ${sn}Sql(rs));
    	}
	
		public Stream<${sn}> stream${sn}(<#list class.getParameterTypes() as param>${param.getSimpleName()} param${param?index+1}<#sep>, <#sep></#list>) throws SQLException {
			<#list class.getParameterTypes() as param>
    		pst${sn}.setObject(${param?index+1}, param${param?index+1});
    </#list>		
    		ResultSet rs = pst${sn}.executeQuery();
    		return streamResultSet(rs,r -> new ${sn}Sql(rs));
		}
	
</#list>
	}
}
