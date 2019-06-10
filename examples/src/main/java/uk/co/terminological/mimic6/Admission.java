package uk.co.terminological.mimic6;

// import javax.annotation.Generated;
import java.sql.JDBCType;
import java.sql.Timestamp;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Table;

import uk.co.terminological.mimic6.Admission;

// @Generated({"uk.co.terminological.javapig.JModelWriter"})
@Table(name="ADMISSIONS", schema="")
public interface Admission  {

	@Column(name="ROW_ID", length=5, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.SMALLINT)
	public Integer getRowId();
	@Column(name="SUBJECT_ID", length=8, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER)
	public Integer getSubjectId();
	@Column(name="HADM_ID", length=8, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER)
	public Integer getHadmId();
	@Column(name="ADMITTIME", length=19, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP)
	public Timestamp getAdmittime();
	@Column(name="DISCHTIME", length=19, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP)
	public Timestamp getDischtime();
	@Column(name="DEATHTIME", length=19, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP)
	public Timestamp getDeathtime();
	@Column(name="ADMISSION_TYPE", length=255, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getAdmissionType();
	@Column(name="ADMISSION_LOCATION", length=255, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getAdmissionLocation();
	@Column(name="DISCHARGE_LOCATION", length=255, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getDischargeLocation();
	@Column(name="INSURANCE", length=255, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getInsurance();
	@Column(name="LANGUAGE", length=255, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getLanguage();
	@Column(name="RELIGION", length=255, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getReligion();
	@Column(name="MARITAL_STATUS", length=255, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getMaritalStatus();
	@Column(name="ETHNICITY", length=255, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR)
	public String getEthnicity();
	@Column(name="EDREGTIME", length=19, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP)
	public Timestamp getEdregtime();
	@Column(name="EDOUTTIME", length=19, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP)
	public Timestamp getEdouttime();
	@Column(name="DIAGNOSIS", length=21845, isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.LONGVARCHAR)
	public String getDiagnosis();
	@Column(name="HOSPITAL_EXPIRE_FLAG", length=3, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TINYINT)
	public Integer getHospitalExpireFlag();
	@Column(name="HAS_CHARTEVENTS_DATA", length=3, isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TINYINT)
	public Integer getHasCharteventsData();
	
}
