package ${packagename};

import javax.annotation.Generated;
import java.util.stream.*;
import java.util.*;

<#list model.getClasses() as class>
import ${class.getName().getCanonicalName()};
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public abstract class ${classname}<OUT> {

	// Visiting
	// ========

<#list model.getClasses() as class>
	<#assign sn>${class.getName().getSimpleName()}</#assign>
	<#assign fqn>${class.getName().getCanonicalName()}</#assign>
	
	public abstract Stream<? extends OUT> visit${sn}(${sn} in<#list class.getMethods() as method><#if method.isCollection()>, 
			Stream<? extends OUT> ${method.getName().field()}</#if></#list>);
	
</#list>

	public abstract Stream<? extends OUT> visitUnknown(Object in);

	// Scanning
	// ========

	public Stream<? extends OUT> scan(Object in) {

<#list model.getClasses() as class>
	<#assign sn>${class.getName().getSimpleName()}</#assign>
	<#assign fqn>${class.getName().getCanonicalName()}</#assign>
		if (in instanceof ${sn}) {
			return visit${sn}((${sn}) in<#list class.getMethods() as method><#if method.isCollection()>, 
				((${sn}) in).${method.getName().getter()}().stream().flatMap(x -> this.scan(x))</#if></#list>);
		}
		
</#list>
		return visitUnknown(in);
	
	}

	// Stream utility methods
	// ======================

	public FluentStreamBuilder<OUT> start() {return new FluentStreamBuilder<OUT>();}

	@Generated({"uk.co.terminological.javapig.JModelWriter"})	
	public static class FluentStreamBuilder<X> {
		List<X> tmp = new ArrayList<>();
		
		public FluentStreamBuilder<X> with(@SuppressWarnings("unchecked") final X... items) {
			for (X item: items)
				tmp.add(item);
			return this;
		}
		
		public FluentStreamBuilder<X> with(Stream<X> items) {
			items.forEach(i -> tmp.add(i));
			return this;
		}
		
		public FluentStreamBuilder<X> with(Optional<X> item) {
			item.ifPresent(i -> tmp.add(i));
			return this;
		}
		
		public Stream<X> end() {return tmp.stream();}
	}
	
	// Acceptor interface
	// ==================
	
	@Generated({"uk.co.terminological.javapig.JModelWriter"})
	public static interface Acceptor {
		public default <X> Stream<? extends X> accept(Visitor<X> visitor) {
			return visitor.scan(this);
		} 
	}
}
