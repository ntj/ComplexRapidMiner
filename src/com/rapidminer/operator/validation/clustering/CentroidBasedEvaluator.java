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

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.learner.clustering.CentroidBasedClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.ClustererPreconditions;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.IterationArrayList;


/**
 * An evaluator for centroid based clustering methods.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: CentroidBasedEvaluator.java,v 1.5 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class CentroidBasedEvaluator extends Operator {

    public static final String PARAMETER_MAIN_CRITERION = "main_criterion";
    
    public static final String PARAMETER_MAIN_CRITERION_ONLY = "main_criterion_only";
    
    public static final String PARAMETER_NORMALIZE = "normalize";
    
    public static final String PARAMETER_MAXIMIZE = "maximize";
	
	private double avgWithinClusterDistance;

    private double daviesBouldin;

    public static final String[] CRITERIA_LIST = { "avg_within_distance", "DaviesBouldin" };

    public static final String[] CRITERIA_LIST_SHORT = { "AVD", "DB" };

    /**
     * Constructor for ClusterDensityEvaluator.
     */
    public CentroidBasedEvaluator(OperatorDescription description) {
        super(description);

        addValue(new Value(CRITERIA_LIST_SHORT[0], CRITERIA_LIST[0], false) {
            public double getValue() {
                return avgWithinClusterDistance;
            }
        });

        addValue(new Value(CRITERIA_LIST_SHORT[1], CRITERIA_LIST[1], false) {
            public double getValue() {
                return daviesBouldin;
            }
        });

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

    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class, ClusterModel.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }

    public IOObject[] apply() throws OperatorException {
    	ClusterModel clusterModel = getInput(ClusterModel.class);
    	if (!(clusterModel instanceof CentroidBasedClusterModel)) {
    		throw new UserError(this, 122, "centroid based cluster model");
    	}
        CentroidBasedClusterModel cm = (CentroidBasedClusterModel)clusterModel;
        ExampleSet es = getInput(ExampleSet.class);
        
        es.remapIds();

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

        int mainCriterionIndex = getParameterAsInt(PARAMETER_MAIN_CRITERION);
        boolean returnMainCritetionOnly = getParameterAsBoolean(PARAMETER_MAIN_CRITERION_ONLY);

        double multFactor = -1.0;
        if (getParameterAsBoolean(PARAMETER_MAXIMIZE))
            multFactor = 1.0;

        double divisionFactor = 1.0;
        if (getParameterAsBoolean(PARAMETER_NORMALIZE))
            divisionFactor = es.getAttributes().size();

        // Average Squared withing cluster distance 0

        double[] avgWithinDistances = calcAvgWithinClusterDistance(cm, es);

        avgWithinClusterDistance = avgWithinDistances[cm.getNumberOfClusters()];
        PerformanceCriterion withinClusterDist = new EstimatedPerformance(CRITERIA_LIST_SHORT[0], (multFactor * avgWithinClusterDistance) / divisionFactor, 1, false);
        if ((mainCriterionIndex == 0) || !returnMainCritetionOnly)
            performance.addCriterion(withinClusterDist);

        for (int i = 0; i < cm.getNumberOfClusters(); i++) {
            PerformanceCriterion withinDistance = new EstimatedPerformance(CRITERIA_LIST_SHORT[0] + "_cluster_" + cm.getClusterAt(i).getId(), (multFactor * avgWithinDistances[i]) / divisionFactor, 1, false);

            if ((mainCriterionIndex == 0) || !returnMainCritetionOnly)
                performance.addCriterion(withinDistance);
        }

        // Davies Bouldin 1
        daviesBouldin = getDaviesBouldin(cm, es);
        PerformanceCriterion daviesBouldinCriterion = new EstimatedPerformance(CRITERIA_LIST_SHORT[1], (multFactor * daviesBouldin) / divisionFactor, 1, false);
        if ((mainCriterionIndex == 1) || !returnMainCritetionOnly)
            performance.addCriterion(daviesBouldinCriterion);

        performance.setMainCriterionName(CRITERIA_LIST_SHORT[mainCriterionIndex]);

        return new IOObject[] { performance };
    }

    private double[] calcAvgWithinClusterDistance(CentroidBasedClusterModel cm, ExampleSet es) {
        double[] result = new double[cm.getNumberOfClusters() + 1];
        int count = 0;
        double sum = 0.0;
        for (int i = 0; i < cm.getNumberOfClusters(); i++) {

            List<String> objs = new IterationArrayList<String>(cm.getClusterAt(i).getObjects());

            double sumForCluster = 0;
            int countForCluster = 0;

            for (int j = 0; j < objs.size(); j++) {

                String d = objs.get(j);
                double v = cm.getDistanceFromCentroid(i, IdUtils.getExampleFromId(es, d));
                sum = sum + v * v;
                sumForCluster = sumForCluster + v * v;
                count++;
                countForCluster++;
            }
            result[i] = sumForCluster / countForCluster;

        }

        result[cm.getNumberOfClusters()] = sum / count;
        return result;
    }

    private double getDaviesBouldin(CentroidBasedClusterModel cm, ExampleSet es) {
        double[] withinClusterDistance = new double[cm.getNumberOfClusters()];

        for (int i = 0; i < cm.getNumberOfClusters(); i++) {
            int count = 0;
            double sum = 0.0;
            List<String> objs = new IterationArrayList<String>(cm.getClusterAt(i).getObjects());
            for (int j = 0; j < objs.size(); j++) {
                String d = objs.get(j);
                double v = cm.getDistanceFromCentroid(i, IdUtils.getExampleFromId(es, d));
                sum = sum + v;
                count++;
            }

            if (count > 0)
                withinClusterDistance[i] = sum / count;
            else
                withinClusterDistance[i] = 0.0;

        }

        double sum2 = 0.0;

        for (int i = 0; i < cm.getNumberOfClusters(); i++) {
            double max = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < cm.getNumberOfClusters(); j++)
                if (i != j) {
                    double val = (withinClusterDistance[i] + withinClusterDistance[j]) / cm.getCentroidDistance(i, j);
                    if (val > max)
                        max = val;
                }
            sum2 = sum2 + max;
        }
        return sum2 / cm.getNumberOfClusters();
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeCategory(PARAMETER_MAIN_CRITERION, "The main criterion to use", CRITERIA_LIST, 0));
        types.add(new ParameterTypeBoolean(PARAMETER_MAIN_CRITERION_ONLY, "return the main criterion only", false));
        types.add(new ParameterTypeBoolean(PARAMETER_NORMALIZE, "divide the criterion by the number of features", false));
        types.add(new ParameterTypeBoolean(PARAMETER_MAXIMIZE, "do not multiply the result by minus one", false));
        return types;
    }
}
