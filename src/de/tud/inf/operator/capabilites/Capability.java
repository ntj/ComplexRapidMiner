package de.tud.inf.operator.capabilites;

import java.util.List;

public interface Capability {
	
	public static final int EXAMPLE_SET_CAPABILITY_TYPE = 0;
	public static final int ATTRIBUTES_CAPABILITY_TYPE = 1;
	public static final int AND_CAPABILITY_TYPE = 2;
	public static final int OR_CAPABILITY_TYPE = 3;
	public static final int ATTRIBUTE_CAPABILITY_TYPE = 4;
	public static final int LABEL_CAPABILITY_TYPE = 5;
	public static final int ID_CAPABILITY_TYPE = 6;
	
	public boolean checkCapability(Capability toCheck);
	
	public int getType();
	
	//public void addCapability(Capability cap);
	
	public List<Capability> getInnerCapabilities();
}
