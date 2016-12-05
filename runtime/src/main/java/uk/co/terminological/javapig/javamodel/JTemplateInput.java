package uk.co.terminological.javapig.javamodel;

public interface JTemplateInput {
	
	public JName getName();
	public JPackageMetadata getMetadata();
	
	default boolean isInPackage(JName pkg) {
		boolean tmp = this.getName().getCanonicalName().startsWith(pkg.getCanonicalName());
		//if (tmp & this.getClass().getCanonicalName().contains("JGetMethod")) 
		//	System.out.println(this.getName().getCanonicalName()+"="+pkg.getCanonicalName());
		return tmp;
	}
}
