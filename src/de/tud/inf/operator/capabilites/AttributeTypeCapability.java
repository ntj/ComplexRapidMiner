package de.tud.inf.operator.capabilites;

import java.util.List;

import com.rapidminer.tools.Ontology;

public class AttributeTypeCapability implements Capability{
	
	private int attributeType;
	
	
	public AttributeTypeCapability(int attributeType){
		this.attributeType = attributeType;
	}
	

	public boolean checkCapability(Capability toCheck) {
		
		if(toCheck.getType() == Capability.ATTRIBUTE_CAPABILITY_TYPE)
			if(((AttributeTypeCapability)toCheck).getAttributeType() == attributeType)
				return true;
		return false;
	}


	
	public int getType() {
		
		return Capability.ATTRIBUTE_CAPABILITY_TYPE;
	}
	
	public int getAttributeType() {
		return attributeType;
	}


	public List<Capability> getInnerCapabilities() {
		return null;
	}


	public String getAsString() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(attributeType);
	}

}
