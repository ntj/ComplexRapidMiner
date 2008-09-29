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
package com.rapidminer.operator.learner.clustering.hierarchical.upgma;

import java.util.LinkedList;
import java.util.List;

public class Cluster {

	private Tree tree;

	private double distances[];

	private List<String> elements = new LinkedList<String>();

	private int index;

	public Cluster(String leaf, double[] distances, int index) {
		this.elements.add(leaf);
		this.tree = new Tree(leaf);
		this.tree.setHeight(0.0);
		this.index = index;
		this.distances = distances;
	}

	public Tree getTree() {
		return tree;
	}

	public void setTree(Tree tree) {
		this.tree = tree;
	}

	public int getIndex() {
		return index;
	}

	public double getDistance(int i) {
		return distances[i];
	}

	public void setDistance(int i, double distance) {
		distances[i] = distance;
	}

	public List getElements() {
		return elements;
	}

	public int size() {
		return elements.size();
	}

	public void union(Cluster other) {
		this.elements.addAll(other.elements);
	}
}
