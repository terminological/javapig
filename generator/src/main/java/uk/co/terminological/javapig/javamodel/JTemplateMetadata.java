package uk.co.terminological.javapig.javamodel;

import java.util.Arrays;

import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.javamodel.tools.JModelComponent;

public class JTemplateMetadata  extends JModelComponent {
	
	
	private String templateFilename;
	private Scope[] scope;
	private String classNameTemplate;
	private String extension;
	private String adaptor;
	
	public String getTemplateFilename() {
		return templateFilename;
	}
	public void setTemplateFilename(String templateFilename) {
		this.templateFilename = templateFilename;
	}
	public Scope[] getScope() {
		return scope;
	}
	public void setScope(Scope[] scope) {
		this.scope = scope;
	}
	public String getClassNameTemplate() {
		return classNameTemplate;
	}
	public void setClassNameTemplate(String classNameTemplate) {
		this.classNameTemplate = classNameTemplate;
	}
	public boolean inScope(Scope scope) {
		return Arrays.asList(this.scope).contains(scope);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((templateFilename == null) ? 0 : templateFilename
						.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		JTemplateMetadata other = (JTemplateMetadata) obj;
		if (templateFilename == null) {
			if (other.templateFilename != null)
				return false;
		} else if (!templateFilename.equals(other.templateFilename))
			return false;
		return true;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	@SuppressWarnings("unchecked")
	public <X> JModelAdaptor<X> getAdaptor(Class<X> input) {
		try {
			return (JModelAdaptor<X>) Class.forName(adaptor).newInstance();
		} catch (InstantiationException | IllegalAccessException | ClassCastException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	public void setAdaptor(String adaptor) {
		this.adaptor = adaptor;
	}
	
}