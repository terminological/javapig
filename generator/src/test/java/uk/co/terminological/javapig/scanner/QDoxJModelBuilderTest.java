package uk.co.terminological.javapig.scanner;

import java.io.File;
import java.util.List;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.junit.Test;
import static org.junit.Assert.*;

import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JMethodName;

public class QDoxJModelBuilderTest {

	/**
	 * Test case 1: Basic package scan
	 * Verify that a single package with one interface and two getters is correctly scanned.
	 */
	@Test
	public void basicPackageScan() {
		File sourceDir = new File("src/test/resources/uk/co/terminological/javapig/scanner/testdata");
		JProject project = QDoxJModelBuilder.scanModel(new File[] { sourceDir });
		
		Collection<JPackage> packages = project.getPackages();
		assertTrue("Should have at least one package", packages.size() > 0);
		
		JPackage pkg = packages.iterator().next();
		assertEquals("Should be the testdata package", "uk.co.terminological.javapig.scanner.testdata", pkg.getName().getSimpleName());
		
		Set<JInterface> interfaces = pkg.getClasses();
		assertTrue("Should have at least one interface", interfaces.size() > 0);
		
		// Find SimpleInterface
		Optional<JInterface> simpleIface = interfaces.stream()
			.filter(i -> i.getName().getSimpleName().equals("SimpleInterface"))
			.findFirst();
		assertTrue("Should find SimpleInterface", simpleIface.isPresent());
		
		JInterface simple = simpleIface.get();
		List<JGetMethod> methods = simple.getMethods();
		assertEquals("SimpleInterface should have exactly 2 methods", 2, methods.size());
	}
	
	/**
	 * Test case 2: Method filtering - parameters excluded
	 * Methods with parameters should be filtered out.
	 */
	@Test
	public void methodFilteringParameters() {
		File sourceDir = new File("src/test/resources/uk/co/terminological/javapig/scanner/testdata");
		JProject project = QDoxJModelBuilder.scanModel(new File[] { sourceDir });
		
		Collection<JPackage> packages = project.getPackages();
		JPackage pkg = packages.iterator().next();
		
		Optional<JInterface> complexIface = pkg.getClasses().stream()
			.filter(i -> i.getName().getSimpleName().equals("ComplexInterface"))
			.findFirst();
		assertTrue("Should find ComplexInterface", complexIface.isPresent());
		
		List<JGetMethod> methods = complexIface.get().getMethods();
		
		// Should have getValidName, getValidCount, getOptionalField, getItems (all no-param, non-void, non-default)
		assertEquals("ComplexInterface should have exactly 4 valid methods", 4, methods.size());
		
		// Verify the specific methods
		Set<String> methodNames = new java.util.HashSet<String>();
		for (JGetMethod m : methods) {
			methodNames.add(m.getName().getSimpleName());
		}
		assertTrue("Should contain getValidName", methodNames.contains("getValidName"));
		assertTrue("Should contain getValidCount", methodNames.contains("getValidCount"));
		assertTrue("Should contain getOptionalField", methodNames.contains("getOptionalField"));
		assertTrue("Should contain getItems", methodNames.contains("getItems"));
		assertFalse("Should NOT contain setSomething", methodNames.contains("setSomething"));
		assertFalse("Should NOT contain calculate", methodNames.contains("calculate"));
		assertFalse("Should NOT contain getNameById", methodNames.contains("getNameById"));
		assertFalse("Should NOT contain getDefaultMethod", methodNames.contains("getDefaultMethod"));
	}
	
	/**
	 * Test case 3: Generated annotation exclusion
	 * Interfaces annotated with javax.annotation.Generated should be excluded.
	 */
	@Test
	public void generatedAnnotationExclusion() {
		File sourceDir = new File("src/test/resources/uk/co/terminological/javapig/scanner/testdata");
		JProject project = QDoxJModelBuilder.scanModel(new File[] { sourceDir });
		
		Collection<JPackage> packages = project.getPackages();
		JPackage pkg = packages.iterator().next();
		
		Optional<JInterface> genIface = pkg.getClasses().stream()
			.filter(i -> i.getName().getSimpleName().equals("GeneratedInterface"))
			.findFirst();
		
		assertTrue("GeneratedInterface should NOT be present (excluded by @Generated)", !genIface.isPresent());
	}
	
	/**
	 * Test case 4: Verify all expected interfaces are present
	 */
	@Test
	public void allInterfacesPresent() {
		File sourceDir = new File("src/test/resources/uk/co/terminological/javapig/scanner/testdata");
		JProject project = QDoxJModelBuilder.scanModel(new File[] { sourceDir });
		
		Collection<JPackage> packages = project.getPackages();
		JPackage pkg = packages.iterator().next();
		
		Set<String> interfaceNames = new java.util.HashSet<String>();
		for (JInterface iface : pkg.getClasses()) {
			interfaceNames.add(iface.getName().getSimpleName());
		}
		
		// SimpleInterface and ComplexInterface should be present
		assertTrue("Should contain SimpleInterface", interfaceNames.contains("SimpleInterface"));
		assertTrue("Should contain ComplexInterface", interfaceNames.contains("ComplexInterface"));
		assertTrue("Should contain AnnotatedInterface", interfaceNames.contains("AnnotatedInterface"));
		// GeneratedInterface should NOT be present
		assertFalse("Should NOT contain GeneratedInterface", interfaceNames.contains("GeneratedInterface"));
	}
}
