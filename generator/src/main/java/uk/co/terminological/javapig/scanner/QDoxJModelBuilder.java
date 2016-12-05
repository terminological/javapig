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

public class QDoxJModelBuilder {

	private JavaProjectBuilder jpb;
	ClassLibraryBuilder libraryBuilder;
	JProject model;

	QDoxJModelBuilder(JProject model) {
		libraryBuilder = new SortedClassLibraryBuilder(); 
		libraryBuilder.appendDefaultClassLoaders();
		jpb = new JavaProjectBuilder( libraryBuilder );
		this.model = model;
	}

	public static JProject scanModel(File[] files, File... others) {
		QDoxJModelBuilder out = new QDoxJModelBuilder(new JProject());
		List<File> tmp = new ArrayList<>(); 
		tmp.addAll(Arrays.asList(files));
		tmp.addAll(Arrays.asList(others));
		out.scanSourceModel(tmp);
		return out.getModel();
	}

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
			System.out.println("Inspecting class: "+clazz.getCanonicalName());
			if (isInModel(clazz) && notGenerated(clazz)) createClass(clazz);
		}
	}

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

	public static class RecursiveVisitor extends QDoxUtils.WorkaroundVisitor {
		
		
		public RecursiveVisitor(JavaAnnotatedElement context, JavaProjectBuilder jpb) {
			super(context, jpb);
			
		}

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


	private boolean notGenerated(JavaClass e) {
		return !QDoxUtils.hasAnnotation(Generated.class,e);
	}

	private boolean isInModel(JavaClass clazz) {
		return isInModel(clazz.getPackage());
		
	}

	private boolean isInModel(JavaPackage tmp) {
		return getModelAnn(tmp) != null;
	}

	private Optional<Model> getModelAnn(JavaPackage tmp) {
		if (QDoxUtils.hasAnnotation(Model.class, tmp)) {
			JAnnotation ann = createAnnotation(QDoxUtils.getAnnotation(Model.class, tmp), new RecursiveVisitor(tmp, this.jpb));
			return Optional.of((Model) ann.convert(Model.class));
		}
		return Optional.empty();
	}
}
