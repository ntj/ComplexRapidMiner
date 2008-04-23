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
package com.rapidminer.operator.learner.associations;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import com.rapidminer.operator.IOObject;
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
 * @version $Id: AssociationRuleGenerator.java,v 1.3 2007/06/22 15:31:44 ingomierswa Exp $
 */
public class AssociationRuleGenerator extends Operator {

	public static final String PARAMETER_MIN_CONFIDENCE = "min_confidence";
	
	public AssociationRuleGenerator(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		double minConfidence = getParameterAsDouble(PARAMETER_MIN_CONFIDENCE);
		
		FrequentItemSets sets = getInput(FrequentItemSets.class);
		AssociationRules rules = new AssociationRules();
		HashMap<Collection<Item>, Integer> setFrequencyMap = new HashMap<Collection<Item>, Integer>();
		
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
						int preconditionFrequency = setFrequencyMap.get(conclusion);
						double confidence = getConfidence(totalFrequency, preconditionFrequency);
						if (confidence >= minConfidence) {
							rules.addItemRule(new AssociationRule(premises, 
								                                  conclusion, 
								                                  confidence, 
								                                  getTotalSupport(totalFrequency, sets.getNumberOfTransactions())));
						}
					}
				}
			}
		}
		return new IOObject[] {rules};
	}
	
	private double getConfidence(int totalFrequency, int preconditionFrequency) {
		return (double)totalFrequency / (double)preconditionFrequency;
	}
	
	private double getTotalSupport(int totalFrequency, int completeSize) {
		return (double)totalFrequency / (double)completeSize;
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
		return types;
	}
}
