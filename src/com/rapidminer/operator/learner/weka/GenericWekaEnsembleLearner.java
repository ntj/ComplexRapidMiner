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
package com.rapidminer.operator.learner.weka;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.operator.learner.Learner;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.WekaInstancesAdaptor;
import com.rapidminer.tools.WekaLearnerCapabilities;
import com.rapidminer.tools.WekaTools;

import weka.classifiers.Classifier;
import weka.core.Instances;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;

/**
 * Performs the ensemble learning scheme of Weka with the same name. An arbitrary
 * number of other Weka learning schemes must be embedded as inner operators. See the
 * Weka javadoc for further classifier and parameter descriptions.<br/>
 * 
 * @author Ingo Mierswa
 * @version $Id: GenericWekaEnsembleLearner.java,v 1.6 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class GenericWekaEnsembleLearner extends OperatorChain implements Learner, TechnicalInformationHandler {

    private static final Class[] INPUT_CLASSES = { ExampleSet.class };

    private static final Class[] OUTPUT_CLASSES = { Model.class };

    public static final String[] WEKA_CLASSIFIERS = WekaTools.getWekaClasses(weka.classifiers.Classifier.class, ".meta.", true);
    
    /** The list with the weka parameters. */
    private List<ParameterType> wekaParameters = new LinkedList<ParameterType>();

    public GenericWekaEnsembleLearner(OperatorDescription description) {
        super(description);
    }

    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        Model model = learn(exampleSet);
        return new IOObject[] { model };
    }

    public Model learn(ExampleSet exampleSet) throws OperatorException {
        // not the parameter tool method of WekaTools!
        String[] wekaParas = getWekaParameters();
        if (wekaParas == null)
            throw new UserError(this, 131, "simple Weka learner");
        
        Classifier classifier = getWekaClassifier(wekaParas);
        
        log("Converting to Weka instances.");
        Instances instances = WekaTools.toWekaInstances(exampleSet, "MetaLearningInstances", WekaInstancesAdaptor.LEARNING);
        try {
            log("Building Weka classifier.");
            classifier.buildClassifier(instances);
        } catch (Exception e) {
            throw new UserError(this, e, 905, new Object[] { getOperatorClassName(), e });
        }
        return new WekaClassifier(exampleSet, getOperatorClassName(), classifier);
    }

    /**
     * This method is used by the {@link GenericWekaEnsembleLearner} to specify the
     * learners name.
     */
    public String getWekaClassPath() {
		String prefixName = getOperatorClassName();
		String actualName = prefixName.substring(WekaTools.WEKA_OPERATOR_PREFIX.length());
        for (int i = 0; i < WEKA_CLASSIFIERS.length; i++) {
            if (WEKA_CLASSIFIERS[i].endsWith(actualName)) {
                return WEKA_CLASSIFIERS[i];
            }
        }
        return null;
    }

    /**
     * This method is used by the {@link GenericWekaMetaLearner} to specify the
     * learners parameters.
     */
    public List getWekaParameterList() {
        return wekaParameters;
    }

    /** Returns the Weka classifier based on the subtype of this operator. */
    private Classifier getWekaClassifier(String[] parameters) throws OperatorException {
        String classifierName = getWekaClassPath();
        Classifier classifier = null;
        try {
            classifier = Classifier.forName(classifierName, parameters);
        } catch (Exception e) {
            throw new UserError(this, e, 904, new Object[] { classifierName, e });
        }
        return classifier;
    }

    public TechnicalInformation getTechnicalInformation() {
        try {
            Classifier classifier = getWekaClassifier(null);
            if (classifier instanceof TechnicalInformationHandler)
                return ((TechnicalInformationHandler)classifier).getTechnicalInformation();
            else
                return null;
        } catch (OperatorException e) {
            return null;
        }
    }
    
    /**
     * This method uses some tool methods and the parameters from the inner
     * learning scheme to build the Weka parameter style. If the inner operator
     * is not of type {@link GenericWekaLearner}, null will be returned. Calling
     * methods should usually throw an exception in this case. 
     */
    private String[] getWekaParameters() throws OperatorException {
        String[] ensebleParameters = WekaTools.getWekaParametersFromTypes(this, wekaParameters);
        List<String> allParameters = new LinkedList<String>();
        for (String p : ensebleParameters)
            allParameters.add(p);
        for (int i = 0; i < getNumberOfOperators(); i++) {
            Operator operator = getOperator(i);
            if (operator instanceof GenericWekaLearner) {
                allParameters.add("-B");
                GenericWekaLearner inner = (GenericWekaLearner)operator;
                StringBuffer innerLearnerString = new StringBuffer(inner.getWekaClassPath());
                String[] innerParameters = WekaTools.getWekaParametersFromTypes(inner, inner.getWekaParameterList());;
                for (int p = 0; p < innerParameters.length; p++)
                    innerLearnerString.append(" " + innerParameters[p]);
                allParameters.add(innerLearnerString.toString());
            } else {
                throw new UserError(this, 127, "Inner operator of a Weka ensemble learning operator '" + getName() + "' must be another Weka learning scheme.");
            } 
        }
        String[] result = new String[allParameters.size()];
        allParameters.toArray(result);
        return result;
    }
    
	/** Returns true. */
	public boolean onlyWarnForNonSufficientCapabilities() {
		return true;
	}
    
	public boolean supportsCapability(LearnerCapability capability) {
		Classifier classifier;
		try {
			classifier = getWekaClassifier(WekaTools.getWekaParametersFromTypes(this, wekaParameters));
		} catch (OperatorException e) {
			return true;
		}
		if (classifier != null) {
			try {
				return WekaLearnerCapabilities.supportsCapability(classifier, capability);
			} catch (Throwable t) {
				return true;
			}
		}
		return true;
	}
	

    /**
     * Returns true if the user wants to estimate the performance (depending on
     * a parameter). In this case the method getEstimatedPerformance() must also
     * be overriden and deliver the estimated performance. The default
     * implementation returns false.
     */
    public boolean shouldEstimatePerformance() {
        return false;
    }

    /**
     * Returns true if the user wants to calculate feature weights (depending on
     * a parameter). In this case the method getWeights() must also be overriden
     * and deliver the calculated weights. The default implementation returns
     * false.
     */
    public boolean shouldCalculateWeights() {
        return false;
    }

    /**
     * Returns the estimated performance. Subclasses which supports the
     * capability to estimate learning performance must override this method.
     * The default implementation throws an exception.
     */
    public PerformanceVector getEstimatedPerformance() throws OperatorException {
        throw new UserError(this, 912, getName(), "estimation of performance not supported.");
    }

    /**
     * Returns the calculated weight vectors. Subclasses which supports the
     * capability to calculate feature weights must override this method. The
     * default implementation throws an exception.
     */
    public AttributeWeights getWeights(ExampleSet exampleSet) throws OperatorException {
        throw new UserError(this, 916, getName(), "calculation of weights not supported.");
    }

    /** Indicates that the consumption of example sets can be user defined. */
    public InputDescription getInputDescription(Class cls) {
        if (ExampleSet.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        } else {
            return super.getInputDescription(cls);
        }
    }

    public int getMinNumberOfInnerOperators() {
        return 1;
    }

    public int getMaxNumberOfInnerOperators() {
        return Integer.MAX_VALUE;
    }

    public Class[] getOutputClasses() {
        return OUTPUT_CLASSES;
    }

    public Class[] getInputClasses() {
        return INPUT_CLASSES;
    }

    /** Returns a simple chain condition. */
    public InnerOperatorCondition getInnerOperatorCondition() {
        return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[] { Model.class });
    }

    public void performAdditionalChecks() throws UserError {
        super.performAdditionalChecks();
        for (int i = 0; i < getNumberOfOperators(); i++)
        if (!(getOperator(i) instanceof GenericWekaLearner))
            throw new UserError(this, 127, "Inner operator of a Weka ensemble learning operator '" + getName() + "' must be another Weka learning scheme.");
    }

    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        Classifier classifier = null;
        try {
            // parameters must be null, not an empty String[0] array!
            classifier = getWekaClassifier(null);
        } catch (OperatorException e) {
            throw new RuntimeException("Cannot instantiate Weka classifier " + getOperatorClassName() + ": " + e.getMessage());
        }
        wekaParameters = new LinkedList<ParameterType>();
        if (classifier != null) {
            WekaTools.addParameterTypes(classifier, types, wekaParameters, true, "B");
        }
        return types;
    }
}
