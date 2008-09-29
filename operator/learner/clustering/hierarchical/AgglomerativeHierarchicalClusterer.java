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
package com.rapidminer.operator.learner.clustering.hierarchical;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.operator.learner.clustering.hierarchical.clustersimilarity.ClusterSimilarity;
import com.rapidminer.operator.similarity.DistanceSimilarityConverter;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.operator.similarity.SimilarityUtil;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * This operator performs generic agglomorative clustering based on a set of ids and a similarity measure. The algorithm implemented here is currently
 * very simple and not very efficient (cubic).
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AgglomerativeHierarchicalClusterer.java,v 1.8 2008/09/12 10:30:13 tobiasmalbrecht Exp $
 */
public class AgglomerativeHierarchicalClusterer extends AbstractHierarchicalClusterer {

	/** The parameter name for &quot;The minimal number of items in a cluster. Clusters with less items are merged.&quot; */
	public static final String PARAMETER_MIN_ITEMS = "min_items";
	
	private AgglomerativeClusterer clusterer = new AgglomerativeClusterer();

	public AgglomerativeHierarchicalClusterer(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		SimilarityMeasure sim = SimilarityUtil.resolveSimilarityMeasure(getParameters(), getInput(), es);
		if (sim.isDistance())
			sim = new DistanceSimilarityConverter(sim);
		ClusterSimilarity csim = AgglomerativeClusterer.resolveClusterSimilarity(getParameters());
		HierarchicalClusterModel result = clusterer.clusterHierarchical(es, sim, csim, getParameterAsInt(PARAMETER_MIN_ITEMS));
		return result;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(SimilarityUtil.generateSimilarityParameter());
		types.add(AgglomerativeClusterer.createClusterSimilarityParameter());
		types.add(new ParameterTypeInt(PARAMETER_MIN_ITEMS, "The minimal number of items in a cluster. Clusters with less items are merged.", 1,
				(int) Double.POSITIVE_INFINITY, 2));
		return types;
	}
}
