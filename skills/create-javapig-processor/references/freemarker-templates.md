# Freemarker Template Reference

This reference documents the data model available to FreeMarker templates (`.ftl` files) in the javapig code generation pipeline.

## Template Loading

Templates are loaded from the adaptor's classpath:
- **Built-ins** — loaded from `/freemarker/` on the `BuiltIn.class` classpath
- **Plugins** — loaded from the adaptor class's own package root

Templates can import shared macros:
```freemarker
<#import "common.ftl" as c>
```

The `common.ftl` macros (`@c.hashCode`, `@c.equals`, `@c.copyAnnotations`) are in `runtime/resources/freemarker/` and are always available for built-ins.

## Template Data Model

`JModelWriter` passes a `Map<String, Object>` as the FreeMarker root object. The keys available depend on the `Scope`:

| Scope | Keys in root map | Description |
|-------|-----------------|-------------|
| `METHOD` | `method` (JGetMethod) | Single method being processed |
| `INTERFACE` | `class` (JInterface) | Single interface being processed |
| `PACKAGE` | `package` (JPackage) | Single package being processed |
| `MODEL` | `model` (JProject) | Full project metamodel |

These keys are always present in every scope:

| Key | Type | Description |
|-----|------|-------------|
| `fqn` | String | Fully-qualified output class name (constructed from the adaptor's `getClassNameTemplate()`) |
| `classname` | String | Simple name of the output class |
| `packagename` | String | Package name in lowercase (derived from `fqn`) |
| `rootPackage` | String | Name of the root package annotated with `@Model` |

### Scope-specific value substitution

The `getClassNameTemplate()` string is processed before rendering. Supported placeholders:

| Placeholder | Set in Scope | Description |
|-------------|-------------|-------------|
| `${method}` | METHOD | Method's class name (e.g. `getFoo()` → `Foo`) |
| `${classFQN}` | INTERFACE | Fully-qualified interface name |
| `${class}` | INTERFACE | Simple interface name |
| `${package}` | PACKAGE | Package name |
| `${rootPackage}` | MODEL | Root package name |

## API Reference by Type

### JInterface (the `class` variable in INTERFACE scope)

| Method | Returns | Description | Example usage |
|--------|---------|-------------|---------------|
| `getName().getSimpleName()` | String | Simple name (e.g. `MyBean`) | `${class.getName().getSimpleName()}` |
| `getName().getCanonicalName()` | String | Fully-qualified name | `${class.getName().getCanonicalName()}` |
| `getJavaDoc()` | String | JavaDoc comment | `${class.getJavaDoc()}` |
| `getMethods()` | List\<JGetMethod\> | All methods (non-default, no-params only) | `<#list class.getMethods() as m>` |
| `getExtendedMethods()` | List\<JGetMethod\> | All methods including defaults | `<#list class.getExtendedMethods() as m>` |
| `getSupertypes()` | Set\<JClassName\> | Direct supertype interfaces | `<#list class.getSupertypes() as s>` |
| `getImports()` | Set\<String\> | Required imports | `<#list class.getImports() as imp>` |
| `getImportsIncludingAnnotations()` | List\<String\> | Imports including annotation FQNs | `<#list class.getImportsIncludingAnnotations() as imp>` |
| `getAnnotations()` | List\<JAnnotation\> | Annotations on this interface | `<#list class.getAnnotations() as ann>` |
| `isAnnotationPresent("FQN")` | boolean | Check for annotation by string | `<#if class.isAnnotationPresent("com.example.X")>` |
| `getAnnotation("FQN")` | Annotation | Get annotation by string (returns proxy) | `<#assign ann = class.getAnnotation("com.example.X")>` |
| `isTypeOf("FQN")` | boolean | Check if this or any supertype matches | `<#if class.isTypeOf("com.example.Base")>` |
| `getCode()` | String | Unique ID for this interface | `${class.getCode()}` |

### JGetMethod (the `method` variable in METHOD scope)

| Method | Returns | Description | Example usage |
|--------|---------|-------------|---------------|
| `getName().getter()` | String | Getter method name (e.g. `getName()`) | `${method.getName().getter()}` |
| `getName().getClassName()` | String | Full method name (e.g. `pkg.Bean#getName`) | `${method.getName().getClassName()}` |
| `getName().getSimpleName()` | String | Method name only (e.g. `getName`) | `${method.getName().getSimpleName()}` |
| `getInterfaceType()` | String | Return type as used in interface (e.g. `String`, `Optional<String>`) | `${method.getInterfaceType()}` |
| `getImplementationType()` | String | Return type for generated impl (wraps collection/optional) | `${method.getImplementationType()}` |
| `getBoxedTypeFQN()` | String | Fully-qualified boxed return type | `${method.getBoxedTypeFQN()}` |
| `getReturnType()` | JClassName | Return type as JClassName object | `${method.getReturnType().getCanonicalName()}` |
| `getUnderlyingType()` | JClassName | Element type for collections (e.g. `String` from `List<String>`) | `${method.getUnderlyingType().getCanonicalName()}` |
| `getReturnTypeDefinition()` | String | Raw return type string as parsed | `${method.getReturnTypeDefinition()}` |
| `getImports()` | Set\<String\> | Imports needed for return type | `<#list method.getImports() as imp>` |
| `getJavaDoc()` | String | JavaDoc comment | `${method.getJavaDoc()}` |
| `getAnnotations()` | List\<JAnnotation\> | Annotations on this method | `<#list method.getAnnotations() as ann>` |

**Boolean type predicates:**

| Method | Returns | Description |
|--------|---------|-------------|
| `isPrimitive()` | boolean | Returns a primitive type (`int`, `boolean`, `double`, etc.) |
| `isParameterised()` | boolean | Returns a parameterised type (`List<String>`, `Optional<X>`) |
| `isArray()` | boolean | Returns an array type (`String[]`) |
| `isCollection()` | boolean | Returns a `Collection` or `Iterable` |
| `isList()` | boolean | Returns a `List` or `ArrayList` |
| `isSet()` | boolean | Returns a `Set` or `HashSet` |
| `isOptional()` | boolean | Returns an `Optional` |
| `isDefault()` | boolean | Is a `default` method on the interface |
| `hasInverseMethod()` | boolean | Has a linked inverse (via `@Inverse` annotation) |
| `isTypeOf("FQN")` | boolean | Return type matches given FQN |
| `isEnum()` | boolean | Return type is an enum |
| `cardinality()` | String | Returns `"0..1"` for optional, `"0..N"` for collection |

**Inverse methods:**

| Method | Returns | Description |
|--------|---------|-------------|
| `getInverseMethod()` | JGetMethod | The inverse method (if any) |
| `declaredBy(JInterface)` | boolean | Method is declared on this interface |

### JAnnotation (from `getAnnotations()`)

| Method | Returns | Description |
|--------|---------|-------------|
| `getName()` | String | Simple annotation name (e.g. `Column`, not `@Column`) |
| `getCanonicalName()` | String | FQN (e.g. `com.example.Column`, or `<unknown>` for package-level) |
| `getEntries()` | List\<JAnnotationEntry\> | Entry/value pairs of the annotation |
| `getValues("keyName")` | List\<JAnnotationValue\> | Values for a specific attribute |
| `getImports()` | Set\<String\> | Imports needed for this annotation |
| `toSourceCode()` | String | Java source code representation of the annotation |

### JAnnotationEntry (from annotation's `getEntries()`)

| Method | Returns | Description |
|--------|---------|-------------|
| `getMethod().getter()` | String | Attribute name (e.g. `name`, `value`) |
| `getValues()` | List\<JAnnotationValue\> | The attribute values |
| `getValues()?size` | Integer | Number of values (for arrays) |

### JAnnotationValue (from entry's `getValues()`)

| Method | Returns | Description |
|--------|---------|-------------|
| `getValue()` | Object | The raw value |
| `isPrimitive()` | boolean | Is a primitive/literal value |
| `isClass()` | boolean | Is a class reference (`SomeClass.class`) |
| `isAnnotation()` | boolean | Is a nested annotation |

For `isClass()` values, the value is a `JClassName` — access with:
```freemarker
<#if annotationValue.isClass()>
  ${annotationValue.getValue().getCanonicalName()}.class
</#if>
```

For `isAnnotation()` values, the value is a `JAnnotation` — access recursively:
```freemarker
<#if annotationValue.isAnnotation()>
  <#assign nestedAnn = annotationValue.getValue()>
  @${nestedAnn.getName()}
</#if>
```

### JClassName (from supertypes, return types, etc.)

| Method | Returns | Description |
|--------|---------|-------------|
| `getSimpleName()` | String | Simple name (e.g. `List`) |
| `getCanonicalName()` | String | Fully-qualified name (e.g. `java.util.List`) |
| `getPackageName()` | String | Package name only (e.g. `java.util`) |
| `importName()` | String | Import-friendly name (same as canonical for simple types) |
| `isCompiled()` | boolean | Is a known compiled class |
| `isArray()` | boolean | Is an array type |
| `equivalent("FQN")` | boolean | Matches given FQN |
| `code()` | String | Unique ID |
| `box()` | String | Boxed version (e.g. `int` → `java.lang.Integer`) |

### JPackage (the `package` variable in PACKAGE scope)

| Method | Returns | Description |
|--------|---------|-------------|
| `getName().getCanonicalName()` | String | Package name |
| `getClasses()` | Set\<JInterface\> | Interfaces in this package |
| `getMethods()` | Set\<JGetMethod\> | Methods in this package |
| `getPackages()` | Set\<JPackage\> | Sub-packages |
| `getImports()` | Set\<String\> | All imports from all interfaces in this package |
| `getCode()` | String | Unique ID |
| `isEnabled("BUILTIN")` | boolean | Whether a built-in is enabled |
| `isRootPackage()` | boolean | Is a root package (has `@Model` metadata) |
| `getParent()` | Optional\<JPackage\> | Parent package |
| `getMetadata()` | JPackageMetadata | Package metadata (built-ins, plugins) |

### JProject (the `model` variable in MODEL scope)

| Method | Returns | Description |
|--------|---------|-------------|
| `getPackages()` | Collection\<JPackage\> | All packages |
| `getRootPackages()` | Collection\<JPackage\> | Root packages (annotated with `@Model`) |
| `getClasses()` | Collection\<JInterface\> | All interfaces |
| `getMethods()` | Collection\<JGetMethod\> | All methods |
| `getMetadata()` | JPackageMetadata | Root package metadata |
| `findPackage("FQN")` | JPackage | Look up package by name |
| `findClass(JClassName)` | JInterface | Look up interface by name |

## Template Examples

### INTERFACE scope — generate a single class for one interface

```freemarker
<#import "common.ftl" as c>
<#assign sn>${class.getName().getSimpleName()}</#assign>
package ${packagename};
import javax.annotation.Generated;
<#list class.getImportsIncludingAnnotations() as imp>
import ${imp};
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
<@c.copyAnnotations class 0/>
public class ${sn} {
  <#list class.getMethods() as method>
    private ${method.getInterfaceType()} ${method.getName().getter()}();
  </#list>
}
```

### METHOD scope — generate code for each method individually

```freemarker
public ${method.getInterfaceType()} ${method.getName().getter()}() {
  <#if method.isOptional()>
    return Optional.ofNullable(${method.getName().getter()}());
  <#elseif method.isCollection()>
    return new ArrayList<>(${method.getName().getter()}());
  <#else>
    return ${method.getName().getter()}();
  </#if>
}
```

### PACKAGE scope — generate code for an entire package

```freemarker
package ${packagename};
import javax.annotation.Generated;
<#list package.getImports() as imp>
import ${imp};
</#list>

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public final class ${classname} {
  <#list package.getMethods() as method>
    <#if method.isPrimitive()>
    // primitive: ${method.getName().getter()}() returns ${method.getInterfaceType()}
    </#if>
  </#list>
}
```

### MODEL scope — generate code for the entire root package

```freemarker
package ${rootPackage};
import javax.annotation.Generated;

@Generated({"uk.co.terminological.javapig.JModelWriter"})
public final class ${classname} {
  <#list model.getMethods() as method>
    <#if method.isCollection()>
    public Set<${method.getUnderlyingType().getSimpleName()}> getAll${method.getName().getClassName()}() { ... }
    </#if>
  </#list>
}
```

### Reading annotation values

```freemarker
<#if class.isAnnotationPresent("com.example.Column")>
  <#assign col = class.getAnnotation("com.example.Column")>
  public static final ColumnDef ${method.getName().getter()} =
      new ColumnDef("${col.getValues("name")[0].value}", ${col.getValues("required")[0].value?string});
</#if>
```

### Iterating supertypes with comma separation

```freemarker
public interface ${class.getName().getSimpleName()}<#list class.getSupertypes() as s>
  <#if s?is_first> extends <#else>, </#if>${s.getSimpleName()}</#list> {
}
```

### Copying annotations from source to generated code

The `@c.copyAnnotations` macro in `common.ftl` handles annotation copying:

```freemarker
<@c.copyAnnotations class 0/>     <!-- indent level 0 -->
<@c.copyAnnotations method 1/>    <!-- indent level 1 -->
```

## Scope Selection

The adaptor's `getScope()` method determines what data is passed into the template:

```java
@Override
public Scope[] getScope() {
    return new Scope[] { Scope.INTERFACE, Scope.METHOD };
}
```

Each scope value causes `doGenerate()` to be called separately. A single adaptor can generate multiple outputs from the same model:

- **MODEL** — one call with `model` in root map, generates one file per adaptor
- **PACKAGE** — one call per sub-package, generates one file per adaptor per package
- **INTERFACE** — one call per interface, generates one file per interface
- **METHOD** — one call per method, generates one file per method

## Template File Location

Templates must be on the classpath:

- **Built-ins** — place in `runtime/src/main/resources/freemarker/` or `plugin/src/main/resources/freemarker/`
- **Plugins** — place in the same directory as the adaptor class or alongside it in `resources/`

When using the Maven plugin (`javapig-maven-plugin`), templates in the plugin jar are automatically discoverable via the adaptor's classpath.
