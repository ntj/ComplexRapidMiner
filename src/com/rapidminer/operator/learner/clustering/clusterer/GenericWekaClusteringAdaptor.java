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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import weka.clusterers.Clusterer;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.DefaultCluster;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaTools;

/**
 * This operator performs the Weka clustering scheme with the same name. The operator expects an example set containing ids and returns a
 * FlatClusterModel or directly annotates the examples with a cluster attribute. Please note: Currently only clusterers that produce a partition of
 * items are supported.
 * 
 * @author Ingo Mierswa
 * @version $Id: GenericWekaClusteringAdaptor.java,v 1.8 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class GenericWekaClusteringAdaptor extends AbstractFlatClusterer implements TechnicalInformationHandler {

	public static final String[] WEKA_CLUSTERERS = WekaTools.getWekaClasses(Clusterer.class);

	/** The list with the weka parameters. */
	private List<ParameterType> wekaParameters = new LinkedList<ParameterType>();

	public GenericWekaClusteringAdaptor(OperatorDescription description) {
		super(description);
	}

	public ClusterModel createClusterModel(ExampleSet exampleSet) throws OperatorException {
		weka.clusterers.Clusterer clusterer = getWekaClusterer(WekaTools.getWekaParametersFromTypes(this, wekaParameters));
		weka.core.Instances instances = WekaTools.toWekaInstances(exampleSet, "ClusterInstances", WekaInstancesAdaptor.CLUSTERING);
		try {
			clusterer.buildClusterer(instances);
			WekaCluster wekaCluster = new WekaCluster(exampleSet, clusterer);
			exampleSet = wekaCluster.apply(exampleSet);
		} catch (Exception e) {
			throw new UserError(this, e, 905, new Object[] {
					getOperatorClassName(), e
			});
		}
		ClusterModel clusterModel = createWekaBasedClusterModel(exampleSet);
		return clusterModel;
	}

	private ClusterModel createWekaBasedClusterModel(ExampleSet exampleSet) {
		Attribute idAttribute = exampleSet.getAttributes().getId();
		Attribute clusterAttribute = exampleSet.getAttributes().getCluster();
		// create cluster models
		FlatCrispClusterModel result = new FlatCrispClusterModel();
		Iterator<String> i = clusterAttribute.getMapping().getValues().iterator();
		while (i.hasNext()) {
			String name = i.next();
			DefaultCluster cluster = new DefaultCluster(name);
			cluster.setDescription(name);
			result.addCluster(cluster);
		}
		Iterator<Example> er = exampleSet.iterator();
		while (er.hasNext()) {
			Example e = er.next();
			DefaultCluster cluster = (DefaultCluster) result.getClusterById(e.getValueAsString(clusterAttribute));
			cluster.addObject(e.getValueAsString(idAttribute));
		}
		return result;
	}

	/** Returns the Weka classifier based on the subtype of this operator. */
	private Clusterer getWekaClusterer(String[] parameters) throws OperatorException {
		String prefixName = getOperatorClassName();
		String actualName = prefixName.substring(WekaTools.WEKA_OPERATOR_PREFIX.length());
		String clustererName = null;
		for (int i = 0; i < WEKA_CLUSTERERS.length; i++) {
			if (WEKA_CLUSTERERS[i].endsWith(actualName)) {
				clustererName = WEKA_CLUSTERERS[i];
				break;
			}
		}
		Clusterer clusterer = null;
		try {
			clusterer = weka.clusterers.Clusterer.forName(clustererName, parameters);
		} catch (Exception e) {
			throw new UserError(this, e, 904, new Object[] {
					clustererName, e
			});
		}
		return clusterer;
	}

	public TechnicalInformation getTechnicalInformation() {
		try {
			Clusterer clusterer = getWekaClusterer(null);
			if (clusterer instanceof TechnicalInformationHandler)
				return ((TechnicalInformationHandler) clusterer).getTechnicalInformation();
			else
				return null;
		} catch (OperatorException e) {
			return null;
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		weka.clusterers.Clusterer clusterer = null;
		try {
			// parameters must be null, not an empty String[0] array!
			clusterer = getWekaClusterer(null);
		} catch (OperatorException e) {
			throw new RuntimeException("Cannot instantiate Weka clusterer " + getOperatorClassName() + ": " + e.getMessage());
		}
		wekaParameters = new LinkedList<ParameterType>();
		if ((clusterer != null) && (clusterer instanceof weka.core.OptionHandler)) {
			WekaTools.addParameterTypes((weka.core.OptionHandler) clusterer, types, wekaParameters, false, null);
		}
		return types;
	}
}
