package de.tud.inf.operator.capabilites;

import java.util.ArrayList;
import java.util.List;


public class OrCapability implements BoolCapability {

	private List<Capability> inner;
	
	
	public OrCapability() {
		inner = new ArrayList<Capability>();
	}
	
	public void addCapability(AttributeTypeCapability cap) {
		inner.add(cap);
	}

	
	public boolean checkCapability(Capability toCheck) {
		
		for(Capability child : inner) {
			if(child.checkCapability(toCheck))
				return true;
		}
		return false;
	}

	
	public List<Capability> getInnerCapabilities() {
		
		return inner;
	}

	
	public int getType() {
		
		return OR_CAPABILITY_TYPE;
	}

}
