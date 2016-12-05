package uk.co.terminological.javapig.test.model;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.persistence.Id;

public interface BestPracticeBean {

	String getString();
	@Id Integer getInteger();
	Optional<Float> getPossibleFloat();
	BasicBean getBean();
	Set<BasicBean> getSetOfBeans();
	List<String> getListOfStrings();
	
}
