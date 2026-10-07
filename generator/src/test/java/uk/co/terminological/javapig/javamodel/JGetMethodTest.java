package uk.co.terminological.javapig.javamodel;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class JGetMethodTest {

	private JProject project;
	private JClassName ifaceName;
	private JMethodName methodName;
	private JClassName declaringClass;
	private JInterface iface;

	@Before
	public void setUp() {
		project = new JProject();
		ifaceName = JClassName.from("pkg.MyInterface");
		methodName = JMethodName.from("pkg.MyInterface#getField1");
		declaringClass = JClassName.from("pkg.MyInterface");

		List<JAnnotation> anns = new ArrayList<>();
		iface = new JInterface(project, "My interface", anns, ifaceName, new LinkedHashSet<>());
		project.addInterface(iface);
	}

	// --- Basic construction ---

	@Test
	public void constructor_setsAllFields() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, "javadoc text", anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		assertEquals("javadoc text", method.getJavaDoc());
		assertEquals(methodName, method.getName());
		assertEquals("String", method.getReturnTypeDefinition());
		assertEquals("java.lang.String", method.getReturnType().toString());
		assertNull(method.getUnderlyingType());
		assertEquals(false, method.isDefault());
	}

	// --- getName ---

	@Test
	public void getName_returnsName() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertEquals(methodName, method.getName());
	}

	// --- declaredBy ---

	@Test
	public void declaredBy_returnsTrue_whenMethodBelongsToInterface() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		project.addMethod(method);
		assertTrue(method.declaredBy(iface));
	}

	@Test
	public void declaredBy_returnsFalse_whenMethodBelongsToDifferentInterface() {
		JClassName otherIfaceName = JClassName.from("pkg.OtherInterface");
		JInterface otherIface = new JInterface(project, null, new ArrayList<>(), otherIfaceName, new LinkedHashSet<>());
		project.addInterface(otherIface);

		JMethodName otherMethodName = JMethodName.from("pkg.OtherInterface#getField1");
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, otherIfaceName, otherMethodName,
				"String", JClassName.from("java.lang.String"), null, false);
		project.addMethod(method);
		assertFalse(method.declaredBy(iface));
	}

	// --- getDeclaringClass ---

	@Test
	public void getDeclaringClass_returnsInterface() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		project.addMethod(method);
		assertSame(iface, method.getDeclaringClass());
	}

	@Test
	public void getDeclaringClass_returnsNull_whenInterfaceNotInModel() {
		JClassName unknownClass = JClassName.from("pkg.Unknown");
		JMethodName unknownMethod = JMethodName.from("pkg.Unknown#getField1");
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, unknownClass, unknownMethod,
				"String", JClassName.from("java.lang.String"), null, false);
		assertNull(method.getDeclaringClass());
	}

	// --- isPrimitive ---

	@Test
	public void isPrimitive_returnsTrue_forInt() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"int", JClassName.from("java.lang.Integer"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forBoolean() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"boolean", JClassName.from("java.lang.Boolean"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forDouble() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"double", JClassName.from("java.lang.Double"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forChar() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"char", JClassName.from("java.lang.Character"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forLong() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"long", JClassName.from("java.lang.Long"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forByte() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"byte", JClassName.from("java.lang.Byte"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forShort() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"short", JClassName.from("java.lang.Short"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsTrue_forVoid() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"void", JClassName.from("java.lang.Void"), null, false);
		assertTrue(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsFalse_forString() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertFalse(method.isPrimitive());
	}

	@Test
	public void isPrimitive_returnsFalse_forObject() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"Object", JClassName.from("java.lang.Object"), null, false);
		assertFalse(method.isPrimitive());
	}

	// --- isParameterised ---

	@Test
	public void isParameterised_returnsTrue_whenUnderlyingTypeSet() {
		List<JAnnotation> anns = new ArrayList<>();
		JClassName underlying = JClassName.from("pkg.MyBean");
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"List", JClassName.from("java.util.List"), underlying, false);
		assertTrue(method.isParameterised());
	}

	@Test
	public void isParameterised_returnsFalse_whenUnderlyingTypeNull() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertFalse(method.isParameterised());
	}

	// --- cardinality ---

	@Test
	public void cardinality_returnsOneDotDotOneForNonGeneric() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertEquals("1..1", method.cardinality());
	}

	@Test
	public void cardinality_returnsZeroDotDotOneForOptional() {
		List<JAnnotation> anns = new ArrayList<>();
		JClassName underlying = JClassName.from("java.lang.String");
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"Optional", JClassName.from("java.util.Optional"), underlying, false);
		try {
			assertEquals("0..1", method.cardinality());
		} catch (Exception e) {
			fail("cardinality should work for Optional types: " + e.getMessage());
		}
	}

	// --- getInterfaceType ---

	@Test
	public void getInterfaceType_returnsDefinitionForPrimitive() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"int", JClassName.from("java.lang.Integer"), null, false);
		assertEquals("int", method.getInterfaceType());
	}

	@Test
	public void getInterfaceType_returnsSimpleTypeForNonGeneric() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertEquals("String", method.getInterfaceType());
	}

	// --- getImplementationType ---

	@Test
	public void getImplementationType_returnsSimpleTypeForNonGeneric() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertEquals("String", method.getImplementationType());
	}

	// --- getImports ---

	@Test
	public void getImports_returnsReturnTypeImport() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		int count = 0;
		for (String imp : method.getImports()) count++;
		assertEquals(1, count);
	}

	// --- equals and hashCode ---

	@Test
	public void equals_sameName_returnsTrue() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod m1 = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		JMethodName sameMethod = JMethodName.from("pkg.MyInterface#getField1");
		JGetMethod m2 = new JGetMethod(project, null, anns, declaringClass, sameMethod,
				"int", JClassName.from("java.lang.Integer"), null, false);
		assertTrue(m1.equals(m2));
	}

	@Test
	public void equals_differentName_returnsFalse() {
		List<JAnnotation> anns = new ArrayList<>();
		JMethodName otherMethod = JMethodName.from("pkg.MyInterface#getOther");
		JGetMethod m1 = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		JGetMethod m2 = new JGetMethod(project, null, anns, declaringClass, otherMethod,
				"String", JClassName.from("java.lang.String"), null, false);
		assertFalse(m1.equals(m2));
	}

	@Test
	public void equals_null_returnsFalse() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertFalse(method.equals(null));
	}

	@Test
	public void equals_sameName_sameHashCode() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod m1 = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		JMethodName sameMethod = JMethodName.from("pkg.MyInterface#getField1");
		JGetMethod m2 = new JGetMethod(project, null, anns, declaringClass, sameMethod,
				"int", JClassName.from("java.lang.Integer"), null, false);
		assertEquals(m1.hashCode(), m2.hashCode());
	}

	// --- Setter tests ---

	@Test
	public void setName_updatesMethodName() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		JMethodName newName = JMethodName.from("pkg.MyInterface#getNewName");
		method.setName(newName);
		assertEquals(newName, method.getName());
	}

	@Test
	public void setReturnType_updatesReturnType() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		JClassName newType = JClassName.from("java.lang.Integer");
		method.setReturnType(newType);
		assertSame(newType, method.getReturnType());
	}

	@Test
	public void setReturnTypeDefinition_updatesDefinition() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		method.setReturnTypeDefinition("Integer");
		assertEquals("Integer", method.getReturnTypeDefinition());
	}

	@Test
	public void setDefault_updatesDefaultFlag() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		method.setDefault(true);
		assertTrue(method.isDefault());
	}

	// --- Clone ---

	@Test
	public void clone_createsNewInstanceWithSameFields() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		Object cloned = method.clone();
		assertNotSame(method, cloned);
		JGetMethod c = (JGetMethod) cloned;
		assertEquals(method.getName(), c.getName());
		assertEquals(method.getReturnTypeDefinition(), c.getReturnTypeDefinition());
		assertEquals(method.isDefault(), c.isDefault());
	}

	@Test
	public void clone_includesAnnotations() {
		List<JAnnotationEntry> entries = new ArrayList<>();
		JAnnotation ann = new JAnnotation(java.util.Optional.of("pkg.MyAnn"), "MyAnn", entries);
		List<JAnnotation> anns = new ArrayList<>();
		anns.add(ann);
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		Object cloned = method.clone();
		JGetMethod c = (JGetMethod) cloned;
		assertEquals(1, c.getAnnotations().size());
		assertNotSame(ann, c.getAnnotations().get(0));
	}

	// --- Copy ---

	@Test
	public void copy_createsNewInstanceWithSameFields() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);

		Object copied = method.copy();
		assertNotSame(method, copied);
		JGetMethod c2 = (JGetMethod) copied;
		assertEquals(method.getName(), c2.getName());
	}

	// --- getBoxedTypeFQN ---

	@Test
	public void getBoxedTypeFQN_returnsFQNForNonGeneric() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertEquals("java.lang.String", method.getBoxedTypeFQN());
	}

	// --- getInterfaceTypeFQN ---

	@Test
	public void getInterfaceTypeFQN_returnsFQNForNonGeneric() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"String", JClassName.from("java.lang.String"), null, false);
		assertEquals("java.lang.String", method.getInterfaceTypeFQN());
	}

	@Test
	public void getInterfaceTypeFQN_returnsPrimitiveDefinitionForPrimitive() {
		List<JAnnotation> anns = new ArrayList<>();
		JGetMethod method = new JGetMethod(project, null, anns, declaringClass, methodName,
				"int", JClassName.from("java.lang.Integer"), null, false);
		assertEquals("int", method.getInterfaceTypeFQN());
	}
}
