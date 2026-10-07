package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import uk.co.terminological.javapig.javamodel.tools.JModelToString;
import uk.co.terminological.javapig.javamodel.tools.JModelVisitor;

/**
 * Abstract base for all javapig metamodel objects.
 *
 * <p>Subclasses implement the visitor pattern via {@link #accept(JModelVisitor)}
 * for traversing the model tree, and provide {@link #clone()} for deep copying
 * and {@link #copy()} for shallow copying. All components are {@link Serializable}
 * to support persistent model storage.</p>
 *
 * <p>The default {@link #toString()} delegates to {@link JModelToString}, producing
 * a human-readable string representation of the component and its children.</p>
 *
 * @see JModelVisitor
 * @see JModelToString
 * @see JProject
 * @see JElement
 */
public abstract class JProjectComponent implements Serializable {
	
	/**
	 * Accepts a visitor to traverse this component and return a typed result.
	 *
	 * @param visitor the visitor to accept
	 * @return a stream of results produced by the visitor
	 * @see JModelVisitor
	 */
	public <OUT extends Object> Stream<? extends OUT> accept(JModelVisitor<OUT> visitor) {
		return visitor.visit(this);
	}
	
	/**
	 * Returns a string representation of this component and its children,
	 * obtained by visiting with {@link JModelToString}.
	 *
	 * @return a human-readable multi-line string
	 * @see JModelToString
	 */
	public String toString() {
		return accept(new JModelToString()).collect(Collectors.joining("\n"));
	}
	
	/**
	 * Creates a deep clone of this component, copying all contained data.
	 *
	 * @return a fully independent copy
	 */
	public abstract JProjectComponent clone();
	
	/**
	 * Creates a shallow copy of this component that shares the same underlying
	 * model reference but is otherwise independent.
	 *
	 * @return a shallow copy
	 */
	public abstract JProjectComponent copy();
}
