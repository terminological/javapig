package uk.co.terminological.javapig.javamodel;

import java.util.Collection;

/**
 * The JModel holds the structure of the basic domain model and provides that
 * to the template mechanism
 * @see JModelImpl
 * @author terminological
 *
 */
public interface Project {
	
	public boolean interfaceIsDefined(JClassName fqn);
	public boolean methodIsDefined(JMethodName fqn);
	public boolean packageIsDefined(String fqn);
	
	public Collection<? extends JTemplateInput> getClasses();
	public Collection<? extends JTemplateInput> getMethods();
	public Collection<? extends JTemplateInput> getPackages();
	public Collection<? extends JTemplateInput> getRootPackages();
	
	public Collection<? extends JTemplateInput> getClasses(JTemplateInput pckge);
	public Collection<? extends JTemplateInput> getMethods(JTemplateInput iface);
	
	
	public JTemplateInput findClass(JClassName classname);
	public JTemplateInput findMethod(JMethodName methodname);
	public JTemplateInput findPackage(String fqn);

	
}
