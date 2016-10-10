<#ftl output_format="XML">
digraph model {
rankdir="LR"
splines="spline"
layout=fdp
K=0.75
KEY [label=<<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="1">
<#list model.getPackages() as package>
<TR><TD><B>${package.getName()}</B></TD></TR>
</#list>
</TABLE>>, shape=box, style="rounded"]
<#list model.getPackages() as package>
subgraph ${package.code()} {
node [style=filled];
label = "${package.getName()}";
	<#list package.getClasses() as class>
${class.getName().code()} [label=<<TABLE BORDER="0" CELLBORDER="0" CELLSPACING="0" CELLPADDING="1">
<TR><TD COLSPAN="2"><I>${class.getName().getPackageName()}</I></TD></TR>
<TR><TD COLSPAN="2"><B>${class.getName().getSimpleName()}<#list class.getSupertypes() as super><#if super?is_first> extends </#if>${super.getSimpleName()}<#if super?has_next>,</#if></#list></B></TD></TR>
		<#list class.getMethods() as method>
<TR><TD ALIGN="RIGHT">${method.getName().getter()}: </TD>
<TD ALIGN="LEFT"><I>${method.getInterfaceType()} (<#if method.isOptional()>0..1<#elseif method.isCollection()>0..*<#else>1..1</#if>)</I></TD></TR>
		</#list>
</TABLE>>, shape=box, style="rounded"];
	</#list>
}
</#list>
<#list model.getClasses() as class>
	<#list class.getSupertypes() as super>
${class.getName().code()} -> ${super.code()} [arrowtail=onormal, color=grey, len=1]
	</#list>
</#list>
<#list model.getMethods() as method>
	<#if method.isInModel()>
${method.getName().code()} [label=${method.getName().getter()}, shape=plaintext, fontsize=16]
${method.getDeclaringClass().getName().code()} -> ${method.getName().code()} [arrowhead=none, color=blue, len=0]
		<#if method.isParameterised()>
${method.getName().code()} -> ${method.getUnderlyingType().code()} [arrowhead=normal, color=blue, len=2]
      		<#else>
${method.getName().code()} -> ${method.getReturnType().code()} [arrowhead=normal, color=blue, len=2]
   		</#if>    
	</#if>
</#list>

}
