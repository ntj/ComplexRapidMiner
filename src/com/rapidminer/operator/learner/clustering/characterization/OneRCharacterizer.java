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
package com.rapidminer.operator.learner.clustering.characterization;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.rules.Rule;
import com.rapidminer.operator.learner.rules.RuleModel;
import com.rapidminer.operator.learner.rules.SingleRuleLearner;
import com.rapidminer.operator.learner.tree.SplitCondition;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.Tools;


/**
 * Characterizes clusters with learned OneR classifiers.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: OneRCharacterizer.java,v 1.4 2007/07/01 01:33:52 ingomierswa Exp $
 */
public class OneRCharacterizer extends AbstractModelBasedCharacterizer {

	public Model trainModel(ExampleSet es) {
		SingleRuleLearner learner = null;
		try {
			learner = OperatorService.createOperator(SingleRuleLearner.class);
		} catch (OperatorCreationException e1) {
			LogService.getGlobal().logError("Could not create operator: " + e1.getMessage());
		}
		Model result = null;
		if (learner != null) {
			try {
				result = learner.learn(es);
			} catch (OperatorException e) {
				LogService.getGlobal().logError("Could not learn cluster characterization: " + e.getMessage());
			}
		}
		return result;
	}

	public String stringRepresentation(Model m, String desiredLabel) {
		if (m == null) {
			return "no model characterization available";
		} else {
			StringBuffer result = new StringBuffer();
			List<Rule> rules = ((RuleModel)m).getRules();
			boolean first = true;
			for (Rule rule : rules) {
				if (rule.getLabel().equals(desiredLabel)) {
					List<SplitCondition> conditions = rule.getTerms();
					for (SplitCondition condition : conditions) {
						if (!first)
							result.append(", ");
						result.append(Tools.escapeXML(condition.toString()));
						first = false;
					}
				}
			}
			String resultString = result.toString();
			if (resultString.trim().length() == 0)
				resultString = "none";
			return resultString;
		}
	}
}
