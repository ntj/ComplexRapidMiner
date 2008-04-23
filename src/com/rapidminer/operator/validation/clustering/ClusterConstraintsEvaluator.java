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
import com.rapidminer.operator.learner.clustering.ClustererPreconditions;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.constrained.constraints.ClusterConstraintList;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;

/**
 * Evaluates a ClusterModel with regard to a given ClusterConstraintList and
 * takes the weight of the violated constraints as performance value.
 * 
 * @author Alexander Daxenberger
 *
 */
public class ClusterConstraintsEvaluator extends Operator {

    public ClusterConstraintsEvaluator(OperatorDescription description) {
        super(description);
    }

    public InputDescription getInputDescription(Class cls) {
        if (ClusterConstraintList.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }
        if (FlatClusterModel.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        return super.getInputDescription(cls);
    }

    public Class[] getInputClasses() {
        return new Class[] { ClusterConstraintList.class, FlatClusterModel.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] {PerformanceVector.class};
    }

    public IOObject[] apply() throws OperatorException {
        PerformanceCriterion clusterConstraints = null;
        PerformanceVector performance = null;
        ClusterConstraintList ccl;
        FlatClusterModel cm;
        double constraintsValue;

        ccl = getInput(ClusterConstraintList.class);

        cm = getInput(FlatClusterModel.class);
        
        ClustererPreconditions.hasClusters(cm);
        ClustererPreconditions.isNonEmpty(cm);

        try {
          performance = getInput(PerformanceVector.class);
        } catch (MissingIOObjectException e) {
          performance = new PerformanceVector();
        }

        constraintsValue = ccl.weightOfViolatedConstraints(cm);

        clusterConstraints = new EstimatedPerformance("cluster constraints", constraintsValue, 1, true);
        performance.addCriterion(clusterConstraints);

        return new IOObject[] { performance };
    }
}
