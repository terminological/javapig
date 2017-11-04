package uk.co.terminological.mimic;


import java.util.*;
import uk.co.terminological.javapig.csvloader.ByField;
import uk.co.terminological.javapig.csvloader.Csv;
import uk.co.terminological.javapig.csvloader.Type;

import uk.co.terminological.mimic.LabEvent;

@Csv(seperator=",", alwaysEnclosed=false, enclosedBy="\"", headerLines=1, lineTerminator="\r\n", escapedBy="\"", value=Type.CSV)
public interface LabEvent  {

	@ByField(value=0)
	public Integer getRowId();
	@ByField(value=1)
	public Integer getSubjectId();
	@ByField(value=2)
	public Integer getItemid();
	@ByField(value=3)
	public Date getCharttime();
	@ByField(value=4)
	public Float getValue();
	@ByField(value=5)
	public Float getValuenum();
	@ByField(value=6)
	public String getValueuom();
	
}
