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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;

/**
 * This similarity is based on a flat cluster model. Two objects are similar, if they belong to the same cluster.
 * 
 * @author Michael Wurst
 * @version $Id: FlatClusterSimilarity.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class FlatClusterSimilarity extends SimilarityAdapter {

	private static final long serialVersionUID = 2973618136551542774L;

	private final FlatClusterModel cm;

	private final Set<String> ids;

	public FlatClusterSimilarity(FlatClusterModel cm) {
		super();
		this.cm = (FlatClusterModel) cm.copy();
		ids = new HashSet<String>();
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			Iterator it = cm.getClusterAt(i).getObjects();
			while (it.hasNext()) {
				ids.add((String) it.next());
			}
		}
	}

	public double similarity(String x, String y) {
		double result = 0.0;
		for (int i = 0; (i < cm.getNumberOfClusters()) && (result < 1.0); i++) {
			Cluster c = cm.getClusterAt(i);
			if (c.contains(x) && c.contains(y))
				result = 1.0;
		}
		return result;
	}

	public boolean isSimilarityDefined(String x, String y) {
		return ids.contains(x) && ids.contains(y);
	}

	public Iterator<String> getIds() {
		return ids.iterator();
	}

    public int getNumberOfIds() {
        return ids.size();
    }
    
	public String explainSimilarity(String x, String y) {
		if (isSimilarityDefined(x, y))
			if (similarity(x, y) > 0.0)
				return "Both objects are in the same cluster, so their similarity is one";
			else
				return "The objects are in different cluster, so their similarity is zero";
		else
			return "One of the objects does not occur in the cluster model";
	}

	public boolean isDistance() {
		return false;
	}
}
