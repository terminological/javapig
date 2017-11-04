<#import "common.ftl" as c>
<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

// import javax.annotation.Generated;
<#list class.getImportsIncludingAnnotations() as import>
import ${import};
</#list>
<#list class.getSupertypes() as supertypeName>
import ${superTypeName.getCanonicalName};
</#list>

import ${fqn};

// @Generated({"uk.co.terminological.javapig.JModelWriter"})
<@c.copyAnnotations class 0/>
public interface ${class.getName().getSimpleName()}<#list class.getSupertypes() as supertypeName><#if supertypeName?is_first> extends <#else>, </#if>${supertypeName.getSimpleName()}</#list>  {

	<#list class.getMethods() as method>
	<@c.copyAnnotations method 1/>
	public ${method.getInterfaceType()} ${method.getName().getter()}();
	</#list>
	
}
