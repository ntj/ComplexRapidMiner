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
package com.rapidminer.operator.olap.aggregation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.preprocessing.filter.ExampleFilter;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.Ontology;


/**
 * <p>This operator creates a new example set from the input example set
 * showing the result of the application of an arbitrary aggregation
 * function (as SUM, COUNT etc. known from SQL). Before the values of 
 * different rows are aggregated into a new row the rows might be
 * grouped by the values of a single attribute (similar to the group-by
 * clause known from SQL). In this case for each group a new line will
 * be created.</p>
 * 
 * <p>Please note that the known HAVING clause from SQL can be simulated
 * by an additional {@link ExampleFilter} operator following this one.</p>
 * 
 * @author Ingo Mierswa
 * @version $Id: AggregationOperator.java,v 1.5 2008/05/09 19:23:02 ingomierswa Exp $
 */
public class AggregationOperator extends Operator {
    
    private static final String PARAMETER_AGGREGATION_FUNCTION = "aggregation_function";

    private static final String PARAMETER_AGGREGATION_ATTRIBUTE = "aggregation_attribute";
    
    private static final String PARAMETER_GROUP_BY_ATTRIBUTE = "group_by_attribute";
    
    private static final String PARAMETER_ONLY_DISTINCT = "only_distinct";
    
    public static final Class[] KNOWN_AGGREGATION_FUNCTIONS = {
        AverageFunction.class,
        VarianceFunction.class,
        CountFunction.class,
        MinFunction.class,
        MaxFunction.class,
        SumFunction.class
    };
    
    public static final String[] KNOWN_AGGREGATION_FUNCTION_NAMES = {
        "average",
        "variance",
        "count",
        "min",
        "max",
        "sum"
    };
    
    public AggregationOperator(OperatorDescription desc) {
        super(desc);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        
        AggregationFunction function;
        try {
            function = createAggregationFunction(getParameterAsString(PARAMETER_AGGREGATION_FUNCTION));
        } catch (InstantiationException e) {
            throw new UserError(this, 904, getParameterAsString(PARAMETER_AGGREGATION_FUNCTION), e.getMessage());
        } catch (IllegalAccessException e) {
            throw new UserError(this, 904, getParameterAsString(PARAMETER_AGGREGATION_FUNCTION), e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new UserError(this, 904, getParameterAsString(PARAMETER_AGGREGATION_FUNCTION), e.getMessage());
        }
        
        String attributeName = getParameterAsString(PARAMETER_AGGREGATION_ATTRIBUTE);
        Attribute aggregationAttribute = exampleSet.getAttributes().get(attributeName);
        
        if (aggregationAttribute == null) {
            throw new UserError(this, 111, this.getParameterAsString(PARAMETER_AGGREGATION_ATTRIBUTE));
        }

        if (!function.supportsAttribute(aggregationAttribute)) {
            throw new UserError(this, 136, aggregationAttribute.getName());
        }
        
        List<Attribute> resultAttributes = new LinkedList<Attribute>();
        Attribute resultGroupAttribute = AttributeFactory.createAttribute("group", Ontology.NOMINAL); 
        resultAttributes.add(resultGroupAttribute);
        resultAttributes.add(AttributeFactory.createAttribute(function.getName() + "(" + aggregationAttribute.getName() + ")", Ontology.REAL));
        boolean onlyDistinct = getParameterAsBoolean(PARAMETER_ONLY_DISTINCT);
        MemoryExampleTable resultTable = new MemoryExampleTable(resultAttributes);
        if (isParameterSet(PARAMETER_GROUP_BY_ATTRIBUTE)) {
            String groupByAttributeName = getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTE);
            Attribute groupByAttribute = exampleSet.getAttributes().get(groupByAttributeName);

            if (groupByAttribute == null) {
                throw new UserError(this, 111, this.getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTE));
            }

            if (!groupByAttribute.isNominal()) {
                throw new UserError(this, 103, new Object[] { this.getParameterAsString(PARAMETER_GROUP_BY_ATTRIBUTE), "grouping by attribute."
                });
            }

            SplittedExampleSet grouped = SplittedExampleSet.splitByAttribute(exampleSet, groupByAttribute);
            for (int i = 0; i < grouped.getNumberOfSubsets(); i++) {
                grouped.selectSingleSubset(i);
                double[] values = getValues(grouped, aggregationAttribute, onlyDistinct);
                double aggregationValue = function.calculate(values);
                double[] data = new double[] { resultGroupAttribute.getMapping().mapString(groupByAttribute.getMapping().mapIndex(i)), aggregationValue };
                resultTable.addDataRow(new DoubleArrayDataRow(data));
            }
        } else {
            double[] values = getValues(exampleSet, aggregationAttribute, onlyDistinct);
            double aggregationValue = function.calculate(values);
            double[] data = new double[] { resultGroupAttribute.getMapping().mapString("all"), aggregationValue };
            resultTable.addDataRow(new DoubleArrayDataRow(data));
        }
        
        ExampleSet resultSet = resultTable.createExampleSet();
        
        return new IOObject[] { resultSet };
    }

    public static AggregationFunction createAggregationFunction(String functionName) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        int typeIndex = -1;
        for (int i = 0; i < KNOWN_AGGREGATION_FUNCTION_NAMES.length; i++) {
            if (KNOWN_AGGREGATION_FUNCTION_NAMES[i].equals(functionName)) {
                typeIndex = i;
                break;
            }
        }
        Class clazz = null;
        if (typeIndex < 0) {
            clazz = Class.forName(functionName);
        } else {
            clazz = KNOWN_AGGREGATION_FUNCTIONS[typeIndex];
        }
        
        return (AggregationFunction)clazz.newInstance();
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
            return new InputDescription(cls, true, true);
        } else {
            return super.getInputDescription(cls);
        }
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeStringCategory(PARAMETER_AGGREGATION_FUNCTION, "The type of the used aggregation function.", KNOWN_AGGREGATION_FUNCTION_NAMES, KNOWN_AGGREGATION_FUNCTION_NAMES[0]);
        type.setExpert(false);
        types.add(type);
         
        types.add(new ParameterTypeString(PARAMETER_AGGREGATION_ATTRIBUTE, "Applies the aggregation function on the attribute with this name.", false));
        
        types.add(new ParameterTypeString(PARAMETER_GROUP_BY_ATTRIBUTE, "Performs a grouping by the values of the attribute with this name."));
        
        types.add(new ParameterTypeBoolean(PARAMETER_ONLY_DISTINCT, "Indicates if only rows with distinct values for the aggregation attribute should be used for the calculation of the aggregation function.", false));
        return types;
    }
}
