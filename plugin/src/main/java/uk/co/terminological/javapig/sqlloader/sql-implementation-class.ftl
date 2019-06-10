<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
<#list class.getImports() as import>
import ${import};
</#list>
import java.sql.ResultSet;
import java.sql.SQLException;

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} extends ${sn}FluentImpl implements ${sn} {

	<#-- // Fields
	// ======

	private Map<String,Object> sqlInput;
	private Integer row; -->

	// Public constructor
	// ==================

	public ${classname}(ResultSet resultSet) throws SQLException {
		<#-- sqlInput = new HashMap<>();-->
		super(
		<#list class.getColumns() as method>
		<#-- sqlInput.put("${method.getColumnName()}",resultSet.getObject("${method.getColumnName()}",${method.getInterfaceType()}.class)); -->
			resultSet.getObject("${method.getColumnName()}",${method.getInterfaceType()}.class)<#sep>,
			</#sep>
		</#list>
			<#if class.isQueryBound()>,
			resultSet.getRow()</#if>
		);
	}
	
	<#--// POJO Getters
	// ============

	<#list class.getColumns() as method>
	public ${method.getInterfaceType()} ${method.getName().getter()}() {
		return (${method.getInterfaceType()}) sqlInput.get("${method.getColumnName()}");
	}
	</#list> 
	
	public Integer getRowNumber() {
		return row;
	}
	
	// hashCode and equals
	// ===================
	
	@Override
	public int hashCode() {
		return this.sqlInput.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		${classname} other = (${classname}) obj;
		return this.sqlInput.equals(other.sqlInput);
	}
	
	@Override
	public String toString() {
		return "row: "+row+"="+sqlInput.toString();
	}-->
}
