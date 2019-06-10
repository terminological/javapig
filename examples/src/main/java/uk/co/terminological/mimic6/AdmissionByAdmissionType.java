package uk.co.terminological.mimic6;

// import javax.annotation.Generated;
import java.sql.JDBCType;
import java.sql.Timestamp;
import javax.persistence.Id;
import uk.co.terminological.javapig.sqlloader.Column;
import uk.co.terminological.javapig.sqlloader.Query;

import uk.co.terminological.mimic6.AdmissionByAdmissionType;

// @Generated({"uk.co.terminological.javapig.JModelWriter"})
@Query(sql="SELECT * FROM ADMISSIONS a WHERE a.ADMISSION_TYPE=?;", parameterTypes={java.lang.Object.class})
public interface AdmissionByAdmissionType  {

	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.SMALLINT, name="ROW_ID", length=5)
	public Integer getRowId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="SUBJECT_ID", length=8)
	public Integer getSubjectId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.INTEGER, name="HADM_ID", length=8)
	public Integer getHadmId();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="ADMITTIME", length=19)
	public Timestamp getAdmittime();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="DISCHTIME", length=19)
	public Timestamp getDischtime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="DEATHTIME", length=19)
	public Timestamp getDeathtime();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="ADMISSION_TYPE", length=255)
	public String getAdmissionType();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="ADMISSION_LOCATION", length=255)
	public String getAdmissionLocation();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="DISCHARGE_LOCATION", length=255)
	public String getDischargeLocation();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="INSURANCE", length=255)
	public String getInsurance();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="LANGUAGE", length=255)
	public String getLanguage();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="RELIGION", length=255)
	public String getReligion();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="MARITAL_STATUS", length=255)
	public String getMaritalStatus();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.VARCHAR, name="ETHNICITY", length=255)
	public String getEthnicity();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="EDREGTIME", length=19)
	public Timestamp getEdregtime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.TIMESTAMP, name="EDOUTTIME", length=19)
	public Timestamp getEdouttime();
	@Column(isNullable=true, isAutoIncrement=false, jdbcType=JDBCType.LONGVARCHAR, name="DIAGNOSIS", length=21845)
	public String getDiagnosis();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TINYINT, name="HOSPITAL_EXPIRE_FLAG", length=3)
	public Integer getHospitalExpireFlag();
	@Column(isNullable=false, isAutoIncrement=false, jdbcType=JDBCType.TINYINT, name="HAS_CHARTEVENTS_DATA", length=3)
	public Integer getHasCharteventsData();
	@Id
	public Integer getRowNumber();
	
}
