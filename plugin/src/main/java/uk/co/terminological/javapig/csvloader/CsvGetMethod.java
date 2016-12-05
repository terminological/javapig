package uk.co.terminological.javapig.csvloader;

import uk.co.terminological.javapig.index.IndexableGetMethod;
import uk.co.terminological.javapig.javamodel.JGetMethod;

public class CsvGetMethod extends IndexableGetMethod {

	public CsvGetMethod(JGetMethod copy) {
		super(copy);
	}

	public boolean isField() {
		return this.isAnnotationPresent(ByField.class);
	}
	
	public int order() {
		return this.getAnnotation(ByField.class).value();
	}
	
}
