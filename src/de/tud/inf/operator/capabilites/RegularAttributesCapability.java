package de.tud.inf.operator.capabilites;

import java.util.ArrayList;
import java.util.List;


public class RegularAttributesCapability implements AttributeCapability{

	private BoolCapability inner;
	
	public void setCapability(BoolCapability cap) {
		
		inner = cap;
	}

	
	public boolean checkCapability(Capability toCheck) {

		if (toCheck.getType() == ATTRIBUTES_CAPABILITY_TYPE) {

		
			boolean valid = false;
			for (Capability toCheckInner : toCheck.getInnerCapabilities()) {
				if (inner.checkCapability(toCheckInner)) {
					valid = true;
					break;
				}
			}
			if (!valid)
				return false;

			return true;
		}

		return false;
	}

	
	public List<Capability> getInnerCapabilities() {
		ArrayList<Capability> list = new ArrayList<Capability>();
		list.add(inner);
		return list;
	}

	
	public int getType() {
		
		return ATTRIBUTES_CAPABILITY_TYPE;
	}

}
