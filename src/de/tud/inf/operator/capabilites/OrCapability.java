package de.tud.inf.operator.capabilites;



public class OrCapability extends BoolCapability {

	

	
	public boolean checkCapability(Capability toCheck) {
		
		for(Capability child : inner) {
			if(child.checkCapability(toCheck))
				return true;
		}
		return false;
	}

	
	public int getType() {
		
		return OR_CAPABILITY_TYPE;
	}

	
}
