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

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator keeps only the examples of a given range (including the borders). 
 * The other examples will be removed from the input example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleRangeFilter.java,v 1.4 2008/05/09 19:22:57 ingomierswa Exp $
 */
public class ExampleRangeFilter extends Operator {


	/** The parameter name for &quot;The first example of the resulting example set.&quot; */
	public static final String PARAMETER_FIRST_EXAMPLE = "first_example";

	/** The parameter name for &quot;The last example of the resulting example set.&quot; */
	public static final String PARAMETER_LAST_EXAMPLE = "last_example";
    public ExampleRangeFilter(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        int[] partition = new int[exampleSet.size()];
        int startIndex = getParameterAsInt(PARAMETER_FIRST_EXAMPLE);
        int endIndex = getParameterAsInt(PARAMETER_LAST_EXAMPLE);
        
        if (endIndex < startIndex)
            throw new UserError(this, 210, "last_example", "first_example");
        
        for (int i = 0; i < partition.length; i++) {
            if ((i >= startIndex - 1) && (i <= endIndex - 1)) {
                partition[i] = 0;
            } else {
                partition[i] = 1;
            }
        }
        
        SplittedExampleSet result = new SplittedExampleSet(exampleSet, new Partition(partition, 2));
        result.selectSingleSubset(0);
        return new IOObject[] { result };
    }

    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> parameterTypes = super.getParameterTypes();
        parameterTypes.add(new ParameterTypeInt(PARAMETER_FIRST_EXAMPLE, "The first example of the resulting example set.", 1, Integer.MAX_VALUE, false));
        parameterTypes.add(new ParameterTypeInt(PARAMETER_LAST_EXAMPLE, "The last example of the resulting example set.", 1, Integer.MAX_VALUE, false));
        return parameterTypes;
    }
}
