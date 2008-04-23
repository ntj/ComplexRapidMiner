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

import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.Averagable;
import com.rapidminer.tools.math.ROCDataGenerator;


/**
 * This criterion calculates the area under the ROC curve.
 * 
 * @author Ingo Mierswa, Martin Scholz
 * @version $Id: AreaUnderCurve.java,v 1.2 2007/07/13 22:52:13 ingomierswa Exp $
 */
public class AreaUnderCurve extends MeasuredPerformance {

	private static final long serialVersionUID = 6877715214974493828L;

	/** The value of the AUC. */
	private double auc = Double.NaN;

	/** A counter for average building. */
	private int counter = 1;

    /** The positive class name. */
    private String positiveClass;
    
    
	/** Clone constructor. */
	public AreaUnderCurve() {}

	public AreaUnderCurve(AreaUnderCurve aucObject) {
		super(aucObject);
		this.auc = aucObject.auc;
		this.counter = aucObject.counter;
        this.positiveClass = aucObject.positiveClass;
	}

	/** Calculates the AUC. */
	public void startCounting(ExampleSet exampleSet) throws OperatorException {
		// create ROC data
		ROCDataGenerator rocDataGenerator = new ROCDataGenerator(1.0d, 1.0d);
		List<double[]> rocData = rocDataGenerator.createROCDataList(exampleSet);
		this.auc = rocDataGenerator.calculateAUC(rocData);
        this.positiveClass = exampleSet.getAttributes().getPredictedLabel().getMapping().getPositiveString();
	}

	/** Does nothing. Everything is done in {@link #startCounting(ExampleSet)}. */
	public void countExample(Example example) {}

	public int getExampleCount() {
		return 1;
	}

	public double getMikroVariance() {
		return Double.NaN;
	}

	public double getMikroAverage() {
		return auc / counter;
	}

	/** Returns the fitness. */
	public double getFitness() {
		return getAverage();
	}

	public String getName() {
		return "AUC";
	}

	public String getDescription() {
		return "The area under a ROC curve. Given example weights are also considered. Please note that the second class is considered to be positive.";
	}

	public void buildSingleAverage(Averagable performance) {
		AreaUnderCurve other = (AreaUnderCurve) performance;
		this.counter += other.counter;
		this.auc += other.auc;
	}
    
    public String toString() {
        return super.toString() + " (positive class: " + positiveClass + ")";
    }
}
