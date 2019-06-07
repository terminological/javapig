<#assign sn>${class.getName().getSimpleName()}</#assign>
<#assign fqn>${class.getName().getCanonicalName()}</#assign>
package ${packagename};

import javax.annotation.Generated;
import ${rootPackage}.Indexes;
<#list class.getImports("java.util.Map","java.util.HashMap") as import>
import ${import};
import java.sql.ResultSet;
import java.sql.SQLException;
</#list>

// TODO: this does not use Optionals (which it could) for nullable values
// this would need a change in the JavaFromSqlMojo also

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} implements ${sn} {

	// Fields
	// ======

	private Map<String,Object> sqlInput;

	// Public constructor
	// ==================

	public ${classname}(ResultSet resultSet) throws SQLException {
		sqlInput = new HashMap<>();
		<#list class.getColumns() as method>
		sqlInput.put("${method.getColumnName()}",resultSet.getObject(${method.getColumnName()},${method.getInterfaceType()}.class));
		</#list>
	}
	
	// POJO Getters
	// ============

	<#list class.getColumns() as method>
	public ${method.getInterfaceType()} ${method.getName().getter()}() {
		return (${method.getInterfaceType()}) sqlInput.get("${method.getColumnName()}");
	}
	</#list> 
	
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
	
}
