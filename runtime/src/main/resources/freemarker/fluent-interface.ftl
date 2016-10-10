<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
<#list class.getImports() as import>
import ${import};
</#list>
import ${fqn};

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public interface ${classname} extends ${sn} {

	// POJO setters
	// ==============

	<#list class.getMethods() as method>
	public void ${method.getName().setter()}(${method.getInterfaceType()} value);

		<#if method.isCollection()>
	public void ${method.getName().adder()}(${method.getUnderlyingType().getSimpleName()} value);

	public void ${method.getName().addAll()}(Collection<? extends ${method.getUnderlyingType().getSimpleName()}> values);

		<#elseif method.isOptional()>
	public void ${method.getName().setter()}(${method.getUnderlyingType().getSimpleName()} value);

		</#if>
	</#list> 
	
	// Fluent setters
	// ==============
	
	<#list class.getMethods() as method>
		<#if method.isOptional()>
	public ${sn}Fluent ${method.getName().with()}(${method.getUnderlyingType().getSimpleName()} value);
	
		<#elseif method.isCollection()>
	public ${sn}Fluent ${method.getName().with()}(${method.getUnderlyingType().getSimpleName()}... values);
	
	public ${sn}Fluent ${method.getName().with()}(Collection<? extends ${method.getUnderlyingType().getSimpleName()}> values);
	
		<#else>
	public ${sn}Fluent ${method.getName().with()}(${method.getInterfaceType()} value);
	
		</#if>	
	</#list>
}
