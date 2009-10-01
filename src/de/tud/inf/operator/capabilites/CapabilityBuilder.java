package de.tud.inf.operator.capabilites;

import com.rapidminer.tools.Ontology;

public class CapabilityBuilder {

	public static Capability buildCapability(int[] regularAttributeCaps, boolean isAnd){
		AttributeCapability ac = new RegularAttributesCapability();
		ac.setInnerTypes(new int[]{Ontology.DATA_MAP}, true);
		return ac;
	}
}
