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
import java.awt.Graphics;
import java.util.LinkedList;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.gui.viewer.ROCViewer;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Renderable;
import com.rapidminer.tools.math.Averagable;
import com.rapidminer.tools.math.ROCData;
import com.rapidminer.tools.math.ROCDataGenerator;


/**
 * This criterion calculates the area under the ROC curve.
 * 
 * @author Ingo Mierswa, Martin Scholz
 * @version $Id: AreaUnderCurve.java,v 1.8 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class AreaUnderCurve extends MeasuredPerformance implements Renderable {

	private static final long serialVersionUID = 6877715214974493828L;

	/** The value of the AUC. */
	private double auc = Double.NaN;

    /** The data generator for this ROC curve. */
    private ROCDataGenerator rocDataGenerator = new ROCDataGenerator(1.0d, 1.0d);
    
    /** The data for the ROC curve. */
    private LinkedList<ROCData> rocData = new LinkedList<ROCData>();
    
	/** A counter for average building. */
	private int counter = 1;

    /** The positive class name. */
    private String positiveClass;
    
    /** The viewer used to show the rocPlot */
    private ROCViewer viewer;
    
	/** Clone constructor. */
	public AreaUnderCurve() {}

	public AreaUnderCurve(AreaUnderCurve aucObject) {
		super(aucObject);
		this.auc = aucObject.auc;
		this.counter = aucObject.counter;
        this.positiveClass = aucObject.positiveClass;
	}

	/** Calculates the AUC. */
	public void startCounting(ExampleSet exampleSet, boolean useExampleWeights) throws OperatorException {
		super.startCounting(exampleSet, useExampleWeights);
		// create ROC data
		this.rocData.add(rocDataGenerator.createROCData(exampleSet, useExampleWeights));
		this.auc = rocDataGenerator.calculateAUC(this.rocData.getLast());
        this.positiveClass = exampleSet.getAttributes().getPredictedLabel().getMapping().getPositiveString();
	}

	/** Does nothing. Everything is done in {@link #startCounting(ExampleSet, boolean)}. */
	public void countExample(Example example) {}

	public double getExampleCount() {
		return 1.0d;
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
        this.rocData.addAll(other.rocData);
	}
    
    /** This implementation returns a confusion matrix viewer based on a JTable. */
    public Component getVisualizationComponent(IOContainer ioContainer) {
        viewer = new ROCViewer(toString(), this.rocDataGenerator, this.rocData);
    	return viewer;
    }
    
    public String toString() {
        return super.toString() + " (positive class: " + positiveClass + ")";
    }

	public int getRenderHeight(int preferredHeight) {
		return viewer.getRenderHeight(preferredHeight);
	}

	public int getRenderWidth(int preferredWidth) {
		return viewer.getRenderWidth(preferredWidth);
	}

	public void render(Graphics graphics, int width, int height) {
		viewer.render(graphics, width, height);
	}
}
