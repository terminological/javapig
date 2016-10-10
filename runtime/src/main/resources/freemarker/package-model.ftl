package ${packagename};

import uk.co.terminological.javapig.mirrorapi.MirrorClass;
import uk.co.terminological.javapig.mirrorapi.MirrorField;

import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JClassName;

import javax.annotation.Generated;
import java.util.*;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} {

	// Mirror factories
	// ================
	
	<#list model.getClasses() as class>
		<#assign sn>${class.getName().getSimpleName()}</#assign>
		<#assign fqn>${class.getName().getCanonicalName()}</#assign>
	public static ${sn} reflect(${fqn} object) {
		return ${sn}.reflect(object);
	}
	
	</#list>

	// Class mirrors
	// =============
	
	<#list model.getClasses() as class>
		<#assign sn>${class.getName().getSimpleName()}</#assign>
		<#assign fqn>${class.getName().getCanonicalName()}</#assign>
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static class ${sn} implements MirrorClass<${fqn}> {
		
		${fqn} mirror = null;
		
		public ${fqn} reflect() {
			return mirror;
		}
		
		public static ${sn} reflect(${fqn} proto) {
			${sn} out = new ${sn}();
			out.mirror = proto;
			return out; 
		} 
		
		public JClassName getName() {return JClassName.from("${fqn}");} 
		
		// Class and method references
		// ===========================
		
		public final static java.lang.String FQN = "${fqn}";
		<#list class.getMethods() as method>
		public final static java.lang.String ${method.getName().getter()} = "${fqn}#${method.getName().getter()}";
		</#list>
		
		// Method mirrors
		// ==============
		
		<#list class.getMethods() as method>
		<#assign cn>${method.getName().className()}</#assign>
		@Generated({"uk.co.terminological.javapig.JModelWriter"})
		public static class ${cn} implements MirrorField<${method.getInterfaceTypeFQN()}> {
			
			${fqn} mirror = null;
			
			public JMethodName getName() {return JMethodName.from("${fqn}#${method.getName().getter()}");}
			
			public static ${cn} reflect(${fqn} proto) {
				${cn} out = new ${cn}();
				out.mirror = proto;
				return out;
			}
			
			public ${method.getInterfaceTypeFQN()} get() {
				return mirror.${method.getName().getter()}();
			}
			
			public void set(${method.getInterfaceTypeFQN()} value) throws UnsupportedOperationException {
				try {
					((${fqn}Fluent) mirror).${method.getName().setter()}(value);
				} catch (ClassCastException e) {
					throw new UnsupportedOperationException("Cannot modify ${fqn} using ${method.getName().setter()}", e);
				} 
			}
		}  

		</#list> 
	}
	
	</#list>
}
