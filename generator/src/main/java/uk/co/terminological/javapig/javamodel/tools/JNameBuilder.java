package uk.co.terminological.javapig.javamodel.tools;

import java.lang.reflect.Method;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JMethodName;
import com.thoughtworks.qdox.model.JavaType;

import com.thoughtworks.qdox.model.JavaMethod;

/**
 * Converts type names and method references from various source systems into
 * javapig's internal {@link JClassName} and {@link JMethodName} representations.
 *
 * <p>This factory class bridges three type systems:</p>
 * <ul>
 *   <li>Java reflection ({@link Method}, {@link Class})</li>
 *   <li>QDox ({@link JavaMethod}, {@link JavaType})</li>
 *   <li>Java APT ({@link ExecutableElement}, {@link TypeElement}, {@link DeclaredType})</li>
 * </ul>
 *
 * <p>Method names are formatted as {@code "fully.qualified.DeclaringClass#methodName"}
 * to uniquely identify a method across the entire class hierarchy.</p>
 *
 * @see JClassName
 * @see JMethodName
 * @see JModelCopier
 * @see JModelToString
 * @see JModelVisitor
 */
public class JNameBuilder {

	/**
	 * Creates a JMethodName from a Java reflection {@link Method}.
	 *
	 * @param m the reflection method, or {@code null}
	 * @return a JMethodName with FQN format {@code declaringClass#methodName}
	 */
	public static JMethodName from(Method m) {
		if (m == null) return null;
		return JMethodName.from(getMethodFQN(m));
	}
	
	/**
	 * Creates a JMethodName from a QDox {@link JavaMethod}.
	 *
	 * @param m the QDox method, or {@code null}
	 * @return a JMethodName with FQN format {@code declaringClass#methodName}
	 */
	public static JMethodName from(JavaMethod m) {
		if (m == null) return null;
		return JMethodName.from(getMethodFQN(m));
	}
	
	/**
	 * Creates a JMethodName from a Java APT {@link ExecutableElement}.
	 *
	 * @param m the APT executable element, or {@code null}
	 * @return a JMethodName with FQN format {@code declaringClass#methodName}
	 */
	public static JMethodName from(ExecutableElement m) {
		if (m == null) return null;
		return JMethodName.from(getMethodFQN(m));
	}
	
	private static String getMethodFQN(Method m) {
		return m.getDeclaringClass().getCanonicalName()+"#"+m.getName();
	}
	
	private static String getMethodFQN(JavaMethod m) {
		return m.getDeclaringClass().getCanonicalName()+"#"+m.getName();
	}
	
	private static String getMethodFQN(ExecutableElement m) {
		return ((TypeElement) m.getEnclosingElement()).getQualifiedName().toString()+"#"+m.getSimpleName();
	}
	
	/**
	 * Creates a JClassName from a Java {@link Class}.
	 *
	 * @param s the class, or {@code null}
	 * @return a JClassName
	 */
	public static JClassName from(Class<?> s) {
		if (null == s) return null;
		return JClassName.from(s.getCanonicalName());
	}
	
	/**
	 * Creates a JClassName from a QDox {@link JavaType}.
	 *
	 * @param s the QDox type, or {@code null}
	 * @return a JClassName
	 */
	public static JClassName from(JavaType s) {
		if (null == s) return null;
		return JClassName.from(s.getFullyQualifiedName());
	}
	
	/**
	 * Creates a JClassName from a Java APT {@link TypeElement}.
	 *
	 * @param s the APT type element, or {@code null}
	 * @return a JClassName
	 */
	public static JClassName from(TypeElement s) {
		if (null == s) return null;
		return JClassName.from(s.getQualifiedName().toString());
	}
	
	/**
	 * Creates a JClassName from a Java APT {@link DeclaredType} by extracting
	 * its enclosing {@link TypeElement}.
	 *
	 * @param s the APT declared type, or {@code null}
	 * @return a JClassName
	 */
	public static JClassName from(DeclaredType s) {
		if (null == s) return null;
		return from((TypeElement) s.asElement());
	}
}
