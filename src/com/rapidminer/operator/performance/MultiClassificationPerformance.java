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
import com.rapidminer.tools.Tableable;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.math.Averagable;


/**
 * Measures the accuracy and classification error for both binary classification
 * problems and multi class problems. Additionally, this performance criterion
 * can also compute the kappa statistics for multi class problems. This is
 * calculated as k = (P(A) - P(E)) / (1 - P(E)) with [ P(A) = diagonal sum /
 * number of examples ] and [ P(E) = sum over i of ((sum of i-th row * sum of i-th
 * column) / (n to the power of 2) ].
 * 
 * @author Ingo Mierswa
 * @version $Id: MultiClassificationPerformance.java,v 2.16 2006/03/21 15:35:50
 *          ingomierswa Exp $
 */
public class MultiClassificationPerformance extends MeasuredPerformance implements Tableable {

	private static final long serialVersionUID = 3068421566038331525L;

	/** Indicates an undefined type (should not happen). */
	public static final int UNDEFINED = -1;

	/** Indicates accuracy. */
	public static final int ACCURACY = 0;

	/** Indicates classification error. */
	public static final int ERROR = 1;
	
	/** Indicates kappa statistics. */
	public static final int KAPPA = 2;

	/** The names of the criteria. */
	public static final String[] NAMES = { 
		"accuracy", 
		"classification_error", 
		"kappa" 
	};

	/** The descriptions of the criteria. */
	public static final String[] DESCRIPTIONS = { 
		"Relative number of correctly classified examples", 
		"Relative number of misclassified examples",
		"The kappa statistics for the classification" 
	};

	/**
	 * The counter for true labels and the prediction.
	 */
	private double[][] counter;

	/** The class names of the label. Used for logging and result display. */
	private String[] classNames;

    /** Maps class names to indices. */
    private Map<String, Integer> classNameMap = new HashMap<String, Integer>();
    
    /** The currently used label attribute. */
    private Attribute labelAttribute;
    
    /** The currently used predicted label attribute. */
    private Attribute predictedLabelAttribute;
    
    /** The weight attribute. Might be null. */
    private Attribute weightAttribute;
    
	/** The type of this performance: accuracy or classification error. */
	private int type = ACCURACY;

	/** The viewer used to show the confusion matrix */
	private ConfusionMatrixViewer viewer;
	
	/** Creates a MultiClassificationPerformance with undefined type. */
	public MultiClassificationPerformance() {
		this(UNDEFINED);
	}

	/** Creates a MultiClassificationPerformance with the given type. */
	public MultiClassificationPerformance(int type) {
		this.type = type;
	}

    /** Clone constructor. */
	public MultiClassificationPerformance(MultiClassificationPerformance m) {
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

	/** Creates a MultiClassificationPerformance with the given type. */
	public static MultiClassificationPerformance newInstance(String name) {
		for (int i = 0; i < NAMES.length; i++) {
			if (NAMES[i].equals(name))
				return new MultiClassificationPerformance(i);
		}
		return null;
	}

	public double getExampleCount() {
		double total = 0;
		for (int i = 0; i < counter.length; i++) {
			for (int j = 0; j < counter[i].length; j++)
				total += counter[i][j];
		}
		return total;
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
        
        if (this.predictedLabelAttribute.getMapping().size() != this.labelAttribute.getMapping().size()) {
        	throw new UserError(null, 118, new Object[] { this.predictedLabelAttribute.getName(), this.predictedLabelAttribute.getMapping().size(), " the same as the different values of the label (" + this.labelAttribute.getMapping().size() + ")" });
        }
     
        if (useExampleWeights)
        	this.weightAttribute = eSet.getAttributes().getWeight();
        
		Collection values = this.labelAttribute.getMapping().getValues();
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

	/** Returns either the accuracy or the classification error. */
	public double getMikroAverage() {
		double diagonal = 0, total = 0;
		for (int i = 0; i < counter.length; i++) {
			diagonal += counter[i][i];
			for (int j = 0; j < counter[i].length; j++)
				total += counter[i][j];
		}
		if (total == 0)
			return Double.NaN;

		// returns either the accuracy, the error, or the kappa statistics
		double accuracy = diagonal / total;
		switch (type) {
			case ACCURACY:
				return accuracy;
			case ERROR:
				return (1.0d - accuracy);
			case KAPPA:
				double pa = accuracy;
				double pe = 0.0d;
				for (int i = 0; i < counter.length; i++) {
					double row = 0.0d;
					double column = 0.0d;
					for (int j = 0; j < counter[i].length; j++) {
						row += counter[i][j];
						column += counter[j][i];
					}
					//pe += ((row * column) / Math.pow(total, counter.length));
					pe += ((row * column) / Math.pow(total, 2.0d));
				}
				return (pa - pe) / (1.0d - pe);
			default:
				throw new RuntimeException("Unknown type " + type + " for multi class performance criterion!");
		}
	}

	/** Returns true. */
	public boolean formatPercent() {
		if (type == KAPPA)
			return false;
		else
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
		if (type == ERROR)
			return (1.0d - getAverage());
		else
			return getAverage();
	}

	/** Returns 1. */
	public double getMaxFitness() {
		return 1.0d;
	}

	public void buildSingleAverage(Averagable performance) {
		MultiClassificationPerformance other = (MultiClassificationPerformance) performance;
		for (int i = 0; i < this.counter.length; i++)
			for (int j = 0; j < this.counter[i].length; j++)
				this.counter[i][j] += other.counter[i][j];
	}

	// ================================================================================

	/** This implementation returns a confusion matrix viewer based on a JTable. */
	public Component getVisualizationComponent(IOContainer ioContainer) {
		viewer = new ConfusionMatrixViewer(super.toString(), classNames, counter);
		return viewer;
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
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

	public String getCell(int row, int column) {
		return viewer.getCell(row, column);
	}

	public int getColumnNumber() {
		return viewer.getColumnNumber();
	}

	public int getRowNumber() {
		return viewer.getRowNumber();
	}
}
