package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.List;

import uk.co.terminological.javapig.javamodel.tools.JModelComponent;

public class JAnnotationEntry extends JModelComponent {

	private JMethodName method;
	private List<JAnnotationValue<?>> values = new ArrayList<>();
	
	public JMethodName getMethod() {
		return method;
	}
	public void setMethod(JMethodName method) {
		this.method = method;
	}
	public List<JAnnotationValue<?>> getValues() {
		return values;
	}
	public void setValues(List<JAnnotationValue<?>> values) {
		this.values = values;
	}
		
	/*public static JAnnotationEntry with(JMethodName method, JAnnotationValue<?> value) {
		JAnnotationEntry out =  new JAnnotationEntry();
		out.method = method;
		out.values.add(value);
		return out;
	}*/
	
	public static JAnnotationEntry with(JMethodName method, List<JAnnotationValue<?>> values) {
		JAnnotationEntry out =  new JAnnotationEntry();
		out.method = method;
		out.values.addAll(values);
		return out;
	}
	
}
