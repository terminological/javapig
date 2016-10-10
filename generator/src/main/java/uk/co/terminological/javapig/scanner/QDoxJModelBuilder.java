package uk.co.terminological.javapig.scanner;

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.List;

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
import uk.co.terminological.javapig.javamodel.JMethodName;
import uk.co.terminological.javapig.javamodel.JModel;
import uk.co.terminological.javapig.javamodel.JPackage;
import uk.co.terminological.javapig.javamodel.JTemplateMetadata;
import uk.co.terminological.javapig.javamodel.tools.JNameBuilder;

public class QDoxJModelBuilder {

	private JavaProjectBuilder jpb;
	ClassLibraryBuilder libraryBuilder;
	JModel model;

	QDoxJModelBuilder(JModel model) {
		libraryBuilder = new SortedClassLibraryBuilder(); 
		libraryBuilder.appendDefaultClassLoaders();
		jpb = new JavaProjectBuilder( libraryBuilder );
		
		this.model = model;
	}

	public static JModel scanModel(File... files) {
		QDoxJModelBuilder out = new QDoxJModelBuilder(new JModel());
		out.scanSourceModel(files);
		
		return out.getModel();
	}

	public JModel getModel() {
		return model;
	}

	private void scanSourceModel(File[] files) {
		for (File sourceFolder: files) {
			jpb.addSourceTree(sourceFolder);
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
				m.getModifiers().contains("default") ||
				!m.getParameters().isEmpty() ||
				m.getReturnType().equals(JavaType.VOID)
			) return null;
		
		JGetMethod out = new JGetMethod();
		out.setModel(model);
		out.setJavaDoc(m.getComment());
		createAnnotations(m.getAnnotations(), out, new RecursiveVisitor(m,this.jpb));

		out.setDeclaringClass(JNameBuilder.from(m.getDeclaringClass()));
		out.setName(JNameBuilder.from(m));
		out.setReturnTypeDefinition(m.getReturnType().getValue());
		out.setReturnType(JNameBuilder.from(QDoxUtils.box(m.getReturnType(), jpb)));
		out.setUnderlyingType(JNameBuilder.from(QDoxUtils.underlyingReturnType(m,jpb)));
		out.setId(QDoxUtils.hasAnnotation(Identifier.class,m));

		model.addMethod(out);
		return out;

	}



	public JInterface createClass(JavaClass clazz) {
		JClassName cn = JNameBuilder.from(clazz);

		JInterface out = new JInterface();
		out.setModel(model);
		out.setJavaDoc(clazz.getComment());
		createAnnotations(clazz.getAnnotations(), out, new RecursiveVisitor(clazz,this.jpb));

		out.setName(cn);
		for (JavaMethod m: clazz.getMethods(true)) {
			if (QDoxUtils.hasAnnotation(Identifier.class, m)) {
				out.setIdentity(JNameBuilder.from(m));
			}
			createMethod(m);
		}
		for (JavaClass iface: clazz.getInterfaces()) { 
			out.getSupertypes().add(JNameBuilder.from(iface));
		}

		JavaPackage pkg = clazz.getPackage();
		if (!model.packageIsDefined(pkg.getName())) createPackage(pkg);
		out.setPkg(pkg.getName());
		model.addInterface(out);
		
		return out;
	}



	@SuppressWarnings("unchecked")
	public JPackage createPackage(JavaPackage pkg) {

		if (model.packageIsDefined(pkg.getName())) return model.findPackage(pkg.getName());

		JPackage out = new JPackage();
		out.setModel(model);
		out.setJavaDoc(pkg.getComment());
		createAnnotations(pkg.getAnnotations(), out, new RecursiveVisitor(pkg, this.jpb));

		out.setName(pkg.getName());

		Model metadata = getModelAnn(pkg) ;
		String directory = metadata.directory();
		out.getMetadata().setDirectory(new File(directory));
		out.getMetadata().getBuiltIn().addAll(Arrays.asList(metadata.builtins()));
		for (Template template: metadata.templates()) {
			JTemplateMetadata tmp = new JTemplateMetadata();
			tmp.setClassNameTemplate(template.classnameTemplate());
			tmp.setScope(template.appliesTo());
			tmp.setTemplateFilename(template.filename());
			tmp.setExtension(template.extension());
			out.getMetadata().getTemplates().add(tmp);
		}

		model.addPackage(out);
		return out;
	}


	private void createAnnotations(List<JavaAnnotation> anns, JElement p, RecursiveVisitor v) {
		for (JavaAnnotation ann: anns) {
			p.getAnnotations().add(createAnnotation(ann,v));
		}
	}

	public static <T extends Annotation> JAnnotation createAnnotation(JavaAnnotation ann, RecursiveVisitor v) {
		JAnnotation out = new JAnnotation();
		out.setName(ann.getType().getValue());
		for (String m: ann.getPropertyMap().keySet()) {
			AnnotationValue tmp = ann.getPropertyMap().get(m);
			Object val = tmp.accept(v); 
			out.getValues().add(JAnnotationEntry.with(
					JMethodName.from(ann.getType().getFullyQualifiedName()+"#"+m),
					(val instanceof JavaType) ? JAnnotationValue.of(JNameBuilder.from((JavaClass) val)) : JAnnotationValue.of(val)
					));
		}
		return out;
	}

	public static class RecursiveVisitor extends QDoxUtils.WorkaroundVisitor {
		
		public RecursiveVisitor(JavaAnnotatedElement prefix, JavaProjectBuilder jpb) {
			super(prefix, jpb);
		}

		@Override
		public Object visit(JavaAnnotation annotation) throws UnsupportedOperationException {
			return createAnnotation(annotation,new RecursiveVisitor(this.prefix, this.jdb));
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

	private Model getModelAnn(JavaPackage tmp) {
		while (tmp != null) {
			tmp = jpb.getPackageByName(tmp.getName());
			// see http://paul-hammant.github.io/Old_Qdox_Issues/255/
			if (QDoxUtils.hasAnnotation(Model.class, tmp)) {
				JAnnotation ann = createAnnotation(QDoxUtils.getAnnotation(Model.class, tmp), new RecursiveVisitor(tmp, this.jpb));
				return (Model) ann.convert(Model.class);
			}
			tmp = tmp.getParentPackage();
		}
		return null;
	}
}
