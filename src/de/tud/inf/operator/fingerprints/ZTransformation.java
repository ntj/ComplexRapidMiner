package de.tud.inf.operator.fingerprints;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.MapAttribute;

/**
 * 
 * @author Antje Gruner
 *
 */
public class ZTransformation extends Operator{

	public static final String PARAMETER_MAP_NAME = "map_attribute_name";
	
	public ZTransformation(OperatorDescription description) {
		super(description);
		// TODO Auto-generated constructor stub
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ComplexExampleSet exampleSet = null;
		MapValue mv = null;
		double mean;
		double stdDerv;
		try {
			// get input
			exampleSet = getInput(ComplexExampleSet.class);
			// get map attribute according to parameters
			try{
				MapAttribute mapAttr = (MapAttribute)exampleSet.getAttributes().get(getParameterAsString(PARAMETER_MAP_NAME));
				Iterator<Example> it = exampleSet.iterator();
				Example e;
				while(it.hasNext()){
					e = it.next();
					mv = e.getMapValue(mapAttr);
					//get former mean and variance
					mean = mv.getAverage();
					stdDerv = Math.sqrt(mv.getVariance());
					//modify values according to zTransform = (value - mean) / sqrt(variance) 
					double[] zValues = mv.getZValues();
					for(int j=0;j<zValues.length;j++){
						zValues[j] = (zValues[j] - mean)/stdDerv;
					}
					//write complex values back to dataRow
					e.setComplexValue(mapAttr, mv);
				}
			}
		catch(ClassCastException e){
			throw new OperatorException("attribute name parameter is no map attribute");
		}
		} catch (Exception e) {
			System.out.println("Exception in ZTransformation " + e.toString());
		}
		
		return new IOObject[] {exampleSet};
	}
	
	
	
	/** Returns a list of ParameterTypes describing the parameters of this operator. */
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_MAP_NAME, "", "map"));
		return types;
	}
	

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] {ComplexExampleSet.class}; 
	}

	
	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] {ComplexExampleSet.class}; 
	}

}
