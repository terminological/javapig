package uk.co.terminological.fromsql;

// import javax.annotation.Generated;
import javax.persistence.Id;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Query;

import uk.co.terminological.fromsql.TestTable;

// @Generated({"uk.co.terminological.javapig.JModelWriter"})
@Query(sql="Select * from testTable")
public interface TestTable  {

	@Column(sql="col1")
	public Integer getCol1();
	@Column(sql="col2")
	public String getCol2();
	@Column(sql="col3")
	public String getCol3();
	@Id
	public String getId();
	
}
