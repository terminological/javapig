package com.bmj.informatics.catalogue.dto.list;


import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.springframework.hateoas.ResourceSupport;
import org.springframework.hateoas.jaxrs.JaxRsLinkBuilder;

import java.util.Collection;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Autowired;

import ${model.getFullyQualifiedName()};
import com.bmj.informatics.catalogue.dto.${model.name}Resource;
import com.bmj.informatics.catalogue.service.${model.name}Service;

@SuppressWarnings("unused")
@XmlRootElement(name="${model.name}List")
public class ${model.name}ResourceList extends ResourceSupport {
	
	private ArrayList<${model.name}Resource> contents = new ArrayList<${model.name}Resource>();
	
	public static ${model.name}ResourceList create(Collection<${model.name}> input) {
		${model.name}ResourceList out = new ${model.name}ResourceList();
		for (${model.name} entry: input) {
			out.contents.add(${model.name}Resource.create(entry));
		}
		out.add(JaxRsLinkBuilder.linkTo(${model.name}Service.class).withSelfRel());
		return out;
	}
	
	@XmlElement(name="${model.name}")
	public ArrayList<${model.name}Resource> getContents() {
		return contents;
	}
	
	public void setContents(ArrayList<${model.name}Resource> contents) {
		this.contents = contents;
	}
	
}	
		
