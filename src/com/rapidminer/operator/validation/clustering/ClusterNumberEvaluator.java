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
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;

/**
 * This operator does actually not compute a performance criterion but simply provides the number of cluster as a value.
 * 
 * @author Cedric Copy, Timm Euler, Ingo Mierswa, Michael Wurst
 * @version $Id: ClusterNumberEvaluator.java,v 1.4 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class ClusterNumberEvaluator extends Operator {

    private int numberOfClusters;

    /**
     * Constructor for ClusterNumberEvaluator.
     */
    public ClusterNumberEvaluator(OperatorDescription description) {
        super(description);
        addValue(new Value("clusternumber", "The number of clusters.", false) {
            public double getValue() {
                return numberOfClusters;
            }
        });
    }

    public InputDescription getInputDescription(Class cls) {
        if (ClusterModel.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, true, true);
        }

        return super.getInputDescription(cls);

    }

    public IOObject[] apply() throws OperatorException {
        ClusterModel clusterModel = getInput(ClusterModel.class);
        
        if (!(clusterModel instanceof FlatClusterModel)) {
        	throw new UserError(this, 122, "flat cluster model");
        }
        
        FlatClusterModel model = (FlatClusterModel)clusterModel;
        this.numberOfClusters = model.getNumberOfClusters();

        int numItems = 0;
        for (int i = 0; i < model.getNumberOfClusters(); i++)
            numItems = +model.getClusterAt(i).getNumberOfObjects();

        PerformanceVector performance = null;

        try {
            performance = getInput(PerformanceVector.class);

        } catch (MissingIOObjectException e) {
            // If no performance vector is available create a new one
        }

        if (performance == null)
            performance = new PerformanceVector();

        PerformanceCriterion pc = new EstimatedPerformance("Number of clusters", 1.0 - (((double) model.getNumberOfClusters()) / ((double) numItems)), 1, false);
        performance.addCriterion(pc);

        return new IOObject[] { performance };
    }

    public Class[] getInputClasses() {
        return new Class[] { FlatClusterModel.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }
}
