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
 * This class encapsulates the well known binary classification criteria
 * precision and recall. Furthermore it can be used to calculate the fallout,
 * the equally weighted f-measure (f1-measure), the lift, and the values for
 * TRUE_POSITIVE, FALSE_POSITIVE, TRUE_NEGATIVE, and FALSE_NEGATIVE. With
 * &quot;positive&quot; we refer to the first class and with
 * &quot;negative&quot; we refer to the second.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: BinaryClassificationPerformance.java,v 2.14 2006/03/21 15:35:50
 *          ingomierswa Exp $
 */
public class BinaryClassificationPerformance extends MeasuredPerformance implements Tableable{

	private static final long serialVersionUID = 7475134460409215015L;

	public static final int PRECISION = 0;

	public static final int RECALL = 1;

	public static final int LIFT = 2;

	public static final int FALLOUT = 3;

	public static final int F_MEASURE = 4;

	public static final int FALSE_POSITIVE = 5;

	public static final int FALSE_NEGATIVE = 6;

	public static final int TRUE_POSITIVE = 7;

	public static final int TRUE_NEGATIVE = 8;

	private static final int NEGATIVE = 0;

	private static final int POSITIVE = 1;

	public static final String[] NAMES = { 
		"precision", 
		"recall", 
		"lift", 
		"fallout", 
		"f_measure",
		"false_positive", 
		"false_negative", 
		"true_positive", 
		"true_negative" 
	};

	public static final String[] DESCRIPTIONS = { 
		"Relative number of correctly as positive classified examples among all examples classified as positive", 
		"Relative number of correctly as positive classified examples among all positive examples", 
		"The lift of the positive class",
		"Relative number of incorrectly as positive classified examples among all negative examples", 
		"Combination of precision and recall: f=2pr/(p+r)",
		"Absolute number of incorrectly as positive classified examples", 
		"Absolute number of incorrectly as negative classified examples", 
		"Absolute number of correctly as positive classified examples", 
		"Absolute number of correctly as negative classified examples" 
	};

	private int type = 0;

	/** true label, predicted label. PP = TP, PN = FN, NP = FP, NN = TN. */
	private double[][] counter = new double[2][2];

	/** Name of the positive class. */
	private String positiveClassName = "";

	/** Name of the negative class. */
	private String negativeClassName = "";

    /** The predicted label attribute. */
    private Attribute predictedLabelAttribute;

    /** The  label attribute. */
    private Attribute labelAttribute;
    
    /** The weight attribute. Might be null. */
    private Attribute weightAttribute;
    
    private ConfusionMatrixViewer viewer;
    
	public BinaryClassificationPerformance() {
		type = -1;
	}

	public BinaryClassificationPerformance(BinaryClassificationPerformance o) {
		super(o);
		this.type = o.type;
		this.counter = new double[2][2];
		this.counter[NEGATIVE][NEGATIVE] = o.counter[NEGATIVE][NEGATIVE];
		this.counter[POSITIVE][NEGATIVE] = o.counter[POSITIVE][NEGATIVE];
		this.counter[NEGATIVE][POSITIVE] = o.counter[NEGATIVE][POSITIVE];
		this.counter[POSITIVE][POSITIVE] = o.counter[POSITIVE][POSITIVE];
        if (o.predictedLabelAttribute != null)
            this.predictedLabelAttribute = (Attribute)o.predictedLabelAttribute.clone();
        if (o.labelAttribute != null)
        this.labelAttribute = (Attribute)o.labelAttribute.clone();
        if (o.weightAttribute != null)
        	this.weightAttribute = (Attribute)o.weightAttribute.clone();
        this.positiveClassName = o.positiveClassName;
        this.negativeClassName = o.negativeClassName;
	}

	public BinaryClassificationPerformance(int type) {
		this.type = type;
	}

