package uk.co.terminological.javapig.scanner.testdata;

import java.util.List;
import java.util.Optional;

/**
 * Complex interface with mixed method types.
 */
public interface ComplexInterface {
	// Valid getters - should be retained
	String getValidName();
	
	int getValidCount();
	
	Optional<String> getOptionalField();
	
	// Should be filtered out: void return
	void setSomething(String value);
	
	// Should be filtered out: has parameters
	int calculate(int x, int y);
	
	// Should be filtered out: has parameters
	String getNameById(String id);
	
	// Should be filtered out: default method
	default String getDefaultMethod() {
		return "default";
	}
	
	// Collection type tests
	List<String> getItems();
}
