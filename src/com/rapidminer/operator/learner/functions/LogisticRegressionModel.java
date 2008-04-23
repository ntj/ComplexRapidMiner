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
package com.rapidminer.operator.learner.functions;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.SimpleBinaryPredictionModel;
import com.rapidminer.tools.Tools;


/**
 * The model determined by the {@link LogisticRegression} operator.
 * 
 * @author Ingo Mierswa
 * @version $Id: LogisticRegressionModel.java,v 1.4 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class LogisticRegressionModel extends SimpleBinaryPredictionModel {
    
    private static final long serialVersionUID = -966943348790852574L;
    
    private double[] beta = null;

    private String[] attributeNames;
    
    public LogisticRegressionModel(ExampleSet exampleSet, double[] beta) {
        super(exampleSet, 0.5d);
        this.attributeNames = com.rapidminer.example.Tools.getRegularAttributeNames(exampleSet);
        this.beta = beta;
    }
    
    public double predict(Example example) {
        double e = 0.0d;
        int i = 0;
        for (Attribute attribute : example.getAttributes()) {
            double value = example.getValue(attribute);
            e += beta[i] * value;
            i++;
        }
        e += beta[beta.length - 1];
        return Math.exp(e) / (1 + Math.exp(e));
    }
    
    public String toString() {
    	StringBuffer result = new StringBuffer();
    	result.append("Bias (offset): " + Tools.formatNumber(beta[beta.length - 1]) + Tools.getLineSeparators(2));
    	result.append("Coefficients:" + Tools.getLineSeparator());
		for (int j = 0; j < beta.length - 1; j++) {
			result.append("beta(" + attributeNames[j] + ") = " + Tools.formatNumber(beta[j]) + Tools.getLineSeparator());
		}
    	result.append(Tools.getLineSeparator() + "Odds Ratios:" + Tools.getLineSeparator());
		for (int j = 0; j < beta.length - 1; j++) {
			result.append("odds_ratio(" + attributeNames[j] + ") = " + Tools.formatNumber(Math.exp(beta[j])) + Tools.getLineSeparator());
		}
    	return result.toString();
    }
}
