package de.tud.inf.operator.capabilites;

import java.util.ArrayList;
import java.util.List;

public abstract class AttributeCapability implements Capability{
	
	protected BoolCapability inner;
	
	
	public void setCapability(BoolCapability cap) {
		this.inner = cap;	
	}
	
	public void setInnerTypes(int[] types, boolean isAnd){
		if(isAnd)
			inner = new AndCapability();
		else inner = new OrCapability();
		
		for(int i=0;i<types.length;i++){
			inner.addCapability(new AttributeTypeCapability(types[i]));
		}
	}
	
	public boolean checkCapability(Capability toCheck) {
		
		if(toCheck.getType() == this.getType()) {
			if(inner == null) return true;
			
			boolean valid = false;
			for(Capability toCheckChild : toCheck.getInnerCapabilities()) {
				if(inner.checkCapability(toCheckChild)) {
					valid = true;
					break;
				}
			}
			if(!valid)
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
	
	public abstract int getType();
	
}
	
