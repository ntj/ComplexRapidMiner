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
package com.rapidminer.operator.learner.associations.fpgrowth;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ExampleSource;
import com.rapidminer.operator.learner.associations.BooleanAttributeItem;
import com.rapidminer.operator.learner.associations.FrequentItemSet;
import com.rapidminer.operator.learner.associations.FrequentItemSets;
import com.rapidminer.operator.learner.associations.Item;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;


/**
 * <p>This operator calculates all frequent items sets from a data set by building 
 * a FPTree data structure on the transaction data base. This is a very compressed
 * copy of the data which in many cases fits into main memory even for large
 * data bases. From this FPTree all frequent item set are derived. A major advantage
 * of FPGrowth compared to Apriori is that it uses only 2 data scans and is therefore
 * often applicable even on large data sets.</p>
 * 
 *  <p>Please note that the given data set is only allowed to contain binominal attributes,
 *  i.e. nominal attributes with only two different values. Simply use the provided
 *  preprocessing operators in order to transform your data set to fit this condition.
 *  The frequent item sets are mined for the positive entries in your data base,
 *  i.e. for those nominal values which are defined as positive in your data base.
 *  If you use an attribute description file (.aml) for the {@link ExampleSource} operator
 *  this corresponds to the second value which is defined via the classes attribute or inner
 *  value tags.</p>
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: FPGrowth.java,v 1.4 2007/06/24 12:05:49 ingomierswa Exp $
 */
public class FPGrowth extends Operator {

	/** The parameter name for &quot;Minimal Support&quot; */
	public static final String PARAMETER_MIN_SUPPORT = "min_support";
	
	private int minTotalSupport;

	private FrequentItemSets rules;

	public FPGrowth(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		
		// check
		Tools.onlyNominalAttributes(exampleSet, "FPGrowth");
		
		double minSupport = getParameterAsDouble(PARAMETER_MIN_SUPPORT);
		this.minTotalSupport = (int) (minSupport * exampleSet.size());
		
		// precomputing data properties
		ExampleSet workingSet = (ExampleSet)exampleSet.clone();
		
		// remove unusuable attributes
		int oldAttributeCount = workingSet.getAttributes().size();
		removeNonBooleanAttribute(workingSet);
		int newAttributeCount = workingSet.getAttributes().size();
		if (oldAttributeCount != newAttributeCount) {
			int removeCount = oldAttributeCount - newAttributeCount;
			String message = null;
			if (removeCount == 1)
				message = "Removed 1 non-binominal attribute, frequent item set mining is only supported for the positive values of binominal attributes.";
			else
				message = "Removed " + removeCount + " non-binominal attributes, frequent item set mining is only supported for the positive values of binominal attributes.";
			logWarning(message);
		}
		
		// map attributes to items
		Map<Attribute, Item> mapping = getAttributeMapping(workingSet);
		// computing frequency of 1-Item Sets
		getItemFrequency(workingSet, mapping);
		// eliminating non frequent items
		removeNonFrequentItems(mapping, minTotalSupport, workingSet);
		
		// generating FP Tree
		FPTree tree = getFPTree(workingSet, mapping);
		
		// mine tree
		rules = new FrequentItemSets(workingSet.size());
		mineTree(tree, 0);
		return new IOObject[] { rules };
	}

	public void mineTree(FPTree tree, int recursionDepth) {
		mineTree(tree, recursionDepth, new FrequentItemSet());
	}

	public void mineTree(FPTree tree, int recursionDepth, FrequentItemSet conditionalItems) {
		if (!(treeIsEmpty(tree, recursionDepth))) {
			// recursivly mine tree
			Map<Item, Header> headerTable = tree.getHeaderTable();
			Iterator<Map.Entry<Item, Header>> headerIterator = headerTable.entrySet().iterator();
			while (headerIterator.hasNext()) {
				Map.Entry<Item, Header> headerEntry = headerIterator.next();
				Item item = headerEntry.getKey();
				Header itemHeader = headerEntry.getValue();
				// check for minSupport
				int itemSupport = itemHeader.getFrequencies().getFrequency(recursionDepth);
				if (itemSupport >= minTotalSupport) {
					// run over sibling chain
					for (FPTreeNode node : itemHeader.getSiblingChain()) {
						// and propagate frequency to root
						int frequency = node.getFrequency(recursionDepth);
						// if frequency is positiv
						if (frequency > 0) {
							FPTreeNode currentNode = node.getFather();
							while (currentNode != tree) {
								// increase node frequency
								currentNode.increaseFrequency(recursionDepth + 1, frequency);
								// increase item frequency in headerTable
								headerTable.get(currentNode.getNodeItem()).getFrequencies().increaseFrequency(recursionDepth + 1, frequency);
								// go up in tree
								currentNode = currentNode.getFather();
							}
						}
					}
					FrequentItemSet recursivConditionalItems = (FrequentItemSet) conditionalItems.clone();
					// add item to conditional items
					recursivConditionalItems.addItem(item, itemSupport);
					// add this conditional items to frequentSets
					rules.addFrequentSet(recursivConditionalItems);
					// recursivly mine new tree
					mineTree(tree, recursionDepth + 1, recursivConditionalItems);
					// run over sibling chain for poping frequency stack
					for (FPTreeNode node : itemHeader.getSiblingChain()) {
						// and remove propagation of frequency
						FPTreeNode currentNode = node.getFather();
						while (currentNode != tree) {
							// pop frequency
							currentNode.popFrequency(recursionDepth + 1);
							// go up in tree
							currentNode = currentNode.getFather();
						}
					}
					// pop frequencies of every header table on current recursion depth
					for (Header currentItemHeader : headerTable.values()) {
						currentItemHeader.getFrequencies().popFrequency(recursionDepth + 1);
					}
				}
			}
		}
	}

