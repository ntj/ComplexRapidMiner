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
package com.rapidminer.operator.learner.clustering.constrained.constraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

/**
 * Creates a ClusterConstraintList of the specified type from a (possibly partially) labeled ExampleSet. For the type
 * 'link' you can choose, if you want the LinkClusterConstraints to be created randomly or orderly, always bounded by
 * the maximal number of constraints to create. Choosing 'random walk' the Must-Link-constraints for each label will
 * form a connected component.
 * 
 * @author Alexander Daxenberger
 * @version $Id: ExampleSet2ClusterConstraintList.java,v 1.7 2008/05/09 19:23:17 ingomierswa Exp $
 */
public class ExampleSet2ClusterConstraintList extends Operator {

	private RandomGenerator randomGenerator;
	
	private int maxmust;

	private int maxcannot;

	private double weight;

	public static final String[] TYPES = { "link" };

	public static final int TYPE_LINK = 0;

	public static final String[] MODE_LINK = { "random", "random walk", "orderly" };

	public static final int MODE_LINK_RND = 0;

	public static final int MODE_LINK_RND_WALK = 1;

	public static final int MODE_LINK_ORDERLY = 2;
	
	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";

	public ExampleSet2ClusterConstraintList(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ClusterConstraintList.class };
	}

	public InputDescription getInputDescription(Class cls) {
		if (ExampleSet.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, true, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		types.add(new ParameterTypeCategory("type", "the type of constraints to create", ExampleSet2ClusterConstraintList.TYPES, 0));
		types.add(new ParameterTypeCategory("link_mode", "the policy how to choose link constraints", ExampleSet2ClusterConstraintList.MODE_LINK, 0));
		types.add(new ParameterTypeInt("link_max_must", "the maximal number of MUST_LINK constraints to create", 0, Integer.MAX_VALUE, 100));
		types.add(new ParameterTypeInt("link_max_cannot", "the maximal number of CANNOT_LINK constraints to create", 0, Integer.MAX_VALUE, 100));
		types.add(new ParameterTypeDouble("link_weight", "the global weight of the created link constraints", 0.0, Double.POSITIVE_INFINITY, 1.0));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));

		return types;
	}

	public IOObject[] apply() throws OperatorException {
		// initializing random generator
		int seed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED); 
		if (seed == -1 ) {
			randomGenerator = RandomGenerator.getGlobalRandomGenerator();
		} else {
			randomGenerator = RandomGenerator.getRandomGenerator(seed);
		}
		
		LinkClusterConstraintList result = null;
		HashMap<Double, Collection<Example>> labelMap;
		ExampleSet es = getInput(ExampleSet.class);

		// checking and creating attributes if needed
		Tools.isLabelled(es);
		Tools.checkAndCreateIds(es);

		maxmust = this.getParameterAsInt("link_max_must");
		maxcannot = this.getParameterAsInt("link_max_cannot");
		weight = this.getParameterAsDouble("link_weight");

		labelMap = getLabelExamplesMap(es);

		switch (this.getParameterAsInt("type")) {
		case TYPE_LINK:
			switch (this.getParameterAsInt("link_mode")) {
			case MODE_LINK_RND:
				result = this.getRandomLinkClusterConstraints(labelMap, es);
				break;
			case MODE_LINK_RND_WALK:
				result = this.getRandomWalkLinkClusterConstraints(labelMap, es);
				break;
			case MODE_LINK_ORDERLY:
				result = this.getOrderlyLinkClusterConstraints(labelMap, es);
				break;
			default:
				throw new OperatorException("Invalid constraint creation mode");
			}
			break;
		default:
			throw new OperatorException("Invalid constraint type");
		}

		return new IOObject[] { result };
	}

	/**
	 * Returns a list of LinkClusterConstraints chosen randomly
	 * 
	 * @param labelMap
	 *            a mapping label -> example
	 * @param es
	 *            an ExampleSet
	 * @return
	 * @throws OperatorException
	 */
	private LinkClusterConstraintList getRandomLinkClusterConstraints(HashMap<Double, Collection<Example>> labelMap, ExampleSet es) throws OperatorException {
		LinkClusterConstraintList constraintList;
		LinkClusterConstraint newConstraint;

		Attribute id = es.getAttributes().getId();
		Example e1;
		Example e2;
		boolean added = false;
		int tries = 0;

		constraintList = new LinkClusterConstraintList("link cluster constraint set", this.maxmust + this.maxcannot);
		ArrayList<Collection<Example>> exList = new ArrayList<Collection<Example>>(labelMap.values());

		while ((tries < 3) && ((this.maxmust > 0) || (this.maxcannot > 0))) {
			for (int list1 = 0; list1 < exList.size(); list1++) {
				for (int list2 = 0; list2 < exList.size(); list2++) {
					if ((list1 != list2) && (this.maxcannot > 0)) {
						e1 = getRandomExample(exList.get(list1));
						e2 = getRandomExample(exList.get(list2));
						while (e1 == e2) {
							e2 = getRandomExample(exList.get(list2));
						}
						newConstraint = new LinkClusterConstraint(e1.getValueAsString(id), e2.getValueAsString(id), weight,
								LinkClusterConstraint.CANNOT_LINK);
						if (constraintList.addConstraint(newConstraint)) {
							maxcannot--;
							added = true;
						}
					}
					if (this.maxmust > 0) {
						e1 = getRandomExample(exList.get(list2));
						e2 = getRandomExample(exList.get(list2));
						while (e1 == e2) {
							e2 = getRandomExample(exList.get(list2));
						}
						newConstraint = new LinkClusterConstraint(e1.getValueAsString(id), e2.getValueAsString(id), this.weight,
								LinkClusterConstraint.MUST_LINK);
						if (constraintList.addConstraint(newConstraint)) {
							this.maxmust--;
							added = true;
						}
					}
				}
			}

			if (!added) {
				tries++;
			}
			added = false;
		}

		return constraintList;
	}

	/**
	 * Returns a list of LinkClusterConstraints chosen randomly. The Must-Link- constraints will form a connected
	 * component
	 * 
	 * @param labelMap
	 *            a mapping label -> example
	 * @param es
	 *            an ExampleSet
	 * @return
	 * @throws OperatorException
	 */
	private LinkClusterConstraintList getRandomWalkLinkClusterConstraints(HashMap<Double, Collection<Example>> labelMap, ExampleSet es)
			throws OperatorException {
		LinkClusterConstraintList constraintList;
		LinkClusterConstraint newConstraint;
		Attribute id = es.getAttributes().getId();
		Example[] mustLinkLastExample;
		Example[][] cannotLinkLastExample;
		Example e1;
		Example e2;
		boolean added = false;
		int tries = 0;

		constraintList = new LinkClusterConstraintList("link cluster constraint set", this.maxmust + this.maxcannot);
		ArrayList<Collection<Example>> exList = new ArrayList<Collection<Example>>(labelMap.values());

		mustLinkLastExample = new Example[exList.size()];
		cannotLinkLastExample = new Example[exList.size()][exList.size()];

		for (int list1 = 0; list1 < exList.size(); list1++) {
			mustLinkLastExample[list1] = null;
			for (int list2 = 0; list2 < exList.size(); list2++) {
				cannotLinkLastExample[list1][list2] = null;
			}
		}

		while ((tries < 3) && ((this.maxmust > 0) || (this.maxcannot > 0))) {
			for (int list1 = 0; list1 < exList.size(); list1++) {
				for (int list2 = 0; list2 < exList.size(); list2++) {
					if ((list1 != list2) && (this.maxcannot > 0)) {
						if (list1 < list2) {
							if (cannotLinkLastExample[list1][list2] == null)
								e2 = this.getRandomExample(exList.get(list2));
							else
								e2 = cannotLinkLastExample[list1][list2];
							e1 = this.getRandomExample(exList.get(list1));
							while (e1 == e2)
								e1 = this.getRandomExample(exList.get(list1));
						} else {
							if (cannotLinkLastExample[list1][list2] == null)
								e1 = this.getRandomExample(exList.get(list1));
							else
								e1 = cannotLinkLastExample[list1][list2];
							e2 = this.getRandomExample(exList.get(list2));
							while (e1 == e2)
								e2 = this.getRandomExample(exList.get(list2));
						}
						newConstraint = new LinkClusterConstraint(e1.getValueAsString(id), e2.getValueAsString(id), this.weight,
								LinkClusterConstraint.CANNOT_LINK);
						if (constraintList.addConstraint(newConstraint)) {
							this.maxcannot--;
							added = true;
							if (list1 < list2)
								cannotLinkLastExample[list2][list1] = e1;
							else
								cannotLinkLastExample[list2][list1] = e2;
						}
					}
					if (this.maxmust > 0) {
						if (mustLinkLastExample[list2] == null)
							e1 = this.getRandomExample(exList.get(list2));
						else
							e1 = mustLinkLastExample[list2];
						e2 = this.getRandomExample(exList.get(list2));
						while (e1 == e2)
							e2 = this.getRandomExample(exList.get(list2));
						newConstraint = new LinkClusterConstraint(e1.getValueAsString(id), e2.getValueAsString(id), this.weight,
								LinkClusterConstraint.MUST_LINK);
						if (constraintList.addConstraint(newConstraint)) {
							this.maxmust--;
							added = true;
							mustLinkLastExample[list2] = e2;
						}
					}
				}
			}

			if (!added) {
				tries++;
			}
			added = false;

		}

		return constraintList;
	}

	/**
	 * Returns a list of LinkClusterConstraints chosen orderly
	 * 
	 * @param labelMap
	 *            a mapping label -> example
	 * @param es
	 *            an ExampleSet
	 * @return
	 * @throws OperatorException
	 */
	private LinkClusterConstraintList getOrderlyLinkClusterConstraints(HashMap<Double, Collection<Example>> labelMap, ExampleSet es) throws OperatorException {
		LinkClusterConstraintList constraintList;
		LinkClusterConstraint newConstraint;
		// Collection<Collection<Example>> exLists;
		Iterator[] mustLinkListIterator;
		Iterator[][] cannotLinkListIterator;
		Object[] example;
		Object examples;
		Attribute id = es.getAttributes().getId();
		boolean hasNext = true;

		constraintList = new LinkClusterConstraintList("link cluster constraint list", this.maxmust + this.maxcannot);
		ArrayList<Collection<Example>> exLists = new ArrayList<Collection<Example>>(labelMap.values());

		mustLinkListIterator = new Iterator[exLists.size()];
		cannotLinkListIterator = new Iterator[exLists.size()][exLists.size()];

		for (int list1 = 0; list1 < exLists.size(); list1++) {
			ArrayList[] newListArray = new ArrayList[2];
			newListArray[0] = (ArrayList) exLists.get(list1);
			newListArray[1] = (ArrayList) exLists.get(list1);
			mustLinkListIterator[list1] = new ListIteratorsIterator(newListArray, false);
			for (int list2 = 0; list2 < exLists.size(); list2++) {
				if (list1 != list2) {
					newListArray = new ArrayList[2];
					newListArray[0] = (ArrayList) exLists.get(list1);
					newListArray[1] = (ArrayList) exLists.get(list2);
					cannotLinkListIterator[list1][list2] = new ListIteratorsIterator(newListArray, true);
				} else
					cannotLinkListIterator[list1][list2] = null;
			}
		}

		while (hasNext && ((this.maxmust > 0) || (this.maxcannot > 0))) {

			hasNext = false;

			for (int list1 = 0; list1 < exLists.size(); list1++) {
				for (int list2 = 0; list2 < exLists.size(); list2++) {
					if ((list1 != list2) && (this.maxcannot > 0) && cannotLinkListIterator[list1][list2].hasNext()) {
						examples = cannotLinkListIterator[list1][list2].next();
						example = (Object[]) examples;
						newConstraint = new LinkClusterConstraint(((Example) example[0]).getValueAsString(id), ((Example) example[1])
								.getValueAsString(id), this.weight, LinkClusterConstraint.CANNOT_LINK);
						if (constraintList.addConstraint(newConstraint)) {
							this.maxcannot--;
						}
						hasNext = true;
					}

					if ((this.maxmust > 0) && mustLinkListIterator[list2].hasNext()) {
						examples = mustLinkListIterator[list2].next();
						example = (Object[]) examples;
						newConstraint = new LinkClusterConstraint(((Example) example[0]).getValueAsString(id), ((Example) example[1])
								.getValueAsString(id), this.weight, LinkClusterConstraint.MUST_LINK);
						if (constraintList.addConstraint(newConstraint)) {
							this.maxmust--;
						}
						hasNext = true;
					}
				}
			}
		}

		return constraintList;
	}

	/* just for testing purposes */
	public Iterator getListIteratorsIterator(List[] list, boolean equalAllowed) {
		return new ListIteratorsIterator(list, equalAllowed);
	}

	private Example getRandomExample(Collection<Example> exampleCollection) {
		int rnd = randomGenerator.nextIntInRange(0, exampleCollection.size() - 1);
		ArrayList<Example> examples = new ArrayList<Example>(exampleCollection);
		return examples.get(rnd);
	}

	/**
	 * this method returns a map, mapping the labels on collections of examples with this label.
	 * 
	 * @param es
	 *            the exampleSet
	 */
	private HashMap<Double, Collection<Example>> getLabelExamplesMap(ExampleSet es) {
		HashMap<Double, Collection<Example>> labelExampleMap = new HashMap<Double, Collection<Example>>();
		for (Example example : es) {
			double currentValue = example.getLabel();
			if (!Double.isNaN(currentValue)) {
				if (labelExampleMap.containsKey(currentValue)) {
					labelExampleMap.get(currentValue).add(example);
				} else {
					ArrayList<Example> newList = new ArrayList<Example>();
					newList.add(example);
					labelExampleMap.put(currentValue, newList);
				}
			}
		}
		return labelExampleMap;
	}

	/**
	 * Iterates over any combination of objects of several lists. Returns these combinations as array of objects.
	 * 
	 * @author Alexander Daxenberger
	 * 
	 */
	private static class ListIteratorsIterator implements Iterator {

		protected List[] list;

		protected Iterator[] iter;

		protected Object[] object;

		protected boolean allowEqual;

		protected boolean prepared;

		protected boolean endReached;

		public ListIteratorsIterator(List[] list, boolean allowEqual) {
			this.list = list;
			this.iter = new Iterator[list.length];
			this.object = new Object[list.length];
			this.allowEqual = allowEqual;
			for (int i = 0; i < list.length; i++) {
				if ((list[i] != null) && (list[i].size() > 0))
					this.iter[i] = list[i].iterator();
				else
					this.iter[i] = null;
				this.object[i] = null;
			}
			this.endReached = !this.init();
			this.prepared = true;
		}

		public boolean hasNext() {
			if (this.endReached)
				return false;
			else {
				if (!this.prepared)
					this.prepareNext();
				this.prepared = true;
				return !this.endReached;
			}
		}

		public Object next() {
			if (this.endReached)
				return null;
			else {
				if (!this.prepared)
					this.prepareNext();
				this.prepared = false;
				if (this.endReached)
					return null;
				else
					return this.object;
			}
		}

		public void remove() {
		}

		private void prepareNext() {
			int i = 0;

			while (i > -1) {
				if (this.iter[i] != null) {
					if (this.iter[i].hasNext()) {
						this.object[i] = this.iter[i].next();
						if (!this.allowEqual && this.equalObjectFound(this.object[i], i))
							continue;
						do {
							i--;
						} while ((i > -1) && (this.iter[i] == null));
					} else {
						this.iter[i] = this.list[i].iterator();
						i++;
					}
				} else
					i++;

				if (i == this.iter.length) {
					this.endReached = true;
					return;
				}
			}
		}

		private boolean init() {
			boolean hasNext = false;
			int i = this.list.length - 1;

			while (i > -1) {
				if (this.iter[i] != null) {
					if (this.iter[i].hasNext()) {
						this.object[i] = this.iter[i].next();
						if (!this.allowEqual && this.equalObjectFound(this.object[i], i))
							continue;
						hasNext = true;
					} else {
						this.iter[i] = null;
					}
				}
				i--;
			}

			return hasNext;
		}

		private boolean equalObjectFound(Object o, int oindex) {
			for (int i = oindex + 1; i < this.object.length; i++) {
				if (o == this.object[i])
					return true;
			}
			return false;
		}
	}
}
