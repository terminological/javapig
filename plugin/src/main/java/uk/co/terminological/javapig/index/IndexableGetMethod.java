package uk.co.terminological.javapig.index;

import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;

public class IndexableGetMethod extends JGetMethod {

	public IndexableGetMethod(JGetMethod copy) {
		super(copy);
	}
	
	public String keyType() {
		//TODO: I need to change this so that in the case where a the returnTypeIsIndexed()
		// we use the primary key of the retrieved object (i.e. the indexedReturnType()) instead of the
		// stated return type of this method
		/* if (this.returnTypeIsIndexed()) {
			return indexedReturnKeyType().getSimpleName();
		} else */
		// or maybe this logic should be in the template as sometimes we will have to derive this 
		// value
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
				((IndexableInterface) this.getModel().findClass(getUnderlyingType())) :
				((IndexableInterface) this.getModel().findClass(getReturnType()));
		if (iface == null) return null;		
		return iface.isIndexed() ? iface : null;
	}
	
	/**
	 * If a method is indexed it is also necessary to know the type of the index key to be able to 
	 * generate a lookup function
	 * @return
	 */
	public JClassName indexedReturnKeyType() {
		return indexedReturnType().getIdentifier().getReturnType();
	}
	
	/** 
	 * If a method returns a value which itself indexed
	 * @return
	 */
	public String inverseIndexFinder() {
		//FIXME this is a single case where we are referencing by primary index.
		// return ((IndexableGetMethod) indexedReturnType().getIdentifier()).indexFinder();
		return "find"+this.indexedReturnType().getName().getSimpleName()+"By"
			+((IndexableInterface) this.getDeclaringClass()).getIdentifier().getName().getClassName();
		//this.indexedReturnKeyType().getSimpleName();
	}
	
	public boolean returnTypeIsIndexed() {
		return indexedReturnType() != null;		
	}
	
}
