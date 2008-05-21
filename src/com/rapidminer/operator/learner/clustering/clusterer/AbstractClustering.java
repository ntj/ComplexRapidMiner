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

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.ClusterModel;
import com.rapidminer.operator.learner.clustering.ClusterUtils;
import com.rapidminer.operator.learner.clustering.FlatClusterModel;
import com.rapidminer.operator.learner.clustering.characterization.OneRCharacterizer;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;

/**
 * Represents an operator that clusters data.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractClustering.java,v 1.7 2008/05/09 19:22:49 ingomierswa Exp $
 */
public abstract class AbstractClustering extends Operator {

	/** The parameter name for &quot;Indicates if a cluster id is generated as new special attribute.&quot; */
	public static final String PARAMETER_ADD_CLUSTER_ATTRIBUTE = "add_cluster_attribute";
	
	/** The parameter name for &quot;if true, a characterization of each cluster is derived by classification&quot; */
	public static final String PARAMETER_ADD_CHARACTERIZATION = "add_characterization";
	
	public static final int NOISE = 0;

	public static final String NOISE_CLUSTER_DESCRIPTION = "Outliers";
	
	
	public AbstractClustering(OperatorDescription description) {
		super(description);
	}

	public abstract ClusterModel createClusterModel(ExampleSet exampleSet) throws OperatorException;
	
    public abstract boolean isFlatClusterer();
    
	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		Tools.isNonEmpty(exampleSet);
		Tools.checkAndCreateIds(exampleSet);
		
		ClusterModel clusterModel = createClusterModel(exampleSet);
		
        if (isFlatClusterer()) {
            if (clusterModel instanceof FlatClusterModel) {
                FlatClusterModel flatClusterModel = (FlatClusterModel)clusterModel;
                if (getParameterAsBoolean(PARAMETER_ADD_CHARACTERIZATION)) {
                    characterizeClustering(exampleSet, flatClusterModel);
                }
                if (getParameterAsBoolean(AbstractClustering.PARAMETER_ADD_CLUSTER_ATTRIBUTE)) {
                    addClusterAttribute(exampleSet, flatClusterModel);
                }
            } else {
                logWarning("The operator states that it can produce flat clusterings but the actual result is not a flat clustering: ignoring characterization and adding of cluster attribute.");
            }
        }
		
		return new IOObject[] { clusterModel };
	}
	
	protected void characterizeClustering(ExampleSet es, FlatClusterModel cm) throws OperatorException {
	    OneRCharacterizer characterizer = new OneRCharacterizer();
	    characterizer.addCharacterization(cm, es);
	}

	protected void addClusterAttribute(ExampleSet es, FlatClusterModel cm) {
	    if (!getParameterAsBoolean("keep_example_set")) {
	        logWarning("Adding a cluster attribute makes only sense, if you keep the example set.");
	    } else {
	        ClusterUtils.addClusterAttribute(es, cm);
	    }
	}
	
	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, true, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ClusterModel.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
        if (isFlatClusterer()) {
            types.add(new ParameterTypeBoolean(PARAMETER_ADD_CLUSTER_ATTRIBUTE, "Indicates if a cluster id is generated as new special attribute.", true));
            types.add(new ParameterTypeBoolean(PARAMETER_ADD_CHARACTERIZATION, "Indicates if a characterization of each cluster is created by a simple classification learner.", false));
        }
		return types;
	}
}
