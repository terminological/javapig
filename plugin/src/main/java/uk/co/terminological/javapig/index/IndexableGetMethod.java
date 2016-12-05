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
	
	/**
	 * If a method references an indexed item as a return value, the type is here.
	 * 
	 * The reference may be either as a one-to-one (which may be optional), or as part of a one-to-many List or Set
	 * If the inverse method is annotated we should also be able to tell a many-to-many relationship  
	 * @return the type that is indexed.
	 */
	public IndexableInterface indexedReturnType() {
		IndexableInterface iface = (this.isParameterised()) ?
				((IndexableInterface) this.getModel().findClass(getReturnType())) :
				((IndexableInterface) this.getModel().findClass(getUnderlyingType()));
		return iface.isIndexed() ? iface : null;
	}
	
	public boolean returnTypeIsIndexed() {
		return indexedReturnType() != null;		
	}
	
}
