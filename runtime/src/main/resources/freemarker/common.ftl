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