	/** For test cases only. */
	public BinaryClassificationPerformance(int type, double[][] counter) {
		this.type = type;
		this.counter[NEGATIVE][NEGATIVE] = counter[NEGATIVE][NEGATIVE];
		this.counter[NEGATIVE][POSITIVE] = counter[NEGATIVE][POSITIVE];
		this.counter[POSITIVE][NEGATIVE] = counter[POSITIVE][NEGATIVE];
		this.counter[POSITIVE][POSITIVE] = counter[POSITIVE][POSITIVE];
	}

	public static BinaryClassificationPerformance newInstance(String name) {
		for (int i = 0; i < NAMES.length; i++) {
			if (NAMES[i].equals(name))
				return new BinaryClassificationPerformance(i);
		}
		return null;
	}

	public double getExampleCount() {
		return counter[POSITIVE][POSITIVE] + counter[NEGATIVE][POSITIVE] + counter[POSITIVE][NEGATIVE] + counter[NEGATIVE][NEGATIVE];
	}

	// ================================================================================

	public void startCounting(ExampleSet eSet, boolean useExampleWeights) throws OperatorException {
		super.startCounting(eSet, useExampleWeights);
		this.predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
        this.labelAttribute = eSet.getAttributes().getLabel();
		if (!labelAttribute.isNominal() || (labelAttribute.getMapping().size() != 2))
			throw new UserError(null, 118, new Object[] { "'" + labelAttribute.getName() + "'", Integer.valueOf(labelAttribute.getMapping().getValues().size()), "2 for calculation of '" + getName() + "'" });
		this.negativeClassName = predictedLabelAttribute.getMapping().getNegativeString();
		this.positiveClassName = predictedLabelAttribute.getMapping().getPositiveString();
		if (useExampleWeights)
			this.weightAttribute = eSet.getAttributes().getWeight();
		this.counter = new double[2][2];
	}

	public void countExample(Example example) {
	    String labelString = example.getNominalValue(labelAttribute);
        int label = predictedLabelAttribute.getMapping().getIndex(labelString);
        String predString = example.getNominalValue(predictedLabelAttribute);
        int plabel = predictedLabelAttribute.getMapping().getIndex(predString);
        
        double weight = 1.0d;
        if (weightAttribute != null)
        	weight = example.getValue(weightAttribute);
		counter[label][plabel] += weight;
	}

