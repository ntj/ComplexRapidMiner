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
package com.rapidminer.operator.preprocessing.filter;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;


/**
 * Converts all numerical attributes to nominal ones.
 * 
 * @author Ingo Mierswa
 * @version $Id: NumericToNominal.java,v 1.7 2008/07/07 07:06:40 ingomierswa Exp $
 */
public abstract class NumericToNominal extends Operator {

    public NumericToNominal(OperatorDescription description) {
        super(description);
    }

    protected abstract void setValue(Example example, Attribute newAttribute, double value) throws OperatorException;
    
    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        Map<Attribute, Attribute> translationMap = new HashMap<Attribute, Attribute>();
        // creating new nominal attributes
        for (Attribute originalAttribute : exampleSet.getAttributes()) {
            if (originalAttribute.isNumerical()) {
                Attribute newAttribute = AttributeFactory.createAttribute(originalAttribute.getName(), Ontology.NOMINAL);
                translationMap.put(originalAttribute, newAttribute);
            }
        }
		// adding to table and exampleSet
		for (Entry<Attribute, Attribute> replacement: translationMap.entrySet()) {
			Attribute newAttribute = replacement.getValue();
			exampleSet.getExampleTable().addAttribute(newAttribute);
			exampleSet.getAttributes().addRegular(newAttribute);
		}

		// over all examples change attribute values
        for(Example example: exampleSet) {
            for(Entry<Attribute, Attribute> replacement: translationMap.entrySet()) {
                Attribute oldAttribute = replacement.getKey();
                Attribute newAttribute = replacement.getValue();
                double oldValue = example.getValue(oldAttribute);
                setValue(example, newAttribute, oldValue);
            }
            checkForStop();
        }
        
		// removing old attributes
		for (Attribute originalAttribute: translationMap.keySet()) {
			exampleSet.getAttributes().remove(originalAttribute);
		}

        return new IOObject[] {exampleSet};
    }
    
    public Class<?>[] getInputClasses() {
        return new Class[] {
            ExampleSet.class
        };
    }

    public Class<?>[] getOutputClasses() {
        return new Class[] {
            ExampleSet.class
        };
    }
}
