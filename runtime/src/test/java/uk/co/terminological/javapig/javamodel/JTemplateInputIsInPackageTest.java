package uk.co.terminological.javapig.javamodel;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests for JTemplateInput.isInPackage() — verifies package-name matching uses
 * exact equality, not prefix matching (which caused the same bug as the old
 * JProject.getClasses(JTemplateInput) startsWith issue).
 */
public class JTemplateInputIsInPackageTest {

	/** Helper to create a JName from a string. */
	private static JName jName(final String name) {
		return new JName() {
			@Override public String getClassName() { return name; }
			@Override public String getCanonicalName() { return name; }
			@Override public String getSimpleName() { return name; }
			@Override public String toString() { return name; }
		};
	}

	/** Create a JTemplateInput whose name is the given string. */
	private static JTemplateInput input(String className) {
		return new JTemplateInput() {
			@Override public JName getName() { return jName(className); }
			@Override public JPackageMetadata getMetadata() { return null; }
		};
	}

	/**
	 * Core correctness test: a class in a *longer* package name should NOT
	 * match a shorter prefix package name.
	 *
	 * Before fix: "com.example.terminological".startsWith("com.example.terminologi") == true  (bug)
	 * After fix:  false  (correct)
	 */
	@Test
	public void isInPackage_prefixPackage_doesNotMatchLongerPackage() {
		JTemplateInput longInput = input("com.example.terminological.MyClass");
		JTemplateInput shortInput = input("com.example.terminologi.MyClass");
		JName shorterPkg = jName("com.example.terminologi");

		// The class in the LONGER package should NOT match the SHORTER package name
		assertFalse("com.example.terminological.MyClass should NOT match package com.example.terminologi",
				longInput.isInPackage(shorterPkg));

		// The class in the exact package SHOULD match
		assertTrue("com.example.terminologi.MyClass SHOULD match package com.example.terminologi",
				shortInput.isInPackage(shorterPkg));
	}

	/**
	 * Verify exact match works correctly.
	 */
	@Test
	public void isInPackage_exactMatch_returnsTrue() {
		JTemplateInput input = input("org.example.MyClass");
		JName pkg = jName("org.example");
		assertTrue("org.example.MyClass should match package org.example",
				input.isInPackage(pkg));
	}

	/**
	 * Verify completely different package returns false.
	 */
	@Test
	public void isInPackage_differentPackage_returnsFalse() {
		JTemplateInput input = input("org.example.MyClass");
		JName otherPkg = jName("com.other");
		assertFalse("org.example.MyClass should NOT match package com.other",
				input.isInPackage(otherPkg));
	}

	/**
	 * Subpackage should NOT match parent (e.g. org.example.sub does not match org.example).
	 */
	@Test
	public void isInPackage_subpackageDoesNotMatchParent() {
		JTemplateInput input = input("org.example.sub.MyClass");
		JName parentPkg = jName("org.example");
		assertFalse("org.example.sub.MyClass should NOT match parent package org.example",
				input.isInPackage(parentPkg));
	}
}
