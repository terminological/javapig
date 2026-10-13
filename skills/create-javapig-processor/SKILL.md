---
name: create-javapig-processor
description: 'Use when asked to create a custom javapig annotation processor, plugin, or code generator that reads annotations from source interfaces and produces generated code. Triggers on: "create a javapig plugin", "add a processor for @X", "extend javapig with a new annotation", "javapig custom adapter", "register a javapig built-in or plugin", "build a generator on top of javapig". Do NOT use for general javadoc generation, database migrations, or standalone annotation readers not integrated with javapig.'
---

# Create a javapig Annotation Processor

Create a custom annotation processor (plugin) for javapig that reads annotations from Java source interfaces and produces generated code.

## What is javapig?

javapig is a Java code generation framework. The user writes plain Java interfaces annotated with metadata, and javapig generates implementations, builders, factories, or other scaffolding from those interfaces.

**The full workflow looks like this:**

```
User's source interfaces (annotated)
       │
       ▼
┌─────────────────┐
│ Scanner          │  Reads source files, finds packages annotated with @Model,
│ (QDox or APT)    │  extracts interfaces and their annotations into a metamodel
└────────┬─────────┘
         │ JProject (metamodel)
         ▼
┌─────────────────┐
│ Plugin Adaptor   │  Optionally transforms the metamodel (e.g. adds
│ (JModelAdaptor)  │  indexing support, CSV reading) via map() methods
└────────┬─────────┘
         │ Adapted JProject
         ▼
┌─────────────────┐
│ Template Engine  │  FreeMarker templates render the adapted model into
│ (FreeMarker)     │  generated Java source files
└────────┬─────────┘
         │ Generated .java files
         ▼
User's output directory
```

javapig ships with built-in generators (IMPL, FLUENT, FACTORY, VISITOR, DEBUG, DOTUML, MIRROR) that live in `runtime/resources/freemarker/`. Your plugin adds **new** generators with custom annotations.

## What you need from javapig

The agent needs access to these javapig dependencies. The user's project must include them:

```xml
<!-- Maven example --><!-- Add to your pom.xml -->
<dependency>
    <groupId>io.github.terminological.javapig</groupId>
    <artifactId>plugin</artifactId>
    <version>1.0.0</version>
</dependency>
```

All types the agent will reference live in these packages:

| Package | Key types |
|---------|-----------|
| `uk.co.terminological.javapig.javamodel` | `JInterface`, `JGetMethod`, `JProject`, `JTemplateInput` |
| `uk.co.terminological.javapig.javamodel.tools` | `JModelCopier`, `JNameBuilder` |
| `uk.co.terminological.javapig.annotations` | `JModelAdaptor`, `BuiltIn`, `Scope` |

## When to Use This Skill

You need a plugin when:
- The user defines a custom annotation (e.g. `@Column`, `@Query`, `@Csv`) and wants javapig to recognise and act on it
- The user wants to extend the metamodel with methods that inspect annotations (e.g. `getColumnInfo()`, `getTableName()`)
- The user wants to generate code conditionally based on annotation presence or values
- The user wants a custom built-in generator or plugin that processes annotated interfaces

## Built-ins vs Plugins

There are two ways to register custom generators with javapig:

| | Built-in | Plugin |
|--|----------|--------|
| **Registration** | `@Model(builtins = {MyBuiltIn.class})` in `package-info.java` | `@Model(plugins = {"com.example.MyPlugin$Model"})` in `package-info.java` |
| **Availability** | Available to all packages in the classpath | Only available when explicitly listed |
| **Template location** | Must be in `/freemarker/` on a class that is a `BuiltIn` implementation | Can be on the plugin's own classpath |
| **Purpose** | Standard generators like IMPL, FLUENT | Domain-specific generators like SQL, CSV, Index |

Built-ins must implement the `BuiltIn` interface and load templates from `/freemarker`. Plugins implement `JModelAdaptor` and load templates from their own package. Most custom work should be a **plugin** (built-ins are for standard generators).

## Prerequisites

