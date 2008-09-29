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
package com.rapidminer.tools.math.similarity;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.parameter.conditions.EqualTypeCondition;
import com.rapidminer.tools.math.similarity.mixed.MixedEuclideanDistance;
import com.rapidminer.tools.math.similarity.nominal.DiceNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.JaccardNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.KulczynskiNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.NominalDistance;
import com.rapidminer.tools.math.similarity.nominal.RogersTanimotoNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.RussellRaoNominalSimilarity;
import com.rapidminer.tools.math.similarity.nominal.SimpleMatchingNominalSimilarity;
import com.rapidminer.tools.math.similarity.numerical.CamberraNumericalDistance;
import com.rapidminer.tools.math.similarity.numerical.ChebychevNumericalDistance;
import com.rapidminer.tools.math.similarity.numerical.CorrelationSimilarity;
import com.rapidminer.tools.math.similarity.numerical.CosineSimilarity;
import com.rapidminer.tools.math.similarity.numerical.DTWDistance;
import com.rapidminer.tools.math.similarity.numerical.DiceNumericalSimilarity;
import com.rapidminer.tools.math.similarity.numerical.EuclideanDistance;
import com.rapidminer.tools.math.similarity.numerical.InnerProductSimilarity;
import com.rapidminer.tools.math.similarity.numerical.JaccardNumericalSimilarity;
import com.rapidminer.tools.math.similarity.numerical.ManhattanDistance;
import com.rapidminer.tools.math.similarity.numerical.MaxProductSimilarity;
import com.rapidminer.tools.math.similarity.numerical.OverlapNumericalSimilarity;

/**
 * This is a convinient class for using the distanceMeasures. It offers methods
 * for integrating the measure classes into operators.
 * 
 * @author Sebastian Land
 * @version $Id: DistanceMeasures.java,v 1.4 2008/08/05 09:40:31 stiefelolm Exp $
 */
public class DistanceMeasures {
	
	public static final String PARAMETER_MEASURE_TYPES = "measure_types";
	public static final String PARAMETER_NOMINAL_MEASURE = "nominal_measure";
	public static final String PARAMETER_NUMERICAL_MEASURE = "numerical_measure";
	public static final String PARAMETER_MIXED_MEASURE = "mixed_measure";
	
	public static final String[] MEASURE_TYPES = new String[] {
		"MixedMeasures",
		"NominalMeasures",
		"NumericalMeasures"
	};
	
	public static final int MIXED_MEASURES_INDEX = 0;
	public static final int NOMINAL_MEASURES_INDEX = 1;
	public static final int NUMERICAL_MEASURES_INDEX = 2;

	public static final String[] NOMINAL_MEASURES = new String[] {
		"NominalDistance",
		"DiceSimilarity",
		"JaccardSimilarity",
		"KulczynskiSimilarity",
		"RogersTanimotoSimilarity",
		"RussellRaoSimilarity",
		"SimpleMatchingSimilarity"
	};
	public static final Class[] NOMINAL_MEASURE_CLASSES = new Class[] {
		NominalDistance.class,
		DiceNominalSimilarity.class,
		JaccardNominalSimilarity.class,
		KulczynskiNominalSimilarity.class,
		RogersTanimotoNominalSimilarity.class,
		RussellRaoNominalSimilarity.class,
		SimpleMatchingNominalSimilarity.class
	};
		
	public static final String[] MIXED_MEASURES = new String[] {
		"MixedEuclideanDistance"
	};
	public static final Class[] MIXED_MEASURE_CLASSES = new Class[] {
		MixedEuclideanDistance.class
	};
	
	public static final String[] NUMERICAL_MEASURES = new String[] {
		"EuclideanDistance",
		"CamberraDistance",
		"ChebychevDistance",
		"CorrelationSimilarity",
		"CosineSimilarity",
		"DiceSimilarity",
		"DynamicTimeWarpingDistance",
		"InnerProductSimilarity",
		"JaccardSimilarity",
		"ManhattanDistance",
		"MaxProductSimilarity",
		"OverlapSimilarity"
	};
    public static final Class[] NUMERICAL_MEASURE_CLASSES = new Class[] {
		EuclideanDistance.class,
		CamberraNumericalDistance.class,
		ChebychevNumericalDistance.class,
		CorrelationSimilarity.class,
		CosineSimilarity.class,
		DiceNumericalSimilarity.class,
		DTWDistance.class,
		InnerProductSimilarity.class,
		JaccardNumericalSimilarity.class,
		ManhattanDistance.class,
		MaxProductSimilarity.class,
		OverlapNumericalSimilarity.class
    };
    
	public static DistanceMeasure createMeasure(Operator operator, ExampleSet exampleSet) throws UndefinedParameterError, OperatorException {
		int measureType = operator.getParameterAsInt(PARAMETER_MEASURE_TYPES);
		Class measureClass = null;
		switch (measureType) {
			case MIXED_MEASURES_INDEX: 
				measureClass = MIXED_MEASURE_CLASSES[operator.getParameterAsInt(PARAMETER_MIXED_MEASURE)];
				break;
			case NOMINAL_MEASURES_INDEX: 
				measureClass = NOMINAL_MEASURE_CLASSES[operator.getParameterAsInt(PARAMETER_NOMINAL_MEASURE)];
				break;
			case NUMERICAL_MEASURES_INDEX: 
				measureClass = NUMERICAL_MEASURE_CLASSES[operator.getParameterAsInt(PARAMETER_NUMERICAL_MEASURE)];
				break;
		}
		if (measureClass != null) {
			DistanceMeasure measure;
				try {
					measure = (DistanceMeasure) measureClass.newInstance();
					measure.init(exampleSet);
					return measure;
				} catch (InstantiationException e) {
					throw new OperatorException("Could not instanciate distance measure " + measureClass);
				} catch (IllegalAccessException e) {
					throw new OperatorException("Could not instanciate distance measure " + measureClass);
				}
		} 
		return null;
	}
	
	/**
	 * This method adds a parameter to chose a distance measure as parameter
	 */
	public static List<ParameterType> getParameterTypes(Operator operator) {
		List<ParameterType> list = new LinkedList<ParameterType>();
		list.add(new ParameterTypeCategory(PARAMETER_MEASURE_TYPES, "The measure type", MEASURE_TYPES, 0));
		ParameterType type = new ParameterTypeCategory(PARAMETER_MIXED_MEASURE, "Select measure", MIXED_MEASURES, 0);
		type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_MEASURE_TYPES, true, 0));
		list.add(type);
		type = new ParameterTypeCategory(PARAMETER_NOMINAL_MEASURE, "Select measure", NOMINAL_MEASURES, 0);
		type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_MEASURE_TYPES, true, 1));
		list.add(type);
		type = new ParameterTypeCategory(PARAMETER_NUMERICAL_MEASURE, "Select measure", NUMERICAL_MEASURES, 0);
		type.registerDependencyCondition(new EqualTypeCondition(operator, PARAMETER_MEASURE_TYPES, true, 2));
		list.add(type);
		return list;
	}
}
