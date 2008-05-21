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
import java.util.Iterator;
import java.util.Map;

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
 * @version $Id: NumericToNominal.java,v 1.4 2008/05/09 19:22:57 ingomierswa Exp $
 */
public abstract class NumericToNominal extends Operator {

    public NumericToNominal(OperatorDescription description) {
        super(description);
    }

    protected abstract void setValue(Example example, Attribute newAttribute, double value) throws OperatorException;
    
    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        Map<Attribute, Attribute> trans = buildTranslationMap(exampleSet);
        // over all examples change attribute values
        Iterator<Example> iterator = exampleSet.iterator();
        while (iterator.hasNext()) {
            Example example = iterator.next();
            Iterator<Map.Entry<Attribute, Attribute>> at = trans.entrySet().iterator();
            while (at.hasNext()) {
                Map.Entry<Attribute, Attribute> e = at.next();
                Attribute oldAttribute = e.getKey();
                Attribute newAttribute = e.getValue();
                double value = example.getValue(oldAttribute);
                setValue(example, newAttribute, value);
            }
            checkForStop();
        }
        return new IOObject[] {
            exampleSet
        };
    }
    
    /* Would be nicer to fold this into the below. */
    private void buildTranslationMap(ExampleSet exampleSet, Map<Attribute, Attribute> trans, Attribute old) {
        if (!old.isNominal()) {
            Attribute _new = exampleSet.getAttributes().replace(old, AttributeFactory.changeValueType(old, Ontology.NOMINAL));
            trans.put(old, _new);
        }
    }

    private Map<Attribute, Attribute> buildTranslationMap(ExampleSet exampleSet) {
        Map<Attribute, Attribute> trans = new HashMap<Attribute, Attribute>();
        for (Attribute attribute : exampleSet.getAttributes()) {
            buildTranslationMap(exampleSet, trans, attribute);
        }
        return trans;
    }
    
    public Class[] getInputClasses() {
        return new Class[] {
            ExampleSet.class
        };
    }

    public Class[] getOutputClasses() {
        return new Class[] {
            ExampleSet.class
        };
    }
}
