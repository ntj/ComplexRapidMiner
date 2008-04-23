/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.postprocessing;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
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
 * @version $Id: AbstractSplitChain.java,v 1.1 2007/05/27 22:02:47 ingomierswa Exp $
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

		//Model model = secondResult.remove(Model.class);
		//return new IOObject[] { model };
		return secondResult.getIOObjects();
	}

	public InnerOperatorCondition getInnerOperatorCondition() {
		CombinedInnerOperatorCondition condition = new CombinedInnerOperatorCondition();
		condition.addCondition(new SpecificInnerOperatorCondition("Training", 0, new Class[] { ExampleSet.class }, new Class[] { Model.class }));
		condition.addCondition(new SpecificInnerOperatorCondition("Testing", 1, new Class[] { ExampleSet.class, Model.class }, new Class[] { Model.class }));
		return condition;
	}

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	/** Returns the maximum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 2;
	}

	/** Returns the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 2;
	}

	/** Returns the the classes this operator expects as input. */
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/** Returns the the classes this operator provides as output. */
	public Class[] getOutputClasses() {
		return new Class[] { Model.class };
	}
}
