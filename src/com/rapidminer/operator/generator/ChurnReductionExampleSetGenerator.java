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
 * @version $Id: ChurnReductionExampleSetGenerator.java,v 1.3 2008/05/09 19:22:50 ingomierswa Exp $
 */
public class ChurnReductionExampleSetGenerator extends Operator {

    /** The parameter name for &quot;The number of generated examples.&quot; */
    public static final String PARAMETER_NUMBER_EXAMPLES = "number_examples";

    /** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
    public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
    
    private static final Class[] INPUT_CLASSES = new Class[0];

    private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };
    
    private static String[] POSSIBLE_VALUES = {
        "New Credit", "Nothing", "End Credit", "Collect Information", "Additional Credit"
    };
    
    public ChurnReductionExampleSetGenerator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        // init
        int numberOfExamples = getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);

        // create table
        List<Attribute> attributes = new ArrayList<Attribute>();
        for (int m = 0; m < 5; m++) {
            Attribute current = AttributeFactory.createAttribute("Year " + (m + 1), Ontology.NOMINAL);
            for (int v = 0; v < POSSIBLE_VALUES.length; v++)
                current.getMapping().mapString(POSSIBLE_VALUES[v]);
            attributes.add(current);
        }
        Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);
        label.getMapping().mapString("ok");
        label.getMapping().mapString("terminate");
        attributes.add(label);

        MemoryExampleTable table = new MemoryExampleTable(attributes);

        // create data
        RandomGenerator random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
        for (int n = 0; n < numberOfExamples; n++) {
            double[] values = new double[6];
            for (int i = 0; i < 5; i++) {
                values[i] = random.nextInt(POSSIBLE_VALUES.length);
            }
            values[5] = 0;
          
            //"New Credit", "Nothing", "End Credit", "Collect Information", "Additional Credit"
            if ((values[0] == 0) &&
                (values[1] == 1)) {
                values[5] = 1;
            } else if ((values[2] == 4) &&
                    (values[4] == 1)) {
                values[5] = 1;
            } else if (values[4] == 5) {
                values[5] = 1;
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
