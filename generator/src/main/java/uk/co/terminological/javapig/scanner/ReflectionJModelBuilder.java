package uk.co.terminological.javapig.scanner;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Generated;

import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import uk.co.terminological.javapig.annotations.Model;
import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;
import uk.co.terminological.javapig.javamodel.tools.JNameBuilder;

/**
 * This is not up to date with the rest of the builders
 * It is here in case it becomes needed again.
 * @author terminological
 *
 */

public class ReflectionJModelBuilder {


	JProject model;


	ReflectionJModelBuilder(JProject model) {
		this.model = model;
	}

	public JProject getModel() {
		return model;
	}

	public static JProject scanModel(Package pkg) {
		ReflectionJModelBuilder out = new ReflectionJModelBuilder(new JProject());
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

		JGetMethod out = new JGetMethod(
				model,
				"",
				createAnnotations(m.getAnnotations()),
				JNameBuilder.from(m.getDeclaringClass()),
				JNameBuilder.from(m),
				m.getReturnType().getCanonicalName(),
				JNameBuilder.from(m.getReturnType()),
				JNameBuilder.from(ReflectionUtils.underlyingReturnType(m)),
				m.isDefault()
				);
		
		model.addMethod(out);
		return out;
	}

	private JInterface createClass(Class<?> clazz) {
		JClassName cn = JNameBuilder.from(clazz);

		JInterface out = new JInterface(
				model,
				"",
				createAnnotations(clazz.getAnnotations()),
				cn,
				Stream.of(clazz.getInterfaces()).map(iface -> JClassName.from(iface.getCanonicalName())).collect(Collectors.toSet())
				);

		
		Package pkg = clazz.getPackage();
		createPackage(pkg);

		for (Method m: clazz.getMethods()) {
			createMethod(m);
		}
		
		model.addInterface(out);
		return out;
	}

	private JPackage createPackage(Package pkg) {

		if (model.packageIsDefined(pkg.getName())) return model.findPackage(pkg.getName());

		Optional<Model> ann = Optional.ofNullable(pkg.getAnnotation(Model.class));
		Optional<JPackageMetadata> meta = ann.map(JPackageMetadata::from);
		
		JPackage out = new JPackage(
				model,
				"",
				createAnnotations(pkg.getAnnotations()),
				pkg.getName(),
				meta);
		
		model.addPackage(out);
		
		return out;
	}

	private List<JAnnotation> createAnnotations(Annotation[] anns) {
		return Stream.of(anns).map(am -> createAnnotation(am)).collect(Collectors.toList());
	}

	private static <T extends Annotation> JAnnotation createAnnotation(T ann) {
		List<JAnnotationEntry> entries =  new ArrayList<>();
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
				entries.add(new JAnnotationEntry(
						JNameBuilder.from(m),JAnnotationValue.of(os)));
			} else if (tmp instanceof Annotation) {
				tmp = createAnnotation((Annotation) tmp);
				entries.add(new JAnnotationEntry(
						JNameBuilder.from(m),JAnnotationValue.of(tmp)));
			} else if (tmp instanceof Class) {
				tmp = JNameBuilder.from((Class<?>) tmp);
				entries.add(new JAnnotationEntry(
						JNameBuilder.from(m),JAnnotationValue.of(tmp)));
			} else {
				entries.add(new JAnnotationEntry(
						JNameBuilder.from(m),JAnnotationValue.of(tmp)));
			}
		}
		
		return new JAnnotation(
				Optional.of(ann.getClass().getCanonicalName()),
				ann.getClass().getSimpleName(),
				entries
				);
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
