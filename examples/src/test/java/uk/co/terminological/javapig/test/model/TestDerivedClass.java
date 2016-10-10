package uk.co.terminological.javapig.test.model;

public class TestDerivedClass {

	public static void main(String[] args) {
		DataBean imp = Factory.Immutable.createDataBean().withField3("A Value").build();
		System.out.println(imp.lc3());
	}
	
}
