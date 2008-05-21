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
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/**
 * Generates a random example set for testing purposes. The data represents a direct mailing
 * example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: DirectMailingExampleSetGenerator.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class DirectMailingExampleSetGenerator extends Operator {

    /** The parameter name for &quot;The number of generated examples.&quot; */
    public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

    /** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
    public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    
    private static final Class[] INPUT_CLASSES = new Class[0];

    private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

    private static String[] ATTRIBUTE_NAMES = {
        "name", "age", "lifestyle", "zip code", "family status", "car", "sports", "earnings"
    };
    
    private static int[] VALUE_TYPES = {
        Ontology.NOMINAL,
        Ontology.INTEGER,
        Ontology.NOMINAL,
        Ontology.INTEGER,
        Ontology.NOMINAL,
        Ontology.NOMINAL,
        Ontology.NOMINAL,
        Ontology.INTEGER
    };
    
    private static String[][] POSSIBLE_VALUES = {
        null,
        null,
        { "healthy", "active", "cozily" },
        null,
        { "married", "single" },
        { "practical", "expensive" },
        { "soccer", "badminton", "athletics" },
        null
    };
    
    public DirectMailingExampleSetGenerator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        // init
        int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);

        // create table
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int m = 0; m < ATTRIBUTE_NAMES.length; m++) {
            Attribute current = AttributeFactory.createAttribute(ATTRIBUTE_NAMES[m], VALUE_TYPES[m]);
            String[] possibleValues = POSSIBLE_VALUES[m];
            if (possibleValues != null) {
                for (int v = 0; v < possibleValues.length; v++)
                    current.getMapping().mapString(possibleValues[v]);
            }
            attributes.add(current);
        }
        Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);
        label.getMapping().mapString("no response");
        label.getMapping().mapString("response");
        attributes.add(label);

        MemoryExampleTable table = new MemoryExampleTable(attributes);

        // create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        for (int n = 0; n < numberOfExamples; n++) {
            double[] values = new double[ATTRIBUTE_NAMES.length + 1];
            values[0] = attributes.get(0).getMapping().mapString(random.nextString(8));
            // "name", "age", "lifestyle", "zip code", "family status", "car", "sports", "earnings"
            values[1] = random.nextIntInRange(15, 70);
            values[2] = random.nextInt(POSSIBLE_VALUES[2].length);
            values[3] = random.nextIntInRange(10000, 100000);
            values[4] = random.nextInt(POSSIBLE_VALUES[4].length);
            values[5] = random.nextInt(POSSIBLE_VALUES[5].length);
            values[6] = random.nextInt(POSSIBLE_VALUES[6].length);
            values[7] = random.nextIntInRange(20000, 150000);
            
            values[8] = label.getMapping().mapString("no response");
            if (values[1] > 65) {
                if (random.nextDouble() > 0.05)
                    values[8] = label.getMapping().mapString("response");
            } else if (values[1] > 60) {
                if (random.nextDouble() > 0.1)
                    values[8] = label.getMapping().mapString("response");
            } else if (values[1] > 55) {
                if (random.nextDouble() > 0.2)
                    values[8] = label.getMapping().mapString("response");
            } else if (values[3] < 15000) {
                if (random.nextDouble() > 0.1)
                    values[8] = label.getMapping().mapString("response");
            } else if (values[7] > 140000) {
                values[8] = label.getMapping().mapString("response");
            }
            table.addDataRow(new DoubleArrayDataRow(values));
        }

        // create example set and return it
        return new IOObject[] { table.createExampleSet(label) };
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
        types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).", -1, Integer.MAX_VALUE, -1));
        return types;
    }
}
