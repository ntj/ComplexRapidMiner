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
package com.rapidminer.operator.meta;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.condition.SimpleChainInnerOperatorCondition;

import weka.clusterers.Clusterer;

/**
 * This operator splits up the input example set according to the clusters and 
 * applies its inner operators <var>number_of_clusters </var> time. 
 * This requires the example set to have a special cluster attribute which 
 * can be either created by a {@link Clusterer} or might be declared in the 
 * attribute description file that was used when the data was loaded.
 * 
 * @author Ingo Mierswa
 * @version $Id: ClusterIterator.java,v 1.3 2008/05/09 19:22:38 ingomierswa Exp $
 */
public class ClusterIterator extends OperatorChain {

    private static final Class[] INPUT_CLASSES = { ExampleSet.class };

    private static final Class[] OUTPUT_CLASSES = {};

    private int numberOfClusters = 0;

    public ClusterIterator(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);

        Attribute clusterAttribute = exampleSet.getAttributes().getCluster();
        if (clusterAttribute == null) {
        	throw new UserError(this, 113, Attributes.CLUSTER_NAME);
        }
        
        SplittedExampleSet splitted = SplittedExampleSet.splitByAttribute(exampleSet, clusterAttribute);
        numberOfClusters = splitted.getNumberOfSubsets();
        for (int i = 0; i < numberOfClusters; i++) {
            splitted.selectSingleSubset(i);
            setInput(getInput().copy().append(new IOObject[] { splitted }));
            super.apply();
            inApplyLoop();
        }
        
        return new IOObject[0];
    }

    /** the clustered example set */
    public Class[] getInputClasses() {
        return INPUT_CLASSES;
    }

    /** no output */
    public Class[] getOutputClasses() {
        return OUTPUT_CLASSES;
    }

    /**
     * Returns true since this operator chain should just return the output of the last inner operator.
     */
    public boolean shouldReturnInnerOutput() {
        return true;
    }

    /** Returns a simple chain condition. */
    public InnerOperatorCondition getInnerOperatorCondition() {
        return new SimpleChainInnerOperatorCondition();
    }

    /** Returns 0 for the minimum number of innner operators. */
    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    /**
     * Returns the highest possible value for the maximum number of innner operators.
     */
    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }
}
