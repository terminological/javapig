package uk.co.terminological.javapig.scanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.Generated;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementScanner8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import javax.lang.model.util.Types;

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
import uk.co.terminological.javapig.scanner.AptUtils.CodeGenerationIncompleteException;

public class AptJModelBuilder extends ElementScanner8<Void, Void> {

	JProject model;
	ProcessingEnvironment penv;
	Elements el;
	Types ty;

	/**
	 * @param list - can be a new PackagesList() or one from a previous compilation
	 * ECJ tends to compile in bits and pieces so previous runs can create 
	 * @param penv - the processing environment from the processor
	 */
	public AptJModelBuilder(JProject list, ProcessingEnvironment penv) {
		this.model = list;
		this.penv = penv;
		this.el = penv.getElementUtils();
		this.ty = penv.getTypeUtils();
	}

	/**
	 * A scanner returns only the root element of the resulting model when
	 * scanning complete
	 * @return
	 */
	public JProject getModel() {
		return model;
	}


	/**
	 * Collects information about packages into the freemarker model
	 */
	@Override
	public Void visitPackage(PackageElement e, Void v) {
		System.out.println("Visiting: "+e.getSimpleName()+": "+e.getKind().toString());
		// this will look at a package and update any existing package 
		// definition with an empty definition. This will remove any
		// old class definitions in there
		if (notGenerated(e) && isInModel(e) && e.getKind().equals(ElementKind.PACKAGE)) {
			
			Optional<Model> ann = getModelAnn(e);
			Optional<JPackageMetadata> meta = ann.map(JPackageMetadata::from);
			
			JPackage out = new JPackage(
					model,
					el.getDocComment(e),
					createAnnotations(e),
					e.getQualifiedName().toString(),
					meta);
			
			this.model.addPackage(out);
			return super.visitPackage(e, null);
		} else {
			return DEFAULT_VALUE;
		}
	}



	/**
	 * Collects information about classes into the freemarker model
	 */
	@Override
	public Void visitType(TypeElement clazz, Void v) {
		JClassName cn = JNameBuilder.from(clazz);
		if (notGenerated(clazz) && isInModel(clazz) && clazz.getKind().equals(ElementKind.INTERFACE)) {

			JInterface out = new JInterface(
					model,
					el.getDocComment(clazz),
					createAnnotations(clazz),
					cn,
					clazz.getInterfaces().stream()
						.map(iface-> (TypeElement) ((DeclaredType) iface).asElement())
						.map(ifaceEl -> JClassName.from(ifaceEl.getQualifiedName().toString()))
						.collect(Collectors.toSet())
					
					);
			
			if (!model.packageIsDefined(cn.getPackageName())) scan(el.getPackageElement(cn.getPackageName()));
			model.addInterface(out);
			return super.visitType(clazz, null);
		} else {
			return DEFAULT_VALUE;
		}
	}

	/**
	 * Collects information about methods into the freemarker model
	 */
	@Override
	public Void visitExecutable(ExecutableElement m, Void v) {
		if (
				!m.getParameters().isEmpty() ||
				m.getReturnType().getKind().equals(TypeKind.VOID)
			) return DEFAULT_VALUE;
		if (notGenerated(m) && isInModel(m) && m.getKind().equals(ElementKind.METHOD)) {
			JGetMethod out = new JGetMethod(
					model,
					el.getDocComment(m),
					createAnnotations(m),
					JNameBuilder.from((TypeElement) m.getEnclosingElement()),
					JNameBuilder.from(m),
					m.getReturnType().toString(),
					JClassName.from(AptUtils.typeToString(m.getReturnType())),
					JClassName.from(AptUtils.firstParameter(m.getReturnType())),
					m.getModifiers().contains(Modifier.DEFAULT)
					);
			if (out.getDeclaringClass() != null) out.getDeclaringClass().getMethods().add(out);
			model.addMethod(out);
			return super.visitExecutable(m, null);
		} else {
			return DEFAULT_VALUE;
		}


	}

	// Private utility methods

	private boolean notGenerated(Element e) {
		if (e.getKind().equals(ElementKind.METHOD)) {
			TypeElement type = el.getTypeElement(((TypeElement) e.getEnclosingElement()).getQualifiedName());
			return notGenerated(type);
		} else if (
				e.getKind().equals(ElementKind.INTERFACE) ||
				e.getKind().equals(ElementKind.CLASS)) {
			Generated tmp = ((TypeElement) e).getAnnotation(Generated.class);
			return tmp == null;
		} else {
			return true;
		}
	}


	private boolean isInModel(Element e) {
		return getModelAnn(e) != null;
	}

	/*
	 * This looks at the processor tree hierarchy. However it does not look into parent packages
	 * as java does not regard packages to nest in the way humans believe they do.
	 */
	private Optional<Model> getModelAnn(Element tmp) {
		return getModelAnn(el.getPackageOf(tmp));
	}
	
	private Optional<Model> getModelAnn(PackageElement tmp) {
		if (tmp.getAnnotation(Model.class) != null) return Optional.of(tmp.getAnnotation(Model.class));
		return Optional.empty();
	}
	
	private List<JAnnotation> createAnnotations(Element e) {
		return el.getAllAnnotationMirrors(e).stream().map(am -> createAnnotation(am,el)).collect(Collectors.toList());
	}
	
	private static JAnnotation createAnnotation(AnnotationMirror am, Elements el) {
		
		List<JAnnotationEntry> values = new ArrayList<>();
		for (Entry<? extends ExecutableElement,? extends AnnotationValue> pair: el.getElementValuesWithDefaults(am).entrySet()) {
			Object value; 
			try {
				value = pair.getValue().accept(new AnnotationConverter(el),null);
				values.add(new JAnnotationEntry(
						JNameBuilder.from(pair.getKey()), 
						JAnnotationValue.of(value)));
			} catch (CodeGenerationIncompleteException ignored) {
				// we ignore this as it if to do with references that cannot be resolved at this
				// stage of compilation
			}
		}
		
		return new JAnnotation(
			Optional.of(((TypeElement) am.getAnnotationType().asElement()).getQualifiedName().toString()),
			am.getAnnotationType().asElement().getSimpleName().toString(),
			values
		);
		
	}

	private static class AnnotationConverter extends SimpleAnnotationValueVisitor8<Object, Void> {
		
		Elements el;
		
		public AnnotationConverter(Elements el) {
			this.el = el;
		}
		
		@Override
		public JAnnotation visitAnnotation(AnnotationMirror a, Void p) {
			return createAnnotation(a, el);
		}
		@Override public String visitString(String s, Void p) {
			if ("<error>".equals(s)) {
				throw new CodeGenerationIncompleteException("Unknown type returned as <error>.");
			} else if ("<any>".equals(s)) {
				throw new CodeGenerationIncompleteException("Unknown type returned as <any>.");
			}
			return s;
		}
		@Override public Object visitType(TypeMirror t, Void p) {
			
			if (t.getKind().equals(TypeKind.DECLARED) || t.getKind().equals(TypeKind.ERROR)) {
				return JNameBuilder.from((DeclaredType) t);
			} 
			return super.visitType(t, p);
		}
		@Override protected Object defaultAction(Object o, Void v) {
			if (o.getClass().isPrimitive()) {
				return o;
			} else {
				return o.toString();
			}
		}
		@Override public Object[] visitArray(List<? extends AnnotationValue> values, Void v) {
			Object[] result = new Object[values.size()];
			for (int i = 0; i < values.size(); i++) {
				result[i] = values.get(i).accept(this, null);
			}
			return result;
		}
	};
	
	

}
