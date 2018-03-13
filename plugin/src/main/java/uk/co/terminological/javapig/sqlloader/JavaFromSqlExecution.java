package uk.co.terminological.javapig.sqlloader;

import org.apache.maven.plugins.annotations.Parameter;

public class JavaFromSqlExecution {

	@Parameter(required=true)
	String sql;

	@Parameter(required=true)
	String targetFQN;
	
	public String getSql() {
		return sql.contains(";") ? sql.substring(0, sql.indexOf(";")) : sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public String getTargetFQN() {
		return targetFQN;
	}

	public void setTargetFQN(String targetFQN) {
		this.targetFQN = targetFQN;
	}

	
}
