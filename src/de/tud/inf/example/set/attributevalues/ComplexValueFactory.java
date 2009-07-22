package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.operator.similarity.attributebased.uncertain.GaussProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.tools.Ontology;


/***
 * creates and manages a list of instantiated ComplexValues
 * @author Antje Gruner
 * @see ComplexValue
 */
public class ComplexValueFactory {

	private static Map<String, ComplexValue> flyweightList = new HashMap<String, ComplexValue>();
	
   
    public static ComplexValue getComplexValueFunction(int valueType,String hint) throws RuntimeException{
    	return getComplexValueFunction(0,valueType,hint);
    }
    
    public static ComplexValue getComplexValueFunction(int nrAttributes,int valueType,String hint) throws RuntimeException{
    	//first of all, check if symbol is already instantiated (appears in flyweightList)
    	String key = valueType+hint;
    	if(flyweightList.containsKey(key))
    		return flyweightList.get(key);
    	
    	ComplexValue cFunc = null;
		if((valueType == Ontology.ATTRIBUTE_VALUE_TYPE.SPARSE_MATRIX) 
								|| (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.MATRIX) 
				 				|| (valueType == Ontology.SPARSE_BINARY_MATRIX)
				 				|| (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.TENSOR)
				 				|| (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.HISTOGRAM)){
			//hint stores integer values to instantiate geometries
			try{
				String[] pList = hint.split(getParameterSep());
				int x = Integer.parseInt(pList[0]);
				if ((valueType == Ontology.ATTRIBUTE_VALUE_TYPE.HISTOGRAM))
					if(nrAttributes != 0)
						cFunc = new Histogram(nrAttributes,x,false);
					else throw new RuntimeException("Histogram instantiation not valid");
				else{
					int y = Integer.parseInt(pList[1]);
					if(valueType == Ontology.ATTRIBUTE_VALUE_TYPE.SPARSE_MATRIX)
						cFunc = new SparseMatrixValue(x,y);
					else if (valueType == Ontology.SPARSE_BINARY_MATRIX)
						cFunc = new SparseBinaryMatrixValue(x,y);
					else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.MATRIX)
						cFunc = new SimpleMatrixValue(x,y);
					else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.TENSOR)
						cFunc = new TensorValue(x,y,false); //TODO: how to check whether simple or sparse tensor????
					if(cFunc != null){
						flyweightList.put(key, cFunc);
						return cFunc;
					}
				}
			}
			catch(Exception e){
				throw new RuntimeException("Could not instantiated attribute "+ " with parameter string "+hint+". expected: 'x_y'");
			}
		}
		else if(valueType == Ontology.ATTRIBUTE_VALUE_TYPE.UNIFORM)
			cFunc = new SimpleProbabilityDensityFunction();
		else if(valueType == Ontology.ATTRIBUTE_VALUE_TYPE.GAUSS){
			if(nrAttributes != 0)
				cFunc = new GaussProbabilityDensityFunction(new SimpleMatrixValue(nrAttributes,nrAttributes));
			//TOTEST: else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(0,0));
			//else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(1,1));
			else throw new RuntimeException("Gauss pdf instantiation not valid");
		}
		
		
		else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.MAP)
			cFunc = new MapValue();
		else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.POINT_LIST)
			cFunc = new PointListValue();
		else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.ARRAY)
			cFunc = new ArrayValue();
		else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.DATA_MAP)
			cFunc = new DataMapValue();
		
		else if (valueType == Ontology.ATTRIBUTE_VALUE_TYPE.COMPLEX_VALUE)
			cFunc = new LinearKorrelation();
		if(cFunc != null){
			flyweightList.put(key, cFunc);
			return cFunc;
		}
		return null;  
    }
    
   
  
    
    /**
     * separates parameter from each other
     * @return
     */
    public static String getParameterSep(){
    	return "_";
    }
       
}
