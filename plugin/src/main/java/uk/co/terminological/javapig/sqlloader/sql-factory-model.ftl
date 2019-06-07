<#import "datatypes.ftl" as d>
package ${packagename};

import javax.annotation.Generated;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.sql.Connection;

<#list model.getCsvClasses() as class>
import ${class.getName().getCanonicalName()};
import ${class.getName().getCanonicalName()}Csv;
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
			prop =  new Properties();
			prop.load(config.);
			Class.forName(prop.getProperty("driver"));
			conn = DriverManager.getConnection(prop.getProperty("url"), prop);
		} catch (Exception e) {
			throw new RuntimeException("exception setting up database connection: "+e.getLocalizedMessage(), e);
		}
	}
	
	public Table readTable() throws SQLException {
		return new Table(conn);
	}
	
	public static class Table {
	
<#list model.getTables() as class>
		PreparedStatement pst${class.getName().getSimpleName()};
</#list>
		 
	
		Table(Connection conn) throws SQLException {
<#list model.getTables() as class>
			pst${class.getName().getSimpleName()} = conn.prepareStatement("select * from ${class.getTableName()}");
</#list>
		}

<#list model.getTables() as class>
	<#assign sn=class.getName().getSimpleName()/>
		public Iterator<${sn}> into${sn}() throws SQLException {

    		ResultSet rs = pst${sn}.executeQuery();
			return new Iterator<${sn}>() {
				boolean started = false;
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
						if (started) rs.next();
						started = true;
						return new ${sn}(rs);
					} catch (SQLException e) {
						throw new NoSuchElementException(e.getMessage());
					}
				}
			};
    	}
	
</#list>
	}

	public class Query {
<#list model.getQueries() as class>
	<#assign sn=class.getName().getSimpleName()/>
		
		public Iterator<${sn}> get${sn}FromQuery(//parameters)  {
    		// ${class.getSql()}
    		// 
    	}
	
	
</#list>
	}

	//Factory classes for each Csv annotated class in model
<#list model.getCsvClasses() as class>
	<#assign sn=class.getName().getSimpleName()/>
	//${sn}
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
    public static class ${sn}FromCsv implements Iterable<${sn}> {
		
		Path in;
		Charset cs;
		
		//File based constructor
		public ${sn}FromCsv(Path in, Charset cs) throws IOException {
			this.in = in;
			this.cs = cs;
			if (!in.toFile().canRead()) throw new IOException("cannot read file: "+in);
		}
	
		// Iterator access to the csv implementation class converted from the parser output.
		public Iterator<${sn}> iterator() {
			Reader reader;
			try {
				reader = Files.newBufferedReader(in, cs);
				Iterable<Deferred<List<String>,ParserException>> output = ${class.getParser("reader")};
				return IterableMapper.create(
						output,
						al ->  {
							try {
								${sn} tmp = new ${sn}Csv(al.get());
								Indexes.get().index(tmp);
								return tmp;
							} catch (Exception e) {
								throw new RuntimeException(e);
							}
						}).iterator();
			} catch (IOException e) {
				// the IOException has been checked in the constructor. This should never be thrown.
				throw new RuntimeException(e);
			}
		}
	}
</#list>

}
