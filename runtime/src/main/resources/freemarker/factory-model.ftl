package ${packagename};

import javax.annotation.Generated;
import java.util.*;

<#list model.getPackages() as package>
	<#list package.getClasses() as class>
import ${class.getName().getCanonicalName()};
		<#if package.isEnabled("IMPL")>
import ${class.getName().getCanonicalName()}Impl;
		</#if>
		<#if package.isEnabled("FLUENT_IMPL")>
import ${class.getName().getCanonicalName()}Fluent;
import ${class.getName().getCanonicalName()}FluentImpl;
		</#if>
	</#list>
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} {


	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class Mutable {
<#list model.getPackages() as package>
	<#if package.isEnabled("FLUENT_IMPL")>
		<#list package.getClasses() as class>
		
		public static ${class.getName().getSimpleName()}Fluent create${class.getName().getSimpleName()}() {
			return ${class.getName().getSimpleName()}FluentImpl.create();
		}
		</#list>
	</#if>
</#list>
	}


	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class Immutable {
<#list model.getPackages() as package>
	<#if package.isEnabled("IMPL")>
	<#list package.getClasses() as class>
		
		public static ${class.getName().getSimpleName()}Builder create${class.getName().getSimpleName()}() {
			return new ${class.getName().getSimpleName()}Builder();
		}
	
		@Generated({"uk.co.terminological.javapig.JModelWriter"})
		public static class ${class.getName().getSimpleName()}Builder {
		
			private ${class.getName().getSimpleName()}Builder() {}
		
		<#list class.getMethods() as method>
			private ${method.getInterfaceType()} tmp${method.getName().field()}<#if method.isCollection()> = new ${method.getImplementationType()}()<#elseif method.isOptional()> = Optional.empty()<#elseif method.isArray()> = {}</#if>;
		</#list>
		
		<#list class.getMethods() as method>
			<#if method.isOptional()>
			public  ${class.getName().getSimpleName()}Builder ${method.getName().with()}(${method.getUnderlyingType().getSimpleName()} value) {
				tmp${method.getName().field()} = Optional.ofNullable(value);
				return this;
			}
	
			<#elseif method.isCollection()>
			public  ${class.getName().getSimpleName()}Builder ${method.getName().with()}(${method.getUnderlyingType().getSimpleName()}... values) {
				tmp${method.getName().field()}.addAll(Arrays.asList(values));
				return this;
			}
	
			public  ${class.getName().getSimpleName()}Builder ${method.getName().with()}(Collection<? extends ${method.getUnderlyingType().getSimpleName()}> values) {
				tmp${method.getName().field()}.addAll(values);
				return this;
			}
	
			<#else>
			public  ${class.getName().getSimpleName()}Builder ${method.getName().with()}(${method.getInterfaceType()} value) {
				tmp${method.getName().field()} = value;
				return this;
			}
			
			</#if>
		</#list>
			public ${class.getName().getSimpleName()} build() {
				return new ${class.getName().getSimpleName()}Impl(
		<#list class.getMethods() as method>
					tmp${method.getName().field()}<#if method?has_next>,</#if>
		</#list>
				);
			}	
		}
	</#list>
	</#if>
</#list>
	}
}
