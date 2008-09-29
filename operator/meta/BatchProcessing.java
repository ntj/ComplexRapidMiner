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
package com.rapidminer.operator.meta;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.FirstInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.preprocessing.MaterializeDataInMemory;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.OperatorService;

/**
 * This operator groups the input examples into batches of the specified size and performs
 * the inner operators on all batches subsequently. This might be useful for very large data 
 * sets which cannot be load into memory but must be handled in a database. In these cases,
 * preprocessing methods or model applications and other tasks can be performed on each batch
 * and the result might be again written into a database table (by using the DatabaseExampleSetWriter
 * in its append mode).
 * 
 * @author Ingo Mierswa
 * @version $Id: BatchProcessing.java,v 1.3 2008/07/15 15:07:24 ingomierswa Exp $
 */
public class BatchProcessing extends OperatorChain {

	public static final String PARAMETER_BATCH_SIZE = "batch_size";
	
	public BatchProcessing(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		Operator materialization = null;
		try {
			materialization = OperatorService.createOperator(MaterializeDataInMemory.class);
		} catch (OperatorCreationException e) {
			throw new OperatorException("Cannot create materialization: " + e);
		}
		
		if (materialization != null) {
			int batchSize = getParameterAsInt(PARAMETER_BATCH_SIZE);
			int size = exampleSet.size();
			int currentStart = 0;
			while (currentStart < size) {
				ExampleSet materializedSet = Tools.getLinearSubsetCopy(exampleSet, batchSize, currentStart);

				IOContainer innerInput = new IOContainer(materializedSet);
				for (int o = 0; o < getNumberOfOperators(); o++) {
					innerInput = getOperator(o).apply(innerInput);
				}
				
				currentStart += batchSize;
			}
		}
		
		return new IOObject[] { exampleSet };
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		return new FirstInnerOperatorCondition(new Class[] { ExampleSet.class });
	}

	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	public int getMinNumberOfInnerOperators() {
		return 1;
	}
	
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_BATCH_SIZE, "This number of examples is processed batch-wise by the inner operators of this operator.", 1, Integer.MAX_VALUE, 1000));
		return types;
	}
}
