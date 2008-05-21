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
package com.rapidminer.operator.performance;

import java.awt.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.viewer.ConfusionMatrixViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.Averagable;


/**
 * Measures the weighted mean of all per class recalls or per class precisions based
 * on the weights defined in the performance evaluator.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightedMultiClassPerformance.java,v 1.6 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class WeightedMultiClassPerformance extends MeasuredPerformance implements ClassWeightedPerformance {

    private static final long serialVersionUID = 8734250559680229116L;

	/** Indicates an undefined type (should not happen). */
    public static final int UNDEFINED = -1;

    /** Indicates accuracy. */
    public static final int WEIGHTED_RECALL = 0;

    /** Indicates classification error. */
    public static final int WEIGHTED_PRECISION = 1;
    

    /** The names of the criteria. */
    public static final String[] NAMES = { 
        "weighted_mean_recall", 
        "weighted_mean_precision"
    };

    /** The descriptions of the criteria. */
    public static final String[] DESCRIPTIONS = { 
        "The weighted mean of all per class recall measurements.", 
        "The weighted mean of all per class precision measurements."
    };

    /**
     * The counter for true labels and the prediction.
     */
    private double[][] counter;

    /** The class names of the label. Used for logging and result display. */
    private String[] classNames;

    /** Maps class names to indices. */
    private Map<String, Integer> classNameMap = new HashMap<String, Integer>();
    
    /** The type of this performance. */
    private int type = WEIGHTED_RECALL;

    /** The different class weights. */
    private double[] classWeights;
    
    /** The sum of all weights. */
    private double weightSum;
    
    /** The currently used label attribute. */
    private Attribute labelAttribute;
    
    /** The currently used predicted label attribute. */
    private Attribute predictedLabelAttribute;
    
    /** The weight attribute. Might be null. */
    private Attribute weightAttribute;
    
    
    /** Creates a WeightedMultiClassPerformance with undefined type. */
    public WeightedMultiClassPerformance() {
        this(UNDEFINED);
    }

    /** Creates a WeightedMultiClassPerformance with the given type. */
    public WeightedMultiClassPerformance(int type) {
        this.type = type;
    }

    public WeightedMultiClassPerformance(WeightedMultiClassPerformance m) {
        super(m);
        this.type = m.type;
        this.classNames = new String[m.classNames.length];
        for (int i = 0; i < this.classNames.length; i++) {
            this.classNames[i] = m.classNames[i];
            this.classNameMap.put(this.classNames[i], i);
        }
        this.counter = new double[m.counter.length][m.counter.length];
        for (int i = 0; i < this.counter.length; i++)
            for (int j = 0; j < this.counter[i].length; j++)
                this.counter[i][j] = m.counter[i][j];
        this.labelAttribute = (Attribute)m.labelAttribute.clone();
        this.predictedLabelAttribute = (Attribute)m.predictedLabelAttribute.clone();
        if (m.weightAttribute != null)
        	this.weightAttribute = (Attribute)m.weightAttribute.clone();
    }

    /** Creates a WeightedMultiClassPerformance with the given type. */
    public static WeightedMultiClassPerformance newInstance(String name) {
        for (int i = 0; i < NAMES.length; i++) {
            if (NAMES[i].equals(name))
                return new WeightedMultiClassPerformance(i);
        }
        return null;
    }

    /** Sets the class weights. */
    public void setWeights(double[] weights) {
        this.weightSum = 0.0d;
        this.classWeights = weights;
        for (double w : this.classWeights) {
            this.weightSum += w;
        }
    }

    /** Initializes the criterion and sets the label. */
    public void startCounting(ExampleSet eSet, boolean useExampleWeights) throws OperatorException {
    	super.startCounting(eSet, useExampleWeights);
        this.labelAttribute = eSet.getAttributes().getLabel();
        if (!this.labelAttribute.isNominal())
            throw new UserError(null, 101, "calculation of classification performance criteria", this.labelAttribute.getName());
        this.predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
        if ((this.predictedLabelAttribute == null) || (!this.predictedLabelAttribute.isNominal()))
            throw new UserError(null, 101, "calculation of classification performance criteria", "predicted label attribute");
        
        if (useExampleWeights)
        	this.weightAttribute = eSet.getAttributes().getWeight();
        
        Collection values = labelAttribute.getMapping().getValues();
        this.counter = new double[values.size()][values.size()];
        this.classNames = new String[values.size()];
        Iterator i = values.iterator();
        int n = 0;
        while (i.hasNext()) {
            classNames[n] = (String) i.next();
            classNameMap.put(classNames[n], n);
            n++;
        }
    }

    /** Increases the prediction value in the matrix. */
    public void countExample(Example example) {
        int label = classNameMap.get(example.getNominalValue(labelAttribute));
        int plabel = classNameMap.get(example.getNominalValue(predictedLabelAttribute));
        double weight = 1.0d;
        if (weightAttribute != null)
        	weight = example.getValue(weightAttribute);
        counter[label][plabel] += weight;
    }

    public double getExampleCount() {
        double total = 0;
        for (int i = 0; i < counter.length; i++) {
            for (int j = 0; j < counter[i].length; j++)
                total += counter[i][j];
        }
        return total;
    }
    
    /** Returns either the accuracy or the classification error. */
    public double getMikroAverage() {
        switch (type) {
            case WEIGHTED_RECALL:
                double[] columnSums = new double[classNames.length];
                for (int c = 0; c < columnSums.length; c++) {
                    for (int r = 0; r < counter[c].length; r++) {
                        columnSums[c] += counter[c][r];
                    }
                }
                double result = 0.0d;
                for (int c = 0; c < columnSums.length; c++) {
                    result += classWeights[c] * (counter[c][c] / columnSums[c]);
                }
                result /= weightSum;
                return result;
            case WEIGHTED_PRECISION:
                double[] rowSums = new double[classNames.length];
                for (int r = 0; r < counter.length; r++) {
                    for (int c = 0; c < counter[r].length; c++) {
                        rowSums[r] += counter[c][r];
                    }
                }
                result = 0.0d;
                for (int r = 0; r < rowSums.length; r++) {
                    result += classWeights[r] * (counter[r][r] / rowSums[r]);
                }
                result /= weightSum;
                return result;
            default:
                throw new RuntimeException("Unknown type " + type + " for weighted multi class performance criterion!");
        }
    }

    /** Returns true. */
    public boolean formatPercent() {
        return true;
    }

    public double getMikroVariance() {
        return Double.NaN;
    }

    /** Returns the name. */
    public String getName() {
        return NAMES[type];
    }

    /** Returns the description. */
    public String getDescription() {
        return DESCRIPTIONS[type];
    }

    // ================================================================================

    /** Returns the accuracy or 1 - error. */
    public double getFitness() {
        return getAverage();
    }

    /** Returns 1. */
    public double getMaxFitness() {
        return 1.0d;
    }

    public void buildSingleAverage(Averagable performance) {
        WeightedMultiClassPerformance other = (WeightedMultiClassPerformance) performance;
        for (int i = 0; i < this.counter.length; i++)
            for (int j = 0; j < this.counter[i].length; j++)
                this.counter[i][j] += other.counter[i][j];
    }

    // ================================================================================

    private String toWeightString() {
        StringBuffer result = new StringBuffer(super.toString());
        result.append(", weights: ");
        boolean first = true;
        for (double w : this.classWeights) {
            if (!first)
                result.append(", ");
            result.append(Tools.formatIntegerIfPossible(w));
            first = false;
        }
        return result.toString();
    }
    
    /** This implementation returns a confusion matrix viewer based on a JTable. */
    public Component getVisualizationComponent(IOContainer ioContainer) {
        return new ConfusionMatrixViewer(toWeightString(), classNames, counter);
    }
    
    public String toString() {
        StringBuffer result = new StringBuffer(toWeightString() + "");
        result.append(Tools.getLineSeparator() + "ConfusionMatrix:" + Tools.getLineSeparator() + "True:");
        for (int i = 0; i < this.counter.length; i++)
            result.append("\t" + classNames[i]);

        for (int i = 0; i < this.counter.length; i++) {
            result.append(Tools.getLineSeparator() + classNames[i] + ":");
            for (int j = 0; j < this.counter[i].length; j++) {
                result.append("\t" + Tools.formatIntegerIfPossible(this.counter[j][i]));
            }
        }
        return result.toString();
    }
}
