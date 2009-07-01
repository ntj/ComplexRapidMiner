package com.rapidminer.operator.io;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;

public class ArffReader {
	
	protected StreamTokenizer tokenizer;
	protected ArffExampleSource arffES;
	
	protected final String PARAMETER_SAMPLE_SIZE;
	protected final String PARAMETER_SAMPLE_RATIO;
	protected final String PARAMETER_DATAMANAGEMENT;
	protected final String PARAMETER_LOCAL_RANDOM_SEED;
	protected final String PARAMETER_DECIMAL_POINT_CHARACTER;
	
	
	
	public ArffReader(	StreamTokenizer tokenizer,
						ArffExampleSource arffES,
						String parameter_sample_size,
						String parameter_sample_ratio,
						String parameter_datamanagement,
						String parameter_local_random_seed,
						String parameter_decimal_point_character) {
		
		this.tokenizer = tokenizer;
		this.arffES = arffES;
		PARAMETER_SAMPLE_SIZE = parameter_sample_size;
		PARAMETER_SAMPLE_RATIO = parameter_sample_ratio;
		PARAMETER_DATAMANAGEMENT = parameter_datamanagement;
		PARAMETER_LOCAL_RANDOM_SEED = parameter_local_random_seed;
		PARAMETER_DECIMAL_POINT_CHARACTER = parameter_decimal_point_character;
	}
	

	public ExampleTable read() throws IOException, UndefinedParameterError{
			List<Attribute> attributes = readAttributes(false);
		
			return readData(attributes);
	}
	
	
	public List<Attribute> readAttributes(boolean relativeAttributesAllowed) throws IOException{
		List<Attribute> attributes = new ArrayList<Attribute>();
		// attributes
        Tools.getFirstToken(tokenizer);
        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
            throw new IOException("unexpected end of file in line " + tokenizer.lineno() + ", attribute description expected...");
        }

