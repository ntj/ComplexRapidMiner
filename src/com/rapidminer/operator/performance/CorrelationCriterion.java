/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.performance;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.Averagable;


/**
 * Computes the empirical corelation coefficient 'r' between label and
 * prediction. For
 * <code>P=prediction, L=label, V=Variance, Cov=Covariance</code> we calculate
 * r by: <br>
 * <code>Cov(L,P) / sqrt(V(L)*V(P))</code>.
 * 
 * Implementation hint: this implementation intensionally recomputes the mean
 * and variance of prediction and label despite the fact that they are available
 * by the Attribute objects. The reason: it can happen, that there are some
 * examples which have a NaN as prediction or label, but not both. In this case,
 * mean and variance stored in tie Attributes and computed here can differ.
 * 
 * @author Robert Rudolph, Ingo Mierswa
 * @version $Id: CorrelationCriterion.java,v 2.8 2006/03/21 15:35:50 ingomierswa
 *          Exp $
 */
public class CorrelationCriterion extends MeasuredPerformance {

	private static final long serialVersionUID = -8789903466296509903L;
    
    private Attribute labelAttribute;
    
    private Attribute predictedLabelAttribute;
    
	private int exampleCount = 0;

	private double sumLabel;

	private double sumPredict;

	private double sumLabelPredict;

	private double sumLabelSqr;

	private double sumPredictSqr;

	public CorrelationCriterion() {
	}

	public CorrelationCriterion(CorrelationCriterion sc) {
		super(sc);
		this.sumLabelPredict = sc.sumLabelPredict;
		this.sumLabelSqr = sc.sumLabelSqr;
		this.sumPredictSqr = sc.sumPredictSqr;
		this.sumLabel = sc.sumLabel;
		this.sumPredict = sc.sumPredict;
		this.exampleCount = sc.exampleCount;
        this.labelAttribute = (Attribute)sc.labelAttribute.clone();
        this.predictedLabelAttribute = (Attribute)sc.predictedLabelAttribute.clone();
	}

	public int getExampleCount() {
		return exampleCount;
	}

	/** Returns the maximum fitness of 1.0. */
	public double getMaxFitness() {
		return 1.0d;
	}

	/** Updates all sums needed to compute the correlation coefficient. */
	public void countExample(Example example) {
		double label = example.getValue(labelAttribute);
		double plabel = example.getValue(predictedLabelAttribute);
        if (labelAttribute.isNominal()) {
            String predLabelString = predictedLabelAttribute.getMapping().mapIndex((int)plabel);
            plabel = labelAttribute.getMapping().getIndex(predLabelString);
        }
		double prod = label * plabel;
		if (!Double.isNaN(prod)) {
			sumLabelPredict += prod;
			sumLabel += label;
			sumLabelSqr += label * label;
			sumPredict += plabel;
			sumPredictSqr += plabel * plabel;
			++exampleCount;
		}
	}

	public String getDescription() {
		return "Returns the correlation coefficient between the label and predicted label.";
	}

	public double getMikroAverage() {
		double r = (exampleCount * sumLabelPredict - sumLabel * sumPredict) / (Math.sqrt((exampleCount * sumLabelSqr - sumLabel * sumLabel) * (exampleCount * sumPredictSqr - sumPredict * sumPredict)));
		return r;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public void startCounting(ExampleSet eset) throws OperatorException {
		super.startCounting(eset);
		exampleCount = 0;
		sumLabelPredict = sumLabel = sumPredict = sumLabelSqr = sumPredictSqr = 0.0d;
        this.labelAttribute = eset.getAttributes().getLabel();
        this.predictedLabelAttribute = eset.getAttributes().getPredictedLabel();
	}

	public void buildSingleAverage(Averagable performance) {
		CorrelationCriterion other = (CorrelationCriterion) performance;
		this.sumLabelPredict += other.sumLabelPredict;
		this.sumLabelSqr += other.sumLabelSqr;
		this.sumPredictSqr += other.sumPredictSqr;
		this.sumLabel += other.sumLabel;
		this.sumPredict += other.sumPredict;
		this.exampleCount += other.exampleCount;
	}

	public double getFitness() {
		return getAverage();
	}

	public String getName() {
		return "correlation";
	}
}
