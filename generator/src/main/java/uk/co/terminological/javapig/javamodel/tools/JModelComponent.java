package uk.co.terminological.javapig.javamodel.tools;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.javapig.javamodel.tools.JModelVisitor;

public abstract class JModelComponent implements Serializable {
	
	public <OUT extends Object> Stream<? extends OUT> accept(JModelVisitor<OUT> visitor) {
		return visitor.visit(this);
	}
	
	public String toString() {
		return accept(new JModelToString()).collect(Collectors.joining("\n"));
	}
}
