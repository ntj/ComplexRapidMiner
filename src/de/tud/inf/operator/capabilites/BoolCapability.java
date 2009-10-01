package de.tud.inf.operator.capabilites;

import java.util.List;

public abstract class BoolCapability implements Capability{
	
	protected List<Capability> inner;
	
	public void addCapability(AttributeTypeCapability cap) {
		inner.add(cap);	
	}
	
	public abstract boolean checkCapability(Capability toCheck);
	
	public List<Capability> getInnerCapabilities() {	
		return inner;
	}
	
	public String getAsString(){
		String res = "";
		for(Capability c: inner)
			res += c.getAsString();
		return res;
	}


}
