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

import com.rapidminer.operator.ContainerModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;
import com.rapidminer.operator.learner.PredictionModel;
import com.rapidminer.operator.learner.meta.AdaBoostModel;
import com.rapidminer.operator.learner.meta.BayBoostModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * Obsolete.
 * 
 * @author Martin Scholz
 * @version $Id: MartinsIterationOperator.java,v 1.10 2006/04/05 08:57:26
 *          ingomierswa Exp $
 */
public class MartinsIterationOperator extends OperatorChain {


	/** The parameter name for &quot;The maximum iteration.&quot; */
	public static final String PARAMETER_MAX_ITERATION = "max_iteration";
	private int iteration = 0;

	private PerformanceVector performance;

	public MartinsIterationOperator(OperatorDescription description) {
		super(description);
		addValue(new Value("performance", "best performance") {

			public double getValue() {
				if (performance != null)
					return performance.getMainCriterion().getAverage();
				else
					return Double.NaN;
			}
		});
		addValue(new Value("iteration", "current iteration") {

			public double getValue() {
				return iteration;
			}
		});
	}
	
	public IOObject[] apply() throws OperatorException {
		IOContainer input = getInput();
		PredictionModel ensemble = findEnsembleModel(input.get(Model.class));
		int maxIteration = getParameterAsInt(PARAMETER_MAX_ITERATION);

		PerformanceVector pv = new PerformanceVector();
		String perfName = "";
		for (iteration = 1; iteration <= maxIteration; iteration++) {
			if (ensemble instanceof BayBoostModel) {
				((BayBoostModel) ensemble).setMaxModelNumber(iteration);
			}
			else if (ensemble instanceof AdaBoostModel) {
				((AdaBoostModel) ensemble).setMaxModelNumber(iteration);
			} 
			else ensemble.setParameter("iteration", iteration + "");
			
			setInput(input.copy());
			performance = getPerformance();

			double auc = performance.getCriterion("AUC").getAverage();
			perfName = "AUC_" + iteration;
			PerformanceCriterion perfCrit = new EstimatedPerformance(perfName, auc, 1, false);
			pv.addCriterion(perfCrit);
			
			double acc = performance.getCriterion("accuracy").getAverage();
			String perfNameAcc = "ACC_" + iteration;
			perfCrit = new EstimatedPerformance(perfNameAcc, acc, 1, false);
			pv.addCriterion(perfCrit);
			
			double rms = performance.getCriterion("root_mean_squared_error").getAverage();
			String perfNameRms = "RMS_" + iteration;
			perfCrit = new EstimatedPerformance(perfNameRms, rms, 1, false);
			pv.addCriterion(perfCrit);

			inApplyLoop();
		}
		pv.setMainCriterionName(perfName);

		return new IOObject[] { pv };
	}

	private PredictionModel findEnsembleModel(Model model) {
		if (model == null)
			return null;
		
		if (model instanceof BayBoostModel
				|| model instanceof AdaBoostModel)
			return (PredictionModel) model;
		
		if (model instanceof ContainerModel) {
			ContainerModel cm = (ContainerModel) model;
			for (int i=0; i<cm.getNumberOfModels(); i++) {
				PredictionModel res = findEnsembleModel(cm.getModel(i));
				if (res != null)
					return res;
			}
		}
		return null;
	}	
	
	/**
	 * Applies the inner operator and employs the PerformanceEvaluator for
	 * calculating a list of performance criteria which is returned.
	 */
	private PerformanceVector getPerformance() throws OperatorException {
		IOObject[] evalout = super.apply();
		IOContainer evalCont = new IOContainer(evalout);
		return evalCont.remove(PerformanceVector.class);
	}

	public Class[] getInputClasses() {
		return new Class[0];
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
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
		types.add(new ParameterTypeInt(PARAMETER_MAX_ITERATION, "The maximum iteration.", 1, Integer.MAX_VALUE, 10));
		return types;
	}
}
