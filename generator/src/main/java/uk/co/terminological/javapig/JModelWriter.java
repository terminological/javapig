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

/**
 * Generates Java source files from a {@link Project} metamodel using FreeMarker templates.
 *
 * <p>The writer iterates over root packages, applies each registered {@link JModelAdaptor}
 * (built-in or plugin), and renders the adaptor's template for every element (model,
 * package, interface, or method) that matches the adaptor's scope and filter.</p>
 *
 * <p>Two execution modes are supported:</p>
 * <ul>
 *   <li><b>Filer mode</b> — when a {@link Filer} is set, output is written through the
 *       annotation processing API (used when javapig runs inside an annotation processor).</li>
 *   <li><b>File mode</b> — when no filer is set, output is written directly to the target
 *       directory using standard file I/O (used when javapig runs via the Maven plugin).</li>
 * </ul>
 *
 * @see JModelMojo
 * @see JModelProcessor
 * @see JModelAdaptor
 * @see BuiltIn
 * @see Scope
 */
public class JModelWriter {

	private Filer filer;
	private File target;
	private Configuration cfg;
	private Project model;
	private boolean overwrite;

	/**
	 * Constructs a writer with no configuration. All fields must be set before
	 * calling {@link #write()}.
	 */
	public JModelWriter() {};

	/**
	 * Sets the {@link Filer} used for annotation-processor-based output.
	 * When a filer is present, generated files are written through the JCP API.
	 *
	 * @param filer the annotation processing filer
	 * @see #setTargetDirectory(File)
	 */
	public void setFiler(Filer filer) {
		this.filer = filer;
	}

	/**
	 * Sets the directory where generated files will be written when no filer is available.
	 *
	 * @param targetDirectory the output directory
	 */
	public void setTargetDirectory(File targetDirectory) {
		this.target = targetDirectory;
	}

	/**
	 * Sets the project metamodel to generate code for.
	 *
	 * @param model the scanned metamodel containing interfaces, methods, and packages
	 */
	public void setModel(Project model) {
		this.model = model;
	}

	/**
	 * Writes generated code for all registered adaptors across all root packages.
	 * Uses overwrite mode ({@code true}), creating or replacing existing files.
	 *
	 * @throws RuntimeException if no target directory has been set
	 * @see #writeSafe()
	 */
	public void write() {
		this.overwrite = true;
		write(model);
	}

