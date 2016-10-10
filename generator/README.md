# Javapig. 
## Annotation processing for the rest of us.

Annotation processing and code generation. Not pretty. This project tries to put enough lipstick on the pig to allow easy domain model driven code generation without requiring a research degree in java. As a code generator only runs during compilation this is essentially throw away code. Standards were sacrificed in the interest of progress, I barely understand some of this stuff. There are other options out there (e.g. Lombok,  and you should probably use them.

This project is functional but in evolution.

Javapig is intended to be used in the compilation phase of a maven project or as an annotation processor. A simple metamodel of the code is produced and fed to a freemarker templating engine, which can take build in or supplied templates to generate code. There are two ways of triggering it:

## As an annotation processor

Using the usual annotation processing framework:

	<dependency>
		<groupId>uk.co.terminological.javapig</groupId>
		<artifactId>generator</artifactId>
		<version>1.0.0-SNAPSHOT</version>
		<scope>provided</scope>
	</dependency>
	<dependency>
		<groupId>uk.co.terminological.javapig</groupId>
		<artifactId>runtime</artifactId>
		<version>1.0.0-SNAPSHOT</version>
	</dependency>

Which should be enough for code generation to happen at compile time. The downside to this is there will be a lot of unnecessary additional dependencies imported into your project. The annotation processor may decide to place the code in various different places, which you can control with the maven-comiler-plugin, and there is no control over what is scanned and what is not.

	<build>
		...
		<plugins>
			...
			<plugin>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.1</version>
				<configuration>
					<source>1.8</source>
					<target>1.8</target>
					<annotationProcessors>
						<annotationProcessor>uk.co.terminological.javapig.JModelProcessor</annotationProcessor>
					</annotationProcessors>
					<generatedSourcesDirectory>${project.build.directory}/generated-sources/annotations</generatedSourcesDirectory>
				</configuration>
			</plugin>
		</plugins>
	</build>

In theory you can get eclipse to run annotation processors during code authoring and get dynamic code generation. In practice I have not been able to make this work reliably.

## As a dedicated maven plugin

This mechanism addresses some of the issues above in that it pulls the code generation out of the annotation processor steps into its own phase. This plugin can be configured as follows:

	<dependencies>
		...
		<dependency>
			<groupId>uk.co.terminological.javapig</groupId>
			<artifactId>runtime</artifactId>
			<version>1.0.0-SNAPSHOT</version>
		</dependency>
		...
	</dependencies>
	...
	<build>
		...
		<plugins>
			...
			<plugin> 
				<groupId>uk.co.terminological.javapig</groupId> 
				artifactId>generator</artifactId> 
				<version>1.0.0-SNAPSHOT</version>
				<executions>
					<execution>
						<id>build-java-resources</id> 
						<phase>generate-sources</phase>
						<goals>
							<goal>javaGenerator</goal>
						</goals> 
					</execution>
				</executions>
				<configuration>
					<sourceCodeDirectory>... path to source code ... </sourceCodeDirectory>
					<targetDirectory>${project.build.directory}/generated-sources/annotations</targetDirectory>
				</configuration> 
			</plugin>
		</plugins>
	</build> 
	
This addresses some of the issues above but requires use of an entirely different code parsing framework.

## What code is scanned?

The scanner will look for getter methods defined in (Java 8) interfaces. This is designed to be a minimal definition of a domain data model, for example, the following interface in the annotated package:

	package uk.co.terminological.javapig.test.model;
	
	import java.util.ArrayList;
	import java.util.List;
	import java.util.Optional;
	import java.util.Set;
	
	public interface DataBean {
		
		//included javabean style getters:
		int getField1();
		float getField2();
		String getField3();
		Optional<String> getFieldRename();
		BasicBean[] getArrayOfBeans();
		List<BasicBean> getlistOfBeans();
		ArrayList<BasicBean> getArrayListOfBeans();
		Set<String> getSetOfStrings();
		
		//excluded from metamodel (but will be present in Java 8 code):
		default String lc3() {
			return getField3().toLowerCase();
		}
		
		//excluded from model and best avoided:
		//takes parameters
		String ignoredUtilityMethod(String input);
		//returns void
		void ignoredMethod();
	}   

Will be used as an input.
The following datatypes are fully supported:

* primitives (int, float, char etc...)
* Boxed types (Integer, Float, Character...)
* Java bean style elements from within domain model
* Optional (Java 8)
* Sets (and concrete subclasses)
* Lists (and concrete subclasses)
* Arrays

other datatypes may work depending on the use case and the template

Other features which are included in the metamodel fed to the templates:

* Annotations - these are made available to freemarker in multiple ways
* Javadoc comments

The preferred style of a bean, however would be the following example, using abstract collections and boxed primitives only:

	package uk.co.terminological.javapig.test.model;

	import java.util.List;
	import java.util.Optional;
	import java.util.Set;
	
	/**
	* defining javadoc
	*/
	public interface BestPracticeBean {
	
		@ACustomAnnotation(
			string="Example text", //will be available during templating
			clazz=BestPracticeBean.class //a string representation or the FQN will be available
		) 
		String getString();
		Integer getInteger();
		Optional<Float> getPossibleFloat();
		BasicBean getBean();
		Set<BasicBean> getSetOfBeans();
		List<String> getListOfStrings();
		
	}

## Configuration and usage

Configuration is done using a package level annotation. There are 2 basic modes - creating your own code generation template, or using a built in template:

A @uk.co.terminological.javapig.annotations.Model in a package will be result in the source tree under that package being scanned and a simple meta model of the code created. then any referenced freemarker templates will be executed with that metamodel.

### Built in templates

so the package-info.java file:

	@Model(
		builtins = { 
			BuiltIn.IMPL,
			BuiltIn.FLUENT,
			BuiltIn.FLUENT_IMPL,
			BuiltIn.FACTORY
		})
	package uk.co.terminological.javapig.test.model;
	import uk.co.terminological.javapig.annotations.Model;
	import uk.co.terminological.javapig.annotations.BuiltIn;

Will 	

### Roll your own templates

so the package-info.java file:

	@Model(
		directory="src/main/resources/freemarker",
		templates={ 
			@Template(
				appliesTo = Scope.MODEL, 
				filename = "my-schema-from-model.ftl", 
				classnameTemplate="${rootPackage}.Model"),
				extension = "xsd"
			@Template(
				appliesTo = Scope.INTERFACE, 
				filename = "my-magic-class.ftl", 
				classnameTemplate="${classFQN}Magic"),
			@Template(
				appliesTo = Scope.PACKAGE, 
				filename = "my-package-utilities.ftl", 
				classnameTemplate="${package}Utils")
		})
	package uk.co.terminological.javapig.test.model;
	import uk.co.terminological.javapig.annotations.Model;
	import uk.co.terminological.javapig.annotations.BuiltIn;
	import uk.co.terminological.javapig.annotations.Scope;
	import uk.co.terminological.javapig.annotations.Template;

Will execute 3 (fictional) freemarker templates (src/main/resources/freemarker/my-schema-from-model.ftl, src/main/resources/freemarker/my-magic-class.ftl and src/main/resources/freemarker/my-package-utilities.ftl) on the current set of annotated packages. 

#### scope:

Javapig can produce code at one of 4 distinct levels:

* MODEL: a single class file is produced for all the packages included in the model
* PACKAGE: a class file is produced per annotated package
* INTERFACE: the most common use case: a class file is produced per eligible domain model class (defined as interfaces)
* METHOD: the least common use case: a class file is produces per eligible domain model method/field (defined as java bean getter methods)   

#### classnameTemplate:

This describes where the resulting class file will be generated based on the fully qualified name of the resulting class:
The classname template can have the 4 following variables:

${rootPackage}: the highest level package that is annotated 
- e.g. uk.co.terminological.javapig.test
${package}: the current package 
- e.g. uk.co.terminological.javapig.test.model
${classFQN}: the fqn of the current class (interface) being inspected 
- e.g. uk.co.terminological.javapig.test.model.DataBean
${class}:
- e.g. DataBean
${method}:
- e.g. getField1

e.g. while looking at the interface above:
classnameTemplate=${classFQN}Magic
would resolve to:
uk.co.terminological.javapig.test.model.BestPracticeBeanMagic.java

__or__
com.mycompany.${class}Magic
could generate a class:
com.mycompany.BestPracticeBeanMagic.java

#### extension (optional):

by default the assumption is that the freemarker template is generating java code. However there is no reason not to generate other code (e.g. client side JavaScript) or resources (e.g. UML diagrams) from the domain model. An file extension here will modify that behaviour.

#### writing the templates

the tricky bit. 

1. Look at the examples in the runtime module (/src/main/resources/freemind) which give you an idea of what can be done with the templates.
2. Generate a simple domain model that you want to generate code from and execute the built in template __Builtin.DEBUG__ on it.
3. This will give you a file with an expansion of all the package level variables available to a freemarker template. Use this to pick the pieces you want.
4. If you are using annotations to define your code generation experiment with their native java methods. If they have been compiled already then they should work, but you will quickly find out if not.
5. Iterate rapidly.

Don't forget, you can put lipstick on a pig, but it's still a pig.
