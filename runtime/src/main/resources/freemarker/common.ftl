<#macro hashCode class>
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
	<#list class.getMethods() as method>
		<#if method.getInterfaceType()=="float">
		result = prime * result + Float.floatToIntBits(${method.getName().getter()}());
		<#elseif method.getInterfaceType()=="double">
		result = prime * result + Double.doubleToIntBits(${method.getName().getter()}());
		<#elseif method.isPrimitive()>
		result = prime * result + ${method.getName().getter()}();
		<#elseif method.isArray()>
		result = prime * result + Arrays.hashCode(${method.getName().getter()}());
		<#else>
		result = prime * result + ((${method.getName().getter()}() == null) ? 0 : ${method.getName().getter()}().hashCode());
		</#if>
	</#list>
		return result;
	}
</#macro>

<#macro equals class classname>
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (this.getClass() != obj.getClass())
			return false;
		${classname} other = (${classname}) obj;
	<#list class.getMethods() as method>
		//testing this.${method.getName().getter()}()
		<#assign lhs>this.${method.getName().getter()}()</#assign>
		<#assign rhs>other.${method.getName().getter()}()</#assign>
		<#if method.isPrimitive()>
		if (${lhs} != ${rhs}) return false;
		<#else>
		if (${lhs} == null ^ ${rhs}==null) return false;
		if (${lhs} != null && ${rhs}!=null) {
			<#if method.isArray()>
			if (!Arrays.equals(${lhs},${rhs})) return false;
			<#elseif method.isCollection()>
			if (${lhs}.size() != ${rhs}.size()) return false;
				<#if method.isList()>
			{
				Iterator<${method.getUnderlyingType().getSimpleName()}> lhs = ${lhs}.iterator();
				Iterator<${method.getUnderlyingType().getSimpleName()}> rhs = ${rhs}.iterator();
				while (lhs.hasNext() && rhs.hasNext()) {
					if (!lhs.next().equals(rhs.next())) return false;
				}
			}
				<#else>
			{
				HashSet<${method.getUnderlyingType().getSimpleName()}> lhs = new HashSet<>(${lhs});
				Iterator<${method.getUnderlyingType().getSimpleName()}> rhs = ${rhs}.iterator();
				while (rhs.hasNext()) {
					if (!lhs.remove(rhs.next())) return false;
				}
				if (!lhs.isEmpty()) return false;
			}
				</#if>
			<#else>
			if (!${lhs}.equals(${rhs})) return false;
			</#if>
		}
		</#if>
	</#list>
		return true;
	}
</#macro>

<#-- 
The following functions copy annotations from source interface to target class
They are a work in progress. The problem is that firstly the imports don't get correctly
set up and secondly it is not clear under what circumstances you want to do this. It is not 
a globally useful feature if annotations are inherited.
-->

<#macro copyAnnotations classOrMethod indent>
<#list classOrMethod.getAnnotations() as annotation><@printAnnotation annotation indent/>
</#list>
</#macro>

<#macro printAnnotation annotation indent>
<#list 0..<indent as i>	</#list>@${annotation.getName()}<#if annotation.getValues()?size gt 0>(<#if annotation.getValues()?size gt 1>
<#list 0..<indent as i>	</#list></#if><#list annotation.getValues() as annotationEntry><@printAnnotationEntry annotationEntry indent+1/><#if !annotationEntry?is_last>,
<#list 0..<indent as i>	</#list></#if></#list>)</#if>
</#macro>

<#macro printAnnotationEntry annotationEntry indent>${annotationEntry.getMethod().getter()}=<#if annotationEntry.getValues()?size gt 1>{
<#list annotationEntry.getValues() as annotationValue>
<#list 0..<indent as i>	</#list><@printAnnotationValue annotationValue indent+1/><#if !annotationValue?is_last>,</#if>
</#list>}<#else><#if annotationEntry.getValues()?first??><@printAnnotationValue annotationEntry.getValues()?first 0/></#if></#if></#macro>

<#macro printAnnotationValue annotationValue indent><#if annotationValue.isPrimitive()>${annotationValue.getValue()}</#if><#if annotationValue.isClass()>${annotationValue.getValue().getCanonicalName()}.class</#if><#if annotationValue.isAnnotation()><@printAnnotation annotationValue.getValue() indent+1/></#if></#macro>

