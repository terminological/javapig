package uk.co.terminological.javapig.test.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;



public interface DataBean {
	int getField1();
	float getField2();
	String getField3();
	Optional<String> getFieldRename();
	BasicBean[] getArrayOfBeans();
	List<BasicBean> getlistOfBeans();
	ArrayList<BasicBean> getArrayListOfBeans();
	Set<String> getSetOfStrings();
	
	default String lc3() {return getField3().toLowerCase();}
}
