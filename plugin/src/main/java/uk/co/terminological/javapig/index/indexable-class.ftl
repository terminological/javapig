<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
import ${fqn}Impl;
<#list class.getImports() as import>
import ${import};
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} extends ${sn}Impl {

	// Public constructor
	// ==================

	public ${classname}(
	<#list class.getMethods() as method>
		${method.getInterfaceType()} ${method.getName().field()}<#if method?has_next>,</#if>
	</#list>	
	) { 
		super( 
	<#list class.getMethods() as method>
			${method.getName().field()}<#if method?has_next>,</#if>
	</#list>
		);
	}
	
	public ${classname}(${sn} clone) {
		super(clone);
	}
	
	public ${classname} clone() {
		return new ${classname}(this); 
	}

	// HashCode and Equals
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
	// using default implementation as no identifier defined for this class
	</#if>
}
