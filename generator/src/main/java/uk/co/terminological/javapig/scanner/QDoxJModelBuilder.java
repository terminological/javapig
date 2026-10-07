package uk.co.terminological.javapig.scanner;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Generated;

import com.thoughtworks.qdox.JavaProjectBuilder;
import com.thoughtworks.qdox.library.ClassLibraryBuilder;
import com.thoughtworks.qdox.library.SortedClassLibraryBuilder;
import com.thoughtworks.qdox.model.JavaAnnotatedElement;
import com.thoughtworks.qdox.model.JavaAnnotation;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaPackage;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.expression.AnnotationValue;

import uk.co.terminological.javapig.annotations.Model;
import uk.co.terminological.javapig.javamodel.JAnnotation;
import uk.co.terminological.javapig.javamodel.JAnnotationEntry;
import uk.co.terminological.javapig.javamodel.JAnnotationValue;
import uk.co.terminological.javapig.javamodel.JClassName;
import uk.co.terminological.javapig.javamodel.JGetMethod;
import uk.co.terminological.javapig.javamodel.JInterface;
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JProject;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JPackageMetadata;
import uk.co.terminological.javapig.javamodel.tools.JNameBuilder;

/**
 * Parses Java source files using QDox and builds a {@link JProject} metamodel
 * for use by the code generation pipeline.
 *
 * <p>The scanner processes all classes in source folders whose packages are
 * annotated with {@code @Model}. Only interfaces (not enums or classes) that
 * are not themselves annotated with {@code @Generated} are included. Methods
 * are filtered to include only non-void, no-argument, non-default accessor methods.</p>
 *
 * <p>Filtering rules for methods:</p>
 * <ul>
 *   <li>Methods with parameters are excluded.</li>
 *   <li>Methods returning {@code void} are excluded.</li>
 *   <li>Default methods are parsed but the {@code isDefault} flag is preserved on
 *       the resulting {@link JGetMethod} for downstream adaptors to decide.</li>
 *   <li>Methods on interfaces annotated with {@code @Generated} are excluded
 *       entirely (the interface itself is skipped).</li>
 * </ul>
 *
 * @see JProject
 * @see JInterface
 * @see JGetMethod
 * @see AptJModelBuilder
 * @see QDoxUtils
 */
public class QDoxJModelBuilder {

	private JavaProjectBuilder jpb;
	ClassLibraryBuilder libraryBuilder;
	JProject model;

	/**
	 * Constructs the scanner with a new, empty {@link SortedClassLibraryBuilder}
	 * initialized with the default class loaders.
	 *
	 * @param model the project metamodel to populate
	 */
	QDoxJModelBuilder(JProject model) {
		libraryBuilder = new SortedClassLibraryBuilder(); 
		libraryBuilder.appendDefaultClassLoaders();
		jpb = new JavaProjectBuilder( libraryBuilder );
		this.model = model;
	}

	/**
	 * Scans the given source directories and returns a populated {@link JProject}.
	 *
	 * <p>This is the main entry point for source-file-based scanning. The scanner
	 * walks each source folder, identifies packages annotated with {@code @Model},
	 * and constructs the metamodel from the declared interfaces and their methods.</p>
	 *
	 * @param files the primary source directories to scan
	 * @param others additional source directories (varargs)
	 * @return a fully populated JProject metamodel
	 */
	public static JProject scanModel(File[] files, File... others) {
		QDoxJModelBuilder out = new QDoxJModelBuilder(new JProject());
		List<File> tmp = new ArrayList<>(); 
		tmp.addAll(Arrays.asList(files));
		tmp.addAll(Arrays.asList(others));
		out.scanSourceModel(tmp);
		return out.getModel();
	}

	/**
	 * Returns the project metamodel that has been (or will be) built by this scanner.
	 *
	 * @return the JProject being populated
	 */
	public JProject getModel() {
		return model;
	}

