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

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.ClassNameMapper;
import com.rapidminer.tools.math.matrix.MatrixComparator;


/**
 * Operator that compares two similarity measures using diverse metrics.
 * 
 * @author Michael Wurst
 * @version $Id: SimilarityComparator.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class SimilarityComparator extends Operator {


	/** The parameter name for &quot;similarity measure to apply&quot; */
	public static final String PARAMETER_MEASURE = "measure";

	/** The parameter name for &quot;the sampling rate used for comparision&quot; */
	public static final String PARAMETER_SAMPLING_RATE = "sampling_rate";
	private final static String[] SIM_MEASURES = {
			"com.rapidminer.tools.math.matrix.AbsoluteDistanceMatrixComparator", "com.rapidminer.tools.math.matrix.CorrelationMatrixComparator"
	};

	private ClassNameMapper SIM_MEASURES_MAP;

	public SimilarityComparator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		SimilarityMeasure sim1 = getInput(SimilarityMeasure.class);
		SimilarityMeasure sim2 = getInput(SimilarityMeasure.class);
		PerformanceVector result = new PerformanceVector();
		MatrixComparator matrixComparator = (MatrixComparator) SIM_MEASURES_MAP.getInstantiation(getParameterAsString(PARAMETER_MEASURE));
		@SuppressWarnings("unchecked")
		double simSim = matrixComparator.compare(new LazySimilarityMatrix(sim1), new LazySimilarityMatrix(sim2),
				getParameterAsDouble(PARAMETER_SAMPLING_RATE));
		@SuppressWarnings("unchecked")
		PerformanceCriterion simCriterion = new EstimatedPerformance("similarity", simSim, 1, matrixComparator.getReciprogalFitness());
		result.addCriterion(simCriterion);
		return new IOObject[] {
			result
		};
	}

	public InputDescription getInputDescription(Class cls) {
		if (SimilarityMeasure.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		}
		return super.getInputDescription(cls);
	}

	public Class[] getInputClasses() {
		return new Class[] {
			SimilarityMeasure.class
		};
	}

	public Class[] getOutputClasses() {
		return new Class[] {
			PerformanceVector.class
		};
	}

	public List<ParameterType> getParameterTypes() {
		SIM_MEASURES_MAP = new ClassNameMapper(SIM_MEASURES);
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeStringCategory(PARAMETER_MEASURE, "similarity measure to apply", SIM_MEASURES_MAP.getShortClassNames());
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeDouble(PARAMETER_SAMPLING_RATE, "the sampling rate used for comparision", 0.0, 1.0, 1.0));
		return types;
	}
}
