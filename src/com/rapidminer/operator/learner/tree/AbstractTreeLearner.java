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
package com.rapidminer.operator.learner.tree;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.AbstractLearner;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Tools;

/**
 * This is the abstract super class for all decision tree learners. The actual
 * type of the tree is determined by the criterion, e.g. using gain_ratio or Gini
 * for CART / C4.5 and chi_squared for CHAID. 
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AbstractTreeLearner.java,v 1.8 2008/05/09 19:22:52 ingomierswa Exp $
 */
public abstract class AbstractTreeLearner extends AbstractLearner {

	/** The parameter name for &quot;Specifies the used criterion for selecting attributes and numerical splits.&quot; */
	public static final String PARAMETER_CRITERION = "criterion";

	/** The parameter name for &quot;The minimal size of all leaves.&quot; */
	public static final String PARAMETER_MINIMAL_LEAF_SIZE = "minimal_leaf_size";

	/** The parameter name for the minimal gain. */
	public static final String PARAMETER_MINIMAL_GAIN = "minimal_gain";
	
    public static final String[] CRITERIA_NAMES = {
        "gain_ratio",
        "information_gain",
        "gini_index",
        "accuracy"
    };
    
    public static final Class[] CRITERIA_CLASSES = {
        GainRatioCriterion.class,
        InfoGainCriterion.class,
        GiniIndexCriterion.class,
        AccuracyCriterion.class
    };
    
    public static final int CRITERION_GAIN_RATIO  = 0;
    
    public static final int CRITERION_INFO_GAIN   = 1;
    
    public static final int CRITERION_GINI_INDEX  = 2;
    
    public static final int CRITERION_ACCURACY    = 3;
    
    
    public AbstractTreeLearner(OperatorDescription description) {
        super(description);
    }

    /** Returns all termination criteria. */
    public abstract List<Terminator> getTerminationCriteria(ExampleSet exampleSet) throws OperatorException;

    /** Returns the pruner for this tree learner. If this method returns null,
     *  pruning will be disabled. */
    public abstract Pruner getPruner() throws OperatorException;
    
    /** The split preprocessing is applied before each new split
     *  The default implementation does nothing and simply returns 
     *  the given example set. Subclasses might want to override this
     *  in order to perform some data preprocessing like random subset 
     *  selections.*/
    public SplitPreprocessing getSplitPreprocessing() {
    	return null;
    }
    
    public Model learn(ExampleSet eSet) throws OperatorException {
    	ExampleSet exampleSet = (ExampleSet)eSet.clone();
    	
    	// create tree builder
    	TreeBuilder builder = new TreeBuilder(createCriterion(),
    			              getTerminationCriteria(exampleSet),
    			              getPruner(),
    			              getSplitPreprocessing(),
    			              getParameterAsInt(PARAMETER_MINIMAL_LEAF_SIZE),
    			              getParameterAsDouble(PARAMETER_MINIMAL_GAIN));
    	    	
    	// learn tree
    	Tree root = builder.learnTree(exampleSet);
        
        // create and return model
        return new TreeModel(exampleSet, root);
    }
    
    protected Criterion createCriterion() throws UndefinedParameterError {
        String criterionName = getParameterAsString(PARAMETER_CRITERION);
        Class criterionClass = null;
        for (int i = 0; i < CRITERIA_NAMES.length; i++) {
            if (CRITERIA_NAMES[i].equals(criterionName)) {
                criterionClass = CRITERIA_CLASSES[i];
            }
        }
        
        if ((criterionClass == null) && (criterionName != null)) {
            try {
                criterionClass = Tools.classForName(criterionName);
            } catch (ClassNotFoundException e) {
                logWarning("Cannot find criterion '"+criterionName+"' and cannot instantiate a class with this name. Using gain ratio criterion instead.");
            }
        }
        
        if (criterionClass != null) {
            try {
                return (Criterion)criterionClass.newInstance();
            } catch (InstantiationException e) {
                logWarning("Cannot instantiate criterion class '"+criterionClass.getName()+"'. Using gain ratio criterion instead.");
                return new GainRatioCriterion();
            } catch (IllegalAccessException e) {
                logWarning("Cannot access criterion class '"+criterionClass.getName()+"'. Using gain ratio criterion instead.");
                return new GainRatioCriterion();
            }
        } else {
        	log("No relevance criterion defined, using gain ratio...");
            return new GainRatioCriterion();
        }
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        ParameterType type = new ParameterTypeStringCategory(PARAMETER_CRITERION, "Specifies the used criterion for selecting attributes and numerical splits.", CRITERIA_NAMES, CRITERIA_NAMES[CRITERION_INFO_GAIN]);
        type.setExpert(false);
        types.add(type);
        type = new ParameterTypeInt(PARAMETER_MINIMAL_LEAF_SIZE, "The minimal size of all leaves.", 1, Integer.MAX_VALUE, 2);
        type.setExpert(false);
        types.add(type);
        types.add(new ParameterTypeDouble(PARAMETER_MINIMAL_GAIN, "The minimal gain which must be achieved in order to produce a split.", 0.0d, Double.POSITIVE_INFINITY, 0.0d));
        return types;
    } 
}
