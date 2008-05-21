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
import com.rapidminer.operator.learner.clustering.ClusterModel;
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
 * @author Alexander Daxenberger, Ingo Mierswa
 * @version $Id: ClusterConstraintsEvaluator.java,v 1.6 2008/05/09 19:23:23 ingomierswa Exp $
 */
public class ClusterConstraintsEvaluator extends Operator {

    public ClusterConstraintsEvaluator(OperatorDescription description) {
        super(description);
    }

    public InputDescription getInputDescription(Class cls) {
        if (ClusterConstraintList.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }
        if (ClusterModel.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        return super.getInputDescription(cls);
    }

    public Class[] getInputClasses() {
        return new Class[] { ClusterConstraintList.class, ClusterModel.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }

    public IOObject[] apply() throws OperatorException {
        PerformanceCriterion clusterConstraints = null;
        PerformanceVector performance = null;
        ClusterConstraintList ccl;
        ClusterModel clusterModel;
        double constraintsValue;

        ccl = getInput(ClusterConstraintList.class);

        clusterModel = getInput(ClusterModel.class);
        
        if (!(clusterModel instanceof FlatClusterModel)) {
        	throw new UserError(this, 122, "flat cluster model");
        }
        
        FlatClusterModel cm = (FlatClusterModel)clusterModel;
        
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
