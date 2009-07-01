package de.tud.inf.operator.io;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.parameter.UndefinedParameterError;

import de.tud.inf.example.table.ComplexExampleTable;
import de.tud.inf.example.table.RelationalAttribute;

/***
 * reads a complex arff file, but ignores complex information, i.e. complex function and its parameters
 * since it is not possible to ignore relational attributes within a complex arff, (e.g. with matrix values for each row) and to map them to a simple attribute makes no
 * sense an exception is thrown if relational attributes occur in the dataset
 * @author Antje Gruner
 *
 */
public class FlatComplexArffReader extends ComplexArffReader{

	public FlatComplexArffReader(StreamTokenizer tokenizer,
			ArffExampleSource arffES, String parameter_sample_size,
			String parameter_sample_ratio, String parameter_datamanagement,
			String parameter_local_random_seed,
			String parameter_decimal_point_character) {
		super(tokenizer, arffES, parameter_sample_size, parameter_sample_ratio,
				parameter_datamanagement, parameter_local_random_seed,
				parameter_decimal_point_character);
	}
	
	/***
	 * 
     * @param tokenizer
     * @param attributeName name of the relational Attribute
     * @param depAttribute is true if relational attribute appears in dependency information of arff file
     * @return relational Attribute with wrapped innerAttributes 
	 
	protected Attribute readRelationalAttribute(StreamTokenizer tokenizer, String attributeName, boolean depAttribute) throws IOException{
    	if(!depAttribute)
    		throw new IOException("complex arff file contains attribute type "+ tokenizer.sval + ", which could not be mapped to a primitive attribute");		
    	else return super.readRelationalAttribute(tokenizer, attributeName, depAttribute);
	}
	*/
	@Override
	/**
	 * the data ExampleTable is build without those attributes which serve as parameters of complex attributes
	 */
	protected ComplexExampleTable buildTable(List<Attribute> attributes,ExampleTable depEt) throws UndefinedParameterError, IOException{
		//read the "real" dataset
		ExampleTable et = readData(attributes);
		//reads rows of dependency information
		DataRowReader reader = depEt.getDataRowReader();
		DataRow row;
		
		Attribute paramA = null;
		//find that concrete attribute in dependency example table which stores parameter information
		for(int i =0; i<depEt.getAttributeCount();i++){
			Attribute a;
			a = depEt.getAttribute(i);
			if(a != null)
				if(a.getName().equals(ComplexArffDescription.depParamName)){
					paramA = a; //TODO: check reference, or clone? (reference should work)
					break;
				}
		}
		
		if(paramA != null){
			//find concrete parameters of complex functions and store names in list
			ArrayList<String> params = new ArrayList<String>(); 
			while (reader.hasNext()){
				row = reader.next();
				//fetch value list of relational parameter attribute 
				double[][] pNames = row.getRelativeValuesFor(paramA.getTableIndex());
				//extract parameter names (first and only entries in pNames list)
				if(pNames != null)
					for(int j= 0;j<pNames.length;j++)
						params.add(((RelationalAttribute)paramA).getInnerAttributeAt(0).getMapping().mapIndex((int)pNames[j][0]));
			}
			//remove parameter attributes from example table attribute list
			for(int i =0;i<et.getNumberOfAttributes();i++){
				Attribute a = et.getAttribute(i);
				if( (a!= null)&&(params.contains(a.getName()))){
					et.removeAttribute(i);
				}
			}
		}
		//check if there are relational attributes in dataSet, which are NO parameter attributes of complex attributes
		for(int i =0; i<et.getAttributeCount();i++){
			Attribute a = et.getAttribute(i);
			if((a != null)&&(a.isRelational()))
				throw new IOException("complex arff file contains relational attribute "+a.getName()+" which could not be mapped to a primitive attribute");
		}
		return new ComplexExampleTable(et);
	}
}
