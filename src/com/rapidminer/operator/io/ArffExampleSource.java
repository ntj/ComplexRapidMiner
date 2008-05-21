/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.Tools;


/**
 * <p>This operator can read ARFF files known from the machine learning library Weka. 
 * An ARFF (Attribute-Relation File Format) file is an ASCII text file that describes 
 * a list of instances sharing a set of attributes. ARFF files were developed by the 
 * Machine Learning Project at the Department of Computer Science of The University 
 * of Waikato for use with the Weka machine learning software.</p>
 * 
 * <p>ARFF files have two distinct sections. The first section is the Header information, 
 * which is followed the Data information. The Header of the ARFF file contains the name 
 * of the relation (@RELATION, ignored by RapidMiner) and a list of the attributes, each of which
 * is defined by a starting @ATTRIBUTE followed by its name and its type.</p>
 * 
 * <p>Attribute declarations take the form of an orderd sequence of @ATTRIBUTE statements. 
 * Each attribute in the data set has its own @ATTRIBUTE statement which uniquely defines 
 * the name of that attribute and it's data type. The order the attributes are declared 
 * indicates the column position in the data section of the file. For example, if an 
 * attribute is the third one declared all that attributes values will be found in the third 
 * comma delimited column.</p>
 * 
 * <p>The possible attribute types are:</p>
 * <ul>
 * <li><code>numeric</code></li>
 * <li><code>integer</code></li>
 * <li><code>real</code></li>
 * <li><code>{nominalValue1,nominalValue2,...}</code> for nominal attributes</li>
 * <li><code>string</code> for nominal attributes without distinct nominal values (it is 
 * however recommended to use the nominal definition above as often as possible)</li>
 * <li><code>date [date-format]</code> (currently not supported by RapidMiner)</li>
 * </ul>
 * 
 * <p>Valid examples for attribute definitions are <br/>
 * <code>@ATTRIBUTE petalwidth REAL</code> <br/>
 * <code>@ATTRIBUTE class {Iris-setosa,Iris-versicolor,Iris-virginica}</code>
 * </p>
 * 
 * <p>The ARFF Data section of the file contains the data declaration line @DATA followed
 * by the actual example data lines. Each example is represented on a single line, with 
 * carriage returns denoting the end of the example. Attribute values for each example 
 * are delimited by commas. They must appear in the order that they were declared in the 
 * header section (i.e. the data corresponding to the n-th @ATTRIBUTE declaration is 
 * always the n-th field of the example line). Missing values are represented by a single 
 * question mark, as in:<br/>
 * <code>4.4,?,1.5,?,Iris-setosa</code></p>
 * 
 * <p>A percent sign (%) introduces a comment and will be ignored during reading. Attribute
 * names or example values containing spaces must be quoted with single quotes ('). Please
 * note that the sparse ARFF format is currently only supported for numerical attributes. 
 * Please use one of the other options for sparse data files provided by RapidMiner if you also 
 * need sparse data files for nominal attributes.</p>
 * 
 * <p>Please have a look at the Iris example ARFF file provided in the data subdirectory 
 * of the sample directory of RapidMiner to get an idea of the described data format.</p>
 *  
 * @rapidminer.index arff
 * @author Ingo Mierswa
 * @version $Id: ArffExampleSource.java,v 1.9 2006/03/21 15:35:46 ingomierswa
 *          Exp $
 */
public class ArffExampleSource extends Operator {


	/** The parameter name for &quot;The path to the data file.&quot; */
	public static final String PARAMETER_DATA_FILE = "data_file";

	/** The parameter name for &quot;The (case sensitive) name of the label attribute&quot; */
	public static final String PARAMETER_LABEL_ATTRIBUTE = "label_attribute";

	/** The parameter name for &quot;The (case sensitive) name of the id attribute&quot; */
	public static final String PARAMETER_ID_ATTRIBUTE = "id_attribute";

	/** The parameter name for &quot;The (case sensitive) name of the weight attribute&quot; */
	public static final String PARAMETER_WEIGHT_ATTRIBUTE = "weight_attribute";

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";

	/** The parameter name for &quot;Character that is used as decimal point.&quot; */
	public static final String PARAMETER_DECIMAL_POINT_CHARACTER = "decimal_point_character";
	
    /** The parameter name for &quot;The fraction of the data set which should be read (1 = all; only used if sample_size = -1)&quot; */
    public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";

    /** The parameter name for &quot;The exact number of samples which should be read (-1 = use sample ratio; if not -1, sample_ratio will not have any effect)&quot; */
    public static final String PARAMETER_SAMPLE_SIZE = "sample_size";

    /** The parameter name for &quot;Use the given random seed instead of global random numbers (only for permutation, -1: use global).&quot; */
    public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    
    public ArffExampleSource(OperatorDescription description) {
        super(description);
    }
    
