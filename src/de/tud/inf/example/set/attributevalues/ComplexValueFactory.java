package de.tud.inf.example.set.attributevalues;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.operator.similarity.attributebased.uncertain.GaussProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.table.ComplexAttributeDescription;


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

	
	private static Map<String, ComplexValue> flyweightList = new HashMap<String, ComplexValue>();
	
   
    public static ComplexValue getComplexValueFunction(String symbol,String hint) throws RuntimeException{
    	return getComplexValueFunction(new ComplexAttributeDescription(null,null,symbol, "", hint));
    }
    
    public static ComplexValue getComplexValueFunction(int nrAttributes,String symbol,String hint) throws RuntimeException{
    	return getComplexValueFunction(new ComplexAttributeDescription(new int[nrAttributes],null,symbol, "", hint));  
    }
    
    public static ComplexValue getComplexValueFunction(ComplexAttributeDescription cad) throws RuntimeException{
    	//first of all, check if symbol is already instantiated (appears in flyweightList)
    	String key = cad.getSymbol()+cad.getHint();
    	if(flyweightList.containsKey(key))
    		return flyweightList.get(key);
    	
    	ComplexValue cFunc = null;
		if(cad.getSymbol().equals(symbol_sparseMatrix) 
								|| cad.getSymbol().equals(symbol_matrix) 
				 				|| cad.getSymbol().equals(symbol_sparseBinaryMatrix )
				 				|| cad.getSymbol().equals(symbol_tensor)
				 				|| cad.getSymbol().equals(symbol_histogram)){
			//hint stores integer values to instantiate geometries
			try{
				String[] pList = cad.getHint().split(getParameterSep());
				int x = Integer.parseInt(pList[0]);
				if (cad.getSymbol().equalsIgnoreCase(symbol_histogram))
					if(cad.getAttributeIndexes() != null)
						cFunc = new Histogram(cad.getAttributeIndexes().length,x,false);
					else throw new RuntimeException("Histogram instantiation not valid");
				else{
					int y = Integer.parseInt(pList[1]);
					if(cad.getSymbol().equals(symbol_sparseMatrix))
						cFunc = new SparseMatrixValue(x,y);
					else if(cad.getSymbol().equals(symbol_sparseBinaryMatrix))
						cFunc = new SparseBinaryMatrixValue(x,y);
					else if (cad.getSymbol().equals(symbol_matrix))
						cFunc = new SimpleMatrixValue(x,y);
					else if (cad.getSymbol().equals(symbol_tensor))
						cFunc = new TensorValue(x,y,false); //TODO: how to check whether simple or sparse tensor????
					if(cFunc != null){
						flyweightList.put(key, cFunc);
						return cFunc;
					}
				}
			}
			catch(Exception e){
				throw new RuntimeException("Could not instantiated attribute "+ cad.getName()+" with parameter string "+cad.getHint()+". expected: 'x_y'");
			}
		}
		else if(cad.getSymbol().equalsIgnoreCase(symbol_uPdf))
			cFunc = new SimpleProbabilityDensityFunction();
		else if(cad.getSymbol().equalsIgnoreCase(symbol_gaussPdf)){
			if(cad.getAttributeIndexes() != null)
				cFunc = new GaussProbabilityDensityFunction(new SimpleMatrixValue(cad.getAttributeIndexes().length,cad.getAttributeIndexes().length));
			//TOTEST: else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(0,0));
			//else cFunc = new GaussProbablitityDensityFunction(new SimpleMatrixValue(1,1));
			else throw new RuntimeException("Gauss pdf instantiation not valid");
		}
		else if(cad.getSymbol().equalsIgnoreCase(symbol_image))
			cFunc = null;
		else if (cad.getSymbol().equals(symbol_complex_value))
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
    
    
    public static int getType(String symbol){
    	//uncertain types
    	if(symbol.equals(symbol_uPdf))
    		return Ontology.UNIFORM;  	
    	if(symbol.equals(symbol_gaussPdf))
    		return Ontology.GAUSS;
    	if(symbol.equals(symbol_histogram))
    		return Ontology.HISTOGRAM;
    	
    	//matrix types
      	if(symbol.equals(symbol_matrix))
      		return Ontology.SIMPLE_MATRIX;
      	if(symbol.equals(symbol_sparseMatrix))
      		return Ontology.SPARSE_MATRIX;
      	if(symbol.equals(symbol_sparseBinaryMatrix))
      		return Ontology.SPARSE_BINARY_MATRIX;
      	
      	//other types
    	if(symbol.equals(symbol_tensor))
      		return Ontology.TENSOR;
    	
    	if(symbol.equals(symbol_complex_value))
      		return Ontology.COMPLEX_VALUE;
    	
    	//TODO: think whether ComplexValue better
      	else return Ontology.ATTRIBUTE_VALUE;
    }
}
