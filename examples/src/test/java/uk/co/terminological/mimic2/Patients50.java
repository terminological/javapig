package uk.co.terminological.mimic2;


import java.util.*;
import uk.co.terminological.javapig.csvloader.ByField;
import uk.co.terminological.javapig.csvloader.Csv;
import uk.co.terminological.javapig.csvloader.Type;

import uk.co.terminological.mimic2.Patients50;

@Csv(seperator=",", alwaysEnclosed=false, enclosedBy="\"", headerLines=1, lineTerminator="\r\n", escapedBy="\"", value=Type.CSV)
public interface Patients50  {

	@ByField(value=0)
	public Integer getRowId();
	@ByField(value=1)
	public Integer getSubjectId();
	@ByField(value=2)
	public Boolean getGender();
	@ByField(value=3)
	public Date getDob();
	@ByField(value=4)
	public Integer getExpireFlag();
	
}
