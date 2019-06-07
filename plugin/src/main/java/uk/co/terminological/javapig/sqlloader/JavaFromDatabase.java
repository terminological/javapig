package uk.co.terminological.javapig.sqlloader;

import org.apache.maven.plugins.annotations.Parameter;

public class JavaFromDatabase {

	public String getName() {
		return name;
	}

	public String getTargetPackage() {
		return targetPackage;
	}
	
	@Parameter(required=true)
	String name;
	
	@Parameter(required=true)
	String targetPackage;

	protected void setName(String name) {
		this.name = name;
	}

	protected void setTargetPackage(String targetPackage) {
		this.targetPackage = targetPackage;
	}

}
