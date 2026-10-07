package uk.co.terminological.javapig.index;

import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;

/**
 * An extended {@link JGetMethod} that provides indexing metadata for the index plugin.
 *
 * <p>Methods that return indexed types (interfaces with {@code @Id}) have their
 * return types resolved to the target interface's identifier type for key lookup
 * code generation.</p>
 *
 * @see IndexablePlugin
 * @see IndexableInterface
 */
public class IndexableGetMethod extends JGetMethod {

	/**
	 * Constructs an indexable method by wrapping the given method.
	 *
	 * @param copy the method to wrap
	 */
	public IndexableGetMethod(JGetMethod copy) {
		super(copy);
	}
	
	/**
	 * Returns the key type for this method's index. For indexed return types,
	 * this resolves to the primary key type of the target indexed interface.
	 * For non-indexed types, it uses the implementation type.
	 *
	 * @return the key type as a simple class name
	 */
	public String keyType() {
		//TODO: I need to change this so that in the case where a the returnTypeIsIndexed()
		// we use the primary key of the retrieved object (i.e. the indexedReturnType()) instead of the
		// stated return type of this method
		if (this.returnTypeIsIndexed()) {
			return indexedReturnKeyType().getSimpleName();
		} else {
		// or maybe this logic should be in the template as sometimes we will have to derive this 
		// value
		if (this.isOptional()) {
			return this.getUnderlyingType().getSimpleName();
		} else {
			return this.getImplementationType();
		}
		}
	}
	
	/**
	 * Returns the value type for this method's index — the declaring interface's simple name.
	 *
	 * @return the value type
	 */
	public String valueType() {
		return this.getDeclaringClass().getName().getSimpleName();
	}

	/**
	 * Returns the field name for this method's index, formatted as
	 * {@code indexXxxByyyy} where Xxx is the declaring class and yyyy is the method name prefix.
	 *
	 * @return the index field name
	 */
	public String indexField() {
		return "index"+this.getDeclaringClass().getName().getSimpleName()+this.getName().prefix("By");
	}
	
	/**
	 * Returns the finder method name for this method's index, formatted as
	 * {@code findXxxByyyyy} where Xxx is the declaring class and yyyy is the method name prefix.
	 *
	 * @return the finder method name
	 */
	public String indexFinder() {
		return "find"+this.getDeclaringClass().getName().getSimpleName()+this.getName().prefix("By");
	}
	
	/**
	 * Returns the indexed return type if this method references an indexed interface,
	 * or {@code null} if the return type is not indexed.
	 *
	 * <p>Handles both one-to-one (Optional) and one-to-many (List/Set) relationships.
	 * For parameterized types, the underlying type is checked; otherwise the direct
	 * return type is resolved.</p>
	 *
	 * @return the indexed return type, or {@code null}
	 */
	public IndexableInterface indexedReturnType() {
		IndexableInterface iface = (this.isParameterised()) ?
				((IndexableInterface) this.getModel().findClass(getUnderlyingType())) :
				((IndexableInterface) this.getModel().findClass(getReturnType()));
		if (iface == null) return null;		
		return iface.isIndexed() ? iface : null;
	}
	
	/**
	 * Returns the identifier type of the indexed return type, used for generating
	 * lookup functions.
	 *
	 * @return the JClassName of the identifier field
	 */
	public JClassName indexedReturnKeyType() {
		return indexedReturnType().getIdentifier().getReturnType();
	}
	
	/**
	 * Returns the inverse index finder method name for this method's indexed return type.
	 *
	 * @return the inverse finder method name
	 */
	public String inverseIndexFinder() {
		return "find"+this.indexedReturnType().getName().getSimpleName()+((IndexableInterface) this.getDeclaringClass()).getIdentifier().getName().prefix("By");
	}
	
	/**
	 * Checks whether this method's return type is an indexed interface.
	 *
	 * @return {@code true} if the return type is indexed
	 */
	public boolean returnTypeIsIndexed() {
		return indexedReturnType() != null;		
	}
	
}
