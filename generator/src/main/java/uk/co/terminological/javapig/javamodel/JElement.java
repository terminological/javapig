package uk.co.terminological.javapig.javamodel;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.co.terminological.javapig.javamodel.tools.JModelComponent;

public class JElement extends JModelComponent {

	private JModel model;
	private List<JAnnotation> annotations = new ArrayList<>();
	private String javaDoc;
	
	public <T extends Annotation> boolean isAnnotationPresent(Class<T> annotation) {
		return getAnnotation(annotation) != null;
	}
	
	public <T extends Annotation> boolean isAnnotationPresent(String annotation) {
		return annotations.stream()
				.filter(a -> annotation.endsWith(a.getName()))
				.findAny().isPresent();
	}
	
	public <T extends Annotation> T getAnnotation(Class<T> annotation) {
		Optional<JAnnotation> ann = annotations.stream()
				.filter(a -> annotation.getCanonicalName().endsWith(a.getName()))
				.findFirst().map(a -> (JAnnotation) a);
		if (!ann.isPresent()) return null;
		else {
			JAnnotation tmp = ann.get();
			return tmp.convert(annotation);
		}
	}

	public List<JAnnotation> getAnnotations() {
		return annotations;
	}

	@SuppressWarnings("unchecked")
	public <T extends Annotation> T getAnnotation(String annotation) throws ClassNotFoundException {
		return getAnnotation((Class<T>) Class.forName(annotation));
	}
	
	public void setAnnotations(List<JAnnotation> annotations) {
		this.annotations = annotations;
	}

	public JModel getModel() {
		return model;
	}

	public void setModel(JModel model) {
		this.model = model;
	}

	public String getJavaDoc() {
		return javaDoc;
	}

	public void setJavaDoc(String javaDoc) {
		this.javaDoc = javaDoc;
	}
	
	
}
