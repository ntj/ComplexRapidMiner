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

	//symbols which represents complex value functions
	//TODO: remove those strings, they already appear when calling Ontology.VALUE_TYPE_NAMES[Ontology.UNIFORM] etc.
	//uncertain symbols
	private static final String symbol_uPdf = "uniform";
	private static final String symbol_gaussPdf = "gauss";
	private static final String symbol_histogram = "hist";
	//matrix symbols
	private static final String symbol_matrix = "matrix";
	private static final String symbol_sparseMatrix = "spMatrix";
	private static final String symbol_sparseBinaryMatrix = "spBinMatrix";
	
	//other symbols
	private static final String symbol_tensor = "tensor";
	
	//other symbols
	private static final String symbol_complex_value = "complex_value";
	
	//other symbols (not implemented)
	private static final String symbol_image = "image";
	private static final String symbol_map = Ontology.VALUE_TYPE_NAMES[Ontology.MAP];

	
	private static Map<String, ComplexValue> flyweightList = new HashMap<String, ComplexValue>();
	
   
    public static ComplexValue getComplexValueFunction(String symbol,String hint) throws RuntimeException{
    	return getComplexValueFunction(0,symbol,hint);
    }
    
    public static ComplexValue getComplexValueFunction(int nrAttributes,String symbol,String hint) throws RuntimeException{
    	//first of all, check if symbol is already instantiated (appears in flyweightList)
    	String key = symbol+hint;
    	if(flyweightList.containsKey(key))
    		return flyweightList.get(key);
    	
    	ComplexValue cFunc = null;
		if(symbol.equals(symbol_sparseMatrix) 
								|| symbol.equals(symbol_matrix) 
				 				|| symbol.equals(symbol_sparseBinaryMatrix )
				 				|| symbol.equals(symbol_tensor)
				 				|| symbol.equals(symbol_histogram)){
			//hint stores integer values to instantiate geometries
			try{
				String[] pList = hint.split(getParameterSep());
				int x = Integer.parseInt(pList[0]);
				if (symbol.equalsIgnoreCase(symbol_histogram))
					if(nrAttributes != 0)
						cFunc = new Histogram(nrAttributes,x,false);
					else throw new RuntimeException("Histogram instantiation not valid");
				else{
					int y = Integer.parseInt(pList[1]);
					if(symbol.equals(symbol_sparseMatrix))
						cFunc = new SparseMatrixValue(x,y);
					else if(symbol.equals(symbol_sparseBinaryMatrix))
						cFunc = new SparseBinaryMatrixValue(x,y);
					else if (symbol.equals(symbol_matrix))
						cFunc = new SimpleMatrixValue(x,y);
					else if (symbol.equals(symbol_tensor))
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
		else if(symbol.equalsIgnoreCase(symbol_uPdf))
			cFunc = new SimpleProbabilityDensityFunction();
		else if(symbol.equalsIgnoreCase(symbol_gaussPdf)){
			if(nrAttributes != 0)
				cFunc = new GaussProbabilityDensityFunction(new SimpleMatrixValue(nrAttributes,nrAttributes));
			//TOTEST: else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(0,0));
			//else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(1,1));
			else throw new RuntimeException("Gauss pdf instantiation not valid");
		}
		else if(symbol.equalsIgnoreCase(symbol_image))
			cFunc = null;
		else if (symbol.equals(symbol_map))
			cFunc = new MapValue();
		else if (symbol.equals(symbol_complex_value))
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
