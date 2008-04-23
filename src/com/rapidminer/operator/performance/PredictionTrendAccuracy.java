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
 * <p>Measures the number of times a regression prediction correctly determines the trend.
 * This performance measure assumes that the attributes of each example represents the 
 * values of a time window, the label is a value after a certain horizon which should
 * be predicted. All examples build a consecutive series description, i.e. the labels
 * of all examples build the series itself (at least it would for windowing step size 
 * of 1). This format will be delivered by the Series2ExampleSet operators provided by
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
 * @version $Id: PredictionTrendAccuracy.java,v 1.1 2007/05/27 21:59:12 ingomierswa Exp $
 */
public class PredictionTrendAccuracy extends MeasuredPerformance {

    private static final long serialVersionUID = 4275593122138248581L;

	private int length = 1;
    
    private int correctCounter = 0;
    
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

    public void startCounting(ExampleSet eSet) throws OperatorException {
        super.startCounting(eSet);
        if (eSet.getAttributes().size() > 0) {
        	// get last attribute
        	Attribute attribute = null;
        	for (Attribute current : eSet.getAttributes()) {
        		attribute = current;
        	}
            
            for (Example example : eSet) {
                double currentLabel = example.getLabel();
                double currentPrediction = example.getPredictedLabel();
                double lastValueBefore = example.getValue(attribute);
            
                double actualTrend = currentLabel - lastValueBefore;
                double predictionTrend = currentPrediction - lastValueBefore;
                if (actualTrend * predictionTrend >= 0)
                    correctCounter++;
                length++;
            }
        }
    }

    public int getExampleCount() {
        return 1;
    }

    public void countExample(Example example) {}

    public double getFitness() {
        return getAverage();
    }

    public double getMikroAverage() {
        return correctCounter / (double)length;
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
