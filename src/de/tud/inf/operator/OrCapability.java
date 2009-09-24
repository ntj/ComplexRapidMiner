package de.tud.inf.operator;

import java.util.ArrayList;
import java.util.List;
import de.tud.inf.operator.capabilites.Capability;

public class OrCapability implements Capability {

	private List<Capability> inner;
	
	public OrCapability(List<Capability> innerCapabilities) {
		inner = innerCapabilities;
	}
	
	public OrCapability() {
		inner = new ArrayList<Capability>();
	}
	
	public void addCapability(Capability cap) {
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
