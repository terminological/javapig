<#import "common.ftl" as c>
<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
<#list class.getImports(fqn+"Fluent", "java.util.*") as import>
import ${import};
</#list>
<#if package.getMetadata().isEnabled("VISITOR")>import ${rootPackage}.Visitor;</#if>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} extends Observable implements ${sn}, ${sn}Fluent<#if package.getMetadata().isEnabled("VISITOR")>, Visitor.Acceptor</#if>  {

	public static ${sn}Fluent create() {
		return new ${classname}();
	}

	// Fields
	// ======

	<#list class.getMethods() as method>
	private ${method.getInterfaceType()} ${method.getName().field()}<#if method.isCollection()> = new ${method.getImplementationType()}()<#elseif method.isOptional()> = Optional.empty()<#elseif method.isArray()> = {}</#if>;
	</#list>

	// Public constructor
	// ==================

	public ${sn}FluentImpl() {}

	public ${sn}FluentImpl(
	<#list class.getMethods() as method>
		${method.getInterfaceType()} ${method.getName().field()}<#if method?has_next>,</#if>
	</#list>	
	) {
	<#list class.getMethods() as method>
		this.${method.getName().field()} = ${method.getName().field()};
	</#list>
	}
	
	<#list class.getMethods() as method>
		<#if method.isTypeOf("java.lang.Cloneable")>
	@SuppressWarnings("unchecked")
		<#break></#if>
	</#list>
	public ${sn}FluentImpl(${sn} clone) {
	<#list class.getMethods() as method>
		<#if method.isTypeOf("java.lang.Cloneable")>
		this.${method.getName().field()} = (${method.getImplementationType()}) clone.${method.getName().getter()}().clone();
		<#elseif method.isCollection()>
		this.${method.getName().field()} = new ${method.getImplementationType()}(clone.${method.getName().getter()}());
		<#elseif method.isInModel()>
		this.${method.getName().field()} = new ${method.getReturnType().getSimpleName()}FluentImpl(clone.${method.getName().getter()}());
		<#else>
		this.${method.getName().field()} = clone.${method.getName().getter()}();
		</#if>
	</#list>
	}
	
	public ${sn}FluentImpl clone() {
		return new ${sn}FluentImpl(this); 
	}
	
	// POJO Getters
	// ============
	
	<#list class.getMethods() as method>
	public ${method.getInterfaceType()} ${method.getName().getter()}() {
		return ${method.getName().field()};
	}
	</#list>
	
	// POJO Setters
	// ============
	
	<#list class.getMethods() as method>
	public void ${method.getName().setter()}(${method.getInterfaceType()} value) {
		this.${method.getName().field()} = value;
		this.setChanged();
		<#if method.hasInverseMethod()>
			<#assign imethod = method.getInverseMethod()/>
			<#if imethod.isCollection()>
		((${method.getInterfaceType()}Fluent) value).${imethod.getName().adder()}(this);
			<#else>
		((${method.getInterfaceType()}Fluent) value).${imethod.getName().setter()}(this);
			</#if>
		</#if>
		this.notifyObservers();
	}
	
		<#if method.isCollection()>
	public void ${method.getName().adder()}(${method.getUnderlyingType().getSimpleName()} value) {
		${method.getName().getter()}().add(value);
		this.setChanged();
		<#if method.hasInverseMethod()>
			<#assign imethod = method.getInverseMethod()/>
			<#if imethod.isCollection()>
		((${method.getInterfaceType()}Fluent) value).${imethod.getName().getter()}().add(this);
			<#else>
		((${method.getInterfaceType()}Fluent) value).${imethod.getName().setter()}(this);
			</#if>
		</#if>
		this.notifyObservers();
	}
	
	public void ${method.getName().addAll()}(Collection<? extends ${method.getUnderlyingType().getSimpleName()}> values) {
		${method.getName().getter()}().addAll(values);
		this.setChanged();
		this.notifyObservers();
	}
	
		<#elseif method.isOptional()>
	public void ${method.getName().setter()}(${method.getUnderlyingType().getSimpleName()} value) {
		${method.getName().setter()}(Optional.ofNullable(value));
	}
	
		</#if>
	</#list> 
	
	// Fluent setters
	// ==============
	
	<#list class.getMethods() as method>
		<#if method.isOptional()>
	public ${sn}Fluent ${method.getName().with()}(${method.getUnderlyingType().getSimpleName()} value) {
		${method.getName().setter()}(value);
		return this;
	}
	
		<#elseif method.isCollection()>
	public ${sn}Fluent ${method.getName().with()}(${method.getUnderlyingType().getSimpleName()}... values) {
		return ${method.getName().with()}(Arrays.asList(values));
	}
	
	public ${sn}Fluent ${method.getName().with()}(Collection<? extends ${method.getUnderlyingType().getSimpleName()}> values) {
		${method.getName().getter()}().addAll(values);
		return this;
	}
	
		<#else>
	public ${sn}Fluent ${method.getName().with()}(${method.getInterfaceType()} value) {
		${method.getName().setter()}(value);
		return this;
	}
		</#if>
	</#list>

	// hashCode and equals
	// ===================
	
	<@c.hashCode class/>

	<@c.equals class classname/>
	
}
