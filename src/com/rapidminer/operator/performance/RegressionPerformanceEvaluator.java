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
package com.rapidminer.operator.performance;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.LogService;

/**
 * <p>This performance evaluator operator should be used for regression tasks, 
 * i.e. in cases where the label attribute has a numerical value type.
 * The operator expects a test {@link ExampleSet}
 * as input, whose elements have both true and predicted labels, and delivers as
 * output a list of performance values according to a list of performance
 * criteria that it calculates. If an input performance vector was already
 * given, this is used for keeping the performance values.</p> 
 * 
 * <p>All of the performance criteria can be switched on using boolean parameters. 
 * Their values can be queried by a
 * {@link com.rapidminer.operator.visualization.ProcessLogOperator} using the same names.
 * The main criterion is used for comparisons and need to be specified only for
 * processes where performance vectors are compared, e.g. feature selection
 * or other meta optimization process setups. 
 * If no other main criterion was selected, the first criterion in the 
 * resulting performance vector will be assumed to be the main criterion.</p> 
 * 
 * <p>The resulting performance vectors are usually compared with a standard
 * performance comparator which only compares the fitness values of the main
 * criterion. Other implementations than this simple comparator can be
 * specified using the parameter <var>comparator_class</var>. This may for
 * instance be useful if you want to compare performance vectors according to
 * the weighted sum of the individual criteria. In order to implement your own
 * comparator, simply subclass {@link PerformanceComparator}. Please note that
 * for true multi-objective optimization usually another selection scheme is
 * used instead of simply replacing the performance comparator.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: RegressionPerformanceEvaluator.java,v 1.3 2007/07/18 12:43:08 ingomierswa Exp $
 */
public class RegressionPerformanceEvaluator extends AbstractPerformanceEvaluator {
	
	/** The proper criteria to the names. */
	private static final Class[] SIMPLE_CRITERIA_CLASSES = {
		com.rapidminer.operator.performance.RootMeanSquaredError.class,
		com.rapidminer.operator.performance.AbsoluteError.class, 
		com.rapidminer.operator.performance.RelativeError.class, 
		com.rapidminer.operator.performance.NormalizedAbsoluteError.class, 
		com.rapidminer.operator.performance.RootRelativeSquaredError.class, 
		com.rapidminer.operator.performance.SquaredError.class, 
		com.rapidminer.operator.performance.CorrelationCriterion.class,
		com.rapidminer.operator.performance.SquaredCorrelationCriterion.class, 
		com.rapidminer.operator.performance.PredictionAverage.class, 
        com.rapidminer.operator.performance.PredictionTrendAccuracy.class
	};
		
	public RegressionPerformanceEvaluator(OperatorDescription description) {
		super(description);
	}
	
	protected void checkCompatibility(ExampleSet exampleSet) throws OperatorException {
		Tools.isLabelled(exampleSet);
		Tools.isNonEmpty(exampleSet);
		
		Attribute label = exampleSet.getAttributes().getLabel();
		if (label.isNominal()) {
			throw new UserError(this, 102, "the calculation of performance criteria for regression tasks", label.getName());
		}
	}

	protected double[] getClassWeights(Attribute label) {
		return null;
	}

	public List<PerformanceCriterion> getCriteria() {
		List<PerformanceCriterion> allCriteria = new LinkedList<PerformanceCriterion>();
		for (int i = 0; i < SIMPLE_CRITERIA_CLASSES.length; i++) {
			try {
				allCriteria.add((PerformanceCriterion)SIMPLE_CRITERIA_CLASSES[i].newInstance());
			} catch (InstantiationException e) {
				LogService.getGlobal().logError("Cannot instantiate " + SIMPLE_CRITERIA_CLASSES[i] + ". Skipping...");
			} catch (IllegalAccessException e) {
				LogService.getGlobal().logError("Cannot instantiate " + SIMPLE_CRITERIA_CLASSES[i] + ". Skipping...");
			}
		}
		
		// rank correlation criteria
		for (int i = 0; i < RankCorrelation.NAMES.length; i++) {
			allCriteria.add(new RankCorrelation(i));
		}
		
		return allCriteria;
	}
}
