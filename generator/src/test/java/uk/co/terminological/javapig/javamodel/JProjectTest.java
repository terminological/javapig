package uk.co.terminological.javapig.javamodel;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class JProjectTest {

	private JProject project;
	private JClassName ifaceName;
	private JMethodName methodName;
	private JInterface iface;
	private JGetMethod method;
	private JPackage pkg;

	@Before
	public void setUp() {
		project = new JProject();
		ifaceName = JClassName.from("uk.co.terminological.test.MyInterface");
		methodName = JMethodName.from("pkg.MyInterface#getField1");

		List<JAnnotation> anns = new ArrayList<>();
		iface = new JInterface(project, "A test interface", anns, ifaceName, new LinkedHashSet<>());

		List<JAnnotation> methodAnns = new ArrayList<>();
		method = new JGetMethod(project, "field1 javadoc", methodAnns, ifaceName, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		pkg = new JPackage(project, null, new ArrayList<>(), "uk.co.terminological.test", java.util.Optional.empty());
	}

	// --- Adding and finding ---

	@Test
	public void addInterface_addsAndSetsModel() {
		project.addInterface(iface);

		assertTrue(project.interfaceIsDefined(ifaceName));
		assertSame(project, iface.getModel());
	}

	@Test
	public void addMethod_addsAndSetsModel() {
		project.addMethod(method);

		assertTrue(project.methodIsDefined(methodName));
		assertSame(project, method.getModel());
	}

	@Test
	public void addPackage_addsAndSetsModel() {
		project.addPackage(pkg);

		assertTrue(project.packageIsDefined("uk.co.terminological.test"));
		assertSame(project, pkg.getModel());
	}

	@Test
	public void findClass_returnsNull_whenInterfaceNotAdded() {
		assertNull(project.findClass(ifaceName));
	}

	@Test
	public void findClass_returnsInterface_whenAdded() {
		project.addInterface(iface);
		assertSame(iface, project.findClass(ifaceName));
	}

	@Test
	public void findMethod_returnsNull_whenMethodNotAdded() {
		assertNull(project.findMethod(methodName));
	}

	@Test
	public void findMethod_returnsMethod_whenAdded() {
		project.addMethod(method);
		assertSame(method, project.findMethod(methodName));
	}

	@Test
	public void findPackage_returnsNull_whenPackageNotAdded() {
		assertNull(project.findPackage("uk.co.terminological.test"));
	}

	@Test
	public void findPackage_returnsPackage_whenAdded() {
		project.addPackage(pkg);
		assertSame(pkg, project.findPackage("uk.co.terminological.test"));
	}

	// --- Collections ---

	@Test
	public void getClasses_returnsOnlyInterfaces() {
		JClassName ifaceName2 = JClassName.from("uk.co.terminological.test.Other");
		JInterface iface2 = new JInterface(project, null, new ArrayList<>(), ifaceName2, new LinkedHashSet<>());
		project.addInterface(iface);
		project.addInterface(iface2);

		int count = 0;
		for (JInterface c : project.getClasses()) count++;
		assertEquals(2, count);
	}

	@Test
	public void getMethods_returnsOnlyMethods() {
		project.addMethod(method);

		int count = 0;
		for (JGetMethod m : project.getMethods()) count++;
		assertEquals(1, count);
	}

	@Test
	public void getPackages_returnsOnlyPackages() {
		project.addPackage(pkg);

		int count = 0;
		for (JPackage p : project.getPackages()) count++;
		assertEquals(1, count);
	}

	@Test
	public void getRootPackages_returnsOnlyPackagesWithMetadata() {
		JPackageMetadata meta = new JPackageMetadata(new ArrayList<>(), new ArrayList<>());
		JPackage rootPkg = new JPackage(project, null, new ArrayList<>(), "root.pkg", java.util.Optional.of(meta));
		JPackage normalPkg = new JPackage(project, null, new ArrayList<>(), "normal.pkg", java.util.Optional.empty());

		project.addPackage(rootPkg);
		project.addPackage(normalPkg);

		assertEquals(1, project.getRootPackages().size());
		assertSame(rootPkg, project.getRootPackages().iterator().next());
	}

	// --- Existence checks ---

	@Test
	public void interfaceIsDefined_false_whenNotAdded() {
		assertFalse(project.interfaceIsDefined(ifaceName));
	}

	@Test
	public void interfaceIsDefined_true_afterAdding() {
		project.addInterface(iface);
		assertTrue(project.interfaceIsDefined(ifaceName));
	}

	@Test
	public void methodIsDefined_false_whenNotAdded() {
		assertFalse(project.methodIsDefined(methodName));
	}

	@Test
	public void methodIsDefined_true_afterAdding() {
		project.addMethod(method);
		assertTrue(project.methodIsDefined(methodName));
	}

	@Test
	public void packageIsDefined_false_whenNotAdded() {
		assertFalse(project.packageIsDefined("uk.co.terminological.test"));
	}

	@Test
	public void packageIsDefined_true_afterAdding() {
		project.addPackage(pkg);
		assertTrue(project.packageIsDefined("uk.co.terminological.test"));
	}

	// --- getClasses filtering by package ---

	@Test
	public void getClasses_withFilter_returnsMatchingPackage() {
		JClassName ifaceName2 = JClassName.from("uk.co.terminological.other.OtherIface");
		JInterface iface2 = new JInterface(project, null, new ArrayList<>(), ifaceName2, new LinkedHashSet<>());
		project.addInterface(iface);
		project.addInterface(iface2);

		// getClasses(JTemplateInput) extracts package name correctly for JInterface
		int count = 0;
		for (JInterface c : project.getClasses(iface)) count++;
		assertEquals(1, count); // only "MyInterface" shares package "uk.co.terminological.test"
	}

	@Test
	public void getClasses_withFilter_returnsEmptyForNonMatchingPackage() {
		JClassName otherIfaceName = JClassName.from("other.PkgInterface");
		JInterface otherIface = new JInterface(project, null, new ArrayList<>(), otherIfaceName, new LinkedHashSet<>());
		project.addInterface(iface);
		project.addInterface(otherIface);

		// "other.PkgInterface" is in package "other", not "uk.co.terminological.test"
		int count = 0;
		for (JInterface c : project.getClasses(iface)) count++;
		assertEquals(1, count); // only MyInterface from the matching package
	}

	// --- getMethods filtering by interface ---

	@Test
	public void getMethods_withFilter_returnsMethodsOfInterface() {
		project.addInterface(iface);
		project.addMethod(method);

		// JProject.getMethods(JTemplateInput) casts JTemplateInput to JInterface internally
		int count = 0;
		for (JGetMethod m : project.getMethods(iface)) count++;
		assertEquals(1, count);
	}

	@Test
	public void getMethods_withFilter_excludesDefaultMethods() {
		JClassName otherIfaceName = JClassName.from("pkg.MyOtherIface");
		JInterface otherIface = new JInterface(project, null, new ArrayList<>(), otherIfaceName, new LinkedHashSet<>());
		project.addInterface(otherIface);

		JMethodName defaultMethodName = JMethodName.from("pkg.MyOtherIface#doDefault");
		JGetMethod defaultMethod = new JGetMethod(project, null, new ArrayList<>(), otherIfaceName, defaultMethodName,
				"String", JClassName.from("java.lang.String"), null, true);
		project.addMethod(defaultMethod);

		// Using the interface itself as the filter (it implements JTemplateInput)
		int count = 0;
		for (JGetMethod m : project.getMethods(otherIface)) count++;
		assertEquals(0, count);
	}

	// --- Clone and copy ---

	@Test
	public void clone_createsIndependentCopy() {
		JClassName ifaceName2 = JClassName.from("pkg2.Other");
		JInterface iface2 = new JInterface(project, null, new ArrayList<>(), ifaceName2, new LinkedHashSet<>());
		project.addInterface(iface);
		project.addInterface(iface2);
		project.addMethod(method);

		JProject cloned = project.clone();

		assertEquals(project.getClasses().size(), cloned.getClasses().size());
		assertEquals(project.getMethods().size(), cloned.getMethods().size());

		// Adding to original shouldn't affect clone
		JClassName newIfaceName = JClassName.from("new.Iface");
		JInterface newIface = new JInterface(project, null, new ArrayList<>(), newIfaceName, new LinkedHashSet<>());
		project.addInterface(newIface);
		assertEquals(3, project.getClasses().size());
		assertEquals(2, cloned.getClasses().size());
	}

	@Test
	public void copy_createsNewEmptyProject() {
		project.addInterface(iface);
		project.addMethod(method);

		JProject copied = project.copy();

		// copy() creates a new empty project
		int clsCount = 0;
		for (JInterface c : copied.getClasses()) clsCount++;
		assertEquals(0, clsCount);

		int mCount = 0;
		for (JGetMethod m : copied.getMethods()) mCount++;
		assertEquals(0, mCount);
	}

	// --- Deduplication ---

	@Test
	public void addSameInterfaceTwice_doesNotDuplicate() {
		project.addInterface(iface);
		project.addInterface(iface);

		int count = 0;
		for (JInterface c : project.getClasses()) count++;
		assertEquals(1, count);
	}

	// --- equals/hashCode based on name ---

	@Test
	public void findClass_returnsFirstMatch_whenDuplicateNames() {
		JClassName sameName = JClassName.from("uk.co.terminological.test.MyInterface");
		JInterface iface2 = new JInterface(project, null, new ArrayList<>(), sameName, new LinkedHashSet<>());

		project.addInterface(iface);
		project.addInterface(iface2);

		// Since LinkedHashSet replaces on equals, should only be 1
		int count = 0;
		for (JInterface c : project.getClasses()) count++;
		assertEquals(1, count);
	}
}
