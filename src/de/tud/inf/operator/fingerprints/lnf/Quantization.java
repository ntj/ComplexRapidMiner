package de.tud.inf.operator.fingerprints.lnf;



import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.ComplexAttribute;
import de.tud.inf.example.table.MapAttribute;

public class Quantization extends Operator {

	public static final String PARAMETER_MAP_NAME = "map_attribute_name";
	
	public Quantization(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		
		// get input
		ComplexExampleSet exampleSet = getInput(ComplexExampleSet.class);
		
		// get input attribute for quantization according to parameters
		ComplexAttribute mapInAttr = (ComplexAttribute)exampleSet.getAttributes().get(getParameterAsString(PARAMETER_MAP_NAME));
		if(mapInAttr == null) throw new OperatorException("map attribute \"" + getParameterAsString(PARAMETER_MAP_NAME) + "\" misses.");
		
		//create output attribute
		MapAttribute mapOutAttr = (MapAttribute)AttributeFactory.createAttribute(mapInAttr.getName() + "_quantization", Ontology.MAP_STRING);
		
		exampleSet.addComplexAttribute(mapOutAttr);
		
		// z discretization
		double stepSize = getParameterAsDouble("step size");
		char startSymbol = getParameterAsString("start letter").charAt(0);
			
		for (Example example: exampleSet)
		{
			MapValue map = example.getMapValue(mapInAttr);
			map.createMapping();
			double[] zValues = map.getZValues();
			for (int h=0; h<zValues.length;h++){
				int quantile = (int) Math.ceil(Math.abs(zValues[h])/stepSize);
				char symbol = startSymbol;
				if (zValues[h] >= 0) {
					for (int i=0; i<quantile-1; i++)
						symbol++;
				}
				else if (zValues[h] < 0) {
					for (int i=0; i<quantile; i++)
						symbol--;
				}
				//write symbol into map (-> zValue will be modified)
				map.mapValueAt(h, "" + symbol);
			} //end processing one map
			//write back
			example.setComplexValue(mapOutAttr, map);
		}
		
		// some statistics
		exampleSet.recalculateAllAttributeStatistics();
	//	ProcessStatistics.getInstance().addNumLetters((int)((Math.ceil(exampleSet.getStatistics(zAttr, Statistics.MAXIMUM) / stepSize)) + (Math.ceil(Math.abs(exampleSet.getStatistics(zAttr, Statistics.MINIMUM)) / stepSize))));
		
		return new IOObject[] {exampleSet};
	}
	
	
	/** Returns a list of ParameterTypes describing the parameters of this operator. */
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeString(PARAMETER_MAP_NAME, "", "map"));
		types.add(new ParameterTypeDouble("step size", "", 0,10,1));
		types.add(new ParameterTypeString("start letter", "", "M"));
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
