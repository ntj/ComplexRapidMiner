package de.tud.inf.operator.fingerprints.ch;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.DataMapValue;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.table.DataMapAttribute;

public class ColorHistogram extends Operator{
	
	private static final String PARA_MAP_NAME = "map attribute name";
	
	public ColorHistogram(OperatorDescription description) {
		super(description);
	}
	
	@Override
	public IOObject[] apply() throws OperatorException {
		ComplexExampleSet input = getInput(ComplexExampleSet.class);
		Attribute mapAttr = input.getAttributes().get(getParameterAsString(PARA_MAP_NAME));
		DataMapAttribute histAttr =  (DataMapAttribute)AttributeFactory.createAttribute("hist",Ontology.DATA_MAP);
		input.addComplexAttribute(histAttr);
	
		Iterator<Example> it = input.iterator();
		while (it.hasNext()){
			Example ex = it.next();
			MapValue mapVal =  ex.getMapValue(mapAttr);
			Map<String, Integer> absoluteHist = new HashMap<String, Integer>();
			int numPix = mapVal.getMapSize();
			for(int i=0;i<numPix;i++){
				String currentSymbol = mapVal.getStringValueAt(i);
				if(absoluteHist.containsKey(currentSymbol)) {
					// increment
					absoluteHist.put(currentSymbol, (absoluteHist.get(currentSymbol) + 1));
				} else {
					// init
					absoluteHist.put(currentSymbol,1);
				}
				
			}
			//write back histogram
			//line 52
			DataMapValue dmValue = new DataMapValue(absoluteHist);
			ex.setComplexValue(histAttr, dmValue);
		}
		return new IOObject[] {input};
	}

	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterTypeString map_name = new ParameterTypeString(PARA_MAP_NAME, "name of the map attribute", "map");
		map_name.setExpert(false);
		types.add(map_name);
		
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
