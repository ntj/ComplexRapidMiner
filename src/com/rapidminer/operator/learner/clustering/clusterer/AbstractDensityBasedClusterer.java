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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * A simple generic density based clusterer.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractDensityBasedClusterer.java,v 1.5 2008/05/09 19:22:49 ingomierswa Exp $
 */
public abstract class AbstractDensityBasedClusterer extends AbstractFlatClusterer {

	protected static final int UNASSIGNED = -1;

	private Map<String, Integer> assign;

	private List<String> ids;

	protected int minPts;

	private static final String MIN_PTS_NAME = "min_pts";

	
	public AbstractDensityBasedClusterer(OperatorDescription description) {
		super(description);
	}

	protected abstract List<String> getNeighbours(ExampleSet es, String id) throws OperatorException;

	protected FlatCrispClusterModel doClustering(ExampleSet es) throws OperatorException {
		assign = new HashMap<String, Integer>();
		int clusterId = 0;
		minPts = getParameterAsInt(MIN_PTS_NAME);
		// Create the mapping
		FlatCrispClusterModel result = new FlatCrispClusterModel();
		ids = new ArrayList<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext()) {
			Example ex = er.next();
			String exId = IdUtils.getIdFromExample(ex);
			ids.add(exId);
			assign.put(exId, UNASSIGNED);
		}
		for (int i = 0; i < ids.size(); i++) {
			String id = ids.get(i);
			Integer status = assign.get(id);
			if (status.intValue() == UNASSIGNED) {
				List<String> l = getNeighbours(es, id);
				if (l.size() >= minPts) {
					clusterId++;
					assign.put(id, clusterId);
					for (int j = 0; j < l.size(); j++) {
						String idx = l.get(j);
						assign.put(idx, clusterId);
					}
					while (l.size() > 0) {
						// Take the first element from the queue
						String idRec = l.get(0);
						l.remove(0);
						// Assign it the current cluster id
						// Find its neighbours and if the density is sufficient
						// recurse through them
						List lRec = getNeighbours(es, idRec);
						if (lRec.size() >= minPts) {
							for (int j = 0; j < lRec.size(); j++) {
								String id3 = (String) lRec.get(j);
								Integer status3 = assign.get(id3);
								// If already identified as noise, just assign
								// it, if its unclassified, add to queue
								if (status3.intValue() < 1) {
									if (status3.intValue() == UNASSIGNED) {
										l.add(id3);
									}
									assign.put(id3, clusterId);
								}
							}
						}
					}
				} else {
					assign.put(id, NOISE);
				}
			}
		}
		for (int i = 0; i < clusterId + 1; i++)
			result.addCluster(new DefaultCluster("" + i));
		for (int j = 0; j < ids.size(); j++) {
			String id = ids.get(j);
			Integer status = assign.get(id);
			if (status.intValue() >= 0) {
				((DefaultCluster) result.getClusterAt(status.intValue())).addObject(id);
			}
		}
		((DefaultCluster) result.getClusterAt(NOISE)).setDescription(NOISE_CLUSTER_DESCRIPTION);
		return result;
	}

	protected List<String> getIds() {
		return ids;
	}

	protected int getAssignment(String id) {
		return assign.get(id);
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(MIN_PTS_NAME, "The minimal number of points in each cluster.", 0, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
