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
package com.rapidminer.operator.preprocessing;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;


/**
 * <p>This operator creates a SplittedExampleSet from an arbitrary example set. 
 * The partitions of the resulting example set are created according to the 
 * values of the specified attribute. This works similar to the 
 * <code>GROUP BY</code> clause in SQL.</p>
 * 
 * <p>Please note that the resulting example set is simply a splitted example
 * set where no subset is selected. Following operators might decide to select
 * one or several of the subsets, e.g. one of the aggregation operators.</p>
 * 
 * @author Christian Bockermann, Ingo Mierswa
 * @version $Id: GroupByOperator.java,v 1.4 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class GroupByOperator extends Operator {
    
    public final static String PARAMETER_ATTRIBUTE_NAME = "attribute_name";

    public GroupByOperator(OperatorDescription desc) {
        super(desc);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        Attribute attribute = exampleSet.getAttributes().get(this.getParameterAsString(PARAMETER_ATTRIBUTE_NAME));

        if (attribute == null) {
            throw new UserError(this, 111, this.getParameterAsString(PARAMETER_ATTRIBUTE_NAME));
        }
        
        if (!attribute.isNominal()) {
            throw new UserError(this, 103, new Object[] { this.getParameterAsString(PARAMETER_ATTRIBUTE_NAME), "grouping by attribute."
            });
        }

        SplittedExampleSet grouped = SplittedExampleSet.splitByAttribute(exampleSet, attribute);

        return new IOObject[] { grouped };
    }

    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { SplittedExampleSet.class };
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME, 
                                           "Name of the attribute which is used to create partitions. If no such attribute is found in the input-exampleset or the attribute is not nominal or not an integer, execution will fail.",
                                           false));
        return types;
    }
}
