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
package com.rapidminer.operator.generator;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;

/**
 * Generates a random example set for testing purposes. The data represents a team profit
 * example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: TransfersExampleSetGenerator.java,v 1.4 2008/05/09 19:22:50 ingomierswa Exp $
 */
public class TransfersExampleSetGenerator extends Operator {

    /** The parameter name for &quot;The number of generated examples.&quot; */
    public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

    /** The parameter name for the creation of a fraud label. */
    public static final String PARAMETER_CREATE_FRAUD_LABEL = "create_fraud_label";
    
    /** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
    public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    
    private static final Class[] INPUT_CLASSES = new Class[0];

    private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

    private static String[] ATTRIBUTE_NAMES = {
        "Source", "Target", "ProjectID", "Reason", "Person", "Amount"
    };
    
    private static int[] VALUE_TYPES = {
        Ontology.NOMINAL,
        Ontology.NOMINAL,
        Ontology.NOMINAL,
        Ontology.INTEGER,
        Ontology.NOMINAL,
        Ontology.REAL
    };
    
    private static String[][] POSSIBLE_VALUES = {
        { "1201", "1302", "4517", "4711", "2323", "1110", "2233" },
        { "1201", "1302", "4517", "4711", "2323", "1110", "2233" },
        { "Prj01", "Prj02", "Prj03", "Prj04", "Prj05", "Prj06", "Prj07", "Prj08" },
        null,
        { "Mr. Brown", "Mr. Miller", "Mrs. Smith", "Mrs. Hanson", "Mrs. Green", "Mr. Chang" },
        null
    };
    
    public TransfersExampleSetGenerator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        // init
        int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);

        // create table
        List<Attribute> attributes = new ArrayList<Attribute>();
        Attribute id = AttributeFactory.createAttribute("TransferID", Ontology.INTEGER);
        attributes.add(id);
        for (int m = 0; m < ATTRIBUTE_NAMES.length; m++) {
            Attribute current = AttributeFactory.createAttribute(ATTRIBUTE_NAMES[m], VALUE_TYPES[m]);
            String[] possibleValues = POSSIBLE_VALUES[m];
            if (possibleValues != null) {
                for (int v = 0; v < possibleValues.length; v++)
                    current.getMapping().mapString(possibleValues[v]);
            }
            attributes.add(current);
        }
        
        boolean createFraudLabel = getParameterAsBoolean(PARAMETER_CREATE_FRAUD_LABEL);
        Attribute label = null;
        if (createFraudLabel) {
        	label = AttributeFactory.createAttribute("fraud", Ontology.NOMINAL);
        	label.getMapping().mapString("yes");
        	label.getMapping().mapString("no");
        	attributes.add(label);
        }
        	
        
        MemoryExampleTable table = new MemoryExampleTable(attributes);

        // create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        for (int n = 0; n < numberOfExamples; n++) {
        	int specialLength = createFraudLabel ? 2 : 1;
            double[] values = new double[ATTRIBUTE_NAMES.length + specialLength];
            values[0] = n + 1;
            // "Source", "Target", "ProjectID", "Reason", "Person", "Amount"
            values[1] = random.nextInt(POSSIBLE_VALUES[0].length);
            values[2] = random.nextInt(POSSIBLE_VALUES[1].length);
            values[3] = random.nextInt(POSSIBLE_VALUES[2].length); 
            values[4] = random.nextIntInRange(10000, 99999);
            values[5] = random.nextInt(POSSIBLE_VALUES[4].length);
            values[6] = Math.round(random.nextGaussian() * 15000) + 50000;
            
            if (createFraudLabel) {
            	values[7] = label.getMapping().mapString("no");
            	if (((values[1] == 3) || (values[1] == 4)) && ((values[2] == 1) || (values[2] == 2)))  {
            		if (random.nextDouble() > 0.05) {
            			values[7] = label.getMapping().mapString("yes");		
            		}
            	}
            }
            table.addDataRow(new DoubleArrayDataRow(values));
        }

        // create example set and return it
        if (createFraudLabel) {
        	return new IOObject[] { table.createExampleSet(label, null, id) };
        } else {
        	return new IOObject[] { table.createExampleSet(null, null, id) };
        }
    }
        
        

    public Class[] getInputClasses() {
        return INPUT_CLASSES;
    }

    public Class[] getOutputClasses() {
        return OUTPUT_CLASSES;
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_EXAMPLES, "The number of generated examples.", 1, Integer.MAX_VALUE, 100);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeBoolean(PARAMETER_CREATE_FRAUD_LABEL, "Indicates if a label should be created for possible frauds.", false);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
