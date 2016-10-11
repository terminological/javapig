package uk.co.terminological.javapig.annotations;

import java.io.Serializable;

import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.NoOp;

public enum BuiltIn implements Serializable {

	FLUENT (
			Scope.INTERFACE, 
			"fluent-interface.ftl", 
			"${classFQN}Fluent", 
			"java", 
			NoOp.class.getCanonicalName()),
	IMPL (
			Scope.INTERFACE, 
			"implementation-class.ftl", 
			"${classFQN}Impl", 
			"java",
			NoOp.class.getCanonicalName()),
	FLUENT_IMPL (
			Scope.INTERFACE, 
			"fluent-implementation-class.ftl", 
			"${classFQN}FluentImpl", 
			"java",
			NoOp.class.getCanonicalName()),
	MIRROR (
			Scope.MODEL, 
			"package-model.ftl", 
			"${rootPackage}.Model", 
			"java",
			NoOp.class.getCanonicalName()),
	VISITOR (
			Scope.MODEL, 
			"visitor-model.ftl", 
			"${rootPackage}.Visitor", 
			"java",
			NoOp.class.getCanonicalName()),
	FACTORY (
			Scope.MODEL, 
			"factory-model.ftl", 
			"${rootPackage}.Factory", 
			"java",
			NoOp.class.getCanonicalName()),
	DOTUML (
			Scope.MODEL, 
			"dot-uml-model.ftl", 
			"${rootPackage}.uml", 
			"dot",
			NoOp.class.getCanonicalName()),
	DEBUG (
			Scope.MODEL, 
			"debug-model.ftl", 
			"${rootPackage}.debug", 
			"txt",
			NoOp.class.getCanonicalName());
	
	private Scope[] scope;
	private String filename;
	private String classnameTemplate;
	private String extension;
	private String writerClass;
	
	private BuiltIn(Scope scope, String filename, String classnameTemplate, String extension, String writerClass) {
		this.scope = new Scope[] {scope};
		this.filename = filename;
		this.classnameTemplate = classnameTemplate;
		this.extension = extension;
		this.writerClass = writerClass;
	}

	public Scope[] getScope() {
		return scope;
	}

	public String getFilename() {
		return filename;
	}

	public String getClassnameTemplate() {
		return classnameTemplate;
	}
	
	public String getExtension() {
		return extension;
	}
	
	@SuppressWarnings("unchecked")
	public <X> JModelAdaptor<X> getAdaptor(Class<X> input) {
		try {
			return (JModelAdaptor<X>) Class.forName(writerClass).newInstance();
		} catch (ClassNotFoundException | ClassCastException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
