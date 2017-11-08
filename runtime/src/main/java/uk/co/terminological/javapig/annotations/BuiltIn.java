package uk.co.terminological.javapig.annotations;

import java.io.Serializable;

import uk.co.terminological.javapig.javamodel.Project;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JTemplateInput;

public enum BuiltIn implements Serializable, JModelAdaptor {

	FLUENT (
			Scope.INTERFACE, 
			"fluent-interface.ftl", 
			"${classFQN}Fluent", 
			"java"),
	IMPL (
			Scope.INTERFACE, 
			"implementation-class.ftl", 
			"${classFQN}Impl", 
			"java"),
	FLUENT_IMPL (
			Scope.INTERFACE, 
			"fluent-implementation-class.ftl", 
			"${classFQN}FluentImpl", 
			"java"),
	MIRROR (
			Scope.PACKAGE, 
			"package-model.ftl", 
			"${package}.Model", 
			"java"),
	VISITOR (
			Scope.MODEL, 
			"visitor-model.ftl", 
			"${rootPackage}.Visitor", 
			"java"),
	FACTORY (
			Scope.MODEL, 
			"factory-model.ftl", 
			"${rootPackage}.Factory", 
			"java"),
	DOTUML (
			Scope.MODEL, 
			"dot-uml-model.ftl", 
			"${rootPackage}.uml", 
			"dot"),
	DEBUG (
			Scope.MODEL, 
			"debug-model.ftl", 
			"${rootPackage}.debug", 
			"txt"),
	CORE (
			Scope.INTERFACE, 
			"core-interface.ftl", 
			"${classFQN}", 
			"java")
	;
	
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

	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.annotations.JWriteSpecification#getScope()
	 */
	@Override
	public Scope[] getScope() {
		return scope;
	}

	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.annotations.JWriteSpecification#getFilename()
	 */
	@Override
	public String getTemplateFilename() {
		return filename;
	}

	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.annotations.JWriteSpecification#getClassnameTemplate()
	 */
	@Override
	public String getClassNameTemplate() {
		return classnameTemplate;
	}
	
	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.annotations.JWriteSpecification#getExtension()
	 */
	@Override
	public String getExtension() {
		return extension;
	}
	
	@Override
	public Project adapt(Project model) {
		return model;
	}

	@Override
	public boolean filter(JTemplateInput input) {
		return true;
	}
}
