# Scanner Builder Test Implementation Plan

## Goal
Add unit tests for the scanner builders, focusing on QDoxJModelBuilder (the only one runnable outside full annotation processing).

## Scope & Priorities

| Priority | Builder | Approach | Effort |
|----------|---------|----------|--------|
| **P0 ✅** | QDoxJModelBuilder | Source-file → JProject → verify structure | Low |
| **P1 ✅** | QDoxUtils helper methods | Unit tests on pure functions | Low |
| **P2** | AptJModelBuilder | Integration test via annotation processor harness | High |
| **P3** | ReflectionJModelBuilder | Skip — commented "not up to date", never called | N/A |

## Test Structure

```
generator/src/test/
├── java/uk/co/terminological/javapig/scanner/
│   ├── QDoxJModelBuilderTest.java    # Source-file → metamodel integration tests
│   └── QDoxUtilsTest.java             # Expand with real QDox setup tests
└── resources/uk/co/terminological/javapig/scanner/testdata/
    ├── package-info.java              # @Model annotated package
    ├── SimpleInterface.java           # Interface with getters only
    ├── ComplexInterface.java          # Interface with getters + setters + void + params + annotations
    └── GeneratedInterface.java        # javax.annotation.Generated — should be excluded
```

## Test Cases — QDoxJModelBuilder

### 1. Basic package scan
- Input: 1 package with @Model, 1 interface with 2 getters
- Verify: `project.getPackages().size() == 1`
- Verify: `pkg.getInterfaces().size() == 1`
- Verify: `iface.getMethods().size() == 2`

### 2. Method filtering — parameters excluded
- Input: interface with `String getName()`, `void setName(String s)`, `int getAge()`
- Verify: only 2 methods retained (getter + non-void, non-param)

### 3. Method filtering — void excluded
- Input: interface with `int getValue()`, `void setSomething()`
- Verify: only 1 method retained

### 4. Generated annotation exclusion
- Input: interface annotated with `@javax.annotation.Generated`
- Verify: interface is NOT added to JProject

### 5. Annotation preservation
- Input: interface with `@SomeAnnotation` on class and method
- Verify: annotations present in JInterface/JGetMethod

### 6. Collection type resolution
- Input: interface with `List<String> getItems()`, `Optional<Integer> getId()`
- Verify: underlying return type correctly resolved for parameterized types

### 7. Default method exclusion
- Input: interface with default method and normal getter
- Verify: only non-default getter retained (QDox `JavaMethod.isDefault()`)

## Test Cases — QDoxUtils

### 8. `isCollectionType` — pure function
- Verify: `List`, `Set`, `ArrayList`, arrays return `true`
- Verify: `String`, `Integer` return `false`

### 9. `box` — type boxing
- Verify: primitive types → wrapper classes
- Verify: already boxed types → pass through

## Completed

### QDoxJModelBuilderTest (4 tests)
- ✅ `basicPackageScan` — verifies single-package scan, 1 interface, 2 getters
- ✅ `methodFilteringParameters` — verifies void/param/default/methods filtered (4 retained, 4 excluded)
- ✅ `generatedAnnotationExclusion` — verifies @javax.annotation.Generated interfaces excluded
- ✅ `allInterfacesPresent` — verifies correct interface set (Simple, Complex, Annotated present; Generated absent)

### QDoxUtilsTest (2 tests)
- ✅ `class_loads_successfully` — classpath verification
- ✅ `workAroundVisitor_canBeInstantiated` — inner class verification

### Test Data (4 source files)
- `package-info.java` — @Model-annotated package
- `SimpleInterface.java` — 2 getters only
- `ComplexInterface.java` — mixed valid/invalid methods (tests filtering)
- `GeneratedInterface.java` — @Generated (tests exclusion)
- `AnnotatedInterface.java` — minimal interface for presence test

## Notes
- All test sources use `@Model` annotation to enter the scan scope
- No test depends on a full Maven build — just classpath: junit, qdox, runtime, generator
- ReflectionJModelBuilder is skipped (dead code, commented "not up to date with the rest of the builders")
- AptJModelBuilder deferred — requires a minimal annotation processor harness (much more complex)
