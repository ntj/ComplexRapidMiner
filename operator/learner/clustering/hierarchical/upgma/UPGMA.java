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
package com.rapidminer.operator.learner.clustering.hierarchical.upgma;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.hierarchical.AbstractHierarchicalClusterer;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;

/**
 * This operator generates a tree each node of which represents a cluster. UPGMA stands for Unweighted Pair Group Method using Arithmetic Means. Since
 * the way cluster distances are calculated can be specified using parameters, this name is slightly misleading. Unfortunately, the name of the
 * algorithm changes depending on the parameters used. <br/>Starting with initial clusters of size 1, the algorithm unites two clusters with minimal
 * distance forming a new tree node. This is iterated until there is only one cluster left which forms the root of the tree. <br/>This operator does
 * not generate a special cluster attribute and does not modify the input example set at all, since it generates too many clusters. The tree generated
 * by this cluster is considered the interesting result of the algorithm.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: UPGMA.java,v 1.10 2008/09/12 10:29:51 tobiasmalbrecht Exp $
 */
public class UPGMA extends AbstractHierarchicalClusterer {

	/** The parameter name for &quot;Specifies the way the distance of two examples is calculated.&quot; */
	public static final String PARAMETER_DISTANCE_MEASURE = "distance_measure";

	/** The parameter name for &quot;Specifies the way the distance of two clusters is calculated.&quot; */
	public static final String PARAMETER_CLUSTER_DISTANCE_MEASURE = "cluster_distance_measure";


	public UPGMA(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet es) throws OperatorException {
		ClusterDistanceMeasure clusterDistanceMeasure = DistanceMeasure.createClusterDistanceMeasure(getParameterAsInt(PARAMETER_CLUSTER_DISTANCE_MEASURE));
		DistanceMatrix distanceMatrix = DistanceMeasure.createDistanceMeasure(getParameterAsInt(PARAMETER_DISTANCE_MEASURE)).calculateDistanceMatrix(es);
		List<Cluster> clusters = new ArrayList<Cluster>();
		for (int i = 0; i < distanceMatrix.getDimension(); i++) {
			double[] distances = new double[distanceMatrix.getDimension()];
			for (int j = 0; j < distanceMatrix.getDimension(); j++) {
				distances[j] = distanceMatrix.getDistance(i, j);
			}
			clusters.add(new Cluster(distanceMatrix.getName(i), distances, i));
		}
		while (clusters.size() > 1) {
			// find two clusters i,j with minimal distance d, j > i
			Cluster clusterI = null;
			Cluster clusterJ = null;
			double d = Double.POSITIVE_INFINITY;
			for (int i = 0; i < clusters.size(); i++) {
				Cluster tempI = clusters.get(i);
				for (int j = 0; j < i; j++) {
					Cluster tempJ = clusters.get(j);
					double distance = tempI.getDistance(tempJ.getIndex());
					if (distance < d) {
						d = distance;
						clusterI = tempI;
						clusterJ = tempJ;
					}
				}
			}
			Tree t1 = clusterI.getTree();
			double h1 = t1.getHeight();
			Tree t2 = clusterJ.getTree();
			double h2 = t2.getHeight();
			Tree newTree = new Tree("" + ((double) (Math.round(d / 2 * 100)) / 100), t1, d / 2 - h1, t2, d / 2 - h2);
			newTree.setHeight(d / 2);
			clusterI.setTree(newTree);
			clusters.remove(clusterJ);
			// recalculate the distances
			Iterator l = clusters.iterator();
			while (l.hasNext()) {
				Cluster clusterL = (Cluster) l.next();
				if (clusterI.getIndex() != clusterL.getIndex()) {
					double newDistance = clusterDistanceMeasure.calculateUnionDistance(clusterI.getDistance(clusterL.getIndex()), clusterJ
							.getDistance(clusterL.getIndex()), clusterI, clusterJ, clusterL);
					clusterL.setDistance(clusterI.getIndex(), newDistance);
					clusterI.setDistance(clusterL.getIndex(), newDistance);
				} else {
					clusterI.setDistance(clusterL.getIndex(), Double.POSITIVE_INFINITY);
				}
			}
			clusterI.union(clusterJ);
		}
		
		UPGMAHierarchicalClusterModel result = new UPGMAHierarchicalClusterModel((clusters.get(0)).getTree(), es);
		return result;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_DISTANCE_MEASURE, "Specifies the way the distance of two examples is calculated.",
				DistanceMeasure.TYPE_NAMES, DistanceMeasure.TYPE_EUCLIDIAN);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_CLUSTER_DISTANCE_MEASURE, "Specifies the way the distance of two clusters is calculated.",
				DistanceMeasure.CLUSTER_TYPE_NAMES, DistanceMeasure.TYPE_AVERAGE);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_CLUSTER_ATTRIBUTE, "if true, a cluster id is generated as new special attribute ", true));
		return types;
	}
}
