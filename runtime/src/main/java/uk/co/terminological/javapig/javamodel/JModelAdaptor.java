package uk.co.terminological.javapig.javamodel;

import uk.co.terminological.javapig.annotations.Scope;

/**
 * The model adaptors can be defined in the package level annotations to allow additional functionality to be bought into the
 * model before the templating is done. This is part of the extension mechanism for javapig. An implementing class can be passed as
 * the adaptor parameter of an Template annotation and the adapt method is called just before the template is executed
 * 
 * @see {@link uk.co.terminological.javapig.annotations.Template}
 *
 * @author terminological
 *
 * @param <X> - the target type of the adaptor
 */
public interface JModelAdaptor {
	
	/**
	 * The adapt method must make provide a means of transforming a JModel instance into another
	 * Typically this will second JModel be a subtype which provides additional functionality compared to the 
	 * provided vanilla JModel
	 * @param model
	 * @return
	 */
	public Project adapt(Project model);
	
	
	/**
	 * the filter function will be called on all {@link JPackage}s, {@link JInterface}s, or {@link JGetMethod}s passed as top level 
	 * inputs to the template writer. These can be filtered out based on their properties. 
	 * 
	 * @param input - {@link JModelImpl}s, {@link JPackage}s, {@link JInterface}s, or {@link JGetMethod}s
	 * @return if this function returns true for an input that is included in this round of processing
	 */
	public boolean filter(JTemplateInput input);
	
	public Scope[] getScope();

	public String getTemplateFilename();

	/**
	 * The classname template defines the FQN of the resulting class and the file name of the resulting output file. 
	 * The template can have some variables which are replace<br/>
	 * <li>${method} - a classname which is like the method being inspected. e.g. getSimplePOJO() gives SimplePOJO
	 * <li>${classFQN} - the FQN of the interface being inspected
	 * <li>${class} - the simple name of the interface being inspected
	 * <li>${package} - the package being inspected
	 * <li>${rootPackage} - the logical parent package which contains the {@link uk.co.terminological.javapig.annotations.Model} annotation
	 * <br/>
	 * depending on the {@link uk.co.terminological.javapig.annotations.Scope} of the template some of these substitutions will not be
	 * applied. For example an INTERFACE level template will not have a single value of ${method} to use as a substitute
	 * 
	 * @return a FQN string used both to determine where the template output is written but also what what it is called
	 * 
	 */
	public String getClassNameTemplate();

	public String getExtension();
}
