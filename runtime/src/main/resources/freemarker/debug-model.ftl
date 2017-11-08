<#setting boolean_format="TRUE,FALSE"/>
fqn: ${fqn}
classname: ${classname}
packagename: ${packagename}
rootPackage: ${rootPackage}
model:
	<#list model.getPackages() as package>
	.getPackages(): [${package.getName()}]
		.getImports():
		<#list package.getImports() as import>
			${import}
		</#list>
		.isEnabled("VISITOR"): ${package.isEnabled("VISITOR")}
		.getName(): ${package.getName()}
<@printElement package 2/>
		.getMetadata(): 
			.getBuiltIns():
		<#list package.getMetadata().getBuiltIns() as builtin>
				.toString(): ${builtin.toString()}
		</#list>
			.getPlugIns():
		<#list package.getMetadata().getPlugIns() as plugin>
				.toString(): ${plugin}
		</#list>
			.isEnabled("FACTORY"): ${package.getMetadata().isEnabled("FACTORY")}
		<#list package.getClasses() as class>
		.getClasses() [${class.getName().getCanonicalName()}]:
			.getName():
<@printClassName class.getName()!"none" 4/>
<@printElement class 3/>
			.getPkg().getName(): ${class.getPkg().getName()}
			.getImports():
			<#list class.getImports() as import>
				${import}
			</#list>
			.isTypeOf("java.lang.String"): ${class.isTypeOf("java.lang.String")}
			.getSupertypes():
			<#list class.getSupertypes() as supertypeName>
<@printClassName supertype!"none" 4/>
			</#list>
			<#list class.getExtendedMethods() as method>
			.getExtendedMethods() / .getMethods() [${class.getName().getCanonicalName()}#${method.getName().getter()}]:
				.getName():
<@printMethodName method.getName()!"none" 5/>
				.getReturnTypeDefinition(): ${method.getReturnTypeDefinition()}
<@printElement method 4/>
				.getReturnType():
<@printClassName method.getReturnType()!"none" 5/>
				.getUnderlyingType():
<@printClassName method.getUnderlyingType()!"none" 5/>
				.getInterfaceType(): ${method.getInterfaceType()}
				.getInterfaceTypeFQN(): ${method.getInterfaceTypeFQN()} 
				.getImplementationType(): ${method.getImplementationType()}
				.getDeclaringClass().getName().getSimpleName(): ${method.getDeclaringClass().getName().getSimpleName()} 
				.isDefault(): ${method.isDefault()}
				.isEnum(): ${method.isEnum()}
				.isPrimitive(): ${method.isPrimitive()}
				.isArray(): ${method.isArray()}
				.isParameterised(): ${method.isParameterised()}
				.isCollection(): ${method.isCollection()}
				.isList(): ${method.isList()}
				.isSet(): ${method.isSet()}
				.isOptional(): ${method.isParameterised()}
				.isTypeOf("java.lang.Cloneable"): ${method.isTypeOf("java.lang.Cloneable")}
				.isInModel(): ${method.isInModel()}
				.hasInverseMethod(): ${method.hasInverseMethod()}
<#if method.hasInverseMethod()>
				.getInverseMethod().getName():
<@printMethodName method.getInverseMethod().getName() 5/>
</#if>
			</#list>
		</#list>
	</#list>
	.getClasses():
		as above
	.getMethods():
		as above

<#macro printElement element indent>
<#list 1..indent as i>    </#list>.getJavaDoc(): ${element.getJavaDoc()!"none"}
<#list 1..indent as i>    </#list>.isAnnotationPresent("@Model"): ${element.isAnnotationPresent("@Model")}
<#list element.getAnnotations() as annotation>
<#list 1..indent as i>    </#list>.getAnnotations() [${annotation.getName()}]
<@printAnnotation annotation indent+1/>
</#list>
</#macro>

<#macro printAnnotation annotation indent>
<#list 1..indent as i>    </#list>.getName(): ${annotation.getName()}
<#list 1..indent as i>    </#list>.getImports():
<#list annotation.getImports() as import>
<#list 1..indent as i>    </#list>    ${import}
</#list>
<#list 1..indent as i>    </#list>.getCanonicalName(): ${annotation.getCanonicalName()}
<#list annotation.getEntries() as annotationEntry>
<@printAnnotationEntry annotationEntry indent+1/>
</#list>
<#-- <#list 1..indent as i>    </#list>.toSourceCode(): ${annotation.toSourceCode()}-->
</#macro>

<#macro printAnnotationEntry annotationEntry indent>
<#list 1..indent as i>    </#list>.getMethod().getter(): ${annotationEntry.getMethod().getter()}
<#list 1..indent as i>    </#list>.getValues():
<#list annotationEntry.getValues() as annotationValue>
<@printAnnotationValue annotationValue indent+1/>
</#list>
</#macro>

<#macro printAnnotationValue annotationValue indent>
<#list 1..indent as i>    </#list>.getValue(): <#if annotationValue.isPrimitive()>${annotationValue.getValue()}</#if>
<#if annotationValue.isClass()><@printClassName annotationValue.getValue() indent+1/></#if>
<#if annotationValue.isAnnotation()><@printAnnotation annotationValue.getValue() indent+1/></#if>
</#macro>


<#macro printClassName className indent>
<#if className!="none">
<#list 1..indent as i>    </#list>.getSimpleName(): ${className.getSimpleName()}
<#list 1..indent as i>    </#list>.getCanonicalName(): ${className.getCanonicalName()}
<#list 1..indent as i>    </#list>.getPackageName(): ${className.getPackageName()}
<#list 1..indent as i>    </#list>.importName(): ${className.importName()}
<#list 1..indent as i>    </#list>.isArray(): ${className.isArray()}
<#list 1..indent as i>    </#list>.isCompiled(): ${className.isCompiled()}
<#list 1..indent as i>    </#list>.code(): ${className.code()}
<#else>
<#list 1..indent as i>    </#list>none
</#if>
</#macro>

<#macro printMethodName methodName indent>
<#if methodName!="none">
<#list 1..indent as i>    </#list>.field(): ${methodName.field()}
<#list 1..indent as i>    </#list>.getter(): ${methodName.getter()}
<#list 1..indent as i>    </#list>.setter(): ${methodName.setter()}
<#list 1..indent as i>    </#list>.adder(): ${methodName.adder()}
<#list 1..indent as i>    </#list>.addAll(): ${methodName.addAll()}
<#list 1..indent as i>    </#list>.className(): ${methodName.getClassName()}
<#list 1..indent as i>    </#list>.code(): ${methodName.code()}
<#list 1..indent as i>    </#list>.constant(): ${methodName.constant()}
<#list 1..indent as i>    </#list>.methodBase(): ${methodName.methodBase()}
<#list 1..indent as i>    </#list>.prefix("prefixed"): ${methodName.prefix("prefixed")}
<#list 1..indent as i>    </#list>.with(): ${methodName.with()}
<#else>
<#list 1..indent as i>    </#list>none
</#if>
</#macro>

