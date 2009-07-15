package de.tud.inf.example;

import java.util.List;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.table.ComplexAttributeDescription;
import de.tud.inf.example.table.RelationalAttribute;
import de.tud.inf.example.table.ComplexAttribute.ComplexClassType;

/**
 * 
 * @author Antje Gruner
 *	checks, if complex attributes, defined by attribute description, can be instantiated (semantically, syntax is checked in complexArffReader
 *	
 *
 */
public class ComplexAttributeConstraintChecker {

	public static boolean checkConstraints(ExampleTable et, List<ComplexAttributeDescription> etDep) throws RuntimeException{
		String messg = "";
		//String lineSep = Tools.getLineSeparators(2);
		String lineSep = "\n";
		
		//test each description
		for(ComplexAttributeDescription cad: etDep){
			int valueType = Ontology.ATTRIBUTE_VALUE_TYPE.mapName(cad.getSymbol());
			if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.COMPLEX_VALUE)){
				if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.UNIFORM) && (cad.getParamIndexes().length >1) )
					messg += "uncertain value with uniform pdf expects exactly one parameter (uncertainty)" + lineSep;
				ComplexClassType ct = ComplexClassType.Undefined;
				if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.MATRIX))
					ct = ComplexClassType.Proxy;
				
				//do some checks on inner attributes
				if(cad.getAttributeIndexes() == null || cad.getAttributeIndexes().length == 0)
					messg +=  "attribute " +cad.getName()+ " must contain at least one inner attribute";
				//do some tests on gauss attribute
				if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.GAUSS))
					//1. gauss expects as parameters! one relational attribute
					if(cad.getParamIndexes().length != 1)
						messg += "gauss attribute must have one relational parameter attribute, which stores values of variance matrix";
					else{
						//1.2 check if parameter attribute is relational
						int pId = cad.getParamIndexes()[0];
						for(int i=0;i<et.getNumberOfAttributes();i++)
							if(et.getAttribute(i).getTableIndex() == pId)
								if(!et.getAttribute(i).isRelational()) messg += "gauss attribute " +cad.getName() + "'s parameter attribute must be relational" + lineSep;
								else { 
									RelationalAttribute relA = (RelationalAttribute)et.getAttribute(i);
									//test if relational parameter attribute can serve as variance matrix attribute
									if(relA.getInnerAttributeCount() == 0  || relA.getInnerAttributeCount() > 2)
										messg += "gauss attribute " +cad.getName() + "'s parameter attribute must have one or two inner attributes, which store values of covariance matrix" + lineSep;
									else if (!relA.getInnerAttributeAt(0).isNumerical())
										messg += "gauss attribute " +cad.getName() + "'s parameter attribute first inner attribute serves as key for matrix entries and therefore must be numerical" + lineSep;
								}
					}
				else if (ct == ComplexClassType.Proxy){
					//1. just one attribute should be wrapped
					if(cad.getAttributeIndexes().length != 1) messg += "attribute " +cad.getName() + " must have exactly one inner attribute " + lineSep;
					//2. this attribute must be relational
					RelationalAttribute relA = null;
					for(int i=0;i<et.getNumberOfAttributes();i++)
						if(et.getAttribute(i).getTableIndex() == cad.getAttributeIndexes()[0])
							if(!et.getAttribute(i).isRelational()) messg += "attribute " +cad.getName() + "'s inner attribute must be relational " + lineSep;
							else relA = (RelationalAttribute)et.getAttribute(i);
					if(relA != null){
						//constraints concerning matrix attributes
						if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.MATRIX)){
							String[] pList = cad.getHint().split(ComplexValueFactory.getParameterSep());
							if(pList.length != 2)
								messg += "Hint of matrix attribute "+ cad.getName() +" is not valid, must be 'rows_columns' ";
							else{
								try{
									Integer.parseInt(pList[0]);
									Integer.parseInt(pList[1]);
								}catch (NumberFormatException e){
									messg += messg += "Hint of matrix attribute "+ cad.getName() +" is not valid, must be 'rows_columns' ";
								}
							}
							if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.SIMPLE_MATRIX)){
								if (relA.getInnerAttributeCount() != 1)
									messg += "matrix attribute " +cad.getName() + " must wrap a relational attribute with exactly one inner attribute" + lineSep;
							}
							else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.SPARSE_MATRIX)){
								//sparse matrix inner relational attributes 
								if (relA.getInnerAttributeCount() != 2)
									messg += "sparse matrix attribute " +cad.getName() + " must wrap relational attribute with exactly two inner attributes";
								else if(!Ontology.ATTRIBUTE_VALUE_TYPE.isA(relA.getInnerAttributeAt(0).getValueType(),Ontology.NUMERICAL)) 
									messg += "sparse matrix attribute " +cad.getName() + " must wrap relational attribute which inner first attribute serves as key and therefore must be numerical" + lineSep;
							}
							else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.SPARSE_BINARY_MATRIX)){
								if (relA.getInnerAttributeCount() != 1)
									messg += "sparse matrix attribute " +cad.getName() + " must wrap relational attribute with exactly one inner attribute";
								else if(!Ontology.ATTRIBUTE_VALUE_TYPE.isA(relA.getInnerAttributeAt(0).getValueType(),Ontology.NUMERICAL)) 
									messg += "sparse matrix attribute " +cad.getName() + " must wrap a relational attribute which inner attribute serves as key and therefore must be numerical" + lineSep;
							}
							else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(valueType, Ontology.MAP)){
								if (relA.getInnerAttributeCount() != 1)
									messg += "map attribute " +cad.getName() + " must wrap relational attribute with exactly one inner attribute";
							}
						}
					}
					else messg+= "complex attribute " + cad.getName() + " must wrap one attribute which is relational";
				}
			}
			else { messg += "symbol "+cad.getSymbol()+" is unknown";}
		}
		if(messg != ""){
			throw new RuntimeException(messg);
		}
		return true;
	}



	

}
