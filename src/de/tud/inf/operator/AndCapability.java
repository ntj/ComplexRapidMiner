package de.tud.inf.operator;

import java.util.ArrayList;
import java.util.List;

import de.tud.inf.operator.capabilites.Capability;

public class AndCapability implements Capability{
	
	List<Capability> inner;

	public AndCapability(List<Capability> inner) {
		this.inner = inner;
	}
	
	public AndCapability() {
		inner = new ArrayList<Capability>();
	}
	
	public void addCapability(Capability cap) {
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