        while ("@attribute".equalsIgnoreCase(tokenizer.sval)) {
            Attribute attribute = createAttribute(tokenizer,relativeAttributesAllowed);
            attributes.add(attribute);
        }
        return attributes;
	}
	
    public MemoryExampleTable readData(List<Attribute> attributes) throws IOException, UndefinedParameterError{
        // expect data declaration
        if (!"@data".equalsIgnoreCase(tokenizer.sval)) {
            throw new IOException("expected keyword '@data' in line " + tokenizer.lineno());
        }
          
        // check attribute number
        if (attributes.size() == 0) {
            throw new IOException("no attributes were declared in the ARFF file, please declare attributes with the '@attribute' keyword.");
        }
       
        // fill data table
        MemoryExampleTable table = new MemoryExampleTable(attributes);
        Attribute[] attributeArray = table.getAttributes();
        DataRowFactory factory = new DataRowFactory(arffES.getParameterAsInt(PARAMETER_DATAMANAGEMENT), arffES.getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0));
        int maxRows = arffES.getParameterAsInt(PARAMETER_SAMPLE_SIZE);
        double sampleProb = arffES.getParameterAsDouble(PARAMETER_SAMPLE_RATIO);
        Random random = RandomGenerator.getRandomGenerator(arffES.getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        
        DataRow dataRow = null;
        int counter = 0;
        while ((dataRow = createDataRow(tokenizer, true, factory, attributeArray)) != null) {
            if ((maxRows > -1) && (counter >= maxRows))
                break;
            counter++;
            
            if (maxRows == -1) {
                if (random.nextDouble() > sampleProb)
                    continue;
            }
            table.addDataRow(dataRow);
        }
        return table;
    }
		
	
	protected Attribute createAttribute(StreamTokenizer tokenizer,boolean relAttAllowed) throws IOException {
	        Attribute attribute = null; 
	        
	        // name
	        Tools.getNextToken(tokenizer);
	        String attributeName = tokenizer.sval;
	        
	        // determine value type
	        Tools.getNextToken(tokenizer);
	        if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
	            // numerical or string value type
	            if (tokenizer.sval.equalsIgnoreCase("real")) {
	                attribute = AttributeFactory.createAttribute(attributeName, Ontology.REAL);
	            } else if (tokenizer.sval.equalsIgnoreCase("integer")) {
	                attribute = AttributeFactory.createAttribute(attributeName, Ontology.INTEGER);
	            } else if (tokenizer.sval.equalsIgnoreCase("numeric")) {
	                attribute = AttributeFactory.createAttribute(attributeName, Ontology.NUMERICAL);
	            } else if (tokenizer.sval.equalsIgnoreCase("string")) {
	                attribute = AttributeFactory.createAttribute(attributeName, Ontology.STRING);
	            } else if (tokenizer.sval.equalsIgnoreCase("date")) {
	                attribute = AttributeFactory.createAttribute(attributeName, Ontology.DATE);
	            } else if (tokenizer.sval.equalsIgnoreCase("file")) {
	                attribute = AttributeFactory.createAttribute(attributeName, Ontology.STRING);
	            } else if (tokenizer.sval.equalsIgnoreCase("relational")) {
	            	attribute = readRelationalAttribute(tokenizer, attributeName,relAttAllowed);
	            }
	            Tools.waitForEOL(tokenizer);
	        } else {
	            // nominal attribute
	            attribute = AttributeFactory.createAttribute(attributeName, Ontology.NOMINAL);          
	            tokenizer.pushBack();
	            // check if nominal value definition starts
	            if (tokenizer.nextToken() != '{') {
	                throw new IOException("{ expected at beginning of nominal values definition in line " + tokenizer.lineno());
	            }
	            // read all nominal values until the end of the definition
	            while (tokenizer.nextToken() != '}') {
	                if (tokenizer.ttype == StreamTokenizer.TT_EOL) {
	                    throw new IOException("} expected at end of the nominal values definition in line " + tokenizer.lineno());
	                } else {
	                    attribute.getMapping().mapString(tokenizer.sval);
	                }
	            }
	            
	            if (attribute.getMapping().size() == 0) {
	                throw new IOException("empty definition of nominal values is not suggested in line " + tokenizer.lineno());
	            }
	        }
	        
	        Tools.getLastToken(tokenizer, false);
	        Tools.getFirstToken(tokenizer);
	        
	        if (tokenizer.ttype == StreamTokenizer.TT_EOF)
	            throw new IOException("unexpected end of file before data section in line " + tokenizer.lineno());
	                
	        return attribute;
	    }
	
	
	protected Attribute readRelationalAttribute(StreamTokenizer tokenizer, String attributeName, boolean depAttribute) throws IOException{
        	throw new IOException("arff file contains attribute type "+ tokenizer.sval + ", which are not supported by Rapidminer in simple arff format");		
	}

	protected Attribute checkInnerAttributeTypes(StreamTokenizer tokenizer, String attributeName) throws IOException{
		throw new IOException("arff file contains attribute type "+ tokenizer.sval +", which are not supported by Rapidminer in simple arff format");		
	}
   
    
    protected DataRow createDataRow(StreamTokenizer tokenizer, boolean checkForCarriageReturn, DataRowFactory factory, Attribute[] allAttributes) throws IOException {
        // return null at the end of file
        Tools.getFirstToken(tokenizer);
        if(!AnnotationFound()){
	        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
	            return null;
	        }
	
	        // create datarow from either dense or sparse format 
	        if (tokenizer.ttype == '{') {
	            return createDataRowFromSparse(tokenizer, checkForCarriageReturn, factory, allAttributes);
	        } else {
	            return createDataRowFromDense(tokenizer, checkForCarriageReturn, factory, allAttributes);
	        }
        }
        else return null;
    }
    
    protected boolean AnnotationFound(){
    	return false;
    }
    
    private DataRow createDataRowFromDense(StreamTokenizer tokenizer, boolean checkForCarriageReturn, DataRowFactory factory, Attribute[] allAttributes) throws IOException {
        String[] tokens = new String[allAttributes.length];

        // fetch all values
        for (int i = 0; i < allAttributes.length; i++) {
            if (i > 0) {
                Tools.getNextToken(tokenizer);
            }
            // check for missing value
            if (tokenizer.ttype == '?') {
                tokens[i] = "?";
            } else {
                if (tokenizer.ttype != StreamTokenizer.TT_WORD) {
                    throw new IOException("not a valid value '" + tokenizer.sval + "' in line " + tokenizer.lineno());
                }
                tokens[i] = tokenizer.sval;
            }
        }
        if (checkForCarriageReturn) {
        	findDataDefinitionEnd();
        }
        // Add instance to dataset
        return factory.create(tokens, allAttributes);
    }     
    
    private DataRow createDataRowFromSparse(StreamTokenizer tokenizer, boolean checkForCarriageReturn, DataRowFactory factory, Attribute[] allAttributes) throws IOException {
        String[] tokens = new String[allAttributes.length];
        for (int t = 0; t < tokens.length; t++)
            tokens[t] = "0";
        
        // Get values
        do {
            if (tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
                throw new IOException("unexpedted end of line " + tokenizer.lineno());
            }
            if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
                throw new IOException("unexpedted end of file in line " + tokenizer.lineno());
            } 
            if (tokenizer.ttype == '}') {
                break;
            }

            // determine index
            int index = Integer.valueOf(tokenizer.sval);

            // determine value
            Tools.getNextToken(tokenizer);

            // Check if value is missing.
            if  (tokenizer.ttype == '?') {
                tokens[index] = "?";
            } else {
                if (tokenizer.ttype != StreamTokenizer.TT_WORD) {
                    throw new IOException("not a valid value '" + tokenizer.sval + "' in line " + tokenizer.lineno());
                }
                tokens[index] = tokenizer.sval;
            }
        } while (true);

        if (checkForCarriageReturn) {
        	findDataDefinitionEnd();
        }
        // Add instance to dataset
        return factory.create(tokens, allAttributes);
    }
    
    protected void findDataDefinitionEnd() throws IOException {
   	 Tools.getLastToken(tokenizer, true);
   }
}
