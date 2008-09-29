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

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.performance.EstimatedPerformance;
import com.rapidminer.operator.performance.PerformanceCriterion;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.WeightedObject;


/**
 * Creates a flat cluster model from a hierarchical one by expanding nodes in the order of their weight until the desired number of clusters is
 * reached.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: FlattenClusterModel.java,v 1.10 2008/09/12 10:30:42 tobiasmalbrecht Exp $
 */
public class FlattenClusterModel extends Operator {

	/** The parameter name for &quot;the maximal number of clusters&quot; */
	public static final String PARAMETER_K = "k";

	/** The parameter name for &quot;return the highest cluster similarity as performance&quot; */
	public static final String PARAMETER_PERFORMANCE = "performance";
    
	public FlattenClusterModel(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ClusterModel cm = getInput(ClusterModel.class);
		if (!(cm instanceof HierarchicalClusterModel)) {
			throw new UserError(this, 122, "hierarchical cluster model");
		}
		HierarchicalClusterModel hcm = (HierarchicalClusterModel)cm;
		FlatCrispClusterModel result = new FlatCrispClusterModel();
		int maxK = getParameterAsInt(PARAMETER_K);
		TreeSet<WeightedObject<ClusterNode>> cns = new TreeSet<WeightedObject<ClusterNode>>(new Comparator<WeightedObject<ClusterNode>>() {
			public int compare(WeightedObject<ClusterNode> arg0, WeightedObject<ClusterNode> arg1) {
				WeightedObject<ClusterNode> obj = arg0;
				WeightedObject<ClusterNode> objToCompare = arg1;
				if (obj.getWeight() > objToCompare.getWeight())
					return 1;
				else if (obj.getWeight() < objToCompare.getWeight())
					return -1;
				else
					return (obj.getObject()).getId().compareTo((objToCompare.getObject()).getId());
			}
		});
		cns.add(new WeightedObject<ClusterNode>(hcm.getRootNode(), hcm.getRootNode().getWeight()));
		boolean terminate = false;
		while ((cns.size() < maxK) && (!terminate)) {
			WeightedObject<ClusterNode> wobj = cns.first();
			ClusterNode cn = wobj.getObject();
			// Find lowest node with children
			Iterator<WeightedObject<ClusterNode>> clIt = cns.iterator();
			while (clIt.hasNext() && (!(cn.getNumberOfSubNodes() == 0))) {
				wobj = clIt.next();
				cn = wobj.getObject();
			}
			if (cn.getNumberOfSubNodes() > 0) {
				cns.remove(wobj);
				Iterator<ClusterNode> subNodes = cn.getSubNodes();
				while (subNodes.hasNext()) {
					ClusterNode cn2 = subNodes.next();
					cns.add(new WeightedObject<ClusterNode>(cn2, cn2.getWeight()));
				}
			} else {
				terminate = true;
			}
		}
		Iterator<WeightedObject<ClusterNode>> clusterIt = cns.iterator();
		double minSimilarity = Double.POSITIVE_INFINITY;
		while (clusterIt.hasNext()) {
			ClusterNode cn = clusterIt.next().getObject();
			if (cn.getWeight() < minSimilarity)
				minSimilarity = cn.getWeight();
			DefaultCluster cl = new DefaultCluster(cn.getId());
			Iterator<String> itemIterator = cn.getObjectsInSubtree();
			while (itemIterator.hasNext())
				cl.addObject(itemIterator.next());
			result.addCluster(cl);
		}
        
		if (getParameterAsBoolean(PARAMETER_PERFORMANCE)) {
			PerformanceVector performance = null;
			try {
				performance = getInput(PerformanceVector.class);
			} catch (MissingIOObjectException e) {
				// If no performance vector is available, create a new one
			}
			if (performance == null)
				performance = new PerformanceVector();
			PerformanceCriterion pc = new EstimatedPerformance("Maximal distance withing a cluster", 1 - (1 / minSimilarity), 1, false);
			performance.addCriterion(pc);
			return new IOObject[] { result, performance	};
		} else {
			return new IOObject[] { result };
		}
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ClusterModel.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ClusterModel.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_K, "the maximal number of clusters", 2, Integer.MAX_VALUE, 2));
		types.add(new ParameterTypeBoolean(PARAMETER_PERFORMANCE, "return the highest cluster similarity as performance", false));
		return types;
	}
}
