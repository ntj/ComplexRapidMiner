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
package com.rapidminer.operator.olap;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.olap.aggregation.AggregationFunction;
import com.rapidminer.operator.olap.aggregation.AverageFunction;
import com.rapidminer.operator.olap.aggregation.VarianceFunction;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.math.AnovaCalculator;
import com.rapidminer.tools.math.SignificanceCalculationException;
import com.rapidminer.tools.math.SignificanceTestResult;


/**
 * <p>This operator creates groups of the input example set based on
 * the defined grouping attribute. For each of the groups the mean and
 * variance of another attribute (the anova attribute) is calculated
 * and an ANalysis Of VAriance (ANOVA) is performed. The result will
 * be a significance test result for the specified significance level
 * indicating if the values for the attribute are significantly different
 * between the groups defined by the grouping attribute.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: GroupedANOVAOperator.java,v 1.4 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class GroupedANOVAOperator extends Operator {
    
    public static final String PARAMETER_ANOVA_ATTRIBUTE = "anova_attribute";
    
    public static final String PARAMETER_GROUP_BY_ATTRIBUTE = "group_by_attribute";

    public static final String PARAMETER_SIGNIFICANCE_LEVEL = "significance_level";
    
    public static final String PARAMETER_ONLY_DISTINCT = "only_distinct";
    
    
    public GroupedANOVAOperator(OperatorDescription desc) {
        super(desc);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        
        // init and checks
        String attributeName = getParameterAsString(PARAMETER_ANOVA_ATTRIBUTE);
        String groupByAttributeName = getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTE);
        boolean onlyDistinct = getParameterAsBoolean(PARAMETER_ONLY_DISTINCT);
        
        Attribute anovaAttribute   = exampleSet.getAttributes().get(attributeName);
        if (anovaAttribute == null) {
        	throw new UserError(this, 111, this.getParameterAsString(PARAMETER_ANOVA_ATTRIBUTE));
        }
        if (anovaAttribute.isNominal()) {
        	throw new UserError(this, 104, new Object[] { this.getParameterAsString(PARAMETER_ANOVA_ATTRIBUTE), "anova calculation" });
        }
        
        Attribute groupByAttribute = exampleSet.getAttributes().get(groupByAttributeName);
        if (groupByAttribute == null) {
        	throw new UserError(this, 111, this.getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTE));
        }
        if (!groupByAttribute.isNominal()) {
        	throw new UserError(this, 103, new Object[] { this.getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTE), "grouping by attribute." });
        }

        // create anova calculator
        AnovaCalculator anovaCalculator = new AnovaCalculator();
        double alpha = getParameterAsDouble(PARAMETER_SIGNIFICANCE_LEVEL);
        anovaCalculator.setAlpha(alpha);
        
        // add groups
        SplittedExampleSet grouped = SplittedExampleSet.splitByAttribute(exampleSet, groupByAttribute);
        AggregationFunction meanFunction = new AverageFunction();
        AggregationFunction varianceFunction = new VarianceFunction();
        for (int i = 0; i < grouped.getNumberOfSubsets(); i++) {
        	grouped.selectSingleSubset(i);
        	double[] values = getValues(grouped, anovaAttribute, onlyDistinct);
        	double mean = meanFunction.calculate(values);
        	double variance = varianceFunction.calculate(values);
        	anovaCalculator.addGroup(grouped.size(), mean, variance);
        }
        
        // calculate and return result
        SignificanceTestResult result = null;
        try {
        	result = anovaCalculator.performSignificanceTest();
        } catch (SignificanceCalculationException e) {
        	throw new UserError(this, 920, e.getMessage());
        }
        return new IOObject[] { result };
    }
    
    private double[] getValues(ExampleSet exampleSet, Attribute attribute, boolean onlyDistinct) {
        Collection<Double> valueCollection = new LinkedList<Double>();
        if (onlyDistinct)
            valueCollection = new TreeSet<Double>();
        
        for (Example e : exampleSet) {
            valueCollection.add(e.getValue(attribute));
        }
        
        double[] result = new double[valueCollection.size()];
        int counter = 0;
        for (double d : valueCollection)
            result[counter++] = d;
        return result;
    }
    
    /** Indicates that the consumption of example sets can be user defined (default: no consumption). */
    public InputDescription getInputDescription(Class cls) {
        if (ExampleSet.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        } else {
            return super.getInputDescription(cls);
        }
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { SignificanceTestResult.class };
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeString(PARAMETER_ANOVA_ATTRIBUTE, "Calculate the ANOVA for this attribute based on the groups defines by " + PARAMETER_GROUP_BY_ATTRIBUTE + ".", false)); 
        types.add(new ParameterTypeString(PARAMETER_GROUP_BY_ATTRIBUTE, "Performs a grouping by the values of the attribute with this name.", false)); 
        types.add(new ParameterTypeDouble(PARAMETER_SIGNIFICANCE_LEVEL, "The significance level for the ANOVA calculation.", 0.0d, 1.0d, 0.05d)); 
        types.add(new ParameterTypeBoolean(PARAMETER_ONLY_DISTINCT, "Indicates if only rows with distinct values for the aggregation attribute should be used for the calculation of the aggregation function.", false));     
        return types;
    }
}
