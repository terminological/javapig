package uk.co.terminological.javapig;

import org.junit.Test;
import static org.junit.Assert.*;

public class StringCasterTest {

	// StringCaster.cast should convert strings to their expected types
	
	@Test
	public void cast_int_returnsInteger() {
		Object result = StringCaster.cast(Integer.class, "42");
		assertEquals(Integer.class, result.getClass());
		assertEquals(42, ((Integer) result).intValue());
	}

	@Test
	public void cast_float_returnsFloat() {
		Object result = StringCaster.cast(Float.class, "3.14");
		assertEquals(Float.class, result.getClass());
		assertEquals(3.14f, (Float) result, 0.01f);
	}

	@Test
	public void cast_boolean_returnsBoolean() {
		Object result = StringCaster.cast(Boolean.class, "true");
		assertEquals(Boolean.class, result.getClass());
		assertTrue((Boolean) result);
	}

	@Test
	public void cast_string_returnsString() {
		Object result = StringCaster.cast(String.class, "hello");
		assertEquals(String.class, result.getClass());
		assertEquals("hello", (String) result);
	}

	@Test
	public void cast_short_returnsShort() {
		Object result = StringCaster.cast(Short.class, "100");
		assertEquals(Short.class, result.getClass());
		assertEquals(100, ((Short) result).shortValue());
	}

	@Test
	public void cast_long_returnsLong() {
		Object result = StringCaster.cast(Long.class, "99999");
		assertEquals(Long.class, result.getClass());
		assertEquals(99999, ((Long) result).longValue());
	}

	@Test
	public void cast_double_returnsDouble() {
		Object result = StringCaster.cast(Double.class, "2.718");
		assertEquals(Double.class, result.getClass());
		assertEquals(2.718, (Double) result, 0.001);
	}

	@Test
	public void cast_byte_returnsByte() {
		Object result = StringCaster.cast(Byte.class, "127");
		assertEquals(Byte.class, result.getClass());
		assertEquals(127, ((Byte) result).byteValue());
	}

	@Test
	public void cast_char_returnsCharacter() {
		Object result = StringCaster.cast(Character.class, "X");
		assertEquals(Character.class, result.getClass());
		assertEquals(Character.valueOf('X'), (Character) result);
	}
}