- javapig project compiled and available (`runtime`, `generator`, `plugin` modules)
- Java 8+ project, Maven multi-module structure
- A custom annotation defined with `@Retention(SOURCE)` — it must survive to the source level for QDox to read it
- An interface or class with the annotation on getters or the class itself

## Pattern Overview

javapig plugins follow a four-file pattern:

| File | Role |
|------|------|
| **Annotation** | Define the annotation on the target interface (e.g. `@Column`, `@Query`, `@Csv`) |
| **Extended model** | Subclass `JInterface` / `JGetMethod` to read annotations via `getAnnotation()` |
| **Plugin adaptor** | Extends `JModelCopier`, implements `JModelAdaptor` — maps model types and registers built-ins |
| **Template** | FreeMarker `.ftl` file that generates code using the adapted model |

The scanner (QDox or APT) has already parsed `@MyAnnotation("hello")` from source into `JAnnotation → JAnnotationEntry → JAnnotationValue` objects and attached them to the metamodel. Your plugin simply **reads** those parsed values — you do not write parsing logic.

## Workflow

### Step 1: Define the annotation

```java
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface MyColumn {
    String name() default "";
    boolean required() default false;
    int width() default 255;
}
```

- Use `@Retention(RetentionPolicy.SOURCE)` — annotations with `CLASS` or `RUNTIME` retention may not be visible to the source-level scanner
- `@Target` is usually `ElementType.METHOD` (for getter annotations) or `ElementType.TYPE` (for class-level)
- Provide a `value()` attribute if the annotation is commonly used without named attributes: `@Column("age")` instead of `@Column(name = "age")`

### Step 2: Create extended model classes

Subclass `JInterface` and `JGetMethod` to expose annotation-driven functionality. The scanner has already attached `JAnnotation` objects to the model — `getAnnotation(Class)` uses Java's Proxy mechanism to create a live annotation instance.

```java
public class MyColumnInterface extends JInterface {

    public MyColumnInterface(JInterface in) {
        super(in);
    }

    public boolean hasMyColumn() {
        return this.isAnnotationPresent(MyColumn.class);
    }

    public String getColumnName() {
        MyColumn ann = this.getAnnotation(MyColumn.class);
        return ann != null ? ann.name() : null;
    }
}
```

```java
public class MyColumnGetMethod extends JGetMethod {

    public MyColumnGetMethod(JGetMethod in) {
        super(in);
    }

    public String columnName() {
        MyColumn ann = this.getAnnotation(MyColumn.class);
        return ann != null ? ann.name() : null;
    }

    public boolean isRequired() {
        MyColumn ann = this.getAnnotation(MyColumn.class);
        return ann != null && ann.required();
    }
}
```

### Step 3: Define the plugin adaptor

Extend `JModelCopier` and implement `JModelAdaptor`. This is the bridge between the metamodel and the template system.

```java
public abstract class ColumnPlugin extends JModelCopier implements JModelAdaptor {

    // Override map() methods to wrap standard types with your extended versions
    @Override
    public MyColumnInterface map(JInterface in) {
        return new MyColumnInterface(in);
    }

    @Override
    public MyColumnGetMethod map(JGetMethod in) {
        return new MyColumnGetMethod(in);
    }

    // Only process interfaces/methods that have the annotation
    @Override
    public boolean filter(JTemplateInput input) {
        if (input instanceof MyColumnInterface) {
            return ((MyColumnInterface) input).hasMyColumn();
        }
        return true;
    }

    // Built-in variant: generate a single class for the entire root package
    public static class Model extends ColumnPlugin {
        @Override
        public Scope[] getScope() {
            return new Scope[] { Scope.MODEL };
        }
        @Override
        public String getTemplateFilename() {
            return "column-model.ftl";
        }
        @Override
        public String getClassNameTemplate() {
            return "${rootPackage}.ColumnDefs";
        }
        @Override
        public String getExtension() {
            return "java";
        }
    }

    // Built-in variant: generate one class per interface
    public static class Interface extends ColumnPlugin {
        @Override
        public Scope[] getScope() {
            return new Scope[] { Scope.INTERFACE };
        }
        @Override
        public String getTemplateFilename() {
            return "column-implementation.ftl";
        }
        @Override
        public String getClassNameTemplate() {
            return "${classFQN}Columns";
        }
        @Override
        public String getExtension() {
            return "java";
        }
    }
}
```

