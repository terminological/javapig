package uk.co.terminological.javapig.javamodel.tools;

import java.lang.reflect.Method;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;

import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JMethodName;
import com.thoughtworks.qdox.model.JavaType;

import com.thoughtworks.qdox.model.JavaMethod;

public class JNameBuilder {

	
	public static JMethodName from(Method m) {
		if (m == null) return null;
		return JMethodName.from(getMethodFQN(m));
	}
	
	public static JMethodName from(JavaMethod m) {
		if (m == null) return null;
		return JMethodName.from(getMethodFQN(m));
	}
	
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
	
	public static JClassName from(Class<?> s) {
		if (null == s) return null;
		return JClassName.from(s.getCanonicalName());
	}
	
	public static JClassName from(JavaType s) {
		if (null == s) return null;
		return JClassName.from(s.getFullyQualifiedName());
	}
	
	public static JClassName from(TypeElement s) {
		if (null == s) return null;
		return JClassName.from(s.getQualifiedName().toString());
	}
	
	public static JClassName from(DeclaredType s) {
		if (null == s) return null;
		return from((TypeElement) s.asElement());
	}
}
