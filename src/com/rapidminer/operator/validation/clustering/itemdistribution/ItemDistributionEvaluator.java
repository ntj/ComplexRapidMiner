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
package com.rapidminer.operator.validation.clustering.itemdistribution;

import java.util.List;

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
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.ClassNameMapper;


/**
 * Evaluates flat cluster models on how well the items are distributed over the clusters.
 * 
 * @author Michael Wurst
 * @version $Id: ItemDistributionEvaluator.java,v 1.4 2008/05/09 19:22:37 ingomierswa Exp $
 * 
 */
public class ItemDistributionEvaluator extends Operator {

    public static final String PARAMETER_MEASURE = "measure";
	
	private final static String[] DEFAULT_MEASURES = { "com.rapidminer.operator.validation.clustering.itemdistribution.SumOfSquares", "com.rapidminer.operator.validation.clustering.itemdistribution.GiniCoefficient" };

    private ClassNameMapper MEASURE_MAP;

    private double itemDistribution = 0;

    /**
     * Constructor for ClusterNumberEvaluator.
     */
    public ItemDistributionEvaluator(OperatorDescription description) {
        super(description);
        addValue(new Value("item_distribution", "The distribution of items over clusters.", false) {
            public double getValue() {
                return itemDistribution;
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

        ItemDistributionMeasure distrMeasure = (ItemDistributionMeasure) MEASURE_MAP.getInstantiation(getParameterAsString(PARAMETER_MEASURE));

        int totalNumberOfItems = 0;
        int[] count = new int[model.getNumberOfClusters()];
        for (int i = 0; i < model.getNumberOfClusters(); i++) {

            int numItemsInCluster = model.getClusterAt(i).getNumberOfObjects();
            totalNumberOfItems = totalNumberOfItems + numItemsInCluster;
            count[i] = numItemsInCluster;
        }

        PerformanceVector performance = null;

        try {
            performance = getInput(PerformanceVector.class);

        } catch (MissingIOObjectException e) {
            // If no performance vector is available create a new one
        }

        if (performance == null)
            performance = new PerformanceVector();

        
        itemDistribution = distrMeasure.evaluate(count, totalNumberOfItems);
        
        PerformanceCriterion pc = new EstimatedPerformance("Item distribution", itemDistribution, 1, false);
        performance.addCriterion(pc);

        return new IOObject[] { performance };
    }

    public Class[] getInputClasses() {
        return new Class[] { FlatClusterModel.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { PerformanceVector.class };
    }

    public List<ParameterType> getParameterTypes() {

        MEASURE_MAP = new ClassNameMapper(DEFAULT_MEASURES);

        List<ParameterType> types = super.getParameterTypes();
        
        ParameterType type = new ParameterTypeStringCategory(PARAMETER_MEASURE, "the item distribution measure to apply", MEASURE_MAP.getShortClassNames());
        type.setExpert(false);
        types.add(type);

        return types;
    }

}
