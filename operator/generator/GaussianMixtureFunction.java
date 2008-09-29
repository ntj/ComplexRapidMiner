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
package com.rapidminer.operator.generator;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/** Generates a gaussian distribution for all attributes. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: GaussianMixtureFunction.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class GaussianMixtureFunction implements TargetFunction {

	/** The number of gaussians per dimension. */
	private static int CLUSTER_PER_DIMENSION = 2;

	/** The list of clusters. */
	private List<Cluster> clusters = new LinkedList<Cluster>();

	/** The label attribute. */
	Attribute label = AttributeFactory.createAttribute("label", Ontology.NOMINAL);

	/** The label for the last generated point. */
	private double currentLabel;

	/** The number of attributes. */
	private int numberOfAttributes = 2;

	/** The lower bound for the dataset. */
	private double lowerBound = -10.0d;

	/** The upper bound for the dataset. */
	private double upperBound = 10.0d;

	/** Since circles are used the upper and lower bounds must be the same. */
	public void setLowerArgumentBound(double lower) {
		this.lowerBound = lower;
	}

	public void setUpperArgumentBound(double upper) {
		this.upperBound = upper;
	}

	/** Initializes some gaussian clusters. */
	public void init(RandomGenerator random) {
		this.clusters.clear();
		double sizeSum = 0.0d;
		int numberOfClusters = (int) Math.pow(CLUSTER_PER_DIMENSION, numberOfAttributes);
		for (int i = 0; i < numberOfClusters; i++) {
			double[] coordinates = new double[numberOfAttributes];
			double[] sigmas = new double[numberOfAttributes];
			for (int j = 0; j < coordinates.length; j++) {
				coordinates[j] = random.nextDoubleInRange(lowerBound, upperBound);
				sigmas[j] = random.nextDouble() * 0.8 + 0.2;
			}
			int labelIndex = label.getMapping().mapString("cluster" + i);
			double size = random.nextDouble();
			sizeSum += size;
			this.clusters.add(new Cluster(coordinates, sigmas, size, labelIndex));
		}
		Iterator i = this.clusters.iterator();
		while (i.hasNext()) {
			Cluster cluster = (Cluster) i.next();
			cluster.size /= sizeSum;
		}
	}

	/** Does nothing. */
	public void setTotalNumberOfExamples(int number) {}

	/** Sets the total number of attributes. */
	public void setTotalNumberOfAttributes(int number) {
		this.numberOfAttributes = number;
	}

	public Attribute getLabel() {
		return label;
	}

	public double calculate(double[] att) throws FunctionException {
		return currentLabel;
	}

	public double[] createArguments(int number, RandomGenerator random) throws FunctionException {
		if (number <= 0)
			throw new FunctionException("Gaussian mixture clustering function", "must have at least one attribute!");
		int c = 0;
		double prob = random.nextDouble();
		double sizeSum = 0.0d;
		Cluster cluster = null;
		do {
			cluster = clusters.get(c);
			sizeSum += cluster.size;
			if (prob < sizeSum)
				break;
			c++;
		} while (sizeSum < 1);
		this.currentLabel = cluster.label;
		return cluster.createArguments(random);
	}
}
