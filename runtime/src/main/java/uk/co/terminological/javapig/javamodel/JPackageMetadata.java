package uk.co.terminological.javapig.javamodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.annotations.Model;

public class JPackageMetadata implements Serializable {
	
	public JPackageMetadata(
			//List<JTemplateMetadata> templates, 
			List<BuiltIn> builtIns,
			//File templateDirectory) {
			List<String> plugins) {
		//this.templates = templates;
		this.builtIns = builtIns;
		this.setPlugins(plugins);
		//this.templateDirectory = templateDirectory;
	}

	//private List<JTemplateMetadata> templates = new ArrayList<JTemplateMetadata>();
	private List<BuiltIn> builtIns = new ArrayList<>();
	private List<String> plugins = new ArrayList<>();
	//private File templateDirectory;

	/*public List<JTemplateMetadata> getTemplates() {
		return templates;
	}

	public void setTemplates(List<JTemplateMetadata> templates) {
		this.templates = templates;
	}
	
	public File getDirectory() {
		return templateDirectory;
	}

	public void setDirectory(File templateDirectory) {
		this.templateDirectory = templateDirectory;
	}*/

	public List<BuiltIn> getBuiltIns() {
		return builtIns;
	}
	
	public boolean isEnabled(String builtInName) {
		return builtIns.stream().anyMatch(b -> b.toString().equals(builtInName));
	}

	public JPackageMetadata clone() {
		return new JPackageMetadata(
				//this.getTemplates().stream().map(tm -> tm.clone()).collect(Collectors.toList()),
				this.getBuiltIns(),
				//this.getDirectory()
				this.getPlugIns()
				);
	}

	public JPackageMetadata copy() {
		return this;
	}
	
	public static JPackageMetadata from (Model ann) {
		return new JPackageMetadata(
				/*Stream.of(ann.templates()).map(
						template -> new JTemplateMetadata(
								template.classnameTemplate(),
								template.appliesTo(),
								template.filename(),
								template.extension(),
								template.adaptor())).collect(Collectors.toList()),*/
				Arrays.asList(ann.builtins()),
				//new File(ann.directory()));
				Arrays.asList(ann.plugins()));
	}

	public List<String> getPlugIns() {
		return plugins;
	}

	public void setPlugins(List<String> plugins) {
		this.plugins = plugins;
	}
}
