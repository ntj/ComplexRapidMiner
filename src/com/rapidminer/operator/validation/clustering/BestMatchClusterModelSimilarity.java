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
package com.rapidminer.operator.validation.clustering;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.Cluster;
import com.rapidminer.operator.learner.clustering.ClusterIterator;
import com.rapidminer.operator.learner.clustering.ClusterNode;
import com.rapidminer.operator.learner.clustering.HierarchicalClusterModel;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.IterationArrayList;


/**
 * Compares two cluster models by searching for each concept a best matching one in the compared cluster model in terms of f-measure. The average f-measure of the best matches is then the overall cluster model similarity.
 * 
 * @author Michael Wurst
 * @version $Id: BestMatchClusterModelSimilarity.java,v 1.5 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public class BestMatchClusterModelSimilarity extends Operator {
	
	public static final String PARAMETER_WEIGHT_CLUSTERS = "weight_clusters";

	public BestMatchClusterModelSimilarity(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		HierarchicalClusterModel cm2 = getInput(HierarchicalClusterModel.class);
		HierarchicalClusterModel cm1 = getInput(HierarchicalClusterModel.class);
		
		
		if(getParameterAsBoolean("switch")) {
			HierarchicalClusterModel cm3 = cm1;
			cm1 = cm2;
			cm2 = cm3;
		}
		
		if ((cm1 == null) || (cm2 == null)) {
			PerformanceVector pv = new PerformanceVector();
			PerformanceCriterion pc = new EstimatedPerformance("f_measure", Double.NaN, 1, false);
			pv.addCriterion(pc);
			logWarning("Could not compare cm, one of them is null");
			return new IOObject[] { pv };
		}

		if ((cm1.getRootNode() == null) || (cm2.getRootNode() == null)) {
			PerformanceVector pv = new PerformanceVector();
			PerformanceCriterion pc = new EstimatedPerformance("f_measure", Double.NaN, 1, false);
			pv.addCriterion(pc);
			logWarning("Could not compare cm, one of them is null");
			return new IOObject[] { pv };
		}

		log("Reference cluster model has root node label " + cm1.getRootNode().getDescription());
		PerformanceVector pv = new PerformanceVector();
		double performance = 0.0;
		if(getParameterAsBoolean("symmetric"))
			performance = (bestMatchSimilarity(cm1, cm2) + bestMatchSimilarity(cm2, cm1))/2;
		else
			performance = bestMatchSimilarity(cm1, cm2);

		PerformanceCriterion pc = new EstimatedPerformance("f-measure", performance, 1, false);
		pv.addCriterion(pc);
		log("sim:" + performance);
		return new IOObject[] { pv };
	}

	public Class[] getInputClasses() {
		return new Class[] { HierarchicalClusterModel.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { PerformanceVector.class };
	}

	private double bestMatchSimilarity(HierarchicalClusterModel referenceModel, HierarchicalClusterModel resultModel) {
		HierarchicalClusterModel cm2 = referenceModel;
		HierarchicalClusterModel cm1 = resultModel;

		List<Cluster> clusterVector1 = new IterationArrayList<Cluster>(new ClusterIterator(cm1));
		List<Cluster> clusterVector2 = new IterationArrayList<Cluster>(new ClusterIterator(cm2));

		Set<String> items1Set = new HashSet<String>(new IterationArrayList<String>(cm1.getRootNode().getObjectsInSubtree()));
		int totalNumItems1 = items1Set.size();

		Set<String> items2Set = new HashSet<String>(new IterationArrayList<String>(cm2.getRootNode().getObjectsInSubtree()));
		int totalNumItems2 = items2Set.size();

		int totalNumItems = totalNumItems1;

		if (totalNumItems1 != totalNumItems2)
			logWarning("Number of items in both cluster models is not the same");

		double sum = 0.0;
		int counter = 0;

		for (int i = 0; i < clusterVector1.size(); i++) {
			ClusterNode cl1 = (ClusterNode) clusterVector1.get(i);
			int numObjsInCl1 = cl1.getNumberOfObjectsInSubtree();
			if (cl1.getNumberOfObjects() > 0) {
				double max = Double.NEGATIVE_INFINITY;
				for (int j = 0; j < clusterVector2.size(); j++) {
					double v = fmeasure(cl1, clusterVector2.get(j), totalNumItems);
					if (v > max)
						max = v;
				}
				
				if (getParameterAsBoolean(PARAMETER_WEIGHT_CLUSTERS)) {
					sum = sum + ((double) numObjsInCl1)*max;
					counter = counter + numObjsInCl1;
				}
				else {
					sum = sum + max;
					counter++;
				}
				
			}
		}
			return sum / counter;
	}

	private double fmeasure(Cluster c1, Cluster c2, int n) {
		Set s1 = null;
		if (c1 instanceof ClusterNode)
			s1 = new HashSet<String>(new IterationArrayList<String>(((ClusterNode) c1).getObjectsInSubtree()));
		else
			s1 = new HashSet<String>(new IterationArrayList<String>(c1.getObjects()));

		Set s2 = null;
		if (c2 instanceof ClusterNode)
			s2 = new HashSet<String>(new IterationArrayList<String>(((ClusterNode) c2).getObjectsInSubtree()));
		else
			s2 = new HashSet<String>(new IterationArrayList<String>(c2.getObjects()));

		if ((s1.size() == 0) || (s2.size() == 0))
			return 0.0;

		int prHits = 0;
		int reHits = 0;

		Iterator it = s1.iterator();
		while (it.hasNext())
			if (s2.contains(it.next()))
				prHits++;

		Iterator it2 = s2.iterator();
		while (it2.hasNext())
			if (s1.contains(it2.next()))
				reHits++;

		if ((reHits == 0) && (prHits == 0))
			return 0.0;

		double pr = ((double) prHits) / ((double) s1.size());
		double re = ((double) reHits) / ((double) s2.size());

		return 2 * ((re * pr) / (re + pr));
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_WEIGHT_CLUSTERS, "should the result clusters be weighted by the fraction of items they contain", true));
		types.add(new ParameterTypeBoolean("switch", "switch the both cluster models", false));
		types.add(new ParameterTypeBoolean("symmetric","build the average of a two-way comparison", false));
		return types;
	}

}