Key points:
- `map()` methods wrap each standard model element with your extended version — the `JModelCopier` base class handles recursive cloning
- `filter()` decides which elements participate in code generation — return `true` to include, `false` to skip
- Inner static classes (`Model`, `Interface`) are the built-in variants the user registers in their `package-info.java`
- Each inner class must override all five methods: `getScope()`, `getTemplateFilename()`, `getClassNameTemplate()`, `getExtension()`

### Step 4: Register the plugin

In the user's `package-info.java`:

```java
@Model(
    builtins = { BuiltIn.IMPL, BuiltIn.DEBUG },
    plugins = { "com.example.ColumnPlugin$Model", "com.example.ColumnPlugin$Interface" }
)
package com.example.models;
import uk.co.terminological.javapig.annotations.*;
```

### Step 5: Write the FreeMarker template

Templates receive the adapted model where interfaces are your extended types. The `JModelWriter` passes a `Map<String, Object>` as the template root — the keys depend on the `Scope`.

```freemarker
<#import "common.ftl" as c>
<#assign sn>${class.getName().getSimpleName()}</#assign>
package ${packagename};

public class ${sn} {
  <#list class.getMethods() as method>
    <#if method.isAnnotationPresent("com.example.MyAnnotation")>
      <#assign ann = class.getAnnotation("com.example.MyAnnotation")>
      value="${ann.value()}"
    </#if>
  </#list>
}
```

Template variable substitutions available in `${classNameTemplate}`:
- `${method}` — method name (simple name, derived from getter method name)
- `${classFQN}` — fully-qualified interface name
- `${class}` — simple interface name
- `${package}` — current package being processed
- `${rootPackage}` — logical parent package with `@Model` annotation

### Step 6: Provide the FreeMarker template file

Place the template on the plugin's classpath:

```
plugin/src/main/java/com/example/ColumnPlugin.java
plugin/src/main/resources/freemarker/column-model.ftl
plugin/src/main/resources/freemarker/column-implementation.ftl
```

The `JModelWriter` loads templates from the adaptor's classpath — built-ins load from `/freemarker/` on `BuiltIn.class`'s classpath, plugins load from their own package root.

## Guidance

### Template data model

`JModelWriter.doGenerate()` builds a `Map<String, Object>` as the template root. The keys depend on your adaptor's `Scope`:

| Scope | Root map keys | Description |
|-------|--------------|-------------|
| `MODEL` | `model` (JProject) | Full project metamodel |
| `PACKAGE` | `package` (JPackage) | Single package |
| `INTERFACE` | `class` (JInterface) | Single interface |
| `METHOD` | `method` (JGetMethod) | Single method |

These are always present regardless of scope:
- `fqn` — fully-qualified output class name (from `getClassNameTemplate()`)  
- `classname` — simple name of the output class
- `packagename` — package name in lowercase (derived from `fqn`)  
- `rootPackage` — name of the root package with `@Model`

A single adaptor can declare multiple scopes, causing multiple template invocations. For example, `Scope.MODEL` + `Scope.INTERFACE` generates one file per root package AND one file per interface.

When your plugin is active, the model has been transformed by `adaptor.adapt(model)`, which calls your `map()` methods — so `model.getClasses()` returns your extended types (e.g. `MyColumnInterface` instead of `JInterface`), giving templates access to your custom methods.

FreeMarker 2.3.25 is used with `DefaultObjectWrapperBuilder`. All standard FreeMarker features are available: `<#if>`, `<#list>`, `<#macro>`, `<#import>`, `<#assign>`, and built-ins (`?size`, `?string`, `?is_first`, `?is_last`).

### Annotation value types and how javapig handles them

