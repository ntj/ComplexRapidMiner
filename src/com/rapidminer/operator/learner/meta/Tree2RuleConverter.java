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
package com.rapidminer.operator.learner.meta;

import java.util.Iterator;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ContainerModel;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.learner.rules.Rule;
import com.rapidminer.operator.learner.rules.RuleModel;
import com.rapidminer.operator.learner.tree.Edge;
import com.rapidminer.operator.learner.tree.SplitCondition;
import com.rapidminer.operator.learner.tree.Tree;
import com.rapidminer.operator.learner.tree.TreeModel;

/**
 * This meta learner uses an inner tree learner and creates a rule model
 * from the learned decision tree.
 *
 * @author Ingo Mierswa
 * @version $Id: Tree2RuleConverter.java,v 1.2 2007/07/13 22:52:11 ingomierswa Exp $
 */
public class Tree2RuleConverter extends AbstractMetaLearner {

	public Tree2RuleConverter(OperatorDescription description) {
		super(description);
	}

	public Model learn(ExampleSet exampleSet) throws OperatorException {
		Model innerModel = applyInnerLearner(exampleSet);
		
		TreeModel treeModel = null;
		ContainerModel containerModel = null;
		if (innerModel instanceof ContainerModel) {
			containerModel = (ContainerModel)innerModel;
			treeModel = containerModel.getModel(TreeModel.class);
			containerModel.removeModel(treeModel);
		}
		
		if (treeModel == null) {
			if (innerModel instanceof TreeModel) {
				treeModel = (TreeModel)innerModel;
			}
		}
		
		if (treeModel == null) {
			throw new UserError(this, 127, "one of the inner learners must produce a tree model.");			
		}
		
		Tree tree = treeModel.getRoot();
		RuleModel ruleModel = new RuleModel(exampleSet);
		
		addRules(ruleModel, new Rule(), tree);
		
		if (containerModel != null) {
			containerModel.addModel(ruleModel);
			return containerModel;
		} else {
			return ruleModel;
		}
	}

	private void addRules(RuleModel ruleModel, Rule currentRule, Tree tree) {
		if (tree.isLeaf()) {
			currentRule.setLabel(tree.getLabel());
			int[] frequencies = new int[ruleModel.getLabel().getMapping().size()];
			int index = 0;
			for (String labelValue : ruleModel.getLabel().getMapping().getValues()) {
				frequencies[index++] = tree.getCount(labelValue);
			}
			currentRule.setFrequencies(frequencies);
			ruleModel.addRule(currentRule);
		} else {
			Iterator<Edge> e = tree.childIterator();
			while (e.hasNext()) {
				Edge edge = e.next();
				SplitCondition condition = edge.getCondition();
				Tree child = edge.getChild();
				Rule clonedRule = (Rule)currentRule.clone();
				clonedRule.addTerm(condition);
				addRules(ruleModel, clonedRule, child);
			}
		}
	}
}
