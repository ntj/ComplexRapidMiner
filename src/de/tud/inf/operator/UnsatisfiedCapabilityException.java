package de.tud.inf.operator;

import com.rapidminer.operator.Operator;

import de.tud.inf.operator.capabilites.Capability;

public class UnsatisfiedCapabilityException extends Exception{
	
	private static final long serialVersionUID = 4767804600176277866L;
	
	private Operator operator;
	
	public UnsatisfiedCapabilityException(Operator o, Class inputClass, Capability inputCap){
		super("Operator requires input class " + inputClass.getName() + "with capability " + inputCap.getAsString());
		operator  = o;
	}
	public Operator getOperator() {
		return operator;
	}

	
}
