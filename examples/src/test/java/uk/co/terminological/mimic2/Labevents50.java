package uk.co.terminological.mimic2;


import java.util.*;
import uk.co.terminological.javapig.csvloader.ByField;
import uk.co.terminological.javapig.csvloader.Csv;
import uk.co.terminological.javapig.csvloader.Type;

import uk.co.terminological.mimic2.Labevents50;

@Csv(seperator=",", alwaysEnclosed=false, enclosedBy="\"", headerLines=1, lineTerminator="\r\n", escapedBy="\"", value=Type.CSV)
public interface Labevents50  {

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
