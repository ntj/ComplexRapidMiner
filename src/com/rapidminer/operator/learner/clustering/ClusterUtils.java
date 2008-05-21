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
package com.rapidminer.operator.learner.clustering;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;


/**
 * Some static utility methods for cluster learning.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: ClusterUtils.java,v 1.5 2008/05/09 19:23:12 ingomierswa Exp $
 */
public class ClusterUtils {

	/**
	 * Add cluster ids as nominal attribute to an example set. If an item is not contained in any of the clusters, its value for the cluster attribute
	 * is set to unknown.
	 * 
	 * @param es
	 *            the example set
	 * @param cm
	 *            the cluster model
	 */
	public static void addClusterAttribute(ExampleSet es, FlatClusterModel cm) {
		Attribute cluster = AttributeFactory.createAttribute("cluster", Ontology.NOMINAL);
		es.getExampleTable().addAttribute(cluster);
		es.getAttributes().setCluster(cluster);
		Iterator<Example> r = es.iterator();
		while (r.hasNext()) {
			Example e = r.next();
			int index = getBestIndex(cm, IdUtils.getIdFromExample(e));
			if (index >= 0) {
				e.setValue(cluster, cluster.getMapping().mapString(cm.getClusterAt(index).getId()));
			} else {
				e.setValue(cluster, Double.NaN);
			}
		}
	}

	/**
	 * Add cluster ids as nominal attribute to an example set. If an item is not contained in any of the clusters, its value for the cluster attribute
	 * is set to unknown.
	 * 
	 * @param es
	 *            the example set
	 * @param cm
	 *            the cluster model
	 */
	public static void addClusterAttribute(ExampleSet es, HierarchicalClusterModel cm) {
		Attribute cluster = AttributeFactory.createAttribute("cluster", Ontology.NOMINAL);
		es.getExampleTable().addAttribute(cluster);
		es.getAttributes().setCluster(cluster);
		addClusterAttributeRec(es, cluster, cm.getRootNode());
	}

	public static void addClusterAttributeRec(ExampleSet es, Attribute att, ClusterNode cn) {
		if (cn.getNumberOfObjects() > 0) {
			int index = att.getMapping().mapString(cn.getId());
			es.remapIds();
			Iterator<String> it = cn.getObjects();
			while (it.hasNext()) {
				Example e = IdUtils.getExampleFromId(es, it.next());
				if (e != null)
					e.setValue(att, index);
			}
		}
		for (int i = 0; i < cn.getNumberOfSubNodes(); i++)
			addClusterAttributeRec(es, att, cn.getSubNodeAt(i));
	}

	private static int getBestIndex(FlatClusterModel cm, String id) {
		int result = -1;
		for (int i = 0; (i < cm.getNumberOfClusters()) && (result < 0); i++)
			if (cm.getClusterAt(i).contains(id)) {
				result = i;
			}
		return result;
	}
}
