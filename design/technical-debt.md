# Technical Debt

Open issues, design smells, and known deficiencies in the codebase.

---

## Generator Module

### `JProject.getMethods(JTemplateInput)` — Missing transitive supertype resolution
**Severity:** High
**Status:** Fixed in revision

The `getMethods(JTemplateInput)` method filtered methods against only the **direct** supertypes of the input interface, missing methods declared on grandparent interfaces in a deeper inheritance chain.

**Before (buggy):**
```java
iface.getSupertypes().contains(m.getDeclaringClass().getName())
```

**After (fixed):** A new `collectAllSupertypes()` helper recursively walks the supertype hierarchy using a `LinkedHashSet` to avoid cycles, then filters against the full set.

**Impact:** Any interface hierarchy with 3+ levels (e.g. `A extends B extends C`) would lose methods declared on `C` when querying via `A`. Affected code generation for any interface with deep inheritance.

**Design note:** The cast `(JInterface) in` is a contract assumption — `JTemplateInput` is the interface, but callers may pass other implementations in the future.

---

### `JProject.getMethods(JTemplateInput)` — Cast from `JTemplateInput` to `JInterface`
**Severity:** Medium
**Status:** Known

```java
JInterface iface = (JInterface) in;
```

This is a hard assumption. `JTemplateInput` is a broader contract; in practice all callers are `JInterface` instances, but this cast could fail if the API is used differently in the future.

---

## Scanner Module

### `AptJModelBuilder.visitType()` does not handle `@` in annotation values
**Severity:** Low
**Status:** Open

When processing annotation values containing array types with `@`, the scanner may produce malformed class names.

---

### Scanner: `QDoxJModelBuilder` vs `AptJModelBuilder` vs `ReflectionUtils` — Three implementations
**Severity:** Medium
**Status:** Open (mitigated)

Three parallel scanning implementations exist with divergent filtering logic. QDoxJModelBuilder now has 4 integration tests covering the canonical behavior (no-param, non-void, non-default, non-Generated filtering). AptJModelBuilder should be verified to match this behavior. ReflectionJModelBuilder is dead code (commented "not up to date", never called, ~130 LOC).

---

## Runtime Module

### `JTemplateInput.isInPackage()` uses `startsWith` on canonical name
**Severity:** Medium
**Status:** Fixed

**Bug:** Used `this.getName().getCanonicalName().startsWith(pkg.getCanonicalName())`, which incorrectly matched `com.example.terminological` against `com.example.terminologi` (prefix match).

**Fix:** Replaced with exact package extraction — splits the canonical name at the last `.` to get the actual package, then uses `equals()`.

**Impact:** Affects generator filtering in `JPackage.getClasses()`, `JPackage.getPackages()`, `JPackage.getMethods()` (used in `JModelWriter` for class/method/package inclusion in generated output).

---

### `StringCaster.cast(byte, ...)` casts to `Character` not `Byte`
**Severity:** High
**Status:** Fixed

---

## Plugin Module

### `SqlMojo.scanModel()` hardcodes `src/main/java` and `src/main/resources` paths
**Severity:** Low
**Status:** Open

The SQL loader plugin's `JavaFromSqlMojo` hardcodes Maven source paths rather than using the Maven plugin API's `sourceDirectory` configuration.

---

## Examples Module

### No unit tests for core code generation logic
**Severity:** High
**Status:** Open

The examples module contains integration-style tests (`PluginTests.java`, `TestReadV2Load.java`, `TestMavenPlugin.java`) that test end-to-end via Mojo execution, but the core code generation — template rendering, interface scanning, method resolution — has no automated test coverage beyond the generator/runtime unit tests.

### `TestMavenPlugin` and `TestReadV2Load` use log4j 1.x (deprecated)
**Severity:** Low
**Status:** Open

Both test files import `org.apache.log4j.*` (log4j 1.x), which is EOL and a security concern. Should migrate to log4j 2.x or SLF4J.

---

## General

### JUnit 4.10 in examples pom.xml
**Severity:** Low
**Status:** Open

The examples module uses JUnit 4.10 — an old version from 2012. Consider upgrading to 4.13.2 for compatibility with newer JVMs (4.10 uses `sun.misc.Unsafe::objectFieldOffset` which is removed in JDK 24+).

### Maven Surefire plugin 2.12.4
**Severity:** Low
**Status:** Open

Using surefire 2.12.4 in parent pom — very old. Consider upgrading to 3.x for better parallel test execution and Java 21+ compatibility.
