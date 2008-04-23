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
package com.rapidminer.operator.validation.clustering;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;

/**
 * This operator does actually not compute a performance criterion but simply provides the number of cluster as a value.
 * 
 * @author Cedric Copy, Timm Euler, Ingo Mierswa, Michael Wurst
 * @version $Id: ClusterNumberEvaluator.java,v 1.1 2007/05/27 22:01:06 ingomierswa Exp $
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

        if (FlatClusterModel.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, true, true);
        }

        return super.getInputDescription(cls);

    }

    public IOObject[] apply() throws OperatorException {

        FlatClusterModel model = getInput(FlatClusterModel.class);
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
