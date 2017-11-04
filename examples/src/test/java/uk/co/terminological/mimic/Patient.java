package uk.co.terminological.mimic;


import java.util.*;
import uk.co.terminological.javapig.csvloader.ByField;
import uk.co.terminological.javapig.csvloader.Csv;
import uk.co.terminological.javapig.csvloader.Type;

import uk.co.terminological.mimic.Patient;

@Csv(seperator=",", alwaysEnclosed=false, enclosedBy="\"", headerLines=1, lineTerminator="\r\n", escapedBy="\"", value=Type.CSV)
public interface Patient  {

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
