<#macro getKey method><#-- Assumes a parameter 'value' exists and is source for our key -->
			Optional<${method.keyType()}> key;
	<#if method.isOptional()>
		<#if method.returnTypeIsIndexed()>
			key = value.${method.getName().getter()}().map(o ->
				o.${method.indexedReturnType().getIdentifier().getName().getter()}());
		<#else>
			key = value.${method.getName().getter()}();
		</#if>
	<#else>
		<#if method.returnTypeIsIndexed()>
			key = Optional.ofNullable(value.${method.getName().getter()}()).map(o ->
				o.${method.indexedReturnType().getIdentifier().getName().getter()}());
		<#else>
			key = Optional.ofNullable(value.${method.getName().getter()}());
		</#if>
	</#if>
</#macro>

package ${rootPackage};

import java.io.Serializable;
import javax.annotation.Generated;
import java.util.*;

<#list model.getIndexedClasses() as class>
import ${class.getName().getCanonicalName()};
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public class ${classname} implements Serializable {

	private static ${classname} instance;
	
	public static ${classname} get() {
		if (instance == null) instance = new ${classname}();
		return instance;
	}

	//Type indexes
<#list model.getIndexedClasses() as class>
	private Set<${class.getName().getSimpleName()}> indexed${class.getName().getSimpleName()} = new HashSet<>();
</#list>

	//Primary indexes
<#list model.getPrimaryIndexes() as method>
	<#if method.returnTypeIsIndexed()>
	private Map<${method.indexedReturnKeyType().getSimpleName()},${method.valueType()}> ${method.indexField()} = new HashMap<${method.indexedReturnKeyType()},${method.valueType()}>();
	<#else>
	private Map<${method.keyType()},${method.valueType()}> ${method.indexField()} = new HashMap<${method.keyType()},${method.valueType()}>();
	</#if>
</#list>

	//Secondary indexes
<#list model.getSecondaryIndexes() as method>
	<#if method.returnTypeIsIndexed()>
	private Map<${method.indexedReturnKeyType().getSimpleName()},Set<${method.valueType()}>> ${method.indexField()} = new HashMap<${method.indexedReturnKeyType().getSimpleName()},Set<${method.valueType()}>>();
	<#else>
	private Map<${method.keyType()},Set<${method.valueType()}>> ${method.indexField()} = new HashMap<${method.keyType()},Set<${method.valueType()}>>();
	</#if>
</#list>
	
<#list model.getIndexedClasses() as class>
	//${class.getName().getSimpleName()} 
	// =================================
	
	// Index [CREATE]
	public void index(${class.getName().getSimpleName()} value) {
		this.indexed${class.getName().getSimpleName()}.add(value);
	<#list class.getPrimaryIndexes() as method>
		{
		<@getKey method/>
			if (key.isPresent()) ${method.indexField()}.put(key.get(), value);
		}
	</#list>
	<#list class.getSecondaryIndexes() as method>
		{
		<@getKey method/>
			if (key.isPresent()) {
				if (${method.indexField()}.get(key.get()) == null) {
					//create if doesn't exist
					${method.indexField()}.put(key.get(), new HashSet<>());
				}
				${method.indexField()}.get(key.get()).add(value);
			}
		}
	</#list>
	}
	
	//Finders [RETRIEVE]
	public Set<${class.getName().getSimpleName()}> findAll${class.getName().getSimpleName()}() {
		return indexed${class.getName().getSimpleName()};
	}
	
	<#list class.getPrimaryIndexes() as method>
	public Optional<${method.valueType()}> ${method.indexFinder()}(${method.keyType()} key) {
		if (key == null) return Optional.empty();
		<#if method.returnTypeIsIndexed()>
		return Optional.ofNullable(${method.indexField()}.get(key.${method.indexedReturnType().getIdentifier().getName().getter()}()));
		<#else>
		return Optional.ofNullable(${method.indexField()}.get(key));
		</#if>
	} 
	
	</#list>
	<#list class.getSecondaryIndexes() as method>
	public Set<${method.valueType()}> ${method.indexFinder()}(${method.keyType()} key) {
		if (key == null) return Collections.emptySet();
		if (!${method.indexField()}.containsKey(key)) return Collections.emptySet();
		return ${method.indexField()}.get(key);
	} 
	
	</#list>
	public Set<${class.getName().getSimpleName()}> findByExample${class.getName().getSimpleName()}(${class.getName().getSimpleName()} value) {
	<#list class.getPrimaryIndexes() as method>
		{
		<@getKey method/>
			if (key.isPresent()) {
				Optional<${method.valueType()}> entry = ${method.indexFinder()}(key.get());
				if (entry.isPresent()) {
					// return single value if any unique key matches
					return Collections.singleton(entry.get()); 
				}
			}
		}
	</#list>
		// look for intersection of hits from all secondary indexes
		Set<${class.getName().getSimpleName()}> out = new HashSet<>();
		boolean found = false;
	<#list class.getSecondaryIndexes() as method>
		{
		<@getKey method/>
			if (key.isPresent()) {
				Set<${method.valueType()}> entry = ${method.indexFinder()}(key.get());
				if (!found) {
					out = entry;
					found = true;
				} else {
					out.retainAll(entry);
				}
			}
		}
	</#list>
		if (!found) out = Collections.emptySet();
		return out;
	}
	
	//Remove [DELETE]
	public void remove(${class.getName().getSimpleName()} value) {
		this.indexed${class.getName().getSimpleName()}.remove(value);
	<#list class.getPrimaryIndexes() as method>
		{
			//Primary index - ${method.indexField()} = ${class.getName().getSimpleName()} by ${method.getName().getter()}
		<@getKey method/>
			if (key.isPresent()) {
				${method.indexField()}.remove(key.get());
			}
		}
	</#list>
	<#list class.getSecondaryIndexes() as method>
		{
			//Secondary index - ${method.indexField()} = ${class.getName().getSimpleName()} by ${method.getName().getter()}
		<@getKey method/>
			if (key.isPresent()) {
				if (${method.indexField()}.get(key.get()) == null) ${method.indexField()}.put(key.get(), new HashSet<>());
				${method.indexField()}.get(key.get()).remove(value);
			}
		}
	</#list>
	}
	
	//Update
	public void update(${class.getName().getSimpleName()} oldValue, ${class.getName().getSimpleName()} newValue) {
		remove(oldValue);
		index(newValue);
	}
	
</#list>	


	//http://stackoverflow.com/questions/6122545/stop-words-and-stemmer-in-java

}
<#-- 
private Index.Unique<${keyType},${valueType}> ${indexField} = new Index.Unique<${keyType},${valueType}>() {
		
		private Map<${keyType},${valueType}> store = new HashMap<>();
		
		public Collection<${valueType}> getAll() {
			return store.values();
		}
		
		public Optional<${keyType}> keyFor(${valueType} value) {
			return Optional.ofNullable(value.${method.getName().getter()}());
		}
		
		public void index(${valueType} value) {
			keyFor(value).ifPresent(key -> store.put(key,value));
		}
		
		public Optional<${valueType}> get(${keyType} key) {
			return Optional.ofNullable(store.get(key));
		}
				
	};
  -->