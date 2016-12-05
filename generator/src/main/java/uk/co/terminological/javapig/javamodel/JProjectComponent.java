package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.javapig.javamodel.tools.JModelToString;
import uk.co.terminological.javapig.javamodel.tools.JModelVisitor;

public abstract class JProjectComponent implements Serializable {
	
	public <OUT extends Object> Stream<? extends OUT> accept(JModelVisitor<OUT> visitor) {
		return visitor.visit(this);
	}
	
	public String toString() {
		return accept(new JModelToString()).collect(Collectors.joining("\n"));
	}
	
	public abstract JProjectComponent clone();
	public abstract JProjectComponent copy();
}
