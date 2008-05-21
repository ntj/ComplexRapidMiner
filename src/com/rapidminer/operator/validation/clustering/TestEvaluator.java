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
package com.rapidminer.operator.validation.clustering;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.RandomGenerator;


/**
 * Evaluates a cluster model by returning a random value. Used for testing.
 * 
 * @author Michael Wurst
 * @version $Id: TestEvaluator.java,v 1.4 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class TestEvaluator extends Operator {

    public TestEvaluator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() {
        PerformanceVector performance = null;

        try {
            performance = getInput(PerformanceVector.class);
        } catch (MissingIOObjectException e) {
            // If no performance vector is available create a new one
        }

        if (performance == null)
            performance = new PerformanceVector();

        PerformanceCriterion perf = new EstimatedPerformance("random_value", RandomGenerator.getGlobalRandomGenerator().nextDouble(), 1, false);
        performance.addCriterion(perf);
        performance.setMainCriterionName("random_value");

        log("Random Performance is: " + perf.getFitness());

        return new IOObject[] { performance };

    }

    public Class[] getInputClasses() {
        return new Class[0];
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }
}
