package uk.co.terminological.javapig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapperBuilder;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import uk.co.terminological.javapig.annotations.BuiltIn;
import uk.co.terminological.javapig.annotations.Scope;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModel;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JTemplateMetadata;

public class JModelWriter {

	private Filer filer;
	private File target;
	private Configuration cfg;
	private JModel model;

	public JModelWriter() {};
	
	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.javamodel.tools.JModelWriter#setFiler(javax.annotation.processing.Filer)
	 */
	public void setFiler(Filer filer) {
		this.filer = filer;
	}

	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.javamodel.tools.JModelWriter#setTargetDirectory(java.io.File)
	 */
	public void setTargetDirectory(File targetDirectory) {
		this.target = targetDirectory;
	}
	
	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.javamodel.tools.JModelWriter#setModel(uk.co.terminological.javapig.javamodel.JModel)
	 */
	public void setModel(JModel model) {
		this.model = model;
	}

	/* (non-Javadoc)
	 * @see uk.co.terminological.javapig.javamodel.tools.JModelWriter#write()
	 */
	public void write() {
		write(model);
	}
	
	private void write(JModel model) {

		if (target == null) throw new RuntimeException("No target directory has been set");
		
		/* see BuiltIn.DEBUG for alternate way of doing this
		try {
			PrintWriter pw = new PrintWriter(new FileOutputStream(new File(target,"debug.txt")));
			pw.print(
					model.accept(new JModelToString()).collect(Collectors.joining("\n\n"))
					);
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.out.println("Could not write debug file to: "+target.getAbsolutePath()+"/debug.txt");
		}*/
		
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);

		// Use the updated JModel model to generate code based on 
		// the freemarker templates. This is done both on a package by package 
		// basis using the PackageTemplates, and on a class by class basis
		// using ClassTemplates. This is done based on the annotations held in 
		// the @Model(@Template) annotation

		for (JPackage pack: model.getPackages()) {

			for (JTemplateMetadata template: pack.getMetadata().getTemplates()) {				
				doGenerate(template, model, pack);
			}

			for (BuiltIn builtin: pack.getMetadata().getBuiltIn()) {
				doGenerate(builtin, model, pack);
			}

		}
	}


	private void doGenerate(JTemplateMetadata template, JModel model, JPackage pkg) {
		try {
			cfg.setDirectoryForTemplateLoading(pkg.getMetadata().getDirectory());
			JModel tmpModel = template.getAdaptor(JModel.class).adapt(model);
			Template tmp = cfg.getTemplate(template.getTemplateFilename());
			doGenerate(template.getScope(), template.getClassNameTemplate(), tmp, tmpModel, pkg, template.getExtension());
		} catch (IOException e) {
			System.out.println("failed to load freemarker template: "+template.getTemplateFilename());
		}
	}

	private void doGenerate(BuiltIn template, JModel model, JPackage pkg) {
		try {
			cfg.setClassForTemplateLoading(BuiltIn.class, "/freemarker");
			JModel tmpModel = template.getAdaptor(JModel.class).adapt(model);
			Template tmp = cfg.getTemplate(template.getFilename());
			doGenerate(template.getScope(), template.getClassnameTemplate(), tmp, tmpModel, pkg, template.getExtension());
		} catch (IOException e) {
			System.out.println("failed to load freemarker template: "+template.getFilename());
		}
	}

	private void doGenerate(Scope[] scopes, String classNameTemplate, Template tmp, JModel model, JPackage pkg, String extension) {
		for (Scope scope: scopes) {
			switch (scope) {
			case METHOD:
				for (JInterface cls: pkg.getClasses()) {
					for (JGetMethod m: cls.getMethods()) {
						doGenerate(scope,classNameTemplate, tmp,model,pkg,cls,m, extension);
					}
				}
				break;
			case INTERFACE:
				for (JInterface cls: pkg.getClasses()) {
					doGenerate(scope,classNameTemplate, tmp,model,pkg,cls,null, extension);
				}
				break;
			case PACKAGE:
				doGenerate(scope,classNameTemplate, tmp,model,pkg,null,null, extension);
				break;
			case MODEL:
				doGenerate(scope,classNameTemplate, tmp,model,pkg,null,null, extension);
			}
		}
	}

	private void doGenerate(Scope scope, String classNameTemplate, Template tmp, JModel model, JPackage pkg, JInterface cl, JGetMethod m, String extension) {
		String classname = classNameTemplate;
		Map<String,Object> root = new HashMap<String,Object>();

		switch (scope) {
		case METHOD:
			classname = classname.replace("${method}", m.getName().className());
			root.put("method", m);
		case INTERFACE:
			classname = classname.replace("${classFQN}", cl.getName().getCanonicalName());
			classname = classname.replace("${class}", cl.getName().getSimpleName());
			root.put("class", cl);
		case PACKAGE:
			classname = classname.replace("${package}", pkg.getName());
			root.put("package", pkg);
		case MODEL:
			classname = classname.replace("${rootPackage}", model.getRootPackage());
			root.put("model", model);

		}
		root.put("fqn", classname);
		root.put("classname", classname.substring(classname.lastIndexOf(".")+1));
		root.put("packagename", classname.substring(0,classname.lastIndexOf(".")).toLowerCase());
		root.put("rootPackage", model.getRootPackage());
		doGenerate(classname, tmp, root, extension);
	}

	// Utility to handle the freemarker generation mechanics.
	private void doGenerate(String classname, Template tmp, Map<String,Object> root, String extension ) {

		try {

			Writer out;
			if (filer != null) {
				try {
					FileObject file;
					if (!extension.equals("java")) {
						file = filer.createResource(StandardLocation.SOURCE_OUTPUT, root.get("packagename").toString(), root.get("classname")+"."+extension, (Element[]) null);
						System.out.println("Writing class: "+classname+" to "+file.toUri().toURL().getFile());
					} else {
						file = filer.createSourceFile(classname);
						System.out.println("Writing resource: "+classname+" to "+file.toUri().toURL().getFile());
					}
					out = file.openWriter();
				
				} catch (FilerException e) {
					
					// This usually happens when the filer has created a source file already in this run
					// unfortunately we do need to write the same file twice if there are dependencies
					// on generated code in the source code, so we have to work around this issue
					
					File file = targetFilePath(target,classname, extension);
					System.out.println("Forcing update: "+classname+" to "+file.getAbsolutePath());
					file.delete();
					out = new PrintWriter(new FileOutputStream(file));
					
				}
			} else {
				
				//This will happen if the generator is triggered from the maven plugin rather than 
				//nby the annotation processor.
				
				File file = targetFilePath(target,classname, extension);
				out = new PrintWriter(new FileOutputStream(file));
				System.out.println("Writing class: "+classname+" to "+file.getAbsolutePath());
				
			}
			tmp.process(root, out);
			out.close();

			
			
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		} catch (TemplateException e) {
			System.out.println("Error in freemarker template: "+classname);
			System.out.println("==============================");
			// Freemarker will let you know what the problem was
		}
	}

	private File targetFilePath(File directory, String fqn, String extension) {
		File out = new File(directory,fqn.replace(".", "/")+"."+extension);
		out.getParentFile().mkdirs();
		return out;
	}

}