	private void scanSourceModel(List<File> files) {
		for (File sourceFolder: files) {
			jpb.addSourceTree(sourceFolder);
		}
		for (JavaPackage pkg: jpb.getPackages()) {
			if (isInModel(pkg)) createPackage(pkg);
		}
		jpb.getClasses().stream().map(c->c.getCanonicalName()).forEach(System.out::println);
		for (JavaClass clazz: jpb.getClasses()) {
			JavaPackage pkg = this.jpb.getPackageByName(clazz.getPackageName());
			/*System.out.println("Inspecting class: "+clazz.getCanonicalName()+" ("
					+(isInModel(clazz)?"in model":"not in model")+" / "
					+(notGenerated(clazz)?"not generated":"generated")+")"
					+" [package: "+pkg.getName()+" "
					+(isInModel(pkg)?"in model":"not in model")+"]"
					+(QDoxUtils.hasAnnotation(Model.class,pkg)? "annotated":"not annotated")
					);*/
			if (isInModel(clazz) && notGenerated(clazz)) {
				createClass(clazz);
				System.out.println("Adding to project: "+clazz.getCanonicalName());
			} else {
				System.out.println("Not part of project: "+clazz.getCanonicalName());
			}
		}
	}

	/**
	 * Creates a {@link JGetMethod} from a QDox {@link JavaMethod}, applying the
	 * filtering rules (no parameters, non-void).
	 *
	 * <p>If the method has already been defined in the project, it is returned
	 * from the project cache. Methods that fail filtering return {@code null}.</p>
	 *
	 * @param m the QDox method to convert
	 * @return the created JGetMethod, or {@code null} if the method is filtered out
	 */
	public JGetMethod createMethod(JavaMethod m) {

		if (model.methodIsDefined(JNameBuilder.from(m))) return model.findMethod(JNameBuilder.from(m));
		if (
				!m.getParameters().isEmpty() ||
				m.getReturnType().equals(JavaType.VOID)
			) return null;
		
		JGetMethod out = new JGetMethod(
				model,
				m.getComment(),
				createAnnotations(m.getAnnotations(), new RecursiveVisitor(m,this.jpb)),
				JNameBuilder.from(m.getDeclaringClass()),
				JNameBuilder.from(m),
				m.getReturnType().getValue(),
				JNameBuilder.from(QDoxUtils.box(m.getReturnType(), jpb)),
				JNameBuilder.from(QDoxUtils.underlyingReturnType(m,jpb)),
				m.isDefault());
		
		model.addMethod(out);
		return out;

	}



	/**
	 * Creates a {@link JInterface} from a QDox {@link JavaClass}, extracting its
	 * annotations, name, and superinterfaces, then recursively scanning its methods.
	 *
	 * @param clazz the QDox class representing the interface
	 * @return the created JInterface
	 */
	public JInterface createClass(JavaClass clazz) {
		JClassName cn = JNameBuilder.from(clazz);
		
		JavaPackage pkg = clazz.getPackage();
		if (!model.packageIsDefined(pkg.getName())) createPackage(pkg);
		
		
		JInterface out = new JInterface(
				model,
				clazz.getComment(),
				createAnnotations(clazz.getAnnotations(), new RecursiveVisitor(clazz,this.jpb)),
				cn,
				clazz.getInterfaces().stream()
					.map(iface -> JNameBuilder.from(iface))
					.collect(Collectors.toSet())
				);

		clazz.getSource().getImports(); //Do something with this
		
		for (JavaMethod m: clazz.getMethods(true)) {
			createMethod(m);
		}

		model.addInterface(out);
		
		return out;
	}



	/**
	 * Creates a {@link JPackage} from a QDox {@link JavaPackage}, extracting
	 * its {@code @Model} annotation (if present) to build package metadata.
	 *
	 * @param pkg the QDox package to convert
	 * @return the created JPackage
	 */
	public JPackage createPackage(JavaPackage pkg) {

		if (model.packageIsDefined(pkg.getName())) return model.findPackage(pkg.getName());

		Optional<Model> ann = getModelAnn(pkg);
		Optional<JPackageMetadata> meta = ann.map(JPackageMetadata::from);
		
		JPackage out = new JPackage(
				model,
				pkg.getComment(),
				createAnnotations(pkg.getAnnotations(), new RecursiveVisitor(pkg, this.jpb)),
				pkg.getName(),
				meta);
		
		model.addPackage(out);
		return out;
	}


	private List<JAnnotation> createAnnotations(List<JavaAnnotation> anns, RecursiveVisitor v) {
		List<JAnnotation> p = new ArrayList<>();
		for (JavaAnnotation ann: anns) {
			p.add(createAnnotation(ann,v));
		}
		return p;
	}