    public IOObject[] apply() throws OperatorException {
        try {
            File file = getParameterAsFile(PARAMETER_DATA_FILE);
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(file), getEncoding()));
            
            // init
            List<Attribute> attributes = new ArrayList<Attribute>();
            Attribute label = null;
            Attribute weight = null;
            Attribute id = null;
            
            // read file
            StreamTokenizer tokenizer = createTokenizer(in);
            Tools.getFirstToken(tokenizer);
            if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
                throw new UserError(this, 302, getParameterAsString(PARAMETER_DATA_FILE), "file is empty");
            }
                        
            if ("@relation".equalsIgnoreCase(tokenizer.sval)) {
                Tools.getNextToken(tokenizer);
                Tools.getLastToken(tokenizer, false);
            } else {
                throw new IOException("expected the keyword @relation in line " + tokenizer.lineno());
            }

            // attributes
            Tools.getFirstToken(tokenizer);
            if (tokenizer.ttype == StreamTokenizer.TT_EOF) {
                throw new IOException("unexpected end of file in line " + tokenizer.lineno() + ", attribute description expected...");
            }

            while ("@attribute".equalsIgnoreCase(tokenizer.sval)) {
                Attribute attribute = createAttribute(tokenizer);
                attributes.add(attribute);
                
                if (attribute.getName().equals(getParameterAsString(PARAMETER_LABEL_ATTRIBUTE))) {
                    label = attribute;
                } else if (attribute.getName().equals(getParameterAsString(PARAMETER_ID_ATTRIBUTE))) {
                    id = attribute;
                } else if (attribute.getName().equals(getParameterAsString(PARAMETER_WEIGHT_ATTRIBUTE))) {
                    weight = attribute;
                }
            }

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
            DataRowFactory factory = new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), getParameterAsString(PARAMETER_DECIMAL_POINT_CHARACTER).charAt(0));
            int maxRows = getParameterAsInt(PARAMETER_SAMPLE_SIZE);
            double sampleProb = getParameterAsDouble(PARAMETER_SAMPLE_RATIO);
            Random random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
            
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
                           
            in.close();
            
            Map<Attribute, String> specialMap = new HashMap<Attribute, String>();
            specialMap.put(label, Attributes.LABEL_NAME);
            specialMap.put(weight, Attributes.WEIGHT_NAME);
            specialMap.put(id, Attributes.ID_NAME);
            return new IOObject[] { table.createExampleSet(specialMap) };
            
        } catch (IOException e) {
            throw new UserError(this, 302, getParameterAsString(PARAMETER_DATA_FILE), e.getMessage());
        }
    }

    private Attribute createAttribute(StreamTokenizer tokenizer) throws IOException {
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
    
    private DataRow createDataRow(StreamTokenizer tokenizer, boolean checkForCarriageReturn, DataRowFactory factory, Attribute[] allAttributes) throws IOException {
        // return null at the end of file
        Tools.getFirstToken(tokenizer);
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
            Tools.getLastToken(tokenizer, true);
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
            Tools.getLastToken(tokenizer, true);
        }
        // Add instance to dataset
        return factory.create(tokens, allAttributes);
    }

    
    /** Creates a StreamTokenizer for reading ARFF files. */
    private StreamTokenizer createTokenizer(Reader in){
        StreamTokenizer tokenizer = new StreamTokenizer(in);
        tokenizer.resetSyntax();         
        tokenizer.whitespaceChars(0, ' ');    
        tokenizer.wordChars(' '+1,'\u00FF');
        tokenizer.whitespaceChars(',',',');
        tokenizer.commentChar('%');
        tokenizer.quoteChar('"');
        tokenizer.quoteChar('\'');
        tokenizer.ordinaryChar('{');
        tokenizer.ordinaryChar('}');
        tokenizer.eolIsSignificant(true);
        return tokenizer;
    }

    public Class[] getInputClasses() {
        return new Class[0];
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeFile(PARAMETER_DATA_FILE, "The path to the data file.", "arff", false));
        ParameterType type = new ParameterTypeString(PARAMETER_LABEL_ATTRIBUTE, "The (case sensitive) name of the label attribute");
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeString(PARAMETER_ID_ATTRIBUTE, "The (case sensitive) name of the id attribute"));
        types.add(new ParameterTypeString(PARAMETER_WEIGHT_ATTRIBUTE, "The (case sensitive) name of the weight attribute"));
        types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
		types.add(new ParameterTypeString(PARAMETER_DECIMAL_POINT_CHARACTER, "Character that is used as decimal point.", "."));
        type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "The fraction of the data set which should be read (1 = all; only used if sample_size = -1)", 0.0d, 1.0d, 1.0d);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The exact number of samples which should be read (-1 = use sample ratio; if not -1, sample_ratio will not have any effect)", -1, Integer.MAX_VALUE, -1));
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (only for permutation, -1: use global).", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
