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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.math.Averagable;


/**
 * <p>Measures the number of times a regression prediction correctly determines the trend.
 * This performance measure assumes that the attributes of each example represents the 
 * values of a time window, the label is a value after a certain horizon which should
 * be predicted. All examples build a consecutive series description, i.e. the labels
 * of all examples build the series itself (this is, for example, the case for a windowing 
 * step size of 1). This format will be delivered by the Series2ExampleSet operators provided by
 * RapidMiner.</p>
 * 
 * <p>Example: Lets think of a series v1...v10 and a sliding window with window width 3, 
 * step size 1 and prediction horizon 1. The resulting example set is then</p>
 * 
 * <pre>
 *    T1 T2 T3 L   P
 *    ---------------
 *    v1 v2 v3 v4  p1
 *    v2 v3 v4 v5  p2
 *    v3 v4 v5 v6  p3
 *    v4 v5 v6 v7  p4
 *    v5 v6 v7 v8  p5
 *    v6 v7 v8 v9  p6
 *    v7 v8 v9 v10 p7
 * </pre>
 * 
 * <p>The second last column (L) corresponds to the label, i.e. the value which should be 
 * predicted and the last column (P) corresponds to the predictions. The columns T1, T2,
 * and T3 correspond to the regular attributes, i.e. the points which should be used as 
 * learning input.</p>
 * 
 * <p>This performance measure then calculates the actuals trend between the last time point 
 * in the series (T3 here) and the actual label (L) and compares it to the trend between T3
 * and the prediction (P), sums the products between both trends, and divides this sum by the 
 * total number of examples, i.e. [(v4-v3)*(p1-v3)+(v5-v4)*(p2-v4)+...] / 7 in this example.</p>     
 * 
 * @author Ingo Mierswa
 * @version $Id: PredictionTrendAccuracy.java,v 1.5 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class PredictionTrendAccuracy extends MeasuredPerformance {

    private static final long serialVersionUID = 4275593122138248581L;

	private double length = 1.0d;
    
    private double correctCounter = 0.0d;
    
    public PredictionTrendAccuracy() {}

    public PredictionTrendAccuracy(PredictionTrendAccuracy pta) {
        super(pta);
    }

    public String getName() {
        return "prediction_trend_accuracy";
    }

    public String getDescription() {
        return "Measures the average of times a regression prediction was able to correctly predict the trend of the regression.";
    }

    public void startCounting(ExampleSet eSet, boolean useExampleWeights) throws OperatorException {
        super.startCounting(eSet, useExampleWeights);
        if (eSet.getAttributes().size() > 0) {
        	// get last attribute
        	Attribute attribute = null;
        	for (Attribute current : eSet.getAttributes()) {
        		attribute = current;
        	}
        	Attribute labelAttribute = eSet.getAttributes().getLabel();
        	Attribute predictedLabelAttribute = eSet.getAttributes().getPredictedLabel();
        	Attribute weightAttribute = null;
        	if (useExampleWeights)
        		weightAttribute = eSet.getAttributes().getWeight();
            
            for (Example example : eSet) {
            	double weight = 1.0d;
            	if (weightAttribute != null)
            		weight = example.getValue(weightAttribute);
            	
                double currentLabel = example.getValue(labelAttribute);
                double currentPrediction = example.getValue(predictedLabelAttribute);
                double lastValueBefore = example.getValue(attribute);
            
                double actualTrend = currentLabel - lastValueBefore;
                double predictionTrend = currentPrediction - lastValueBefore;
                if (actualTrend * predictionTrend >= 0) {
                    correctCounter += weight;
                }
                length += weight;
            }
        }
    }

    public double getExampleCount() {
        return length;
    }

    public void countExample(Example example) {}

    public double getFitness() {
        return getAverage();
    }

    public double getMikroAverage() {
        return correctCounter / length;
    }

    public double getMikroVariance() {
        return Double.NaN;
    }

    public void buildSingleAverage(Averagable averagable) {
        PredictionTrendAccuracy other = (PredictionTrendAccuracy) averagable;
        this.length += other.length;
        this.correctCounter += other.correctCounter;
    }
}
