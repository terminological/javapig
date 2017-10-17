package ${rootPackage};

import java.io.Serializable;
import javax.annotation.Generated;
import java.util.*;

<#list model.getPrimaryIndexes() as method>
import ${method.getDeclaringClass().getName().getCanonicalName()};
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
		<#if method.returnTypeIsIndexed()>
			//indexed return type: ${method.keyType()} 
			${method.indexedReturnKeyType().getSimpleName()} key = value.${method.getName().getter()}().${method.indexedReturnType().getIdentifier().getName().getter()}();
		<#else>
			//simple return type: ${method.keyType()}
			<#if method.isOptional()>
			Optional<${method.keyType()}> key = value.${method.getName().getter()}();
			if (key.isPresent()) ${method.indexField()}.put(key.get(), value);
			<#else>
			${method.keyType()} key = value.${method.getName().getter()}();
			if (key != null) ${method.indexField()}.put(key, value);
			</#if>
		</#if>
		}
	</#list>
	<#list class.getSecondaryIndexes() as method>
		{
		<#if method.returnTypeIsIndexed()>
			//indexed return type: ${method.keyType()} 
			${method.indexedReturnKeyType().getSimpleName()} key = value.${method.getName().getter()}().${method.indexedReturnType().getIdentifier().getName().getter()}();
			if (key != null) {
				if (${method.indexField()}.get(key) == null) ${method.indexField()}.put(key, new HashSet<>());
				${method.indexField()}.get(key).add(value);
			}
			<#-- support for optionals here? -->
		<#else>
			//simple return type: ${method.keyType()}
			<#if method.isOptional()>
			Optional<${method.keyType()}> key = value.${method.getName().getter()}();
			if (key.isPresent()) {
				if (${method.indexField()}.get(key.get()) == null) ${method.indexField()}.put(key.get(), new HashSet<>());
				${method.indexField()}.get(key.get()).add(value);
			}
			<#else>
			${method.keyType()} key = value.${method.getName().getter()}();
			if (key != null) {
				if (${method.indexField()}.get(key) == null) ${method.indexField()}.put(key, new HashSet<>());
				${method.indexField()}.get(key).add(value);
			}
			</#if>
		</#if>
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
		<#if method.returnTypeIsIndexed()>
		${method.indexedReturnKeyType().getSimpleName()} keyId = key.${method.indexedReturnType().getIdentifier().getName().getter()}();
		if (!${method.indexField()}.containsKey(keyId)) return Collections.emptySet();
		return ${method.indexField()}.get(keyId);
		<#else>
		if (!${method.indexField()}.containsKey(key)) return Collections.emptySet();
		return ${method.indexField()}.get(key);
		</#if>
	} 
	
	</#list>
	public Set<${class.getName().getSimpleName()}> findByExample${class.getName().getSimpleName()}(${class.getName().getSimpleName()} proto) {
	<#list class.getPrimaryIndexes() as method>
		{
			${method.keyType()} key = proto.${method.getName().getter()}();
			Optional<${method.valueType()}> value = ${method.indexFinder()}(key);
			if (value.isPresent()) {
				return Collections.singleton(value.get()); //return single value if any unique key matches
			}
		}
	</#list>
		Set<${class.getName().getSimpleName()}> out = new HashSet<>();
		boolean found = false;
	<#list class.getSecondaryIndexes() as method>
		{
		<#if method.isOptional()>
			Optional<${method.keyType()}> key = proto.${method.getName().getter()}();
			if (key.isPresent()) {
				Set<${method.valueType()}> value = ${method.indexFinder()}(key.get());
		<#else>
			${method.keyType()} key = proto.${method.getName().getter()}();
			if (key != null) {
				Set<${method.valueType()}> value = ${method.indexFinder()}(key);
		</#if>
				if (!found) {
					out = value;
					found = true;
				} else {
					out.retainAll(value);
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
			//Primary index - ${class.getName().getSimpleName()} by ${method.getName().getter()}
			${method.keyType()} key = value.${method.getName().getter()}();
			if (key != null) ${method.indexField()}.remove(key);
		}
	</#list>
	<#list class.getSecondaryIndexes() as method>
		{
		<#if method.returnTypeIsIndexed()>
			//indexed return type: ${method.keyType()} 
			${method.indexedReturnKeyType().getSimpleName()} key = value.${method.getName().getter()}().${method.indexedReturnType().getIdentifier().getName().getter()}();
			if (key != null) {
				if (${method.indexField()}.get(key) == null) ${method.indexField()}.put(key, new HashSet<>());
				${method.indexField()}.get(key).remove(value);
			}
			<#-- support for optionals here? -->
		<#else>
			//simple return type: ${method.keyType()}
			<#if method.isOptional()>
			Optional<${method.keyType()}> key = value.${method.getName().getter()}();
			if (key.isPresent()) {
				if (${method.indexField()}.get(key.get()) == null) ${method.indexField()}.put(key.get(), new HashSet<>());
				${method.indexField()}.get(key.get()).remove(value);
			}
			<#else>
			${method.keyType()} key = value.${method.getName().getter()}();
			if (key != null) {
				if (${method.indexField()}.get(key) == null) ${method.indexField()}.put(key, new HashSet<>());
				${method.indexField()}.get(key).remove(value);
			}
			</#if>
		</#if>
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