| Annotation attribute type | Stored in | Access pattern |
|--------------------------|-----------|----------------|
| `String`, `int`, `boolean`, `float`, `double`, `long`, `short`, `byte` | `JAnnotationValue.Primitive` | `ann.value()` via proxy, string is cast |
| `SomeClass.class` | `JAnnotationValue.Class` (contains `JClassName`) | `ann.value()` via proxy — returns `Class<?>` |
| `@NestedAnnotation` | `JAnnotationValue.Annotation` (contains `JAnnotation`) | `ann.value()` via proxy — recursive proxy creation |
| `String[]`, `int[]` | Multiple `JAnnotationValue.Primitive` entries | `ann.value()` via proxy — returns array |
| `enum` values | Stored as `String` in `JAnnotationValue.Primitive` | Cast back via `StringCaster` on proxy invocation |

The `JAnnotation.convert(Class<T>)` method creates a dynamic proxy that maps each annotation method to the corresponding `JAnnotationEntry`, unwrapping values through `JAnnotationValue.unwrap()`. This means `iface.getAnnotation(MyColumn.class).name()` works seamlessly — the proxy handles all the conversion.

### When to use a plugin vs. a built-in vs. annotation-only

- **Annotation-only**: If you just need to read annotations from interfaces without generating new code, extend `JInterface`/`JGetMethod` and add methods that call `getAnnotation()` — no plugin adaptor needed
- **Plugin**: If you need to conditionally include/exclude interfaces based on annotation values or generate new code from the annotated interfaces
- **Built-in**: If the generator should be available to every package annotated with `@Model` without requiring the user to register it in `plugins = {...}`

## Validation

1. Compile the plugin with `mvn compile -pl plugin` — no errors
2. Register the plugin in a test `package-info.java` with `@Model(plugins = {...})`
3. Run javapig scan on an interface annotated with `@MyColumn` — verify the adapted model is used
4. Check generated output for correct annotation values
5. Verify `filter()` correctly excludes unannotated interfaces

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| `getAnnotation()` returns null | Annotation retention is `CLASS` or `RUNTIME` not `SOURCE` | Change to `@Retention(RetentionPolicy.SOURCE)` |
| `convert()` throws `ClassNotFoundException` | Annotation FQN is unknown (package-level annotation) | Check `JAnnotation.getCanonicalName()` — if `<unknown>`, use string-based `getAnnotation(String)` or provide FQN in annotation registration |
| Enum values come as strings | Enums stored as strings in `JAnnotationValue.Primitive` | Proxy converter handles it automatically via `StringCaster`; standalone access needs manual enum conversion |
| Template can't find annotation method | The annotation attribute name doesn't match the getter name | Annotation methods become entries keyed by `AnnotationName#methodName` — verify the method name matches |
| Filter excludes interfaces that should be included | `filter()` checks wrong type or condition | Debug by printing `input.getClass().getName()` — the input may already be wrapped by an earlier plugin |
| Plugin not loaded | Plugin class name is wrong or not on classpath | Verify FQN in `plugins = {"com.example.Plugin$Model"}` matches the class name; ensure jar contains the class |
| Nested annotation values not resolved | Nested annotations need their own `JAnnotation` created | The scanner's `createAnnotation()` method handles this recursively — ensure your scanner extends `RecursiveVisitor` |

## References

- **Template Reference** — `references/freemarker-templates.md`: full data model API, available variables per Scope, example templates
- `JElement.getAnnotation(Class<T>)` — reads annotations via proxy from the metamodel
- `JModelAdaptor` — interface for plugin adaptors: `adapt()`, `filter()`, `getScope()`, `getTemplateFilename()`, `getClassNameTemplate()`, `getExtension()`
- `JModelCopier` — abstract base for model transformation: override `map()` methods
- `JModelWriter` — generates code from the adapted model using FreeMarker templates
- `QDoxJModelBuilder` — source-file scanner that parses annotations into the metamodel
- `AptJModelBuilder` — annotation-processor scanner that parses annotations into the metamodel
- `JAnnotation` — represents a parsed annotation with entries and proxy conversion
- `JAnnotationValue` — holds annotation attribute values: `Primitive`, `Annotation`, `Class` subtypes
- `JClassName` — class name representation with FQN, simple name, and type conversion
