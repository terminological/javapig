package uk.co.terminological.javapig.javamodel;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.javamodel.tools.JModelComponent;

public class JPackageMetadata extends JModelComponent {
	
	private ArrayList<JTemplateMetadata> templates = new ArrayList<JTemplateMetadata>();
	private ArrayList<BuiltIn> builtIns = new ArrayList<>();
	private File templateDirectory;

	public ArrayList<JTemplateMetadata> getTemplates() {
		return templates;
	}

	public File getDirectory() {
		return templateDirectory;
	}

	public void setDirectory(File templateDirectory) {
		this.templateDirectory = templateDirectory;
	}

	public List<BuiltIn> getBuiltIn() {
		return builtIns;
	}
	
	public boolean isEnabled(String builtInName) {
		return builtIns.stream().anyMatch(b -> b.toString().equals(builtInName));
	}
}
