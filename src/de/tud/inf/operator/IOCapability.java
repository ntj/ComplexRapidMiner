package de.tud.inf.operator;

import de.tud.inf.operator.capabilites.Capability;

public class IOCapability {

	private Class clazz;
	private Capability cap;
	
	
	public IOCapability(Class clazz, Capability cap) {
		this.clazz = clazz;
		this.cap = cap;
	}
	
	
	public Class getClazz() {
		return clazz;
	}
	public Capability getCapability() {
		return cap;
	}
	
	
}