	/** Removes every non boolean attribute.
	 *  @param exampleSet exampleSet, which attributes are tested
	 */
	private void removeNonBooleanAttribute(ExampleSet exampleSet) {
		// removing non boolean attributes
		Collection<Attribute> deleteAttributes = new ArrayList<Attribute>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.getMapping().size() != 2) {
				deleteAttributes.add(attribute);
			}
		}
		for (Attribute attribute : deleteAttributes) {
			exampleSet.getAttributes().remove(attribute);
		}
	}

	/**
	 * This method maps the attributes of the given exampleSet to an Item.
	 * @param exampleSet the exampleSet which attributes are mapped
	 */
	private Map<Attribute, Item> getAttributeMapping(ExampleSet exampleSet) {
		// computing Attributes to test, because only boolean attributes are used
		Map<Attribute, Item> mapping = new HashMap<Attribute, Item>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			mapping.put(attribute, new BooleanAttributeItem(attribute));
		}
		return mapping;
	}

	/**
	 * This method scans the exampleSet and counts the frequency of every item
	 * 
	 * @param exampleSet
	 *            the exampleSet to be scaned
	 * @param mapping
	 *            the mapping of attributes to items
	 */
	private void getItemFrequency(ExampleSet exampleSet, Map<Attribute, Item> mapping) {
		// iterate over exampleSet, counting item frequency
		Attributes attributes = exampleSet.getAttributes();
		for (Example currentExample : exampleSet) {
			for (Attribute attribute : attributes) {
				// if attribute is boolean and if attribute is the positive one --> increase frequency of item
				if (currentExample.getValue(attribute) == attribute.getMapping().getPositiveIndex()) {
					mapping.get(attribute).increaseFrequency();
				}
			}
		}
	}

	private void removeNonFrequentItems(Map<Attribute, Item> mapping, int minFrequency, ExampleSet exampleSet) {
		Collection<Attribute> deleteMappings = new ArrayList<Attribute>();
		Iterator<Map.Entry<Attribute, Item>> it = mapping.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Attribute, Item> entry = it.next();
			if (entry.getValue().getFrequency() < minFrequency) {
				deleteMappings.add(entry.getKey());
			}
		}
		for (Attribute attribute : deleteMappings) {
			exampleSet.getAttributes().remove(attribute);
		}
	}

	/**
	 * Returns a new FPTree, representing the complete ExampleSet.
	 * 
	 * @param exampleSet
	 *            is the exampleSet, which shall be represented
	 * @param mapping
	 *            is the mapping of attributes of the exampleSet to items
	 */
	private FPTree getFPTree(ExampleSet exampleSet, Map<Attribute, Item> mapping) {
		FPTree tree = new FPTree();
		for (Example currentExample : exampleSet) {
			List<Item> itemSet = new ArrayList<Item>();
			for (Attribute currentAttribute : exampleSet.getAttributes()) {
				if (currentExample.getValue(currentAttribute) == currentAttribute.getMapping().getPositiveIndex()) {
					itemSet.add(mapping.get(currentAttribute));
				}
			}
			Collections.sort(itemSet);
			tree.addItemSet(itemSet, 1);
		}
		return tree;
	}

	private boolean treeIsEmpty(FPTree tree, int recursionDepth) {
		// tree is empty if every child of rootnode has frequency of 0 on top of stack
		for (FPTreeNode node : tree.getChildren().values()) {
			if (node.getFrequency(recursionDepth) > 0) {
				return false;
			}
		}
		return true;
	}

	public Class[] getInputClasses() {
		return new Class[] {
			ExampleSet.class
		};
	}

	public Class[] getOutputClasses() {
		return new Class[] {
			FrequentItemSets.class
		};
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_MIN_SUPPORT, "The minimal support necessary in order to be a frequent item (set).", 0.0d, 1.0d, 0.5d);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
