package de.tud.inf.operator.io;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.operator.io.ArffReader;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;

import de.tud.inf.example.table.ComplexAttributeDescription;
import de.tud.inf.example.table.ComplexAttributeFactory;
import de.tud.inf.example.table.ComplexExampleTable;
import de.tud.inf.example.table.RelationalAttribute;

public class ComplexArffReader extends ArffReader{

	
	public ComplexArffReader(StreamTokenizer tokenizer,
			ArffExampleSource arffES, String parameter_sample_size,
			String parameter_sample_ratio, String parameter_datamanagement,
			String parameter_local_random_seed,
			String parameter_decimal_point_character) {
		
		super(tokenizer, arffES, parameter_sample_size, parameter_sample_ratio,
				parameter_datamanagement, parameter_local_random_seed,
				parameter_decimal_point_character);
	}

	
	/**
	 * builds two example tables, one with dependency information, one with data information, and merges them to create a complex example table
	 */
	@Override
	public ComplexExampleTable read() throws IOException,UndefinedParameterError {
			Tools.getFirstToken(tokenizer);
			
			if(ComplexArffDescription.depAnnotation.equalsIgnoreCase(tokenizer.sval)) {
				Tools.getNextToken(tokenizer);
           		Tools.getLastToken(tokenizer, false);
			}
			//extract dependency information from following attributes
			List<Attribute> depAttributes = readAttributes(true);
			if(depAttributes.size() == 0) new IOException("no attribute defintion for dependency relation found");
			
			//check if dependency information only contains attribute names which are allowed in dependency part (recognize misspelled attribute names)
			List<String> names = Arrays.asList(new String[]{ComplexArffDescription.depAttName,
															ComplexArffDescription.depParamName,
															ComplexArffDescription.depClassName,
															ComplexArffDescription.depInnerAttributesName,
															ComplexArffDescription.depHintName});
			for(Attribute a: depAttributes)
				if(!names.contains(a.getName()))
					throw new IOException("attribute name '" +a.getName()+"' is not allowed in dependency section of complex arff file");
			//terminates @data of dependency by checking if there is a @relation - Annotation (thats why additional function)
			ExampleTable depEt = readDependencyData(depAttributes);
			
			if("@relation".equalsIgnoreCase(tokenizer.sval)) {
				Tools.getNextToken(tokenizer);
           		Tools.getLastToken(tokenizer, false);
			}
			List<Attribute> attributes = readAttributes(false);
			
			//now check if dependency information is correct, if records of dependency table contain valid references to attribute names
			//1. collect table indexes of attributes (not necessary if table attributes, but with names)
			names = new LinkedList<String>();
			for(Attribute a: attributes)
				names.add(a.getName());
			
			/*
				String incorrAtt = "";
				boolean corrDeps = true;
				for(Attribute a : depEt.getAttributes())
					//2. check if attribute could be valid (here: just check if possible nominal values are valid (no dependency information rows)
					if(a.getName().equals(ComplexArffDescription.depInnerAttributesName)){
						List<String> atts = ((RelationalAttribute)a).getInnerAttributes().get(0).getMapping().getValues();
						for(String strA: atts)
							if(!names.contains(strA)){
								corrDeps = false;
								incorrAtt = strA;
								break;
							}
					}
	
				if(corrDeps){ //indicates that dependency section does not contain innerAttriubte stuff, which is invalid (not really)
					return buildTable(attributes, depEt);
				}
				else{
					throw new IOException("dependency information attribute contains inncorrect attribute name "+incorrAtt+".");
				}
			*/
			return buildTable(attributes, depEt);
	}

	/**
	 * read the data section of an ARFF - file
	 * @param attributes attribute information of attribute section in arff, which is already read
	 * @param depEt dependency information example table
	 * @return ComplexExampleTable which contains attributes + dependency information
	 * @throws UndefinedParameterError
	 * @throws IOException
	 */
	protected ComplexExampleTable buildTable(List<Attribute> attributes,ExampleTable depEt) throws UndefinedParameterError, IOException{
		//read the "real" dataset
		ExampleTable et = readData(attributes);

		List<ComplexAttributeDescription> depList = createValidDependencyList(depEt,et);
		try{
			//ComplexAttributeConstraintChecker.checkConstraints(et, depList);
			for(ComplexAttributeDescription desc : depList)
				desc.checkConstraints(et);
		}catch(RuntimeException e){
			throw new IOException(e.getMessage());
		}
		return new ComplexExampleTable(et,depList);
	}
	

