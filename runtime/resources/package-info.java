@Model(
		directory="src/main/resources",
		templates={ 
	@Template(appliesTo = Scope.PACKAGE, filename = "package-model.ftl", classnameTemplate="${package}.Model"),
	@Template(appliesTo = {Scope.CLASS}, filename = "class-util.ftl", classnameTemplate="${class}Util"),
	@Template(appliesTo = Scope.INTERFACE, filename = "owl-service.ftl", classnameTemplate="com.bmj.informatics.catalogue.service.${className}Service"),
	@Template(appliesTo = Scope.INTERFACE, filename = "hateoas-resource.ftl", classnameTemplate="com.bmj.informatics.catalogue.dto.${className}Resource"),
	@Template(appliesTo = Scope.INTERFACE, filename = "hateoas-lists.ftl", classnameTemplate="com.bmj.informatics.catalogue.dto.list.${className}ResourceList")
})

package com.bmj.informatics.example;
import com.bmj.informatics.annotations.annotation.Model;
import com.bmj.informatics.annotations.annotation.Template;
import com.bmj.informatics.annotations.annotation.Template.Scope;

