package uk.co.terminological.javapig.sqlloader;

import org.apache.maven.plugins.annotations.Parameter;

public class JavaFromTable {

	public String getName() {
		return name;
	}

	public String getTargetFQN() {
		return targetFQN;
	}

	@Parameter(required=true)
	String name;
	
	@Parameter(required=true)
	String targetFQN;

	protected void setName(String name) {
		this.name = name;
	}

	protected void setTargetFQN(String targetFQN) {
		this.targetFQN = targetFQN;
	}
	
}
