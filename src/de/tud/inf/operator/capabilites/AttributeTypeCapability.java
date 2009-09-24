package de.tud.inf.operator.capabilites;

import java.util.ArrayList;
import java.util.List;

public class AttributeTypeCapability implements Capability{
	
	private int attributeType;
	
	private List<Capability> inner = new ArrayList<Capability>();
	
	public AttributeTypeCapability(int attributeType){
		this.attributeType = attributeType;
	}
	
	

	public boolean checkCapability(Capability toCheck) {
		
		if(toCheck.getType() == Capability.ATTRIBUTE_CAPABILITY_TYPE)
			if(((AttributeTypeCapability)toCheck).getAttributeType() == attributeType)
				return true;
		
		for(Capability child : toCheck.getInnerCapabilities()) {
			if(this.checkCapability(child))
				return true;
		}
		
		return false;
	}

	
	public List<Capability> getInnerCapabilities() {
		
		return inner;
	}

	
	public int getType() {
		
		return Capability.ATTRIBUTE_CAPABILITY_TYPE;
	}
	
	public int getAttributeType() {
		return attributeType;
	}

}
