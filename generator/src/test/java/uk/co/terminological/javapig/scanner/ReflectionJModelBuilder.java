package uk.co.terminological.javapig.scanner;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Generated;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import edu.emory.mathcs.backport.java.util.Arrays;
import uk.co.terminological.javapig.annotations.Identifier;
import uk.co.terminological.javapig.annotations.Model;
import uk.co.terminological.javapig.annotations.Template;
import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JElement;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JModel;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JTemplateMetadata;
import uk.co.terminological.javapig.javamodel.tools.JNameBuilder;

/**
 * This is not up to date with the rest of the builders
 * It is here in case it becomes needed again.
 * @author terminological
 *
 */
@Deprecated
public class ReflectionJModelBuilder {


	JModel model;


	ReflectionJModelBuilder(JModel model) {
		this.model = model;
	}

	public JModel getModel() {
		return model;
	}

	public static JModel scanModel(Package pkg) {
		ReflectionJModelBuilder out = new ReflectionJModelBuilder(new JModel());
		out.scanReflectedModel(pkg);
		return out.getModel();
	}

	private void scanReflectedModel(Package pkg) {

		List<ClassLoader> classLoadersList = new LinkedList<ClassLoader>();
		classLoadersList.add(ClasspathHelper.contextClassLoader());
		classLoadersList.add(ClasspathHelper.staticClassLoader());

		Reflections reflections = new Reflections(
				new ConfigurationBuilder()
				.setScanners(
						new SubTypesScanner(false),  // don't exclude Object.class
						new ResourcesScanner())
				.setUrls(
						ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
				.filterInputsBy(
						new FilterBuilder().include(FilterBuilder.prefix(pkg.getName()))
						)
				);

		for (Class<?> clazz: reflections.getSubTypesOf(Object.class)) {
			if (isInModel(clazz) && notGenerated(clazz)) createClass(clazz);
		}
	}

	

	private JGetMethod createMethod(Method m) {

		if (model.methodIsDefined(JNameBuilder.from(m))) return model.findMethod(JNameBuilder.from(m));

		JGetMethod out = new JGetMethod();
		out.setModel(model);
		out.setJavaDoc("");
		createAnnotations(m.getAnnotations(), out);

		out.setDeclaringClass(JNameBuilder.from(m.getDeclaringClass()));
		out.setName(JNameBuilder.from(m));
		out.setReturnType(JNameBuilder.from(m.getReturnType()));
		out.setUnderlyingType(JNameBuilder.from(ReflectionUtils.underlyingReturnType(m)));
		out.setId(m.isAnnotationPresent(Identifier.class));

		model.addMethod(out);
		return out;
	}

	private JInterface createClass(Class<?> clazz) {
		JClassName cn = JNameBuilder.from(clazz);

		JInterface out = new JInterface();
		out.setModel(model);
		out.setJavaDoc("");
		createAnnotations(clazz.getAnnotations(), out);

		out.setName(cn);

		for (Method m: clazz.getMethods()) {
			if (m.isAnnotationPresent(Identifier.class)) {
				out.setIdentity(JNameBuilder.from(m));
			}
			out.getMethods().add(this.createMethod(m));
		}

		for (Class<?> iface: clazz.getInterfaces()) {
			out.getSupertypes().add(JClassName.from(iface.getCanonicalName()));
		}

		Package pkg = clazz.getPackage();
		createPackage(pkg);

		out.setPkg(pkg.getName());

		model.addInterface(out);
		return out;
	}

	@SuppressWarnings("unchecked")
	private JPackage createPackage(Package pkg) {

		if (model.packageIsDefined(pkg.getName())) return model.findPackage(pkg.getName());

		JPackage out = new JPackage();
		out.setModel(model);
		out.setJavaDoc("");
		createAnnotations(pkg.getAnnotations(), out);	out.setName(pkg.getName());
		

		Model metadata = pkg.getAnnotation(Model.class);
		out.getMetadata().setDirectory(new File(metadata.directory()));
		for (Template template: metadata.templates()) {
			JTemplateMetadata tmp = new JTemplateMetadata();
			tmp.setClassNameTemplate(template.classnameTemplate());
			tmp.setScope(template.appliesTo());
			tmp.setTemplateFilename(template.filename());
			out.getMetadata().getTemplates().add(tmp);
		}
		out.getMetadata().getBuiltIn().addAll(Arrays.asList(metadata.builtins()));
		
		model.addPackage(out);
		return out;

	}

	private void createAnnotations(Annotation[] anns, JElement p) {
		for (Annotation am: anns) {
			p.getAnnotations().add(createAnnotation(am));
		}
	}

	private static <T extends Annotation> JAnnotation createAnnotation(T ann) {
		JAnnotation out = new JAnnotation();
		out.setName(ann.getClass().getSimpleName());
		for (Method m: ann.getClass().getMethods()) {
			Object tmp;
			try {
				tmp = m.invoke(ann);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				e.printStackTrace(System.out);
				tmp = null;
			}
			if (tmp.getClass().isArray()) {
				List<Object> os = new ArrayList<>();
				for (Object o: (Object[]) tmp) {
					if (o instanceof Annotation) {
						os.add(createAnnotation((Annotation) o));
					} else if (o instanceof Class) {
						os.add(JNameBuilder.from((Class<?>) o));
					} else {
						os.add(o);
					}
				}
				out.getValues().add(JAnnotationEntry.with(
						JNameBuilder.from(m),JAnnotationValue.of(os)));
			} else if (tmp instanceof Annotation) {
				tmp = createAnnotation((Annotation) tmp);
				out.getValues().add(JAnnotationEntry.with(
						JNameBuilder.from(m),JAnnotationValue.of(tmp)));
			} else if (tmp instanceof Class) {
				tmp = JNameBuilder.from((Class<?>) tmp);
				out.getValues().add(JAnnotationEntry.with(
						JNameBuilder.from(m),JAnnotationValue.of(tmp)));
			} else {
				out.getValues().add(JAnnotationEntry.with(
						JNameBuilder.from(m),JAnnotationValue.of(tmp)));
			}
		}
		return out;
	}
	
	private boolean isInModel(Class<?> clazz) {
		Package tmp = clazz.getPackage();
		while (tmp != null) {
			if (tmp.isAnnotationPresent(Model.class)) return true;
			tmp = Package.getPackage(tmp.getName().substring(0, tmp.getName().lastIndexOf(".")));
		}
		return false;
	}
	
	private boolean notGenerated(Class<?> e) {
		return !e.isAnnotationPresent(Generated.class);
	}
}
