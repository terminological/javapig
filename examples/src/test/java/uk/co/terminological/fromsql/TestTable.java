package uk.co.terminological.fromsql;

// import javax.annotation.Generated;
import java.sql.JDBCType;
import javax.persistence.Id;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Query;

import uk.co.terminological.fromsql.TestTable;

// @Generated({"uk.co.terminological.javapig.JModelWriter"})
@Query(sql="Select * from testTable", parameterTypes={})
public interface TestTable  {

	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.NULL, name="col1", length=0)
	public Object getCol1();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.NULL, name="col2", length=0)
	public Object getCol2();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.NULL, name="col3", length=0)
	public Object getCol3();
	@Id
	public String getRowNumber();
	
}
