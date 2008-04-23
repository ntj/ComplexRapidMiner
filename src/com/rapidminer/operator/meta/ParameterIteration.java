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
package com.rapidminer.operator.meta;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeParameterValue;
import com.rapidminer.parameter.ParameterTypeSingle;
import com.rapidminer.tools.Tools;


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
 * interval definition in the format [start;end;step] (e.g. [10;25;5]). </p>
 * 
 * <p>Please note that this operator has two modes: synchronized and non-synchronized.
 * In the latter, all parameter combinations are generated and the inner operators
 * are applied for each combination. In the synchronized mode, no combinations are
 * generated but the set of all pairs of the increasing number of parameters
 * are used. For the iteration over a single parameter there is no difference
 * between both modes. Please note that the number of parameter possibilities must be 
 * the same for all parameters in the synchronized mode.</p> 
 * 
 * @author Ingo Mierswa
 * @version $Id: ParameterIteration.java,v 1.11 2006/04/05 08:57:26 ingomierswa
 *          Exp $
 */
public class ParameterIteration extends OperatorChain {


	/** The parameter name for &quot;A list of parameters to optimize&quot; */
	public static final String PARAMETER_PARAMETERS = "parameters";

	/** The parameter name for &quot;Synchronize parameter iteration&quot; */
	public static final String PARAMETER_SYNCHRONIZE = "synchronize";

	/** The parameter name for &quot;Keep the output of the last operator in th operator chain&quot; */
	public static final String PARAMETER_KEEP_OUTPUT = "keep_output";
	private int numberOfCombinations = 0;

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
		Iterator i = parameterList.iterator();
		int index = 0;
        int lastNumberOfValues = -1;
		numberOfCombinations = 1;
		while (i.hasNext()) {
			Object[] keyValue = (Object[]) i.next();
			String[] parameter = ((String) keyValue[0]).split("\\.");
			if (parameter.length != 2)
				throw new UserError(this, 907, keyValue[0]);
			Operator operator = getProcess().getOperator(parameter[0]);
			if (operator == null)
				throw new UserError(this, 109, parameter[0]);
			operators[index] = operator;
			parameters[index] = parameter[1];
			ParameterType targetType = operators[index].getParameters().getParameterType(parameters[index]);
			if (targetType == null) {
				throw new UserError(this, 906, parameter[0] + "." + parameter[1]);
			}
			if (!(targetType instanceof ParameterTypeSingle)) {
				throw new UserError(this, 908, parameter[0] + "." + parameter[1]);
			}
            String paraString = (String)keyValue[1];
            int startIndex = paraString.indexOf("["); 
            if (startIndex >= 0) {
                try {
                    int endIndex = paraString.indexOf("]");
                    if (endIndex > startIndex) {
                        String[] startEndStepArray = paraString.substring(startIndex + 1, endIndex).trim().split(";");
                        if (startEndStepArray.length != 3)
                            throw new Exception("number of entries must be 3 (start, end, and step size)");
                        double startValue = Double.parseDouble(startEndStepArray[0]);
                        double endValue   = Double.parseDouble(startEndStepArray[1]);
                        double step       = Double.parseDouble(startEndStepArray[2]);
                        if (step == 0) {
                            throw new Exception("step size of 0 is not allowed");                            
                        }
                        if (endValue <= startValue + step) {
                            throw new Exception("end value must at least be as large as start value plus step size");
                        }
                        String[] allValues = new String[(int)((endValue - startValue) / step) + 1];
                        double currentValue = startValue;
                        int counter = 0;
                        while (currentValue <= endValue) {
                            allValues[counter] = Tools.formatIntegerIfPossible(currentValue, 16);
                            currentValue += step;
                            counter++;
                        }
                        values[index] = allValues;
                    } else {
                        throw new Exception("']' was missing");
                    }
                } catch (Exception e) {
                    throw new UserError(this, 116, keyValue[0], "Parameter values must be defined as a comma separated list or by [start;end;step], was '" + keyValue[1] + "'. Error: " + e.getMessage());
                }
            } else {
                values[index] = ((String) keyValue[1]).split(",");
            }
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

	/**
	 * Returns the highest possible value for the maximum number of innner
	 * operators.
	 */
	public int getMaxNumberOfInnerOperators() {
		return Integer.MAX_VALUE;
	}

	/** Returns 1 for the minimum number of innner operators. */
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeList(PARAMETER_PARAMETERS, "A list of parameters to optimize", new ParameterTypeParameterValue("value", "The values of the parameter (comma-separated"));
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_SYNCHRONIZE, "Synchronize parameter iteration", false));
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_OUTPUT, "Keep the output of the last operator in th operator chain", false));
		return types;
	}
}
