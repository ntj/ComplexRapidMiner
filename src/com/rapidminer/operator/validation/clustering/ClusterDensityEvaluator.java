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

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.learner.clustering.ClustererPreconditions;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.tools.IterationArrayList;


/**
 * This operator is used to evaluate a flat cluster model based on diverse density measures. Currently, only the avg. within cluster similarity/distance (depending on the type of SimilarityMeasure input object used) is supported.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterDensityEvaluator.java,v 1.1 2007/05/27 22:01:05 ingomierswa Exp $
 * 
 */
public class ClusterDensityEvaluator extends Operator {

    private double avgClusterSim = 0.0;

    /**
     * Constructor for ClusterDensityEvaluator.
     */
    public ClusterDensityEvaluator(OperatorDescription description) {
        super(description);

        addValue(new Value("clusterdensity", "Avg. within cluster similarity/distance", false) {
            public double getValue() {
                return avgClusterSim;
            }
        });
    }

    public InputDescription getInputDescription(Class cls) {
        if (SimilarityMeasure.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        if (FlatClusterModel.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        }

        return super.getInputDescription(cls);

    }

    public Class[] getInputClasses() {
        return new Class[] { FlatClusterModel.class, SimilarityMeasure.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }

    public IOObject[] apply() throws OperatorException {

        SimilarityMeasure sim = getInput(SimilarityMeasure.class);
        FlatClusterModel cm = getInput(FlatClusterModel.class);

        ClustererPreconditions.hasClusters(cm);
        ClustererPreconditions.isNonEmpty(cm);

        PerformanceVector performance = null;

        try {
            performance = getInput(PerformanceVector.class);

        } catch (MissingIOObjectException e) {
            // If no performance vector is available create a new one
        }

        if (performance == null)
            performance = new PerformanceVector();

        double[] avgWithinClusterSims = withinClusterAvgSim(cm, sim);

        avgClusterSim = avgWithinClusterSims[cm.getNumberOfClusters()];

        PerformanceCriterion withinClusterSim = null;

        if (sim.isDistance())
            withinClusterSim = new EstimatedPerformance("Avg. within cluster distance", avgClusterSim, 1, true);
        else
            withinClusterSim = new EstimatedPerformance("Avg. within cluster similarity", avgClusterSim, 1, false);

        performance.addCriterion(withinClusterSim);

        for (int i = 0; i < cm.getNumberOfClusters(); i++) {

            PerformanceCriterion withinSingleClusterSim = null;

            if (sim.isDistance())
                withinSingleClusterSim = new EstimatedPerformance("Avg. within cluster distance for cluster " + cm.getClusterAt(i).getId(), avgWithinClusterSims[i], 1, true);
            else
                withinSingleClusterSim = new EstimatedPerformance("Avg. within cluster similarity for cluster " + cm.getClusterAt(i).getId(), avgWithinClusterSims[i], 1, false);

            performance.addCriterion(withinSingleClusterSim);

        }

        return new IOObject[] { performance };
    }

    private double[] withinClusterAvgSim(FlatClusterModel cm, SimilarityMeasure sim) {

        double sum = 0.0;
        int count = 0;

        double[] result = new double[cm.getNumberOfClusters() + 1];

        for (int i = 0; i < cm.getNumberOfClusters(); i++) {

            List<String> objs = new IterationArrayList<String>(cm.getClusterAt(i).getObjects());

            double sumForCluster = 0;
            int countForCluster = 0;

            for (int j = 0; j < objs.size(); j++) {

                String d1 = objs.get(j);
                for (int k = j; k < objs.size(); k++) {

                    String d2 = objs.get(k);

                    if (sim.isSimilarityDefined(d1, d2)) {
                        double v = sim.similarity(d1, d2);
                        sum = sum + v;
                        sumForCluster = sumForCluster + v;
                        count++;
                        countForCluster++;
                    }

                }
            }

            result[i] = sumForCluster / countForCluster;

        }

        result[cm.getNumberOfClusters()] = sum / count;
        return result;

    }

}
