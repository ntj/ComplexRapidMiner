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
package com.rapidminer.operator.learner;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Tools;


/**
 * A <tt>Learner</tt> is an operator that encapsulates the learning step of a
 * machine learning method. New learning schemes should extend this class to
 * support the same parameters as other RapidMiner learners. The main purpose of this
 * class is to perform some compatibility checks.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractLearner.java,v 1.10 2008/05/09 19:23:25 ingomierswa Exp $
 */
public abstract class AbstractLearner extends Operator implements Learner {


	/** The property name for &quot;Indicates if only a warning should be made if learning capabilities are not fulfilled (instead of breaking the process).&quot; */
	public static final String PROPERTY_RAPIDMINER_GENERAL_CAPABILITIES_WARN = "rapidminer.general.capabilities.warn";
	static {
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GENERAL_CAPABILITIES_WARN, "Indicates if only a warning should be made if learning capabilities are not fulfilled (instead of breaking the process).", false));	
	}
	
	/** Creates a new abstract learner. */
	public AbstractLearner(OperatorDescription description) {
		super(description);
	}

	/**
	 * Trains a model using an ExampleSet from the input. 
     * Uses the method learn(ExampleSet).
	 */
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		// some checks
		if (exampleSet.getAttributes().getLabel() == null) {
			throw new UserError(this, 105);
		}
        if (exampleSet.getAttributes().getLabel().isNominal() && (exampleSet.getAttributes().getLabel().getMapping().size() <= 1)) {
            throw new UserError(this, 118, new Object[] { exampleSet.getAttributes().getLabel(), exampleSet.getAttributes().getLabel().getMapping().size(), "at least 2" });
        }
		if (exampleSet.getAttributes().size() == 0) {
			throw new UserError(this, 106);
		}
        if (exampleSet.size() == 0) {
            throw new UserError(this, 117);
        }
        
		// check capabilities and produce errors if they are not fulfilled
        CapabilityCheck check = new CapabilityCheck(this, Tools.booleanValue(System.getProperty(PROPERTY_RAPIDMINER_GENERAL_CAPABILITIES_WARN), true) || onlyWarnForNonSufficientCapabilities());
        check.checkLearnerCapabilities(this, exampleSet);

		List<IOObject> results = new LinkedList<IOObject>();
		Model model = learn(exampleSet);
		results.add(model);

		// weights must be calculated _after_ learning
		if (shouldCalculateWeights()) {
			AttributeWeights weights = getWeights(exampleSet);
			if (weights != null)
				results.add(weights);
		}

        PerformanceVector perfVector = null;
		if (shouldEstimatePerformance()) {
			perfVector = getEstimatedPerformance();
		}

        if (shouldDeliverOptimizationPerformance()) {
            PerformanceVector optimizationPerformance = getOptimizationPerformance();
            if (optimizationPerformance != null) {
                if (perfVector != null) {
                    
                } else {
                    perfVector = optimizationPerformance;
                }
            }
        }
        if (perfVector != null)
            results.add(perfVector);
        
		IOObject[] resultArray = new IOObject[results.size()];
		results.toArray(resultArray);
		return resultArray;
	}

	/**
	 * Returns true if the user wants to estimate the performance (depending on
	 * a parameter). In this case the method getEstimatedPerformance() must also
	 * be overriden and deliver the estimated performance. The default
	 * implementation returns false.
	 */
	public boolean shouldEstimatePerformance() {
		return false;
	}

	/**
	 * Returns true if the user wants to calculate feature weights (depending on
	 * a parameter). In this case the method getWeights() must also be overriden
	 * and deliver the calculated weights. The default implementation returns
	 * false.
	 */
	public boolean shouldCalculateWeights() {
		return false;
	}

    /** 
     * Returns true it the user wants to deliver the performance of the original optimization
     * problem. Since many learners are basically optimization procedures for a certain type
     * of objective function the result of this procedure might also be of interest in some cases. 
     */
    public boolean shouldDeliverOptimizationPerformance() {
        return false;
    }
    
	/**
	 * Returns the estimated performance. Subclasses which supports the
	 * capability to estimate the learning performance must override this
	 * method. The default implementation throws an exception.
	 */
	public PerformanceVector getEstimatedPerformance() throws OperatorException {
		throw new UserError(this, 912, getName(), "estimation of performance not supported.");
	}

    /**
     * Returns the resulting performance of the original optimization problem. 
     * Subclasses which supports the capability to deliver this performance 
     * must override this method. The default implementation throws an exception.
     */
    public PerformanceVector getOptimizationPerformance() throws OperatorException {
        throw new UserError(this, 912, getName(), "delivering the original optimization performance is not supported.");
    }
    
	/**
	 * Returns the calculated weight vectors. Subclasses which supports the
	 * capability to calculate feature weights must override this method. The
	 * default implementation throws an exception.
	 */
	public AttributeWeights getWeights(ExampleSet exampleSet) throws OperatorException {
		throw new UserError(this, 916, getName(), "calculation of weights not supported.");
	}

	/** Indicates that the consumption of example sets can be user defined. */
	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	/** Returns true. */
	public boolean onlyWarnForNonSufficientCapabilities() {
		return false;
	}
	
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		List<Class> classList = new LinkedList<Class>();
		classList.add(Model.class);
		if (shouldEstimatePerformance())
			classList.add(PerformanceVector.class);
		if (shouldCalculateWeights())
			classList.add(AttributeWeights.class);
		Class[] result = new Class[classList.size()];
		classList.toArray(result);
		return result;
	}
}
