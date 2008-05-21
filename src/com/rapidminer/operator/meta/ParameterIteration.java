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

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.value.ParameterValues;
import com.rapidminer.parameter.value.ParameterValueRange;


/**
 * <p>In contrast to the {@link GridSearchParameterOptimizationOperator} operator this
 * operators simply uses the defined parameters and perform the inner operators
 * for all possible combinations. This can be especially usefull for plotting
 * or logging purposes and sometimes also for simply configuring the parameters for 
 * the inner operators as a sort of meta step (e.g. learning curve generation).</p>
 * 
 * <p>This operator iterates through a set of parameters by using all possible
 * parameter combinations. The parameter <var>parameters</var> is a list of key value pairs
 * where the keys are of the form <code>operator_name.parameter_name</code> and
 * the value is either a comma separated list of values (e.g. 10,15,20,25) or an 
 * interval definition in the format [start;end;stepsize] (e.g. [10;25;5]). Additionally,
 * the format [start;end;steps;scale] is allowed.</p>
 * 
 * <p>Please note that this operator has two modes: synchronized and non-synchronized.
 * In the latter, all parameter combinations are generated and the inner operators
 * are applied for each combination. In the synchronized mode, no combinations are
 * generated but the set of all pairs of the increasing number of parameters
 * are used. For the iteration over a single parameter there is no difference
 * between both modes. Please note that the number of parameter possibilities must be 
 * the same for all parameters in the synchronized mode.</p> 
 * 
 * @author Ingo Mierswa, Tobias Malbrecht
 * @version $Id: ParameterIteration.java,v 1.11 2006/04/05 08:57:26 ingomierswa
 *          Exp $
 */
public class ParameterIteration extends ParameterIteratingOperatorChain {

	/** The parameter name for &quot;A list of parameters to optimize&quot; */
	public static final String PARAMETER_PARAMETERS = "parameters";

	/** The parameter name for &quot;Synchronize parameter iteration&quot; */
	public static final String PARAMETER_SYNCHRONIZE = "synchronize";

	/** The parameter name for &quot;Keep the output of the last operator in the operator chain&quot; */
	public static final String PARAMETER_KEEP_OUTPUT = "keep_output";
	
	private PerformanceVector performance;

	private int iteration = 0;

	public ParameterIteration(OperatorDescription description) {
		super(description);
		addValue(new Value("performance", "The last performance.") {

			public double getValue() {
				if (performance != null)
					return performance.getMainCriterion().getAverage();
				else
					return Double.NaN;
			}
		});
		addValue(new Value("iteration", "The current iteration.") {

			public double getValue() {
				return iteration;
			}
		});
	}

	public int getParameterValueMode() {
		return VALUE_MODE_DISCRETE;
	}
	
	public IOObject[] apply() throws OperatorException {

		IOContainer input = getInput();
		LinkedList<IOObject> output = new LinkedList<IOObject>();
		boolean isSynchronized = getParameterAsBoolean(PARAMETER_SYNCHRONIZE);
		boolean keepOutput = getParameterAsBoolean(PARAMETER_KEEP_OUTPUT);
		
		List parameterList = getParameterList(PARAMETER_PARAMETERS);
		Operator[] operators = new Operator[parameterList.size()];
		String[] parameters = new String[parameterList.size()];
		String[][] values = new String[parameterList.size()][];
		int[] currentIndex = new int[parameterList.size()];

		// check parameter values
		List<ParameterValues> parameterValuesList = parseParameterValues(getParameterList("parameters"));
		int numberOfCombinations = 1;
        int lastNumberOfValues = -1;
		for (Iterator<ParameterValues> iterator = parameterValuesList.iterator(); iterator.hasNext(); ) {
			ParameterValues parameterValues = iterator.next();
			if (parameterValues instanceof ParameterValueRange) {
				logWarning("found (and deleted) parameter values range (" + parameterValues.getKey() + ") which makes no sense in grid parameter optimization");
				iterator.remove();
			}
			numberOfCombinations *= parameterValues.getNumberOfValues();
		}

		// initialize data structures
        operators = new Operator[parameterValuesList.size()];
        parameters = new String[parameterValuesList.size()];
        values = new String[parameterValuesList.size()][];
        currentIndex = new int[parameterValuesList.size()];
		
        // get parameter values and fill data structures
        int index = 0;
		for (Iterator<ParameterValues> iterator = parameterValuesList.iterator(); iterator.hasNext(); ) {
			ParameterValues parameterValues = iterator.next();
			operators[index] = parameterValues.getOperator();
			parameters[index] = parameterValues.getParameterType().getKey();
			values[index] = parameterValues.getValuesArray();
            if (!isSynchronized) {
            	numberOfCombinations *= values[index].length;
            }
            else {
            	numberOfCombinations = values[index].length;
                if (lastNumberOfValues < 0) {
                    lastNumberOfValues = values[index].length;
                } else {
                    if (lastNumberOfValues != values[index].length)
                        throw new UserError(this, 926);
                }
            }
			index++;
		}
		
		this.iteration = 0;
		while (true) {
			log("Using parameter set");
			// set all parameter values
			for (int j = 0; j < operators.length; j++) {
                operators[j].setParameter(parameters[j], values[j][currentIndex[j]].trim());
				//operators[j].getParameters().setParameter(parameters[j], values[j][currentIndex[j]].trim());
				log(operators[j] + "." + parameters[j] + " = " + values[j][currentIndex[j]].trim());
			}
			
			setInput(input.copy());
			IOObject[] evalout = super.apply();
			IOContainer evalCont = new IOContainer(evalout);
			try {
				this.performance = evalCont.remove(PerformanceVector.class);
			} catch (MissingIOObjectException e) {
				log("Inner operators of ParameterIteration do not provide performance vectors: performance cannot be plotted!");
			}
			if (keepOutput) {
				// get the output of the operator chain
				for (int j = 0; j < evalout.length; j++) {
                    evalout[j].setSource(evalout[j].getSource() + " (" + this.getName() + ", iter.: " + (this.iteration + 1) + ")");
					output.add(evalout[j]);
				}
			}
			this.iteration++;
			
			boolean ok = true;
			if (!isSynchronized) { 
				// next parameter values
				int k = 0;
				while (!(++currentIndex[k] < values[k].length)) {
					currentIndex[k] = 0;
					k++;
					if (k >= currentIndex.length) {
						ok = false;
						break;
					}
				}
			}
			else {				
				for (int k = 0; k < currentIndex.length; k++) {			
					currentIndex[k]++;									
				}
				if (!(currentIndex[0] < values[0].length)) {
					ok = false;
					break;					
				}
			}
			
			if (!ok) {				
				break;
			}				

			inApplyLoop();
		}
		
		// return IOObjects of last operator in chain
		if (keepOutput) {
			getInput().removeAll();
		}
		IOObject [] temp = new IOObject[output.size()];
		output.toArray(temp);
		
		return temp;
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {		
		return new Class[0];		
	}

	/** Returns a simple chain condition. */
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new SimpleChainInnerOperatorCondition();
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_SYNCHRONIZE, "Synchronize parameter iteration", false));
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_OUTPUT, "Delivers the merged output of the last operator of all the iterations, delivers the original input otherwise.", false));
		return types;
	}
}
