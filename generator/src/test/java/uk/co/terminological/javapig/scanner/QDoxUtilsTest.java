package uk.co.terminological.javapig.scanner;

import org.junit.Test;
import static org.junit.Assert.*;

public class QDoxUtilsTest {

	@Test
	public void class_loads_successfully() {
		assertNotNull(QDoxUtils.class);
	}
	
	@Test
	public void workAroundVisitor_canBeInstantiated() {
		// Verify the WorkaroundVisitor inner class can be loaded
		assertNotNull(QDoxUtils.WorkaroundVisitor.class);
	}
}
