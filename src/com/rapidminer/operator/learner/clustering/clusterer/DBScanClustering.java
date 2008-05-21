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
package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.similarity.DistanceSimilarityConverter;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.SimilarityUtil;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;

/**
 * This operator represents a simple implementation of the DBSCAN algorithm. {@rapidminer.cite Ester/etal/96a}).
 * 
 * @rapidminer.reference Ester/etal/96a
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: DBScanClustering.java,v 1.6 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class DBScanClustering extends AbstractDensityBasedClusterer {

	private double maxDistance = 0.2;

	private SimilarityMeasure sim;

	private static final String MAX_DISTANCE_NAME = "max_distance";

	public DBScanClustering(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		es.remapIds();
		sim = SimilarityUtil.resolveSimilarityMeasure(getParameters(), getInput(), es);
		if (!sim.isDistance())
			sim = new DistanceSimilarityConverter(sim);
		
		maxDistance = getParameterAsDouble(MAX_DISTANCE_NAME);
		FlatClusterModel result = doClustering(es);
		return result;
	}

	protected List<String> getNeighbours(ExampleSet es, String id) {
		List<String> result = new LinkedList<String>();
		for (int i = 0; i < getIds().size(); i++) {
			String id2 = getIds().get(i);
			double v = sim.similarity(id, id2);
			if (v <= maxDistance)
				result.add(id2);
		}
		return result;
	}

	public InputDescription getInputDescription(Class cls) {
		if (SimilarityMeasure.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType p;
		p = new ParameterTypeDouble(MAX_DISTANCE_NAME, "maximal distance", 0.0, Double.POSITIVE_INFINITY, 0.8);
		p.setExpert(false);
		types.add(p);
		types.add(SimilarityUtil.generateSimilarityParameter());
		return types;
	}
}
