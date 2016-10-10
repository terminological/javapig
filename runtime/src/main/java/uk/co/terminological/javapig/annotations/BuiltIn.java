package uk.co.terminological.javapig.annotations;

import java.io.Serializable;

public enum BuiltIn implements Serializable {

	FLUENT (Scope.INTERFACE, "fluent-interface.ftl", "${classFQN}Fluent", "java"),
	IMPL (Scope.INTERFACE, "implementation-class.ftl", "${classFQN}Impl", "java"),
	FLUENT_IMPL (Scope.INTERFACE, "fluent-implementation-class.ftl", "${classFQN}FluentImpl", "java"),
	MIRROR (Scope.MODEL, "package-model.ftl", "${rootPackage}.Model", "java"),
	VISITOR (Scope.MODEL, "visitor-model.ftl", "${rootPackage}.Visitor", "java"),
	FACTORY (Scope.MODEL, "factory-model.ftl", "${rootPackage}.Factory", "java"),
	DOTUML (Scope.MODEL, "dot-uml-model.ftl", "${rootPackage}.uml", "dot"),
	DEBUG (Scope.MODEL, "debug-model.ftl", "${rootPackage}.debug", "txt");
	
	private Scope[] scope;
	private String filename;
	private String classnameTemplate;
	private String extension;
	
	private BuiltIn(Scope scope, String filename, String classnameTemplate, String extension) {
		this.scope = new Scope[] {scope};
		this.filename = filename;
		this.classnameTemplate = classnameTemplate;
		this.extension = extension;
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
}