	/**
	 * Writes generated code for all registered adaptors across all root packages,
	 * skipping any files that already exist (safe mode).
	 *
	 * @see #write()
	 */
	public void writeSafe() {
		this.overwrite = false;
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
				JPackage tmpRootPack = (JPackage) tmpModel.findPackage(rootPack.getName().toString());

				System.out.println("Root package container: "+tmpRootPack.getName().toString());
				//doGenerate(adaptor, tmpModel, tmpRootPack, tmpRootPack);
				
				for (JTemplateInput pack: tmpRootPack.getPackages()) {

					System.out.println("Actual package: "+pack.getName().toString());
					doGenerate(adaptor, tmpModel, tmpRootPack, pack);
				}


			}
		}
	}


	/**
	 * Loads and dispatches the adaptor's FreeMarker template for the given package.
	 *
	 * @param adaptor the model adaptor providing the template and scope
	 * @param model the (potentially adapted) metamodel
	 * @param rootPkg the root package for scope resolution
	 * @param pkg the package to generate code for
	 */
	private void doGenerate(JModelAdaptor adaptor, Project model, JTemplateInput rootPkg, JTemplateInput pkg) {
		try {
			if (adaptor instanceof BuiltIn) {
				cfg.setClassForTemplateLoading(BuiltIn.class, "/freemarker");
			} else {
				cfg.setClassForTemplateLoading(adaptor.getClass(),"");
			}
			Template tmp = cfg.getTemplate(adaptor.getTemplateFilename());
			System.out.println("using template: "+adaptor.getTemplateFilename());
			doGenerate(adaptor, tmp, model, rootPkg, pkg);
		} catch (IOException e) {
			System.out.println("failed to load freemarker template: "+adaptor.getTemplateFilename());
		}
	}

	/**
	 * Iterates over the adaptor's scopes and calls {@link #doGenerate} for each matching element
	 * (methods, interfaces, or packages) within the target package.
	 *
	 * @param adaptor the adaptor controlling scope, filtering, and template selection
	 * @param model the metamodel
	 * @param rootPkg the root package used for package-name filtering
	 * @param pkg the current package being generated
	 */
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

	/**
	 * Renders a FreeMarker template for a single element (method, interface, package, or model)
	 * and writes the output file.
	 *
	 * <p>The adaptor's class-name template is populated using placeholders ({@code ${method}},
	 * {@code ${class}}, {@code ${classFQN}}, {@code ${package}}, {@code ${rootPackage}})
	 * and the result is used as the fully-qualified output class name.</p>
	 *
	 * @param adaptor the adaptor providing template and naming configuration
	 * @param scope the element scope being processed
	 * @param tmp the FreeMarker template
	 * @param model the metamodel
	 * @param rootPkg the root package
	 * @param pkg the current package
	 * @param cl the interface element (null for non-interface scopes)
	 * @param m the method element (null for non-method scopes)
	 */
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
			root.put("model", model);
		}
		root.put("fqn", classname);
		root.put("classname", classname.substring(classname.lastIndexOf(".")+1));
		root.put("packagename", classname.substring(0,classname.lastIndexOf(".")).toLowerCase());
		root.put("rootPackage", rootPkg.getName().toString());
		doGenerate(classname, tmp, root, adaptor.getExtension());
	}

	/**
	 * Writes the rendered template output to a file, using either the annotation processing
	 * {@link Filer} API or direct file I/O depending on whether a filer is configured.
	 *
	 * @param classname the fully-qualified class name for the output file
	 * @param tmp the rendered FreeMarker template
	 * @param root the template data model
	 * @param extension the file extension (e.g. {@code "java"})
	 */
	// Utility to handle the freemarker generation mechanics.
	private void doGenerate(String classname, Template tmp, Map<String,Object> root, String extension ) {

		try {

			Writer out;

			// the filer is available in the context of a annotation processor execution.
			// if javapig is excuted from maven we will find out where to write stuff ourselves.

			if (filer != null) {
				try {
					FileObject file;
					File filename = targetFilePath(target,classname, extension);
					if (!filename.exists() || overwrite == true) {
						if (!extension.equals("java")) {
							file = filer.createResource(StandardLocation.SOURCE_OUTPUT, root.get("packagename").toString(), root.get("classname")+"."+extension, (Element[]) null);
							System.out.println("Writing class: "+classname+" to "+file.toUri().toURL().getFile());
						} else {
							file = filer.createSourceFile(classname);
							System.out.println("Writing resource: "+classname+" to "+file.toUri().toURL().getFile());
						}
						out = file.openWriter();
						tmp.process(root, out);
						out.close();
					} else {
						System.out.println("Safe mode: skipping pre-existing file: "+filename.getAbsolutePath());
					}

				} catch (FilerException e) {

					// This usually happens when the filer has created a source file already in this run
					// unfortunately we do need to write the same file twice if there are dependencies
					// on generated code in the source code, so we have to work around this issue

					File file = targetFilePath(target,classname, extension);
					if (!file.exists() || overwrite == true) {
						System.out.println("Forcing update: "+classname+" to "+file.getAbsolutePath());
						file.delete();
						out = new PrintWriter(new FileOutputStream(file));
						tmp.process(root, out);
						out.close();
					} else {
						System.out.println("Safe mode: skipping pre-existing file: "+file.getAbsolutePath());
					}

				}
			} else {

				//This will happen if the generator is triggered from the maven plugin rather than 
				//nby the annotation processor.

				File file = targetFilePath(target,classname, extension);
				if (!file.exists() || overwrite == true) {
					out = new PrintWriter(new FileOutputStream(file));
					System.out.println("Writing class: "+classname+" to "+file.getAbsolutePath());
					tmp.process(root, out);
					out.close();
				} else {
					System.out.println("Safe mode: skipping pre-existing file: "+file.getAbsolutePath());
				}

			}


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

	/**
	 * Resolves the file path for a generated class by converting its fully-qualified name
	 * into a directory structure under the target directory.
	 *
	 * @param directory the root output directory
	 * @param fqn the fully-qualified class name
	 * @param extension the file extension (e.g. {@code "java"})
	 * @return the target file path
	 */
	private File targetFilePath(File directory, String fqn, String extension) {
		File out = new File(directory,fqn.replace(".", "/")+"."+extension);
		out.getParentFile().mkdirs();
		return out;
	}

}
