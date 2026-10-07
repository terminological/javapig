package uk.co.terminological.javapig.javamodel;

import org.junit.Test;
import static org.junit.Assert.*;

public class JMethodNameTest {

	@Test
	public void from_methodFQN_returnsCorrectCanonicalName() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("pkg.MyClass#getField1", name.getCanonicalName());
		assertEquals("getField1", name.getSimpleName());
	}

	@Test
	public void from_methodFQN_returnsClassSimpleName() {
		JMethodName name = JMethodName.from("org.example.test.MyClass#getField1");
		assertEquals("org.example.test.MyClass#getField1", name.getCanonicalName());
	}

	@Test
	public void from_methodFQN_returnsField() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("_field1", name.field());
	}

	@Test
	public void from_methodFQN_returnsSetter() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("setField1", name.setter());
	}

	@Test
	public void from_methodFQN_returnsClassName() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("Field1", name.getClassName());
	}

	@Test
	public void from_methodFQN_returnsMethodBase() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("field1", name.methodBase());
	}

	@Test
	public void from_methodFQN_returnsWith() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("withField1", name.with());
	}

	@Test
	public void from_methodFQN_returnsAdder() {
		JMethodName name = JMethodName.from("pkg.MyClass#getListOfThings");
		assertEquals("addListOfThing", name.adder());
	}

	@Test
	public void from_methodFQN_returnsAddAll() {
		JMethodName name = JMethodName.from("pkg.MyClass#getListOfThings");
		assertEquals("addAllListOfThings", name.addAll());
	}

	@Test
	public void from_methodFQN_returnsConstant() {
		JMethodName name = JMethodName.from("pkg.MyClass#getSomeConstant");
		assertEquals("SOME_CONSTANT", name.constant());
	}

	@Test
	public void from_withIsPrefix_returnsMethodBaseWithoutIs() {
		JMethodName name = JMethodName.from("pkg.MyClass#isActive");
		assertEquals("active", name.methodBase());
	}

	@Test
	public void from_withIsPrefix_returnsSetter() {
		JMethodName name = JMethodName.from("pkg.MyClass#isActive");
		assertEquals("setActive", name.setter());
	}

	@Test
	public void from_null_returnsNull() {
		assertNull(JMethodName.from(null));
	}

	@Test
	public void from_empty_returnsNull() {
		assertNull(JMethodName.from(""));
	}

	@Test
	public void from_withoutHash_throwsRuntimeException() {
		try {
			JMethodName.from("pkg.MyClass");
			fail("Should throw RuntimeException");
		} catch (RuntimeException e) {
			// Expected
		}
	}

	@Test
	public void equals_sameMethod_returnsTrue() {
		JMethodName name1 = JMethodName.from("pkg.MyClass#getField1");
		JMethodName name2 = JMethodName.from("pkg.MyClass#getField1");
		assertTrue(name1.equals(name2));
	}

	@Test
	public void equals_differentMethod_returnsFalse() {
		JMethodName name1 = JMethodName.from("pkg.MyClass#getField1");
		JMethodName name2 = JMethodName.from("pkg.MyClass#getOther");
		assertFalse(name1.equals(name2));
	}

	@Test
	public void hashCode_sameMethod_sameHashcode() {
		JMethodName name1 = JMethodName.from("pkg.MyClass#getField1");
		JMethodName name2 = JMethodName.from("pkg.MyClass#getField1");
		assertEquals(name1.hashCode(), name2.hashCode());
	}

	@Test
	public void hashCode_differentMethod_differentHashcode() {
		JMethodName name1 = JMethodName.from("pkg.MyClass#getField1");
		JMethodName name2 = JMethodName.from("pkg.MyClass#getOther");
		// Hash codes should differ for different methods
		// (not guaranteed but highly likely)
		// We'll just check it doesn't crash
		int h1 = name1.hashCode();
		int h2 = name2.hashCode();
		assertFalse(h1 == h2);
	}

	@Test
	public void code_returnsAlphanumeric() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		String code = name.code();
		assertTrue("code should be alphanumeric: " + code, code.matches("[A-Za-z0-9]+"));
	}

	@Test
	public void getter_returnsMethodName() {
		JMethodName name = JMethodName.from("pkg.MyClass#getField1");
		assertEquals("getField1", name.getter());
	}
}
