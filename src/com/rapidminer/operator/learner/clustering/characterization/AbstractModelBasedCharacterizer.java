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
package com.rapidminer.operator.learner.clustering.characterization;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.learner.clustering.MutableCluster;
import com.rapidminer.operator.learner.clustering.clusterer.AbstractClustering;
import com.rapidminer.tools.Ontology;


/**
 * This is the abstract superclass for all cluster characterizers
 * which uses some classification model as a base for its 
 * characterization.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractModelBasedCharacterizer.java,v 1.2 2007/06/18 16:27:58 ingomierswa Exp $
 */
public abstract class AbstractModelBasedCharacterizer {

	/**
	 * Train the model for a single cluster.
	 * 
	 * @param es the example set
	 * @return a model
	 */
	public abstract Model trainModel(ExampleSet es);

	/**
	 * Extract a string representation well suited for a tooltip.
	 * 
	 * @param m
	 *            the model
	 * @return a String
	 */
	public abstract String stringRepresentation(Model m, String desiredLabel);

	public void addCharacterization(FlatClusterModel cm, ExampleSet es) throws OperatorException {
		Attribute originalLabel = es.getAttributes().getLabel();
		Attribute clusterLabel = AttributeFactory.createAttribute("label", Ontology.NOMINAL);
		es.getExampleTable().addAttribute(clusterLabel);
		es.getAttributes().setLabel(clusterLabel);
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			MutableCluster cl = (MutableCluster) cm.getClusterAt(i);
			if (!AbstractClustering.NOISE_CLUSTER_DESCRIPTION.equals(cl.getDescription())) {
				for (Example e : es) {
					String id = IdUtils.getIdFromExample(e);
					if (cl.contains(id)) {
						e.setLabel(clusterLabel.getMapping().mapString("yes"));
					} else {
						e.setLabel(clusterLabel.getMapping().mapString("no"));
					}
				}
				Model characteristicModel = trainModel(es);
				cl.setDescription(stringRepresentation(characteristicModel, "yes"));
			}
		}
		es.getExampleTable().removeAttribute(clusterLabel);
		es.getAttributes().remove(clusterLabel);
		if (originalLabel != null)
			es.getAttributes().setLabel(originalLabel);
	}
}
