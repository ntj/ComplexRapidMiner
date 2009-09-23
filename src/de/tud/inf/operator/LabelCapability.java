
package de.tud.inf.operator;

import java.util.ArrayList;
import java.util.List;

public class LabelCapability implements Capability{

	private List<Capability> inner;
	
	public LabelCapability(List<Capability> inner) {
		this.inner = inner;
	}
	
	public LabelCapability() {
		this.inner = new ArrayList<Capability>();
	}
	
	public void addCapability(Capability cap) {
		inner.add(cap);
	}

	public boolean checkCapability(Capability toCheck) {
		
		if(toCheck.getType() == LABEL_CAPABILITY_TYPE) {
			if(inner == null || inner.size() == 0)
				return true;
			for(Capability child : inner) {
				boolean valid = false;
				for(Capability toCheckChild : toCheck.getInnerCapabilities()) {
					if(child.checkCapability(toCheckChild)) {
						valid = true;
						break;
					}
				}
				if(!valid)
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
		return Capability.LABEL_CAPABILITY_TYPE;
	}

}
