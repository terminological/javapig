<#import "datatypes.ftl" as d>
<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
import uk.co.terminological.javapig.StringCaster;
import ${rootPackage}.Indexes;
<#list class.getImports() as import>
import ${import};
</#list>
<#list class.getMethods() as method>
	<#if !method.isCollection() && method.isInModel()>
import ${method.getReturnType().getCanonicalName()}Csv;
	</#if>
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
			<#else>
		${method.getInterfaceType()} tmp = <@d.mapByType "csvField" method.getInterfaceTypeFQN()/>;
		return tmp;
			</#if>
		<#else>
		//FIXME
		return null;
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
		for (int i=0; i < csvInput.size; i++) { 
			if (!this.csvInput.get(i).equals(other.csvInput.get(i)) return false;
		}
		return true;
	}
	</#if>
}
