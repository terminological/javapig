package uk.co.terminological.javapig.sqlloader;

import org.apache.maven.plugins.annotations.Parameter;

public class JavaFromQuery {

	public String getSql() {
		return sql;
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

	@Parameter(required=true)
	String sql;
	
	@Parameter(required=true)
	String targetFQN;
	
	// TODO: default values and explicit sqltypes for parameterised queries.
	// At the moment the parameter data is 
}
