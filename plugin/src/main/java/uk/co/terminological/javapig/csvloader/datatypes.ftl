<#-- type is as defined by method.getInterfaceTypeFQN() -->
<#-- this enforces an empty means null for the string -->
<#macro mapByType s type><@compress single_line=true>
	<#if type=="java.lang.String">(${s} == null || ${s}.isEmpty()) ? null : ${s}
	<#elseif type=="java.lang.Boolean" || type=="boolean">${s} != null && (${s}.equalsIgnoreCase("true") || ${s}.equals("1") || ${s}.equalsIgnoreCare("yes"))
	<#elseif type=="java.lang.Short">(${s} == null || ${s}.isEmpty()) ? null : Short.parseShort(${s})
	<#elseif type=="java.lang.Long">(${s} == null || ${s}.isEmpty()) ? null : Long.parseLong(${s})
	<#elseif type=="java.lang.Double">(${s} == null || ${s}.isEmpty()) ? null : Double.parseDouble(${s})
	<#elseif type=="java.lang.Float">(${s} == null || ${s}.isEmpty()) ? null : Float.parseFloat(${s})
	<#elseif type=="java.lang.Integer">(${s} == null || ${s}.isEmpty()) ? null : Integer.parseInt(${s})
	<#elseif type=="java.lang.Character">(${s} == null || ${s}.isEmpty()) ? null : ${s}.charAt(0)
	<#elseif type=="java.lang.Byte">(${s} == null || ${s}.isEmpty()) ? null : ${s}.charAt(0)
	<#elseif type=="short">(${s} == null || ${s}.isEmpty()) ? 0 : Short.parseShort(${s})
	<#elseif type=="long">(${s} == null || ${s}.isEmpty()) ? 0L : Long.parseLong(${s})
	<#elseif type=="double">(${s} == null || ${s}.isEmpty()) ? 0D : Double.parseDouble(${s})
	<#elseif type=="float">(${s} == null || ${s}.isEmpty()) ? 0F : Float.parseFloat(${s})
	<#elseif type=="int">(${s} == null || ${s}.isEmpty()) ? 0 : Integer.parseInt(${s})
	<#elseif type=="byte">(${s} == null || ${s}.isEmpty()) ? 0 : ${s}.charAt(0)
	<#elseif type=="java.util.UUID">(${s} == null || ${s}.isEmpty()) ? null : UUID.fromString(${s})
	<#elseif type=="java.util.URI">(${s} == null || ${s}.isEmpty()) ? null : URI.create(${s})
	<#else>uk.co.terminological.javapig.StringCaster.get(${s}).cast(${type}.class)
	</#if>
</@compress></#macro>

