package com.bmj.informatics.catalogue.service;

import javax.annotation.Generated;

import javax.jws.WebService;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.ArrayList;

import com.bmj.informatics.catalogue.dto.list.${model.name}ResourceList;
import com.bmj.informatics.catalogue.dto.${model.name}Resource;
import ${model.getFullyQualifiedName()};
import com.bmj.informatics.catalogue.owl.factory.CatalogueFactory;
import com.bmj.informatics.catalogue.util.OWLUtil;

import com.bmj.informatics.catalogue.util.Error;
import com.bmj.informatics.catalogue.util.InjectLogger;

import java.util.Collection;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.ExposesResourceFor;
import org.springframework.stereotype.Component;

<#list model.methods as method>
	<#if method.isGetter() && method.name != "getOwlIndividual" && method.name != "getOwlOntology">
		<#if method.isCollection()>
			<#if method.parameterIsTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
import com.bmj.informatics.catalogue.dto.list.${method.getParameterTypeSimpleName()}ResourceList;
import com.bmj.informatics.catalogue.dto.${method.getParameterTypeSimpleName()}Resource;
			</#if>
import ${method.parameterType};
		</#if>
	</#if>
</#list> 

@SuppressWarnings("unused")
@WebService(portName = "${model.name}Port",
serviceName = "${model.name}Service",
targetNamespace = "http://localhost:8080")
@Component
@Path("/${model.name}")
@ExposesResourceFor(${model.name}.class)
@Generated({"uk.co.pointofcare.echobase.processor.FreemarkerProcessor"})
public class ${model.name}Service {

	@InjectLogger static Logger logger;
	@Autowired CatalogueFactory factory;
	@Autowired OWLUtil util;
	
	
	@GET
	public ${model.name}ResourceList getAll${model.name}() {
		try {
			@SuppressWarnings("unchecked")
			Collection<${model.name}> instances = (Collection<${model.name}>) factory.getAll${model.name}Instances();
			return ${model.name}ResourceList.create(instances);
		} catch (Exception e) {
			throw Error.logStackTrace("No instances can be retrieved for ${model.name}", e);
		}
	}

	@Path("/{id}")
	@GET
	public ${model.name}Resource get${model.name}(@PathParam("id") String id) {
		try {
			${model.name} instance = factory.get${model.name}(OWLUtil.IRIFromName(id));
			return ${model.name}Resource.create(instance);
		} catch (Exception e) {
			throw Error.logStackTrace("No instance of ${model.name} exists with identifier: "+id, e);
		}
	}
	
	@POST
	@Consumes({javax.ws.rs.core.MediaType.APPLICATION_JSON,javax.ws.rs.core.MediaType.APPLICATION_XML})
	public ${model.name}Resource create${model.name}(final ${model.name}Resource param) {
		try {
			return param.write(factory);
		} catch (Exception e) {
			throw Error.logStackTrace("could not create new ${model.name}", e);
		}
	}
	
	<#list model.methods as method>
		<#if method.isGetter() && method.name != "getOwlIndividual" && method.name != "getOwlOntology">
			<#if method.isCollection()>
				<#if method.parameterIsTypeOf("org.protege.owl.codegeneration.WrappedIndividual")>
	@Path("/{id}/${method.fieldName()}")
	@GET 
	public ${method.getParameterTypeSimpleName()}ResourceList ${method.name}For${model.name}(@PathParam("id") String id) {
		try {
			${model.name} instance = factory.get${model.name}(OWLUtil.IRIFromName(id));
			@SuppressWarnings("unchecked")
			Collection<${method.getParameterTypeSimpleName()}> associated = (Collection<${method.getParameterTypeSimpleName()}>) instance.${method.name}();
			return ${method.getParameterTypeSimpleName()}ResourceList.create(associated);
		} catch (Exception e) {
			throw Error.logStackTrace("No instance of ${model.name} exists with identifier: "+id, e);
		}
	}
				<#else>
	@Path("/{id}/${method.fieldName()}")
	@GET 
	public ArrayList<${method.getParameterTypeSimpleName()}> ${method.name}For${model.name}(@PathParam("id") String id) {
		try {
			${model.name} instance = factory.get${model.name}(OWLUtil.IRIFromName(id));
			@SuppressWarnings("unchecked")
			Collection<${method.getParameterTypeSimpleName()}> associated = (Collection<${method.getParameterTypeSimpleName()}>) instance.${method.name}();
			ArrayList<${method.getParameterTypeSimpleName()}> out = new ArrayList<${method.parameterType}>();
			out.addAll(associated); 
			return out;
		} catch (Exception e) {
			throw Error.logStackTrace("No instance of ${model.name} exists with identifier: "+id, e);
		}
	}
				</#if>
			</#if>
		</#if>
	</#list> 

}
