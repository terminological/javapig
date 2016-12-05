package uk.co.terminological.javapig.javamodel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JAnnotationEntry extends JProjectComponent {

	public JAnnotationEntry(JMethodName method, List<JAnnotationValue<?>> values) {
		this.method = method;
		this.values = values;
	}
	
	public JAnnotationEntry(JAnnotationEntry copy) {
		this(
				copy.getMethod(),
				new ArrayList<>()
		);
	}

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
	@Override
	public JAnnotationEntry clone() {
		JAnnotationEntry out = copy();
		out.setValues(this.getValues().stream().map(ae -> ae.clone()).collect(Collectors.toList()));
		return out;
	}
	@Override
	public JAnnotationEntry copy() {
		return new JAnnotationEntry(this);
	}
		
	/*public static JAnnotationEntry with(JMethodName method, JAnnotationValue<?> value) {
		JAnnotationEntry out =  new JAnnotationEntry();
		out.method = method;
		out.values.add(value);
		return out;
	}*/
	
	/*public static JAnnotationEntry with(JMethodName method, List<JAnnotationValue<?>> values) {
		JAnnotationEntry out =  new JAnnotationEntry(method, values);
		return out;
	}*/
	
}
