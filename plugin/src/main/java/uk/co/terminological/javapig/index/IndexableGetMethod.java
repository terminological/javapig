package uk.co.terminological.javapig.index;

import uk.co.terminological.javapig.javamodel.JGetMethod;

public class IndexableGetMethod extends JGetMethod {

	public IndexableGetMethod(JGetMethod copy) {
		super(copy);
	}
	
	public String keyType() {
		if (this.isOptional()) {
			return this.getUnderlyingType().getSimpleName();
		} else {
			return this.getImplementationType();
		}
	}
	
	public String valueType() {
		return this.getDeclaringClass().getName().getSimpleName();
	}

	public String indexField() {
		return "index"+this.getDeclaringClass().getName().getSimpleName()+this.getName().prefix("By");
	}
	
	public String indexFinder() {
		return "find"+this.getDeclaringClass().getName().getSimpleName()+this.getName().prefix("By");
	}
}
