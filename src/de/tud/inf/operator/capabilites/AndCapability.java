package de.tud.inf.operator.capabilites;

import java.util.ArrayList;
import java.util.List;

public class AndCapability implements BoolCapability{
	
	List<Capability> inner;

	
	public AndCapability() {
		inner = new ArrayList<Capability>();
	}
	
	public void addCapability(AttributeTypeCapability cap) {
		inner.add(cap);
		
	}

	public boolean checkCapability(Capability toCheck) {
		
		for(Capability child : inner) {
			if(!child.checkCapability(toCheck))
				return false;
		}
		return true;
	}

	
	public List<Capability> getInnerCapabilities() {
		
		return inner;
	}

	
	public int getType() {
		
		return AND_CAPABILITY_TYPE;
	}

}
