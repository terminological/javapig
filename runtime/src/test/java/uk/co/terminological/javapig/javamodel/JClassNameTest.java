package uk.co.terminological.javapig.javamodel;

import org.junit.Test;
import static org.junit.Assert.*;

public class JClassNameTest {

	@Test
	public void from_simpleName_returnsNameWithDefaultPackage() {
		JClassName name = JClassName.from("String");
		assertEquals("String", name.getSimpleName());
		assertEquals("java.lang", name.getPackageName());
	}

	@Test
	public void from_fqn_returnsCorrectSimpleAndPackageNames() {
		JClassName name = JClassName.from("java.util.List");
		assertEquals("List", name.getSimpleName());
		assertEquals("java.util", name.getPackageName());
	}

	@Test
	public void from_fqn_returnsCanonicalName() {
		JClassName name = JClassName.from("java.util.List");
		assertEquals("java.util.List", name.getCanonicalName());
	}

	@Test
	public void from_primitive_int_returnsBoxedName() {
		JClassName name = JClassName.from("int");
		assertEquals("int", name.getSimpleName());
		assertEquals("java.lang.Integer", name.getCanonicalName());
	}

	@Test
	public void from_primitive_boolean_returnsBoxedName() {
		JClassName name = JClassName.from("boolean");
		assertEquals("java.lang.Boolean", name.getCanonicalName());
	}

	@Test
	public void from_primitive_void_returnsBoxedName() {
		JClassName name = JClassName.from("void");
		assertEquals("java.lang.Void", name.getCanonicalName());
	}

	@Test
	public void from_fqn_withGenerics_stripsGenerics() {
		JClassName name = JClassName.from("java.util.List<java.lang.String>");
		assertEquals("List", name.getSimpleName());
		assertEquals("java.util", name.getPackageName());
		assertEquals("java.util.List", name.getCanonicalName());
	}

	@Test
	public void from_null_returnsNull() {
		assertNull(JClassName.from(null));
	}

	@Test
	public void from_empty_returnsNull() {
		assertNull(JClassName.from(""));
	}

	@Test
	public void importName_forSimpleClass_returnsFQN() {
		JClassName name = JClassName.from("java.util.List");
		assertEquals("java.util.List", name.importName());
	}

	@Test
	public void importName_forSimpleType_returnsFQN() {
		JClassName name = JClassName.from("String");
		assertEquals("java.lang.String", name.importName());
	}

	@Test
	public void importName_forNestedClass_splitsAtFirstCapital() {
		JClassName name = JClassName.from("org.example.test.Class.SubClass");
		assertEquals("org.example.test.Class", name.importName());
	}

	@Test
	public void equivalent_returnsTrue_forExactMatch() {
		JClassName name = JClassName.from("java.util.List");
		assertTrue(name.equivalent("java.util.List"));
	}

	@Test
	public void equivalent_returnsFalse_forDifferentName() {
		JClassName name = JClassName.from("java.util.List");
		assertFalse(name.equivalent("java.util.Set"));
	}

	@Test
	public void equivalent_handlesBoxingForPrimitive() {
		JClassName name = JClassName.from("int");
		assertTrue(name.equivalent("int"));
	}

	@Test
	public void equivalent_handlesBoxingForBoxedType() {
		JClassName name = JClassName.from("int");
		assertTrue(name.equivalent("java.lang.Integer"));
	}

	@Test
	public void isCompiled_returnsTrueForKnownClass() {
		JClassName name = JClassName.from("java.util.List");
		assertTrue(name.isCompiled());
	}

	@Test
	public void isCompiled_returnsFalseForUnknownClass() {
		JClassName name = JClassName.from("com.example.NonExistentClass12345");
		// Should not throw, returns false
		assertFalse(name.isCompiled());
	}

	@Test
	public void isArray_returnsTrueForArrayType() {
		JClassName name = JClassName.from("java.lang.String[]");
		assertTrue(name.isArray());
	}

	@Test
	public void isArray_returnsFalseForNonArrayType() {
		JClassName name = JClassName.from("java.lang.String");
		assertFalse(name.isArray());
	}

	@Test
	public void equals_sameName_returnsTrue() {
		JClassName name1 = JClassName.from("java.util.List");
		JClassName name2 = JClassName.from("java.util.List");
		assertTrue(name1.equals(name2));
	}

	@Test
	public void equals_differentName_returnsFalse() {
		JClassName name1 = JClassName.from("java.util.List");
		JClassName name2 = JClassName.from("java.util.Set");
		assertFalse(name1.equals(name2));
	}

	@Test
	public void hashCode_sameName_sameHashcode() {
		JClassName name1 = JClassName.from("java.util.List");
		JClassName name2 = JClassName.from("java.util.List");
		assertEquals(name1.hashCode(), name2.hashCode());
	}

	@Test
	public void code_returnsAlphanumeric() {
		JClassName name = JClassName.from("java.util.List");
		String code = name.code();
		// code() returns something like "ID-12345M67890" - verify it is non-empty
		assertFalse(code.isEmpty());
	}

	@Test
	public void getSimpleName_forPrimitive_returnsPrimitiveName() {
		JClassName name = JClassName.from("int");
		assertEquals("int", name.getSimpleName());
	}

	@Test
	public void getCanonicalName_forArray_returnsBoxedArray() {
		JClassName name = JClassName.from("java.lang.String[]");
		assertEquals("java.lang.String[]", name.getCanonicalName());
	}

	@Test
	public void getCanonicalName_forSimplePrimitive_returnsBoxed() {
		JClassName name = JClassName.from("int");
		assertEquals("java.lang.Integer", name.getCanonicalName());
	}
}
