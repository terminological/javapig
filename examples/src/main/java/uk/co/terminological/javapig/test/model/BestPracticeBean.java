package uk.co.terminological.javapig.test.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface BestPracticeBean {

	String getString();
	Integer getInteger();
	Optional<Float> getPossibleFloat();
	BasicBean getBean();
	Set<BasicBean> getSetOfBeans();
	List<String> getListOfStrings();
	
}
