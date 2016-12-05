package uk.co.terminological.javapig;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import uk.co.terminological.javapig.javamodel.Project;
import uk.co.terminological.javapig.javamodel.JModelAdaptor;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JTemplateInput;

public class JModelWriter {

	private Filer filer;
	private File target;
	private Configuration cfg;
	private Project model;

	public JModelWriter() {};
	
	public void setFiler(Filer filer) {
		this.filer = filer;
	}

	public void setTargetDirectory(File targetDirectory) {
		this.target = targetDirectory;
	}
	
	public void setModel(Project model) {
		this.model = model;
	}

	public void write() {
		write(model);
	}
	
	private void write(Project model) {

		if (target == null) throw new RuntimeException("No target directory has been set");
		
		cfg = new Configuration(Configuration.VERSION_2_3_25);
		cfg.setObjectWrapper(new DefaultObjectWrapperBuilder(Configuration.VERSION_2_3_25).build());
		cfg.setDefaultEncoding("UTF-8");
		cfg.setTemplateExceptionHandler(TemplateExceptionHandler.DEBUG_HANDLER);

		
		for (JTemplateInput rootPack : model.getRootPackages()) {
			
			List<JModelAdaptor> userDefined = new ArrayList<>(); 
			userDefined.addAll(rootPack.getMetadata().getBuiltIns());
			for (String clsName: rootPack.getMetadata().getPlugIns()) {
				try {
					userDefined.add((JModelAdaptor) Class.forName(clsName).newInstance());
				} catch (InstantiationException | IllegalAccessException | ClassCastException | ClassNotFoundException e) {
					throw new RuntimeException("Plugin :"+clsName+" cannot be resolved",e);
				}
			}
			
			for (JModelAdaptor adaptor: userDefined) {
				
				Project tmpModel = adaptor.adapt(model);
				JTemplateInput tmpRootPack = tmpModel.findPackage(rootPack.getName().toString());
			
				for (JTemplateInput pack: ((JPackage) tmpRootPack).getPackages()) {

					doGenerate(adaptor, tmpModel, tmpRootPack, pack);
				}


			}
		}
	}


	private void doGenerate(JModelAdaptor adaptor, Project model, JTemplateInput rootPkg, JTemplateInput pkg) {
		try {
			if (adaptor instanceof BuiltIn) {
				cfg.setClassForTemplateLoading(BuiltIn.class, "/freemarker");
			} else {
				cfg.setClassForTemplateLoading(adaptor.getClass(),"");
			}
			Template tmp = cfg.getTemplate(adaptor.getTemplateFilename());
			doGenerate(adaptor, tmp, model, rootPkg, pkg);
		} catch (IOException e) {
			System.out.println("failed to load freemarker template: "+adaptor.getTemplateFilename());
		}
	}

	private void doGenerate(JModelAdaptor adaptor, Template tmp, Project model, JTemplateInput rootPkg, JTemplateInput pkg) {
		for (Scope scope: adaptor.getScope()) {
			switch (scope) {
			case METHOD:
				for (JTemplateInput cls: model.getClasses(pkg)) {
					for (JTemplateInput m: model.getMethods(cls)) {
						if (adaptor.filter(m) && m.isInPackage(rootPkg.getName())) {
							doGenerate(adaptor, scope, tmp,model,rootPkg,pkg,cls,m);
						}
					}
				}
				break;
			case INTERFACE:
				for (JTemplateInput cls: model.getClasses(pkg)) {
					if (adaptor.filter(cls) && cls.isInPackage(rootPkg.getName())) {
						doGenerate(adaptor, scope, tmp,model,rootPkg,pkg,cls,null);
					}
				}
				break;
			case PACKAGE:
				if (adaptor.filter(pkg) && pkg.isInPackage(rootPkg.getName())) {
					doGenerate(adaptor, scope, tmp,model,rootPkg,pkg,null,null);
				}
				break;
			case MODEL:
				doGenerate(adaptor, scope, tmp,model,rootPkg,pkg,null,null);
			}
		}
	}

	private void doGenerate(JModelAdaptor adaptor, Scope scope, Template tmp, Project model, JTemplateInput rootPkg, JTemplateInput pkg, JTemplateInput cl, JTemplateInput m) {
		String classname = adaptor.getClassNameTemplate();
		Map<String,Object> root = new HashMap<String,Object>();

		switch (scope) {
		case METHOD:
			classname = classname.replace("${method}", m.getName().getClassName());
			root.put("method", m);
		case INTERFACE:
			classname = classname.replace("${classFQN}", cl.getName().getCanonicalName());
			classname = classname.replace("${class}", cl.getName().getSimpleName());
			root.put("class", cl);
		case PACKAGE:
			classname = classname.replace("${package}", pkg.getName().toString());
			root.put("package", pkg);
		case MODEL:
			classname = classname.replace("${rootPackage}", rootPkg.getName().toString());
			root.put("model", rootPkg);
		}
		root.put("fqn", classname);
		root.put("classname", classname.substring(classname.lastIndexOf(".")+1));
		root.put("packagename", classname.substring(0,classname.lastIndexOf(".")).toLowerCase());
		root.put("rootPackage", rootPkg.getName().toString());
		doGenerate(classname, tmp, root, adaptor.getExtension());
	}

	// Utility to handle the freemarker generation mechanics.
	private void doGenerate(String classname, Template tmp, Map<String,Object> root, String extension ) {

		try {

			Writer out;
			
			// the filer is available in the context of a annotation processor execution.
			// if javapig is excuted from maven we will find out where to write stuff ourselves.
			
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
			// this should not happen. 
			
		} catch (TemplateException e) {
			System.out.println("Error in freemarker template: "+classname);
			System.out.println("==============================");
			// Freemarker will let you know what the problem was and generate a
			// file that contains a stacktrace. This will most likely crash the 
			// compiler
		}
	}

	private File targetFilePath(File directory, String fqn, String extension) {
		File out = new File(directory,fqn.replace(".", "/")+"."+extension);
		out.getParentFile().mkdirs();
		return out;
	}

}
