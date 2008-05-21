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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.CombinedInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SpecificInnerOperatorCondition;


/**
 * <p>An operator chain that split an {@link ExampleSet} into two disjunct parts
 * and applies the first child operator on the first part and applies the second
 * child on the second part and the result of the first child. The total result
 * is the result of the second operator.</p>
 * 
 * <p>Subclasses must define how the example set is divided.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractSplitChain.java,v 1.2 2008/05/09 19:22:38 ingomierswa Exp $
 */
public abstract class AbstractSplitChain extends OperatorChain {

	public AbstractSplitChain(OperatorDescription description) {
		super(description);
	}

	/** Creates the splitted example for this operator. Please note that the results must contain
	 *  two parts. */
	protected abstract SplittedExampleSet createSplittedExampleSet(ExampleSet exampleSet) throws OperatorException;
	
	public IOObject[] apply() throws OperatorException {
		ExampleSet inputSet = getInput(ExampleSet.class);
		SplittedExampleSet exampleSet = createSplittedExampleSet(inputSet);

		exampleSet.selectSingleSubset(0);
		IOContainer firstInput = new IOContainer(new IOObject[] { exampleSet });
		IOContainer firstResult = getOperator(0).apply(firstInput);
		
		exampleSet.selectSingleSubset(1);
		IOContainer secondInput = firstResult.append(new IOObject[] { exampleSet });
		IOContainer secondResult = getOperator(1).apply(secondInput);

		return secondResult.getIOObjects();
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new SpecificInnerOperatorCondition("First Part", 0, new Class[] { ExampleSet.class }, new Class[0]));
		condition.addCondition(new SpecificInnerOperatorCondition("Second Part", 1, new Class[] { ExampleSet.class }, new Class[0]));
		return condition;
	}

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	/** Returns the maximum number of inner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 2;
	}

	/** Returns the minimum number of inner operators. */
	public int getMinNumberOfInnerOperators() {
		return 2;
	}

	/** Returns the the classes this operator expects as input. */
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/** Returns the the classes this operator provides as output. */
	public Class[] getOutputClasses() {
		return new Class[0];
	}
}
