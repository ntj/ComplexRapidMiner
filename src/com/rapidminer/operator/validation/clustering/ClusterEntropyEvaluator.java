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

import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
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
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.tools.IterationArrayList;


/**
 * This operator evaluates the quality of a flat cluster model based on given class labels using an entropy based measure.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterEntropyEvaluator.java,v 1.5 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class ClusterEntropyEvaluator extends Operator {

    private int numClasses;

    public ClusterEntropyEvaluator(OperatorDescription description) {
        super(description);
    }

    public Class[] getInputClasses() {
        return new Class[] { ClusterModel.class, ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }

    public InputDescription getInputDescription(Class cls) {
        if (ClusterModel.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        if (ExampleSet.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        return super.getInputDescription(cls);
    }

    public IOObject[] apply() throws OperatorException {
        ClusterModel clusterModel = getInput(ClusterModel.class);
        ExampleSet es = getInput(ExampleSet.class);

        if (!(clusterModel instanceof FlatClusterModel)) {
        	throw new UserError(this, 122, "flat cluster model");
        }
        
        FlatClusterModel cm = (FlatClusterModel)clusterModel;
        
        Tools.hasNominalLabels(es);
        Tools.checkAndCreateIds(es);
        ClustererPreconditions.isNonEmpty(cm);

        PerformanceVector performance = null;
        es.remapIds();
        try {
            performance = getInput(PerformanceVector.class);

        } catch (MissingIOObjectException e) {
            // If no performance vector is available create a new one

        }

        if (performance == null)
            performance = new PerformanceVector();

        numClasses = es.getAttributes().getLabel().getMapping().getValues().size();

        double entr = entropy(cm, es);

        PerformanceCriterion entropyCriterion = new EstimatedPerformance("Entropy", entr, 1, false);
        performance.addCriterion(entropyCriterion);

        return new IOObject[] { performance };
    }

    private double entropy(FlatClusterModel cm, ExampleSet es) {
        double totalEntropy = 0.0;
        int numObjs = 0;

        for (int i = 0; i < cm.getNumberOfClusters(); i++) {

            double clusterEntropy = 0.0;
            int[] count = new int[numClasses];
            for (int k = 0; k < numClasses; k++)
                count[k] = 0;

            List<String> idsInCluster = new IterationArrayList<String>(cm.getClusterAt(i).getObjects());

            for (int j = 0; j < idsInCluster.size(); j++) {

                Example ex = IdUtils.getExampleFromId(es, idsInCluster.get(j));
                int index = (int) ex.getLabel() - 1;
                if ((index < numClasses) && (index >= 0))
                    count[index]++;
                else
                    logWarning("Class index out of bound");
            }
            if (idsInCluster.size() > 0)
                for (int k = 0; k < numClasses; k++)
                    if (count[k] > 0)
                        clusterEntropy = clusterEntropy - (((double) count[k]) / ((double) idsInCluster.size())) * Math.log(((double) count[k]) / ((double) idsInCluster.size()));

            totalEntropy = totalEntropy + clusterEntropy * idsInCluster.size();
            numObjs = numObjs + idsInCluster.size();
        }
        return totalEntropy / numObjs;
    }
}
