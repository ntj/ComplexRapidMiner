package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.operator.similarity.attributebased.uncertain.GaussProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.table.ComplexAttribute;


/***
 * creates and manages a list of instantiated ComplexValues
 * @author Antje Gruner
 * @see ComplexValue
 */
public class ComplexValueFactory {

	private static Map<String, ComplexValue> flyweightList = new HashMap<String, ComplexValue>();
	
	public static ComplexValue getComplexValueFunction(ComplexAttribute a){
		return getComplexValueFunction(a.getInnerAttributeCount(),a.getValueType(),a.getHint());
	}
	
    public static ComplexValue getComplexValueFunction(int valueType,String hint) throws RuntimeException{
    	return getComplexValueFunction(0,valueType,hint);
    }
    
    public static ComplexValue getComplexValueFunction(int nrAttributes,int valueType,String hint) throws RuntimeException{
    	//first of all, check if symbol is already instantiated (appears in flyweightList)
    	String key = valueType+ getParameterSep()+hint;
    	if(flyweightList.containsKey(key))
    		return flyweightList.get(key);
    	
    	ComplexValue cFunc = null;
		if((valueType == Ontology.SPARSE_MATRIX) 
								|| (valueType == Ontology.MATRIX) 
				 				|| (valueType == Ontology.SPARSE_BINARY_MATRIX)
				 				|| (valueType == Ontology.TENSOR)
				 				|| (valueType == Ontology.HISTOGRAM)){
			//hint stores integer values to instantiate geometries
			try{
				String[] pList = hint.split(getParameterSep());
				int x = Integer.parseInt(pList[0]);
				if ((valueType == Ontology.HISTOGRAM))
					if(nrAttributes != 0)
						cFunc = new Histogram(nrAttributes,x,false);
					else throw new RuntimeException("Histogram instantiation not valid");
				else{
					int y = Integer.parseInt(pList[1]);
					if(valueType == Ontology.SPARSE_MATRIX)
						cFunc = new SparseMatrixValue(x,y);
					else if (valueType == Ontology.SPARSE_BINARY_MATRIX)
						cFunc = new SparseBinaryMatrixValue(x,y);
					else if (valueType == Ontology.MATRIX)
						cFunc = new SimpleMatrixValue(x,y);
					else if (valueType == Ontology.TENSOR)
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
		else if(valueType == Ontology.UNIFORM)
			cFunc = new SimpleProbabilityDensityFunction();
		else if(valueType == Ontology.GAUSS){
			if(nrAttributes != 0)
				cFunc = new GaussProbabilityDensityFunction(new SimpleMatrixValue(nrAttributes,nrAttributes));
			//TOTEST: else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(0,0));
			//else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(1,1));
			else throw new RuntimeException("Gauss pdf instantiation not valid");
		}
		
		
		else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType,Ontology.MAP))
			cFunc = new MapValue();
		else if (valueType == Ontology.POINT_LIST)
			cFunc = new PointListValue();
		else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType,Ontology.ARRAY)){
			//check if ConstantArrayValue (hint contains information about dimensions of array) or if parameterized array value)
			try{
				String[] pList = hint.split(getParameterSep());
				int x = Integer.parseInt(pList[0]);
				int y = Integer.parseInt(pList[1]);
				cFunc = new ConstantArrayValue(x,y);
			}
			catch(Exception e){
				throw new RuntimeException("array value instantiation not valid");
			}
		}
		else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType,Ontology.DATA_MAP))
			cFunc = new DataMapValue();
		
		else if (valueType == Ontology.COMPLEX_VALUE)
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
