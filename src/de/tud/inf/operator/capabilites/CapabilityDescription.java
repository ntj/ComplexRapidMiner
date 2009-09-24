package de.tud.inf.operator.capabilites;

import java.util.List;

import com.rapidminer.operator.Operator;


/**
 * stores Capabilities
 * 
 *
 */
public class CapabilityDescription {

	/**
	 *  for each input IOObject the capability required by this operator, is stored 
	 */
	private List<Capability> requiredInput;
	
	/**
	 * defines output capability, which describe IOObjects created by this operator
	 */
	private List<Capability> output;
	
	public CapabilityDescription(){
		this.requiredInput = null;
		this.output = null;
	}
	
	public CapabilityDescription(List<Capability> input, List<Capability> output) {
		this.requiredInput = input;
		this.output = output;
	}

	public List<Capability> getDeliveredOutputCapability(List<Capability> input, Operator operator){
		if(input != null){
			for(int i=0;i<input.size();i++)
				requiredInput.get(i).checkCapability(input.get(i));
		}
		return null;
	}
	
	public  List<Capability> getInputCapability(){
		return requiredInput;
	}
}
