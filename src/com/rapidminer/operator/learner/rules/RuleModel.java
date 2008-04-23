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
package com.rapidminer.operator.learner.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.learner.SimplePredictionModel;
import com.rapidminer.tools.Tools;

/**
 * The basic rule model.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: RuleModel.java,v 1.2 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class RuleModel extends SimplePredictionModel {

	private static final long serialVersionUID = 7792658268037025366L;

	private List<Rule> rules = new ArrayList<Rule>();

	public RuleModel(ExampleSet exampleSet) {
		super(exampleSet);
	}

	public double predict(Example example) {
        for (Rule rule : rules) {
            if (rule.coversExample(example)) {
                double[] confidences = rule.getConfidences();
                for (int index = 0; index < confidences.length; index++) {
                    example.setConfidence(getLabel().getMapping().mapIndex(index), confidences[index]);
                }
                return getLabel().getMapping().getIndex(rule.getLabel());
            }
        }
        return (Double.NaN); // return unkown if no rule exists
	}
    
    public double getPrediction(Example example) {
        for (Rule rule : rules) {
            if (rule.coversExample(example)) {
                double label = getLabel().getMapping().getIndex(rule.getLabel());
                return (label);
            }
        }
        return (Double.NaN); // return unkown if no rule exists
    }

	public void addRule(Rule rule) {
		this.rules.add(rule);
	}

	public void addRules(Collection<Rule> newRules) {
		this.rules.addAll(newRules);
	}

	public List<Rule> getRules() {
		return this.rules;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		int correct = 0;
        int wrong = 0;
        for (Rule rule : rules) {
			buffer.append(rule.toString());
			buffer.append(Tools.getLineSeparator());
            int label = getLabel().getMapping().getIndex(rule.getLabel());
            int[] frequencies = rule.getFrequencies();
            if (frequencies != null) {
                for (int i = 0; i < frequencies.length; i++) {
                    if (i == label) {
                        correct += frequencies[i];
                    } else {
                        wrong += frequencies[i];
                    }
                }
            }
		}
        buffer.append(Tools.getLineSeparator());
        buffer.append("correct: " + correct + " out of " + (correct + wrong) + " training examples.");
		return buffer.toString();
	}
}
