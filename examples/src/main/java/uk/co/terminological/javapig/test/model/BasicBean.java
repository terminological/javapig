package uk.co.terminological.javapig.test.model;

import java.util.Optional;

import uk.co.terminological.javapig.annotations.Inverse;
import uk.co.terminological.javapig.test.annotation.ClassHolder;
import uk.co.terminological.javapig.test.annotation.Tester;

/**
 * @author robchallen
 * Here we are... finally a comment!
 */
@Tester
public interface BasicBean {

	String getBeanString();
	
	@ClassHolder(SomeInterface.class)
	Optional<String> getBeanString2();
	
	
	@Inverse(Model.DataBean.getArrayListOfBeans)
	DataBean getData();
	
	int getLittleNumber();
	int getAnotherNumber();
	
}
