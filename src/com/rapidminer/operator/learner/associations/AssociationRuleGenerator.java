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
package com.rapidminer.operator.learner.associations;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.associations.fpgrowth.FPGrowth;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;

/**
 * <p>This operator generates association rules from frequent item sets. 
 * In RapidMiner, the process of frequent item set mining is divided
 * into two parts: first, the generation of frequent item sets and
 * second, the generation of association rules from these sets.</p>
 * 
 * <p>For the generation of frequent item sets, you can use for example
 * the operator {@link FPGrowth}. The result will be a set of frequent item
 * sets which could be used as input for this operator.</p>
 *  
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: AssociationRuleGenerator.java,v 1.8 2008/05/09 19:23:21 ingomierswa Exp $
 */
public class AssociationRuleGenerator extends Operator {

	public static final String PARAMETER_MIN_CONFIDENCE = "min_confidence";
	private static final String PARAMETER_GAIN_THETA = "gain_theta";
	private static final String PARAMETER_LAPLACE_K = "laplace_k";
	
	public AssociationRuleGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		double minConfidence = getParameterAsDouble(PARAMETER_MIN_CONFIDENCE);
		double theta = getParameterAsDouble(PARAMETER_GAIN_THETA);
		double laPlaceK = getParameterAsDouble(PARAMETER_LAPLACE_K);
		FrequentItemSets sets = getInput(FrequentItemSets.class);
		AssociationRules rules = new AssociationRules();
		HashMap<Collection<Item>, Integer> setFrequencyMap = new HashMap<Collection<Item>, Integer>();
		int numberOfTransactions = sets.getNumberOfTransactions();
		
		// iterating sorted over every frequent Set, generating every possible rule and building frequency map
		sets.sortSets();
		for (FrequentItemSet set: sets) {
			setFrequencyMap.put(set.getItems(), set.getFrequency());
			// generating rule by splitting set in every two parts for head and body of rule 
			if (set.getItems().size() > 1) {
				PowerSet<Item> powerSet = new PowerSet<Item>(set.getItems());
				for (Collection<Item> premises: powerSet) {
					if (premises.size() > 0 && premises.size() < set.getItems().size()) {
						Collection<Item> conclusion = powerSet.getComplement(premises);
						int totalFrequency = set.getFrequency();
						int preconditionFrequency = setFrequencyMap.get(premises);
						int conclusionFrequency = setFrequencyMap.get(conclusion);
						double confidence = getConfidence(totalFrequency, preconditionFrequency);
						if (confidence >= minConfidence) {
							AssociationRule rule = new AssociationRule(premises, 
	                                  conclusion, 
	                                  confidence, 
	                                  getSupport(totalFrequency, numberOfTransactions));
							rule.setLift(getLift(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setConviction(getConviction(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setPs(getPs(totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setGain(getGain(theta, totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rule.setLaplace(getLaPlace(laPlaceK, totalFrequency, preconditionFrequency, conclusionFrequency, numberOfTransactions));
							rules.addItemRule(rule);
						}
					}
				}
			}
		}
		return new IOObject[] {rules};
	}
	private double getGain(double theta, int totalFrequency, int preconditionFrequency, int conclusionFrequency, int numberOfTransactions) {
		return getSupport(totalFrequency, numberOfTransactions) - theta * getSupport(preconditionFrequency, numberOfTransactions);
	}
	
	private double getLift(int totalFrequency, int preconditionFrequency, int conclusionFrequency, int numberOfTransactions) {
		return ((double) totalFrequency * ((double) numberOfTransactions)) / ((double)preconditionFrequency * conclusionFrequency);
	}
	private double getPs(int totalFrequency, int preconditionFrequency, int conclusionFrequency, int numberOfTransactions) {
		return getSupport(totalFrequency, numberOfTransactions) - getSupport(preconditionFrequency, numberOfTransactions) * getSupport(conclusionFrequency, numberOfTransactions);
	}
	private double getLaPlace(double k, int totalFrequency, int preconditionFrequency, int conclusionFrequency, int numberOfTransactions) {
		return (getSupport(totalFrequency, numberOfTransactions) + 1d) / (getSupport(preconditionFrequency, numberOfTransactions) + k);
	}
	
		
	private double getConviction(int totalFrequency, int preconditionFrequency, int conclusionFrequency, int numberOfTransactions) {
		double numerator = preconditionFrequency * (numberOfTransactions - conclusionFrequency);
		double denumerator = numberOfTransactions * (preconditionFrequency - totalFrequency);
		return numerator / denumerator;
	}
	
	/** Indicates that the consumption of frequent item sets can be user defined. */
	public InputDescription getInputDescription(Class cls) {
		if (FrequentItemSets.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}
	
	private double getConfidence(int totalFrequency, int preconditionFrequency) {
		return (double)totalFrequency / (double)preconditionFrequency;
	}
	
	private double getSupport(int frequency, int completeSize) {
		return (double)frequency / (double)completeSize;
	}

	public Class[] getInputClasses() {
		return new Class[] { FrequentItemSets.class	};
	}

	public Class[] getOutputClasses() {
		return new Class[] {FrequentItemSets.class, AssociationRules.class};
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_CONFIDENCE, "The minimum confidence of the rules", 0.0d, 1.0d, 0.8d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_GAIN_THETA, "The Parameter Theta in Gain calculation", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 2d);
		type.setExpert(true);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_LAPLACE_K, "The Parameter k in LaPlace function calculation", 1, Double.POSITIVE_INFINITY, 1d);
		type.setExpert(true);
		types.add(type);

		return types;
	}
}
