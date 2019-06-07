package uk.co.terminological.javapig.sqlloader;

import uk.co.terminological.javapig.javamodel.JGetMethod;

public class SqlGetMethod extends JGetMethod {

	public SqlGetMethod(JGetMethod copy) {
		super(copy);
	}

	public String getColumnName() {
		return this.getAnnotation(Column.class).name();
	}
	
	
}
