package de.tud.inf.operator.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamTokenizer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.io.ArffExampleSource;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.table.ComplexExampleTable;

/**
 * creates a ComplexExampleSet from and ARFF file, when file does not contain complex information (i.e dependency information) exception is thrownm 
 * @author Antje Gruner
 *
 */
public class ComplexArffExampleSource extends ArffExampleSource{

    /** The parameter name for &quot;select whether read or ignore complex information in complex arff file &quot; */
    public static final String PARAMETER_READ_COMPLEX_ATTRIBUTES = "read_complex_attributes";

	public ComplexArffExampleSource(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply()throws OperatorException {
		try{
			ComplexArffReader reader;
	        
	    	//store new Annotation somewhere else
	    	String complexArffAnnotation = "@DATATABLE";
	    	File file = getParameterAsFile(PARAMETER_DATA_FILE);
	        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), getEncoding()));
	        
	        // init
	        Attribute label = null;
	        Attribute weight = null;
	        Attribute id = null;
	        
	        // read file
	        StreamTokenizer tokenizer = createTokenizer(in);
	        
	        Tools.getFirstToken(tokenizer);
	        if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
	            throw new UserError(this, 302, getParameterAsString(PARAMETER_DATA_FILE), "file is empty");
	        }
	                          
	        if(complexArffAnnotation.equalsIgnoreCase(tokenizer.sval)){
	        	Tools.getNextToken(tokenizer);
            	Tools.getLastToken(tokenizer, false);
	        	if(getParameterAsBoolean(PARAMETER_READ_COMPLEX_ATTRIBUTES))
	        	//first read relations which exists (if just one, normal case, if two should be dependency case, three are not supported yet (and make no sense))
	        	
	            reader = new ComplexArffReader(tokenizer,
	            		 						this,
	            		 						PARAMETER_SAMPLE_SIZE,
	    										PARAMETER_SAMPLE_RATIO,
	    										PARAMETER_DATAMANAGEMENT,
	    										PARAMETER_LOCAL_RANDOM_SEED,
	    										PARAMETER_DECIMAL_POINT_CHARACTER
	            								);
	        	
	        	else
	        		   reader = new FlatComplexArffReader(tokenizer,
		 						this,
		 						PARAMETER_SAMPLE_SIZE,
								PARAMETER_SAMPLE_RATIO,
								PARAMETER_DATAMANAGEMENT,
								PARAMETER_LOCAL_RANDOM_SEED,
								PARAMETER_DECIMAL_POINT_CHARACTER
								);
	        		
	        }else throw new IOException("expected the keyword "+ complexArffAnnotation +" in line " + tokenizer.lineno());
	        
	        ComplexExampleTable table = reader.read();
	
	        for(Attribute attribute: table.getAttributes()){
	        	if(attribute != null){
	            	   if (attribute.getName().equals(getParameterAsString(PARAMETER_LABEL_ATTRIBUTE))) {
	   	                label = attribute;
	   	            } else if (attribute.getName().equals(getParameterAsString(PARAMETER_ID_ATTRIBUTE))) {
	   	                id = attribute;
	   	            } else if (attribute.getName().equals(getParameterAsString(PARAMETER_WEIGHT_ATTRIBUTE))) {
	   	                weight = attribute;
	   	            }
	        	}
	        }
	        
	        in.close();
	        
	        Map<Attribute, String> specialMap = new HashMap<Attribute, String>();
	        specialMap.put(label, Attributes.LABEL_NAME);
	        specialMap.put(weight, Attributes.WEIGHT_NAME);
	        specialMap.put(id, Attributes.ID_NAME);
	        ComplexExampleSet ces = table.createExampleSet(specialMap);
	        
	        return new IOObject[] { ces };
	        
    } catch (IOException e) {
        throw new UserError(this, 302, getParameterAsString(PARAMETER_DATA_FILE), e.getMessage());
    	}
	}
	

	@Override
	public Class<?>[] getOutputClasses() {
	     return new Class[] { ComplexExampleSet.class };
	}
	
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_READ_COMPLEX_ATTRIBUTES,"select whether read or ignore complex information in complex arff file",true);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