	private static <T extends Annotation> JAnnotation createAnnotation(JavaAnnotation ann, RecursiveVisitor v) {
		
		List<JAnnotationEntry> entries = new ArrayList<>();
		for (String m: ann.getPropertyMap().keySet()) {
			AnnotationValue tmp = ann.getPropertyMap().get(m);
			Object val = tmp.accept(v); 
			entries.add(new JAnnotationEntry(
					JMethodName.from(ann.getType().getFullyQualifiedName()+"#"+m),
					(val instanceof JavaType) ? JAnnotationValue.of(JNameBuilder.from((JavaClass) val)) : JAnnotationValue.of(val)
					));
		}
		
		
		
		return new JAnnotation(
				v.fqnForSimpleName(ann),
				ann.getType().getValue(),
				entries);
		
	}

	/**
	 * A QDox annotation value visitor that resolves {@link JavaAnnotation} elements
	 * by looking up their fully-qualified names through the context's imports.
	 *
	 * <p>Also overrides {@link #visit(JavaAnnotation)} to recursively process
	 * nested annotations by delegating to {@link #createAnnotation(JavaAnnotation, RecursiveVisitor)}.</p>
	 */
	public static class RecursiveVisitor extends QDoxUtils.WorkaroundVisitor {
		
		
		public RecursiveVisitor(JavaAnnotatedElement context, JavaProjectBuilder jpb) {
			super(context, jpb);
			
		}

		/**
		 * Resolves the fully-qualified class name for a simple annotation type name
		 * by searching the imports of the context class.
		 *
		 * @param ann the QDox annotation
		 * @return the FQN if an import resolves the simple name, empty otherwise
		 */
		public Optional<String> fqnForSimpleName(JavaAnnotation ann) {
			String simpleName = ann.getType().getValue();
			JavaClass tmp;
			if (context instanceof JavaClass) {
				tmp = (JavaClass) context;
			} else if (context instanceof JavaMethod) {
				tmp = ((JavaMethod) context).getDeclaringClass();
			} else {
				return Optional.empty(); 
			}
			return tmp.getSource().getImports().stream().filter(s -> s.endsWith(simpleName)).findFirst();
		}
		
		@Override
		public Object visit(JavaAnnotation annotation) throws UnsupportedOperationException {
			return createAnnotation(annotation,new RecursiveVisitor(this.context, this.jdb));
		}
		
	};


	/**
	 * Checks whether a class is annotated with {@code @Generated} (from {@code javax.annotation}).
	 *
	 * @param e the element to check
	 * @return {@code true} if the element is a generated class
	 */
	private boolean notGenerated(JavaClass e) {
		return !QDoxUtils.hasAnnotation(Generated.class,e);
	}

	private boolean isInModel(JavaClass clazz) {
		return isInModel(this.jpb.getPackageByName(clazz.getPackageName()));
	}

	/**
	 * Determines whether a package (or any of its ancestors) is annotated with {@code @Model},
	 * meaning it should be included in the metamodel.
	 *
	 * <p>The unnamed (default) package is never in the model. It must be screened out
	 * before calling {@link JavaPackage#getParentPackage()}: QDox implements that as an
	 * unguarded {@code name.substring(0, name.lastIndexOf('.'))}, which throws
	 * {@link StringIndexOutOfBoundsException} for an empty package name. The unnamed
	 * package is contributed to the builder by {@code appendDefaultClassLoaders()}.</p>
	 *
	 * @param tmp the QDox package to check, may be {@code null}
	 * @return {@code true} if the package or an ancestor package carries {@code @Model}
	 */
	private boolean isInModel(JavaPackage tmp) {
		if (tmp == null) return false;
		if (tmp.getName() == null || tmp.getName().isEmpty()) return false;
		if (getModelAnn(tmp).isPresent()) return true;
		if (tmp.getParentPackage() != null) {
			return isInModel(tmp.getParentPackage());
		}
		return false;
	}

	private Optional<Model> getModelAnn(JavaPackage tmp) {
		if (QDoxUtils.hasAnnotation(Model.class, tmp)) {
			JAnnotation ann = createAnnotation(QDoxUtils.getAnnotation(Model.class, tmp), new RecursiveVisitor(tmp, this.jpb));
			return Optional.of((Model) ann.convert(Model.class));
		}
		return Optional.empty();
	}
}
