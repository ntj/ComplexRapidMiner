/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.learner.clustering.clusterer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeString;

/**
 * Operator that clusters items along one given nominal attribute.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ExampleSet2ClusterModel.java,v 1.4 2007/07/11 13:43:35 ingomierswa Exp $
 */
public class ExampleSet2ClusterModel extends AbstractFlatClusterer {

	private static final String CLUSTER_ATT_PARAM_NAME = "cluster_attribute";

	public ExampleSet2ClusterModel(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		Attribute clusterAttribute = es.getAttributes().getSpecial(getParameterAsString(CLUSTER_ATT_PARAM_NAME));
		if (clusterAttribute == null)
			clusterAttribute = es.getAttributes().getRegular(getParameterAsString(CLUSTER_ATT_PARAM_NAME));
		if (clusterAttribute == null)
			throw new UserError(this, 111, getParameterAsString(CLUSTER_ATT_PARAM_NAME));
		ClusterModel result = createClusterModel(es, clusterAttribute);
		return result;
	}

	private ClusterModel createClusterModel(ExampleSet exampleSet, Attribute clusterAttribute) {
		FlatCrispClusterModel result = new FlatCrispClusterModel();
		Map<String, List<String>> collector = new HashMap<String, List<String>>();
		Iterator<Example> er = exampleSet.iterator();
		while (er.hasNext()) {
			Example e = er.next();
			String val = clusterAttribute.getMapping().mapIndex((int) e.getValue(clusterAttribute));
			String id = IdUtils.getIdFromExample(e);
			List<String> values = collector.get(val);
			if (values == null) {
				values = new ArrayList<String>();
				collector.put(val, values);
			}
			values.add(id);
		}
		int counter = 0;
		Iterator<Map.Entry<String, List<String>>> v = collector.entrySet().iterator();
		while (v.hasNext()) {
			Map.Entry<String, List<String>> entry = v.next();
			DefaultCluster cluster = new DefaultCluster("" + (counter++));
			cluster.setDescription(entry.getKey());
			List<String> objIds = entry.getValue();
			for (String objId : objIds)
				cluster.addObject(objId);
			result.addCluster(cluster);
		}
		return result;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType p;
		p = new ParameterTypeString(CLUSTER_ATT_PARAM_NAME, "The name of the cluster attribute (the attribute along which the clusters are build.", Attributes.CLUSTER_NAME);
		p.setExpert(false);
		types.add(p);
		return types;
	}
}
