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
package com.rapidminer.operator.learner.tree;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Tools;

/**
 * Build a tree from an example set.
 *
 * @author Ingo Mierswa
 * @version $Id: TreeBuilder.java,v 1.12 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class TreeBuilder {

	private Terminator minSizeTerminator;

	private List<Terminator> otherTerminators;

	private Criterion criterion;

	private NumericalSplitter splitter;

	private SplitPreprocessing preprocessing = null;

	private Pruner pruner;

	private LeafCreator leafCreator = new DecisionTreeLeafCreator();

	private double minimalGain = 0.02;

	public TreeBuilder(Criterion criterion, 
			List<Terminator> terminationCriteria, 
			Pruner pruner,
			SplitPreprocessing preprocessing,
			int minLeafSize, 
			double minimalGain) {
		this(criterion, terminationCriteria, pruner, preprocessing, minLeafSize, minimalGain, -1);
	}

	public TreeBuilder(Criterion criterion, 
			List<Terminator> terminationCriteria, 
			Pruner pruner,
			SplitPreprocessing preprocessing,
			LeafCreator lcreator,
			int minLeafSize, 
			double minimalGain) {
		this(criterion, terminationCriteria, pruner, preprocessing, lcreator, minLeafSize, minimalGain, -1);
	}

	
	public TreeBuilder(Criterion criterion, 
			List<Terminator> terminationCriteria, 
			Pruner pruner,
			SplitPreprocessing preprocessing,
			int minLeafSize, 
			double minimalGain, 
			int numericalSampleSize) {
		this.minSizeTerminator = new MinSizeTermination(minLeafSize);
		this.otherTerminators = terminationCriteria;
		this.otherTerminators.add(this.minSizeTerminator);
		this.criterion = criterion;
		this.splitter = new NumericalSplitter(this.criterion);
		this.pruner = pruner;
		this.preprocessing = preprocessing;
		this.minimalGain = minimalGain;
	}


	public TreeBuilder(Criterion criterion, 
			List<Terminator> terminationCriteria, 
			Pruner pruner,
			SplitPreprocessing preprocessing,
			LeafCreator leafCreator,
			int minLeafSize, 
			double minimalGain, 
			int numericalSampleSize) {
		
		this( criterion, terminationCriteria, pruner, preprocessing, minLeafSize, minimalGain, numericalSampleSize );
		this.leafCreator = leafCreator;
	}



	public Tree learnTree(ExampleSet exampleSet) throws OperatorException {
		// grow tree
		Tree root = new Tree((ExampleSet)exampleSet.clone());
		if (shouldStop(exampleSet, 0)) {
			leafCreator.changeTreeToLeaf(root, exampleSet);
		} else {
			buildTree(root, exampleSet, 1);
		}

		// prune
		if (pruner != null)
			pruner.prune(root);

		return root;
	}

	/** This method calculates the benefit of the given attribute. This implementation
	 *  utilizes the defined {@link Criterion}. Subclasses might want to override this
	 *  method in order to calculate the benefit in other ways. */
	protected Benefit calculateBenefit(ExampleSet trainingSet, Attribute attribute) throws OperatorException {
		SplittedExampleSet splitted = null;
		double splitValue = Double.NaN;
		if (attribute.isNominal()) {
			// nominal attribute
			splitted = SplittedExampleSet.splitByAttribute(trainingSet, attribute);
		} else {
			// numerical attribute
			splitValue = splitter.getBestSplit(trainingSet, attribute);
			if (!Double.isNaN(splitValue))
				splitted = SplittedExampleSet.splitByAttribute(trainingSet, attribute, splitValue);
		}

		if (splitted != null) {
			return new Benefit(criterion.getBenefit(splitted), attribute, splitValue);
		} else {
			return null;
		}
	}

	private boolean shouldStop(ExampleSet exampleSet, int depth) {
		for(Terminator terminator : otherTerminators) {
			if (terminator.shouldStop(exampleSet, depth))
				return true;
		}
		return false;
	}

	private void buildTree(Tree current, ExampleSet exampleSet, int depth) throws OperatorException {
		// terminate (beginning of recursive method!)
		if (shouldStop(exampleSet, depth)) {
			leafCreator.changeTreeToLeaf(current, exampleSet);
			return;
		}

		// preprocessing
		if (preprocessing != null) {
			exampleSet = preprocessing.preprocess(exampleSet);
		}

		// determine best attribute (and best split for numerical attributes)
		ExampleSet trainingSet = (ExampleSet)exampleSet.clone();
		Benefit bestBenefit = null;
		for (Attribute attribute : trainingSet.getAttributes()) {
			Benefit currentBenefit = calculateBenefit(trainingSet, attribute);
			if (currentBenefit != null) {
				if ((bestBenefit == null) || (currentBenefit.getBenefit() > bestBenefit.getBenefit())) {
					bestBenefit = currentBenefit;
				}
			}
		}

		// make a leaf and return if no best attribute was found
		if ((bestBenefit == null) || (Tools.isLessEqual(bestBenefit.getBenefit(), this.minimalGain))) {
			leafCreator.changeTreeToLeaf(current, trainingSet);
			return;
		}

		// split by best attribute
		SplittedExampleSet splitted = null;
		Attribute bestAttribute = bestBenefit.getAttribute();
		double bestSplitValue = bestBenefit.getSplitValue();
		if (bestAttribute.isNominal()) {
			splitted = SplittedExampleSet.splitByAttribute(trainingSet, bestAttribute);
		} else {
			splitted = SplittedExampleSet.splitByAttribute(trainingSet, bestAttribute, bestSplitValue);
		}

		// check if children all have the minimum size
		boolean splitOK = true;
		for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
			splitted.selectSingleSubset(i);
			if ((splitted.size()) > 0 && (minSizeTerminator.shouldStop(splitted, depth))) {
				splitOK = false;
				break;
			}
		}
		// if all have minimum size --> remove nominal attribute and recursive call for each subset
		if (splitOK) {
			if (bestAttribute.isNominal()) {
				splitted.getAttributes().remove(bestAttribute);
			}
			for (int i = 0; i < splitted.getNumberOfSubsets(); i++) {
				splitted.selectSingleSubset(i);
				if (splitted.size() > 0) {
					Tree child = new Tree((ExampleSet)splitted.clone());
					SplitCondition condition = null;
					if (bestAttribute.isNominal()) {
						condition = new NominalSplitCondition(bestAttribute, splitted.getExample(0).getValueAsString(bestAttribute));
					} else {
						if (i == 0) {
							condition = new LessEqualsSplitCondition(bestAttribute, bestSplitValue);
						} else {
							condition = new GreaterSplitCondition(bestAttribute, bestSplitValue);
						}
					}
					current.addChild(child, condition);
					buildTree(child, splitted, depth + 1);
				}
			}
		} else {
			// min size not fulfilled: transform to leaf
			leafCreator.changeTreeToLeaf(current, trainingSet);
		}
	}
}
