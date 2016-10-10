@Model(
	//	directory="src/main/resources/freemarker",
	/*	templates={ 
	@Template(appliesTo = Scope.MODEL, filename = "package-model.ftl", classnameTemplate="${rootPackage}.Model"),
	@Template(appliesTo = {Scope.INTERFACE}, filename = "mutable-class.ftl", classnameTemplate="${class}Mutable"),
	@Template(appliesTo = {Scope.INTERFACE}, filename = "fluent-class.ftl", classnameTemplate="${class}Fluent")
		},*/ 
		builtins = { 
				BuiltIn.IMPL,
				BuiltIn.MIRROR,
				BuiltIn.VISITOR,
				BuiltIn.FLUENT,
				BuiltIn.FLUENT_IMPL,
				BuiltIn.FACTORY,
				BuiltIn.DOTUML,
				BuiltIn.DEBUG
		})
package uk.co.terminological.javapig.test.model;
import uk.co.terminological.javapig.annotations.Model;
import uk.co.terminological.javapig.annotations.BuiltIn;
//import uk.co.terminological.javapig.annotations.Scope;
//import uk.co.terminological.javapig.annotations.Template;














