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
package com.rapidminer.operator.similarity.clustermodel;

import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.tools.ClassNameMapper;


/**
 * This operator converts a (hierarchical) cluster model to a similarity measure.
 * 
 * @author Michael Wurst
 * @version $Id: ClusterModel2Similarity.java,v 1.4 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class ClusterModel2Similarity extends Operator {


	/** The parameter name for &quot;measure used to convert a cluster model into a similarity&quot; */
	public static final String PARAMETER_MEASURE = "measure";
	private static final String[] DEFAULT_MEASURES = {
			"com.rapidminer.operator.similarity.clustermodel.TreeDistance", "com.rapidminer.operator.similarity.clustermodel.TreeInfGainSimilarity"
	};

	private ClassNameMapper DEFAULT_MEASURES_MAP;

	public ClusterModel2Similarity(OperatorDescription description) {
		super(description);
	}

	public InputDescription getInputDescription(Class cls) {
		if (ClusterModel.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		}
		return super.getInputDescription(cls);
	}

	public Class[] getInputClasses() {
		return new Class[] {
			ClusterModel.class
		};
	}

	public Class[] getOutputClasses() {
		return new Class[] {
			SimilarityMeasure.class
		};
	}

	public IOObject[] apply() throws OperatorException {
		ClusterModel cm = getInput(ClusterModel.class);
		ClusterModelSimilarity csim = (ClusterModelSimilarity) DEFAULT_MEASURES_MAP.getInstantiation(getParameterAsString(PARAMETER_MEASURE));
		csim.init(cm);
		return new IOObject[] {
			csim
		};
	}

	public List<ParameterType> getParameterTypes() {
		DEFAULT_MEASURES_MAP = new ClassNameMapper(DEFAULT_MEASURES);
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeStringCategory(PARAMETER_MEASURE, "measure used to convert a cluster model into a similarity",
				DEFAULT_MEASURES_MAP.getShortClassNames());
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
