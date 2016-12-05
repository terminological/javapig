<#import "common.ftl" as c>
<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
<#list class.getImports() as import>
import ${import};
</#list>
<#list class.getMethods() as method>
	<#if !method.isCollection() && method.isInModel()>
import ${method.getReturnType().getCanonicalName()}Impl;
	</#if>
</#list>
<#if package.getMetadata().isEnabled("VISITOR")>import ${rootPackage}.Visitor;</#if>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
<#--<@c.copyAnnotations class 0/>-->
public class ${classname} implements ${sn}, Cloneable<#if package.getMetadata().isEnabled("VISITOR")>, Visitor.Acceptor</#if> {

	// Fields
	// ======

	<#list class.getMethods() as method>
	<#if method.isOptional()>
	final private ${method.getUnderlyingType().getSimpleName()} ${method.getName().field()};
	<#else>
	final private ${method.getInterfaceType()} ${method.getName().field()};
	</#if>
	</#list>

	// Public constructor
	// ==================

	public ${sn}Impl(
	<#list class.getMethods() as method>
		${method.getInterfaceType()} ${method.getName().field()}<#if method?has_next>,</#if>
	</#list>	
	) {
	<#list class.getMethods() as method>
		<#if method.isList(true)>
			this.${method.getName().field()} = Collections.unmodifiableList(${method.getName().field()});
		<#elseif method.isSet(true)>
			this.${method.getName().field()} = Collections.unmodifiableSet(${method.getName().field()});
		<#elseif method.isCollection()>
			this.${method.getName().field()} = new ${method.getImplementationType()}(${method.getName().field()});
		<#elseif method.isOptional()>
			this.${method.getName().field()} = ${method.getName().field()}.orElse(null);
		<#else>
			this.${method.getName().field()} = ${method.getName().field()};
		</#if>
	</#list>
	}
	
	<#list class.getMethods() as method>
		<#if method.isTypeOf("java.lang.Cloneable")>
	@SuppressWarnings("unchecked")
		<#break></#if>
	</#list>
	public ${sn}Impl(${sn} clone) {
	<#list class.getMethods() as method>
		<#if method.isTypeOf("java.lang.Cloneable")>
		this.${method.getName().field()} = (${method.getImplementationType()}) clone.${method.getName().getter()}().clone();
		<#elseif method.isList(true)>
		this.${method.getName().field()} = Collections.unmodifiableList(
			new ${method.getImplementationType()}(clone.${method.getName().getter()}()));
		<#elseif method.isSet(true)>
		this.${method.getName().field()} = Collections.unmodifiableSet(
			new ${method.getImplementationType()}(clone.${method.getName().getter()}()));
		<#elseif method.isCollection()>
		this.${method.getName().field()} = new ${method.getImplementationType()}(clone.${method.getName().getter()}());
		<#elseif method.isOptional()>
		this.${method.getName().field()} = clone.${method.getName().getter()}().orElse(null);
		<#elseif method.isInModel()>
		this.${method.getName().field()} = new ${method.getReturnType().getSimpleName()}Impl(clone.${method.getName().getter()}());
		<#else>
		this.${method.getName().field()} = clone.${method.getName().getter()}();
		</#if>
	</#list>
	}
	
	public ${sn}Impl clone() {
		return new ${sn}Impl(this); 
	}

	// POJO Getters
	// ============

	<#list class.getMethods() as method>
	<#-- <@c.copyAnnotations method 1/>-->
	public ${method.getInterfaceType()} ${method.getName().getter()}() {
	<#if method.isOptional()>
		return Optional.ofNullable(${method.getName().field()});
	<#else>
		return ${method.getName().field()};
	</#if>
	}
	</#list> 
	
	// hashCode and equals
	// ===================
	
	<@c.hashCode class/>

	<@c.equals class classname/>
}
