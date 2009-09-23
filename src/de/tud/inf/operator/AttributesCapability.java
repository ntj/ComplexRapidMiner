package de.tud.inf.operator;

import java.util.List;

public class AttributesCapability implements Capability{

	private List<Capability> inner;
	
	public void addCapability(Capability cap) {
		
		inner.add(cap);
	}

	
	public boolean checkCapability(Capability toCheck) {

		if (toCheck.getType() == ATTRIBUTES_CAPABILITY_TYPE) {

			for (Capability child : inner) {
				boolean valid = false;
				for (Capability toCheckInner : toCheck.getInnerCapabilities()) {
					if (child.checkCapability(toCheckInner)) {
						valid = true;
						break;
					}
				}
				if (!valid)
					return false;
			}
			return true;
		}

		return false;
	}

	
	public List<Capability> getInnerCapabilities() {
		
		return inner;
	}

	
	public int getType() {
		
		return ATTRIBUTES_CAPABILITY_TYPE;
	}

}