	/***  
     * @param tokenizer
     * @param attributeName name of the relational Attribute
     * @param depAttribute is true if relational attribute appears in dependency information of .arff file
     * @return relational Attribute with wrapped innerAttributes 
	 */
	@Override
	protected  Attribute readRelationalAttribute(StreamTokenizer tokenizer, String attributeName, boolean depAttribute) throws IOException{
				
			RelationalAttribute attribute = null; 
	        // get the name
	        Tools.getFirstToken(tokenizer);
	        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
	            throw new IOException("unexpected end of file in line " + tokenizer.lineno() + ", attribute description expected...");
	        }
	        ArrayList<Attribute> innerAttributes = createInnerAttributes(tokenizer,attributeName);
	        if(innerAttributes != null && innerAttributes.size()>0){
	        	attribute = (RelationalAttribute)AttributeFactory.createAttribute(attributeName,Ontology.RELATIONAL);
		        attribute.setInnerAttributes(innerAttributes);
		    	return attribute;
	        }
	        else throw new IOException("relational attributes should contain at least one inner attribute");
	}
	
	/***
     * create list of inner attributes of a relational attribute
     * @param tokenizer
     * @param attributeName name of the relational Attribute
     * @return innerAttributes
     */
    private ArrayList<Attribute> createInnerAttributes(StreamTokenizer tokenizer, String attributeName) throws IOException{
    	ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    	  while ("@attribute".equalsIgnoreCase(tokenizer.sval)) {
              Attribute attribute = createAttribute(tokenizer,false);
              attributes.add(attribute);
    	  }
    	  if("@end".equalsIgnoreCase(tokenizer.sval)){
    		  tokenizer.nextToken();
    		  if(attributeName.equalsIgnoreCase(tokenizer.sval))
    			  return attributes;
    		  else throw new IOException("relational attribute definition is false, should be '@end "+ attributeName +"'");
    	  }
    	  else throw new IOException("relational attribute end definition is false, should be '@end "+ attributeName +"'");
    }
    
    
    private ExampleTable readDependencyData(List<Attribute> depAttributes) throws IOException{
    	if (!"@data".equalsIgnoreCase(tokenizer.sval)) {
           throw new IOException("expected keyword '@data' in line " + tokenizer.lineno());
        }  
        // check attribute number
        if (depAttributes.size() == 0) {
               throw new IOException("no attributes were declared in the ARFF file, please declare attributes with the '@attribute' keyword.");
        }
          
        // fill data table
        MemoryExampleTable table = new MemoryExampleTable(depAttributes);
        Attribute[] attributeArray = table.getAttributes();
        DataRowFactory factory = new DataRowFactory(DataRowFactory.TYPE_INT_ARRAY, '.');
        DataRow dataRow = null;

      
        while (((dataRow = createDataRow(tokenizer, true, factory, attributeArray)) != null)) {
             table.addDataRow(dataRow);
        }   
        return table;
    }
    
    /***
     * creates the list of information about valid complex attributes from the two exampleTables
     * @param depEt created from description section in ARFF file
     * @param et created from relation section in ARFF file
     * @return 
     * @throws IOException 
     */
    private List<ComplexAttributeDescription> createValidDependencyList(ExampleTable depEt, ExampleTable et) throws IOException{
    	
    	//collect map String - TableIndexes from et
    	Map<String,Integer> nameIndexMap = new HashMap<String,Integer>();
    	for(int i=0;i<et.getNumberOfAttributes();i++)
    		if(et.getAttribute(i) != null)
    			nameIndexMap.put(et.getAttribute(i).getName(), et.getAttribute(i).getTableIndex());
    		
    	List<ComplexAttributeDescription> etDependencies = new ArrayList<ComplexAttributeDescription>();
    	//name of the current dependency information attribute
		String name;
		//className, parameter tableIndex list and attribute table index list form a ExampleTableDependency
		String symbol;
		String attName = null;
		String hint;
		int[] params = null;
		int[] attributes = null;
		//identifies the attribute within a relational attribute -> in our case just one nominal attribute is necessary
		//and should be the first (and only) innerAttribute in attributes and parameters description
		Attribute innerA;
		
		DataRowReader reader = depEt.getDataRowReader();
		//example table dependency id = nr of datarow
		//complex dataRow information for a concrete relational attribute
		double[][] relValues;
		int count =0;
		Integer attIndex;
		//read dataRows of dependency section
		while(reader.hasNext()){
			DataRow row = reader.next();
			count++;
			//find symbol
			symbol = null; hint = null; params = null; attributes = null;
			for(Attribute a1: depEt.getAttributes()){
				name = a1.getName();
				if(a1.getName().equals(ComplexArffDescription.depClassName) && a1.isNominal())
					 symbol = a1.getMapping().mapIndex((int)a1.getValue(row));
				else if(a1.getName().equals(ComplexArffDescription.depHintName))
			 	   	 hint = a1.getMapping().mapIndex((int)a1.getValue(row));
				else if(name.equals(ComplexArffDescription.depAttName)) {
					 	attName = a1.getMapping().mapIndex((int)a1.getValue(row));
				}else if( a1.isRelational()  && (a1.getName().equals(ComplexArffDescription.depParamName))||(a1.getName().equals(ComplexArffDescription.depInnerAttributesName))){
					 relValues = row.getRelativeValuesFor(a1.getTableIndex());
					 if (((RelationalAttribute)a1).getInnerAttributeCount()!=1)
						 throw new IOException("relational attribute '"+a1.getName()+"' must contain exactly one inner attribute");
					 //the first (and only) innerAttribute should be a nominal one
					 innerA = ((RelationalAttribute)a1).getInnerAttributes().get(0);
					 if(innerA.isNominal()){
						 //fetch all table indexes of correlating attributes/parameters
						 if(name.equals(ComplexArffDescription.depParamName)){
							//parameters must not be there
							 if((relValues!=null) && relValues.length>0){
									 params = new int[relValues.length];
									 for(int i=0;i<relValues.length;i++){
										 int tId = (int)relValues[i][0]; //relational parameter attribute just has ONE inner attribute
										 String tName = innerA.getMapping().mapIndex(tId);
										 attIndex = nameIndexMap.get(tName);
										 if(attIndex == null)
											 throw new IOException("parameter attribute "+ tName + " does not exist in the dataset");
										 else params[i] = nameIndexMap.get(tName).intValue();
									 }
								 }
						 }else if(name.equals(ComplexArffDescription.depInnerAttributesName))
							 if((relValues!=null) && relValues.length>0){
								 attributes = new int[relValues.length];
								 for(int i=0;i<relValues.length;i++){
									 int tId = (int)relValues[i][0]; //relational innerAttribute attribute just has ONE inner attribute
									 String tName = innerA.getMapping().mapIndex(tId);
									 attIndex = nameIndexMap.get(tName);
									 if(attIndex == null)
										 throw new IOException("dependency attribute "+ tName +" does not exist in the dataset");
									 else attributes[i] = nameIndexMap.get(tName).intValue();
								 }
							 }else throw new IOException("dependency data in row "+ count+" must contain at least one inner attribute");
					 }
					 else throw new IOException("inner attribute "+ innerA.getName() +" of attribute "+ name +" must be nominal");
				}
			}
			//there should be at least information about correlating attributes
			if(attributes != null)
				//etDependencies.add(new ComplexAttributeDescription(attributes,params,symbol,attName,hint));
				etDependencies.add(ComplexAttributeFactory.createAttributeDescription(attributes, params, symbol, attName, hint));
			else throw new IOException("no correlating attributes defined for complex attribute "+ attName);
		}
    	return etDependencies;
    }
    
    @Override
    protected void findDataDefinitionEnd() throws IOException {
   	 	//maybe true is also working
    	Tools.getLastToken(tokenizer, false);
    }
    
    protected boolean AnnotationFound(){
    	 if("@relation".equalsIgnoreCase(tokenizer.sval))
    		 return true;
    	 else return false; 
    }
    
}
