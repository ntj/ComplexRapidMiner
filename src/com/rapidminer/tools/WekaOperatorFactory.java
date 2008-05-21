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
package com.rapidminer.tools;

import java.util.HashMap;
import java.util.Map;

/**
 * Registers all Weka core operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: WekaOperatorFactory.java,v 2.8 2006/03/21 15:35:53 ingomierswa
 *          Exp $
 */
public class WekaOperatorFactory implements GenericOperatorFactory {

    private static final String[] SKIPPED_META_CLASSIFIERS = new String[] {
        "weka.classifiers.meta.AttributeSelectedClassifier",
        "weka.classifiers.meta.CVParameterSelection",
        "weka.classifiers.meta.ClassificationViaRegression",
        "weka.classifiers.meta.FilteredClassifier",
        "weka.classifiers.meta.MultiScheme",
        "weka.classifiers.meta.Vote",
        "weka.classifiers.meta.Grading",
        "weka.classifiers.meta.Stacking",
        "weka.classifiers.meta.StackingC"
    };
    
    private static final String[] SKIPPED_CLASSIFIERS = new String[] {
        ".meta.",
        "weka.classifiers.functions.LibSVM",
        "MISVM",
        "UserClassifier",
        "LMTNode",
        "PreConstructedLinearModel",
        "RuleNode",
        "FTInnerNode",
        "FTLeavesNode",
        "FTNode"
    };
    
    private static final String[] SKIPPED_ASSOCIATORS = new String[] {
        "FilteredAssociator"
    };

    private static final String[] ENSEMBLE_CLASSIFIERS = new String[] {
        "weka.classifiers.meta.MultiScheme",
        "weka.classifiers.meta.Vote",
        "weka.classifiers.meta.Grading",
        "weka.classifiers.meta.Stacking",
        "weka.classifiers.meta.StackingC"
    };    
    
    private static final Map<String,String> DEPRECATED_CLASSIFIER_INFOS = new HashMap<String,String>();
    
    static {
    	DEPRECATED_CLASSIFIER_INFOS.put("weka.classifiers.bayes.NaiveBayesSimple", "Deprecated: please use Y-Naive Bayes instead.");
    	DEPRECATED_CLASSIFIER_INFOS.put("weka.classifiers.bayes.NaiveBayesUpdateable", "Deprecated: please use Y-Naive Bayes instead.");
    	DEPRECATED_CLASSIFIER_INFOS.put("weka.classifiers.bayes.NaiveBayes", "Deprecated: please use Y-Naive Bayes instead.");
    	DEPRECATED_CLASSIFIER_INFOS.put("weka.classifiers.functions.LibSVM", "Deprecated: please use the operator LibSVMLearner instead.");    	
    }
    
	public void registerOperators(ClassLoader classLoader) {
	    // learning schemes
        try {
            WekaTools.registerWekaOperators(classLoader, WekaTools.getWekaClasses(weka.classifiers.Classifier.class, null, SKIPPED_CLASSIFIERS), DEPRECATED_CLASSIFIER_INFOS, "com.rapidminer.operator.learner.weka.GenericWekaLearner", "The weka learner", "Learner.Supervised.Weka.", null);
        } catch (Throwable e) {
            LogService.getGlobal().log("Cannot register Weka learners: " + e, LogService.WARNING);
        }
        
	    // meta learning schemes
        try {
            WekaTools.registerWekaOperators(classLoader, WekaTools.getWekaClasses(weka.classifiers.Classifier.class, new String[] { ".meta." } , SKIPPED_META_CLASSIFIERS), "com.rapidminer.operator.learner.weka.GenericWekaMetaLearner", "The weka meta learner", "Learner.Supervised.Weka.", null);
        } catch (Throwable e) {
            LogService.getGlobal().log("Cannot register Weka meta learners: " + e, LogService.WARNING);
        }
        
	    // ensemble learning schemes
        try {
            WekaTools.registerWekaOperators(classLoader, WekaTools.getWekaClasses(weka.classifiers.Classifier.class, ENSEMBLE_CLASSIFIERS , null), "com.rapidminer.operator.learner.weka.GenericWekaEnsembleLearner", "The weka ensemble learner", "Learner.Supervised.Weka.", null);
        } catch (Throwable e) {
            LogService.getGlobal().log("Cannot register Weka ensemble learners: " + e, LogService.WARNING);
        }

	    // association rule learners
        try {
            WekaTools.registerWekaOperators(classLoader, WekaTools.getWekaClasses(weka.associations.Associator.class, null, SKIPPED_ASSOCIATORS), "com.rapidminer.operator.learner.weka.GenericWekaAssociationLearner", "The weka associator", "Learner.Unsupervised.Itemsets.Weka", null);
        } catch (Throwable e) {
            LogService.getGlobal().log("Cannot register Weka association rule learners: " + e, LogService.WARNING);
        }
        
	    // feature weighting
        try {
            WekaTools.registerWekaOperators(classLoader, WekaTools.getWekaClasses(weka.attributeSelection.AttributeEvaluator.class), "com.rapidminer.operator.features.weighting.GenericWekaAttributeWeighting", "The weka attribute evaluator", "Preprocessing.Attributes.Weighting.Weka", null);
        } catch (Throwable e) {
            LogService.getGlobal().log("Cannot register Weka feature weighting schemes: " + e, LogService.WARNING);
        }
        
        // clusterers
        try {
            WekaTools.registerWekaOperators(classLoader, WekaTools.getWekaClasses(weka.clusterers.Clusterer.class, new String[] { "weka.clusterers.OPTICS", "weka.clusterers.DBScan", "weka.clusterers.MakeDensityBasedClusterer" }, false), "com.rapidminer.operator.learner.clustering.clusterer.GenericWekaClusteringAdaptor", "The weka clusterer", "Learner.Unsupervised.Clustering.Weka", null);
        } catch (Throwable e) {
            LogService.getGlobal().log("Cannot register Weka clusterers: " + e, LogService.WARNING);
        }
	}
}
