<#import "datatypes.ftl" as d>
package ${packagename};

import javax.annotation.Generated;
import uk.co.terminological.parser.DelimitedParserBuilder;
import uk.co.terminological.parser.StateMachineException;
import uk.co.terminological.datatypes.IterableMapper;
import uk.co.terminological.datatypes.Deferred;
import java.nio.charset.StandardCharsets;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.io.Reader;
import java.util.*;


<#list model.getClasses() as class>
import ${class.getName().getCanonicalName()};
</#list>

/**
*	The CsvFactory
*
*	@author: javapig. oink, oink! 
*/
@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} {

	//Coordination and indexing of the output
	


	//static factory methods for individual class level factories
<#list model.getCsvClasses() as class>
	<#assign sn=class.getName().getSimpleName()/>
	public static ${sn}FromCsv get${sn}(Path in) throws IOException {
		return get${sn}(in, StandardCharsets.UTF_8);
	}
	
    public static ${sn}FromCsv get${sn}(Path in, Charset cs) throws IOException {
		return new ${sn}FromCsv(in, cs);
	}
	
</#list>

	
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
				Iterable<Deferred<List<String>,StateMachineException>> output = ${class.getParser("reader")};
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
<#-- <#list class.getMethods() as method>
		<#if method.isField()>
			<#assign getFromList>input.get(${method.order()})</#assign>
			<#if method.isOptional()>
			${method.getUnderlyingType().getSimpleName()} ${method.getName().field()} = <@d.mapByType getFromList method.getUnderlyingType().getCanonicalName()/>;
			<#else>
			${method.getInterfaceType()} ${method.getName().field()} = <@d.mapByType getFromList method.getInterfaceTypeFQN()/>;
			</#if>
		<#else>
			${method.getInterfaceType()} ${method.getName().field()} = null; //not part of csv mapping 
		</#if>
	</#list>
			return new ${sn}Indexable(
	<#list class.getMethods() as method>
		<#if method.isOptional()>
				Optional.ofNullable(${method.getName().field()}<#if method?has_next>),</#if>
		<#else>
				${method.getName().field()}<#if method?has_next>,</#if>
		</#if>
	</#list>	
				);
	 -->