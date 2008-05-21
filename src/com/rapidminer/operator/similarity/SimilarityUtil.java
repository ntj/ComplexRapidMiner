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
package com.rapidminer.operator.similarity;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.similarity.attributebased.ExampleBasedSimilarityMeasure;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.ClassNameMapper;


/**
 * Some utilities for similarities.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SimilarityUtil.java,v 1.5 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class SimilarityUtil {


	/** The parameter name for &quot;similarity measure to apply&quot; */
	public static final String PARAMETER_MEASURE = "measure";
	private static final String[] DEFAULT_SIM_MEASURES = {
		    "com.rapidminer.operator.similarity.attributebased.MixedEuclideanDistance",
			"com.rapidminer.operator.similarity.attributebased.EuclideanDistance",
			"com.rapidminer.operator.similarity.attributebased.CosineSimilarity",
			"com.rapidminer.operator.similarity.attributebased.ManhattanDistance",
			"com.rapidminer.operator.similarity.attributebased.CamberraNumericalDistance",
			"com.rapidminer.operator.similarity.attributebased.ChebychevNumericalDistance",
			"com.rapidminer.operator.similarity.attributebased.CorrelationSimilarity",
			"com.rapidminer.operator.similarity.attributebased.DiceNominalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.DiceNumericalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.InnerProductSimilarity",
			"com.rapidminer.operator.similarity.attributebased.JaccardNominalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.JaccardNumericalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.KulczynskiNominalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.MaxProductSimilarity",
			"com.rapidminer.operator.similarity.attributebased.OverlapNumericalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.RogersTanimotoNominalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.RussellRaoNominalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.SimpleMatchingNominalSimilarity",
			"com.rapidminer.operator.similarity.attributebased.NominalDistance"
	};

	private static ClassNameMapper SIM_CLASSES_MAP = new ClassNameMapper(DEFAULT_SIM_MEASURES);

	public static ParameterType generateSimilarityParameter() {
		ParameterType result = new ParameterTypeStringCategory(PARAMETER_MEASURE, "similarity measure to apply", SIM_CLASSES_MAP.getShortClassNames(),
				SIM_CLASSES_MAP.getShortClassNames()[0]);
		result.setExpert(false);
		return result;
	}

	public static SimilarityMeasure resolveSimilarityMeasure(Parameters parameters, IOContainer ioContainer, ExampleSet es) throws OperatorException,
			MissingIOObjectException, UndefinedParameterError, UserError {
		Tools.checkAndCreateIds(es);
		String simClassName = (String) parameters.getParameter(PARAMETER_MEASURE);
		if ((ioContainer != null) && ioContainer.contains(SimilarityMeasure.class)) {
			if (simClassName != null)
				es.getLog().log("External similarity measure found. This measure is used instead of the one specified in the parameter \"measure\"");
			return ioContainer.get(SimilarityMeasure.class);
		} else if (simClassName != null) {
			ExampleBasedSimilarityMeasure sim = (ExampleBasedSimilarityMeasure) SIM_CLASSES_MAP.getInstantiation(simClassName);
			sim.init(es);
			return sim;
		} else {
			return null;
		}
	}
}
