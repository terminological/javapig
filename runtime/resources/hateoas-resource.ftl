package com.bmj.informatics.catalogue.dto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.EntityLinks;
import org.springframework.hateoas.Link;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.jaxrs.JaxRsLinkBuilder;

import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.protege.owl.codegeneration.WrappedIndividual;
import com.bmj.informatics.catalogue.util.NamedStub;
import java.util.ArrayList;
import java.util.UUID;

import com.bmj.informatics.catalogue.util.OWLUtil;
import com.bmj.informatics.catalogue.owl.factory.CatalogueFactory;

import ${model.getFullyQualifiedName()};
import com.bmj.informatics.catalogue.service.${model.name}Service;

@SuppressWarnings("unused")
@XmlRootElement(name="${model.name}")
public class ${model.name}Resource extends NamedStub {
	
	@Autowired @XmlTransient OWLUtil util;
	
	<#list model.methods as method>
		<#if method.isGetter() && method.name != "getOwlIndividual" && method.name != "getOwlOntology">
			<#if method.isCollection()>
				<#if method.parameterIsTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
	private ArrayList<NamedStub> ${method.fieldName()} = new ArrayList<NamedStub>();
				<#else>
	private ArrayList<${method.parameterType}> ${method.fieldName()} = new ArrayList<${method.parameterType}>();
				</#if>
			<#else>
				<#if method.isTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
	private ${method.implementationType()}Resource ${method.fieldName()};
				<#else>
	private ${method.implementationType()} ${method.fieldName()};
				</#if>
			</#if>
		</#if>
	</#list> 
	
	<#list model.methods as method>
		<#if method.isGetter() && method.name != "getOwlIndividual" && method.name != "getOwlOntology">
			<#if method.isCollection()>
				<#if method.parameterIsTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
	@XmlElement(name="${method.fieldName()}")
	public ArrayList<NamedStub> ${method.name}() {
		return ${method.fieldName()};
	}
	
	public void ${method.setterName()}(java.util.ArrayList<NamedStub> input) {
		this.${method.fieldName()} = input;
	}
				<#else>
	@XmlElement(name="${method.fieldName()}")
	public ArrayList<${method.parameterType}> ${method.name}() {
		return ${method.fieldName()};
	}
	
	public void ${method.setterName()}(java.util.ArrayList<${method.parameterType}> input) {
		this.${method.fieldName()} = input;
	}
				</#if>
			<#else>
				<#if method.isTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
	@XmlElement(name="${method.fieldName()}")
	public ${method.type}Resource ${method.name}() {
		return ${method.fieldName()};
	}
	
	public void ${method.setterName()}(${method.type}Resource input) {
		this.${method.fieldName()} = input;
	}
				<#else>
	@XmlElement(name="${method.fieldName()}")
	public ${method.type} ${method.name}() {
		return ${method.fieldName()};
	}
	
	public void ${method.setterName()}(${method.type} input) {
		this.${method.fieldName()} = input;
	}
				</#if>
			</#if>
		</#if>
	</#list>
	
	public static ${model.name}Resource create(${model.name} instance) {
		${model.name}Resource out = NamedStub.create(instance,new ${model.name}Resource());
	<#list model.methods as method>
		<#if method.isGetter() && method.name != "getOwlIndividual" && method.name != "getOwlOntology">
			<#if method.isCollection()>
				<#if method.parameterIsTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
		for (WrappedIndividual tmp: instance.${method.name}()) {
			out.${method.name}().add(NamedStub.create(tmp));
		}
				<#else>
		for (${method.parameterType} tmp: instance.${method.name}()) {
			out.${method.name}().add(tmp);
		}
				</#if>
			<#else>
				<#if method.isTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
		out.${method.setterName()}(
			${method.implementationType()}Resource.create(
				instance.${method.name}()
				)
			);
				<#else>
		out.${method.setterName()}(instance.${method.name}());
				</#if>
			</#if>
		</#if>
	</#list>
	
		//Do entityLinks
		//This is not correct: what it should be I do not know. 
	<#list model.methods as method>
		<#if method.isGetter() && method.name != "getOwlIndividual" && method.name != "getOwlOntology">
			<#if method.isCollection()>
				<#if method.parameterIsTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
		for (${method.parameterType} subinstance: instance.${method.name}()) {
			out.add(
				new Link(
					out.getBaseUri()+
					"${model.name}/"+
					OWLUtil.nameFromIRI(instance.getOwlIndividual().getIRI())+
					"/${method.getParameterTypeSimpleName()}/"+
					OWLUtil.nameFromIRI(subinstance.getOwlIndividual().getIRI())
				).withRel("${method.fieldName()}")
			);
		}
		
				</#if>
			</#if>
		</#if>
	</#list>
		
		return out;
	}
	
	public ${model.name}Resource write(CatalogueFactory factory) throws OWLOntologyStorageException {
		${model.name} tmp = factory.create${model.name}(UUID.randomUUID().toString());
		//Do some more mapping here?
		factory.saveOwlOntology();
		return ${model.name}Resource.create(tmp);
	}
}	
		
