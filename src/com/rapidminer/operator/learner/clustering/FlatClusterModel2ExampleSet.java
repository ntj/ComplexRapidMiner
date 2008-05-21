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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.example.set.Condition;
import com.rapidminer.example.set.ConditionedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Ontology;

/**
 * Labels an example set with the cluster ids from a given cluster model.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: FlatClusterModel2ExampleSet.java,v 1.9 2008/05/09 19:23:12 ingomierswa Exp $
 */
public class FlatClusterModel2ExampleSet extends Operator {

	/** The parameter name for &quot;should the cluster values be added as label as well&quot; */
	public static final String PARAMETER_ADD_LABEL = "add_label";

	/** The parameter name for &quot;delete the unlabelled examples&quot; */
	public static final String PARAMETER_DELETE_UNLABELED = "delete_unlabeled";
	
	public FlatClusterModel2ExampleSet(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet es = getInput(ExampleSet.class);
		Tools.checkAndCreateIds(es);
		
		ClusterModel clusterModel = getInput(ClusterModel.class);		
		if(clusterModel instanceof HierarchicalClusterModel) {
			clusterModel = new FlattendClusterModel((HierarchicalClusterModel)clusterModel);		
		}
		
		FlatClusterModel cm = (FlatClusterModel)clusterModel;
		boolean labelAttribute = getParameterAsBoolean(PARAMETER_ADD_LABEL);
		String[] uniqueLabels = new String[cm.getNumberOfClusters()];
		Map<String, Integer> duplicatesCounter = new HashMap<String, Integer>();
		
		for (int i = 0; i < cm.getNumberOfClusters(); i++) {
			String description = cm.getClusterAt(i).getDescription();
			if (description.length() == 0)
				description = "cl";
			if (duplicatesCounter.get(description) == null) {
				duplicatesCounter.put(description, 1);
				uniqueLabels[i] = description;
			} else {
				int count = duplicatesCounter.get(description);
				String baseDescription = (description + "_" + count);
				String newDescription = baseDescription;
				int secondaryCounter = 0;
				while (duplicatesCounter.get(newDescription) != null) {
					newDescription = baseDescription + "_" + secondaryCounter;
					secondaryCounter++;
				}
				uniqueLabels[i] = newDescription;
				if (newDescription.equals(baseDescription)) {
					duplicatesCounter.put(description, count + 1);
				} else {
					duplicatesCounter.put(newDescription, 1);
				}
			}
		}
		Attribute cluster = AttributeFactory.createAttribute("cluster", Ontology.NOMINAL);
		Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);
		es.getExampleTable().addAttribute(cluster);
		es.getAttributes().setCluster(cluster);
		if (labelAttribute) {
			es.getExampleTable().addAttribute(label);
			es.getAttributes().setLabel(label);
		}
		Iterator<Example> r = es.iterator();
		int numExamples = 0;
		while (r.hasNext()) {
			Example e = r.next();
			int index = getBestIndex(cm, IdUtils.getIdFromExample(e));
		
			log("Index of id " + IdUtils.getIdFromExample(e) + ":" + index);
			
			if (index >= 0) {
				e.setValue(cluster, cluster.getMapping().mapString(uniqueLabels[index]));
			} else {
				e.setValue(cluster, Double.NaN);
			}
			if (labelAttribute) {
				if (index >= 0) {
					e.setLabel(label.getMapping().mapString(uniqueLabels[index]));
					numExamples++;
				} else {
					e.setLabel(Double.NaN);
				}
			}
		}
		if (getParameterAsBoolean(PARAMETER_DELETE_UNLABELED)) {
			return new IOObject[] {
				new ConditionedExampleSet(es, new Condition() {

					private static final long serialVersionUID = -305063040412493813L;

					public boolean conditionOk(Example example) {
						return !Double.isNaN(example.getLabel());
					}

					 /** 
					  * @deprecated Conditions should not be able to be changed dynamically and hence there is no need for a copy
					  */
					@Deprecated
					public Condition duplicate() {
						return this;
					}
				})
			};
		} else {
			return new IOObject[] {	es};
		}
	}

	private int getBestIndex(FlatClusterModel cm, String id) {
		int result = -1;
		for (int i = 0; (i < cm.getNumberOfClusters()) && (result < 0); i++) {
			if (cm.getClusterAt(i).contains(id)) {
				result = i;
			}
		}
		return result;
	}

	public InputDescription getInputDescription(Class cls) {
		if (ClusterModel.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, true, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class[] getInputClasses() {
		return new Class[] { ClusterModel.class, ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_ADD_LABEL, "should the cluster values be added as label as well", false));
		types.add(new ParameterTypeBoolean(PARAMETER_DELETE_UNLABELED, "delete the unlabelled examples", false));
		return types;
	}
}
