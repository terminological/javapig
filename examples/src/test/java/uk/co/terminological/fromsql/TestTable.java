package uk.co.terminological.fromsql;

// import javax.annotation.Generated;
import javax.persistence.Id;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Sql;

import uk.co.terminological.fromsql.TestTable;

// @Generated({"uk.co.terminological.javapig.JModelWriter"})
@Sql(value="Select * from testTable")
public interface TestTable  {

	@Column(value="col1")
	public Integer getCol1();
	@Column(value="col2")
	public String getCol2();
	@Column(value="col3")
	public String getCol3();
	@Id
	public String getId();
	
}