	public double getMikroAverage() {
		double x = 0.0d, y = 0.0d;
		switch (type) {
			case PRECISION:
				x = counter[POSITIVE][POSITIVE];
				y = counter[POSITIVE][POSITIVE] + counter[NEGATIVE][POSITIVE];
				break;
			case RECALL:
				x = counter[POSITIVE][POSITIVE];
				y = counter[POSITIVE][POSITIVE] + counter[POSITIVE][NEGATIVE];
				break;
			case LIFT:
				x = (double) counter[POSITIVE][POSITIVE] / (double) (counter[POSITIVE][POSITIVE] + counter[POSITIVE][NEGATIVE]);
				y = (double) (counter[POSITIVE][POSITIVE] + counter[NEGATIVE][POSITIVE]) / (double) (counter[POSITIVE][POSITIVE] + counter[POSITIVE][NEGATIVE] + counter[NEGATIVE][POSITIVE] + counter[NEGATIVE][NEGATIVE]);
				break;
			case FALLOUT:
				x = counter[NEGATIVE][POSITIVE];
				y = counter[NEGATIVE][POSITIVE] + counter[NEGATIVE][NEGATIVE];
				break;

			case F_MEASURE:
				x = counter[POSITIVE][POSITIVE];
				x *= x;
				x *= 2;
				y = x + counter[POSITIVE][POSITIVE] * counter[POSITIVE][NEGATIVE] + counter[POSITIVE][POSITIVE] * counter[NEGATIVE][POSITIVE];
				break;

			case FALSE_NEGATIVE:
				x = counter[POSITIVE][NEGATIVE];
				y = 1;
				break;
			case FALSE_POSITIVE:
				x = counter[NEGATIVE][POSITIVE];
				y = 1;
				break;
			case TRUE_NEGATIVE:
				x = counter[NEGATIVE][NEGATIVE];
				y = 1;
				break;
			case TRUE_POSITIVE:
				x = counter[POSITIVE][POSITIVE];
				y = 1;
				break;
			default:
				throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
		if (y == 0)
			return Double.NaN;
		return x / y;
	}

	public double getFitness() {
		switch (type) {
			case PRECISION:
			case RECALL:
			case LIFT:
			case TRUE_POSITIVE:
			case TRUE_NEGATIVE:
			case F_MEASURE:
				return getAverage();
			case FALLOUT:
			case FALSE_POSITIVE:
			case FALSE_NEGATIVE:
				if (getAverage() == 0.0d)
					return Double.POSITIVE_INFINITY;
				return 1.0d / getAverage();
			default:
				throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
	}

	public double getMaxFitness() {
		switch (type) {
			case PRECISION:
			case RECALL:
			case F_MEASURE:
				return 1.0d;
			case LIFT:
			case TRUE_POSITIVE:
			case TRUE_NEGATIVE:
			case FALLOUT:
			case FALSE_POSITIVE:
			case FALSE_NEGATIVE:
				return Double.POSITIVE_INFINITY;
			default:
				throw new RuntimeException("Illegal value for type in BinaryClassificationPerformance: " + type);
		}
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	// ================================================================================

	public String getName() {
		return NAMES[type];
	}

	public String getDescription() {
		return DESCRIPTIONS[type];
	}

	public boolean formatPercent() {
		switch (type) {
			case TRUE_POSITIVE:
			case TRUE_NEGATIVE:
			case FALSE_POSITIVE:
			case FALSE_NEGATIVE:
				return false;
			default:
				return true;
		}
	}

	public void buildSingleAverage(Averagable performance) {
		BinaryClassificationPerformance other = (BinaryClassificationPerformance) performance;
		if (this.type != other.type)
			throw new RuntimeException("Cannot build average of different error types (" + NAMES[this.type] + "/" + NAMES[other.type] + ").");
		if (!this.positiveClassName.equals(other.positiveClassName))
			throw new RuntimeException("Cannot build average for different positive classes (" + this.positiveClassName + "/" + other.positiveClassName + ").");
		this.counter[NEGATIVE][NEGATIVE] += other.counter[NEGATIVE][NEGATIVE];
		this.counter[NEGATIVE][POSITIVE] += other.counter[NEGATIVE][POSITIVE];
		this.counter[POSITIVE][NEGATIVE] += other.counter[POSITIVE][NEGATIVE];
		this.counter[POSITIVE][POSITIVE] += other.counter[POSITIVE][POSITIVE];
	}

	public String toString() {
		StringBuffer result = new StringBuffer(super.toString());
		result.append(" (positive class: " + positiveClassName + ")");
		result.append(Tools.getLineSeparator() + "ConfusionMatrix:" + Tools.getLineSeparator() + "True:");
		result.append("\t" + negativeClassName);
		result.append("\t" + positiveClassName);
		result.append(Tools.getLineSeparator() + negativeClassName + ":");
		result.append("\t" + Tools.formatIntegerIfPossible(counter[NEGATIVE][NEGATIVE]));
		result.append("\t" + Tools.formatIntegerIfPossible(counter[POSITIVE][NEGATIVE]));
		result.append(Tools.getLineSeparator() + positiveClassName + ":");
		result.append("\t" + Tools.formatIntegerIfPossible(counter[NEGATIVE][POSITIVE]));
		result.append("\t" + Tools.formatIntegerIfPossible(counter[POSITIVE][POSITIVE]));
		return result.toString();
	}

	/** This implementation returns a confusion matrix viewer based on a JTable. */
	public Component getVisualizationComponent(IOContainer ioContainer) {
		viewer = new ConfusionMatrixViewer(super.toString() + " (positive class: " + positiveClassName + ")", 
				new String[] { negativeClassName, positiveClassName },
				this.counter); 
		return viewer;
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
