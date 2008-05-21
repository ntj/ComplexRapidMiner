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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * For each example set the ExampleSetIterator finds in its input, the inner
 * operators are applied as if it was an OperatorChain. This operator can be
 * used to conduct a process consecutively on a number of different data
 * sets.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSetIterator.java,v 1.10 2006/04/12 18:04:24 ingomierswa
 *          Exp $
 */
public class ExampleSetIterator extends OperatorChain {


	/** The parameter name for &quot;Return only best result? (Requires a PerformanceVector in the inner result).&quot; */
	public static final String PARAMETER_ONLY_BEST = "only_best";
	private List<ExampleSet> eSetList;

	public ExampleSetIterator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		eSetList = new LinkedList<ExampleSet>();
		ExampleSet eSet = null;
		try {
			while (true) {
				eSet = getInput(ExampleSet.class);
				eSetList.add(eSet);
			}
		} catch (MissingIOObjectException e) {
			log("No more example sets found. Use " + eSetList.size() + " example sets.");
		}

		Iterator i = eSetList.iterator();
		IOContainer innerResult = new IOContainer(new IOObject[0]);
		boolean onlyBest = getParameterAsBoolean(PARAMETER_ONLY_BEST);
		double bestFitness = Double.NEGATIVE_INFINITY;
		while (i.hasNext()) {
			IOContainer result = getOperator(0).apply(new IOContainer(new IOObject[] { (IOObject) i.next() }));
			if (onlyBest) {
				PerformanceVector pv = result.get(PerformanceVector.class);
				double fitness = pv.getMainCriterion().getFitness();
				if (fitness > bestFitness) {
					bestFitness = fitness;
					innerResult = result;
				};
			} else {
				innerResult = innerResult.append(result.getIOObjects());
			}
            inApplyLoop();
		}
		return innerResult.getIOObjects();
	}

	/** All inner operators must be able to handle an example set. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[0]);
	}

	/** Returns the maximum number of innner operators. */
	public int getMaxNumberOfInnerOperators() {
		return 1;
	}

	/** Returns the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public Class[] getOutputClasses() {
		return new Class[0];
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeBoolean(PARAMETER_ONLY_BEST, "Return only best result? (Requires a PerformanceVector in the inner result).", false);
		type.setExpert(false);
		types.add(type);

		return types;
	}
}
