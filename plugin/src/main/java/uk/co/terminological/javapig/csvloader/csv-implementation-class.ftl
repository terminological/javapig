<#import "datatypes.ftl" as d>
<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
import ${rootPackage}.Indexes;
<#list class.getImports("java.util.List") as import>
import ${import};
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} implements ${sn} {

	// Fields
	// ======

	private List<String> csvInput;

	// Public constructor
	// ==================

	public ${classname}(List<String> csvInput) {
		this.csvInput = csvInput;
		Indexes.get().index(this);
	}
	
	// POJO Getters
	// ============

	<#list class.getMethods() as method>
	public ${method.getInterfaceType()} ${method.getName().getter()}() {
		<#if method.isField()>
		String csvField = csvInput.get(${method.order()}); 
			<#if method.isOptional()>
		${method.getUnderlyingType().getSimpleName()} tmp = <@d.mapByType "csvField" method.getUnderlyingType().getCanonicalName()/>;
		return Optional.ofNullable(tmp);
			<#elseif method.returnTypeIsIndexed()>
		${method.indexedReturnKeyType().getSimpleName()} key = <@d.mapByType "csvField" method.indexedReturnKeyType().getCanonicalName()/>;
		return Indexes.get().${method.indexedReturnType().getIdentifier().indexFinder()}(key).orElse(null);
			<#else>
		${method.getInterfaceType()} tmp = <@d.mapByType "csvField" method.getInterfaceTypeFQN()/>;
		return tmp;
			</#if>
		<#else>
			<#if method.returnTypeIsIndexed()>
		//Looking for an index entry using this as a key
		return Indexes.get().${method.inverseIndexFinder()}(this.${class.getIdentifier().getName().getter()}());
			<#else>
		//FIXME
		return null;
			</#if>
		</#if>
	}
	</#list> 
	
	// hashCode and equals
	// ===================
	<#if class.hasIdentifier()>
	<#assign id=class.getIdentifier().getName().getter()/>

	@Override
	public int hashCode() {
		return this.${id}().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		${classname} other = (${classname}) obj;
		if (other.${id}() == null)
			return false;
		return this.${id}().equals(other.${id}());
	}
	<#else>
	
	@Override
	public int hashCode() {
		return this.csvInput.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		${classname} other = (${classname}) obj;
		for (int i=0; i < csvInput.size(); i++) { 
			if (!this.csvInput.get(i).equals(other.csvInput.get(i))) return false;
		}
		return true;
	}
	</#if>
}
