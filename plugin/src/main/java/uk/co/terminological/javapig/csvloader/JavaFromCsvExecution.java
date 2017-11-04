package uk.co.terminological.javapig.csvloader;

import java.io.File;

import org.apache.maven.plugins.annotations.Parameter;

public class JavaFromCsvExecution {

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getSeperator() {
		return seperator;
	}

	public void setSeperator(String seperator) {
		this.seperator = seperator;
	}

	@Parameter(required=true)
	File file;
	
	@Parameter(required=true)
	Type type;
	
	@Parameter(required=true)
	String targetFQN;
	
	
	
	
	public String getTargetFQN() {
		return targetFQN;
	}

	public void setTargetFQN(String targetFQN) {
		this.targetFQN = targetFQN;
	}

	@Parameter
	String seperator;
	
	
	
	
}
