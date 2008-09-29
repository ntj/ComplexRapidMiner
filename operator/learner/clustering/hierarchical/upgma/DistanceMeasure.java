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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;

public abstract class DistanceMeasure {

	public static final String[] CLUSTER_TYPE_NAMES = new String[] {
			"minimum", "maximum", "average", "unweighted_average"
	};

	public static final int TYPE_MINIMUM = 0;

	public static final int TYPE_MAXIMUM = 1;

	public static final int TYPE_AVERAGE = 2;

	public static final int TYPE_UNWEIGHTED = 3;

	public static final String[] TYPE_NAMES = new String[] {
			"euclidian", "nominal"
	};

	public static final int TYPE_EUCLIDIAN = 0;

	public static final int TYPE_NOMINAL = 1;

	public static ClusterDistanceMeasure createClusterDistanceMeasure(int type) {
		switch (type) {
			case TYPE_MINIMUM:
				return new MinClusterDistanceMeasure();
			case TYPE_MAXIMUM:
				return new MaxClusterDistanceMeasure();
			case TYPE_AVERAGE:
				return new AverageClusterDistanceMeasure();
			case TYPE_UNWEIGHTED:
				return new UnweightedClusterDistanceMeasure();
			default:
				throw new IllegalArgumentException("Illegal ClusterDistanceMeasure type: " + type);
		}
	}

	public static DistanceMeasure createDistanceMeasure(int type) {
		switch (type) {
			case TYPE_EUCLIDIAN:
				return new EuklidianDistanceMeasure();
			case TYPE_NOMINAL:
				return new NominalDistanceMeasure();
			default:
				throw new IllegalArgumentException("Illegal DistanceMeasure type: " + type);
		}
	}

	public abstract double calculateDistance(Example e1, Example e2);

	public DistanceMatrix calculateDistanceMatrix(ExampleSet exampleSet) {
		DistanceMatrix matrix = new DistanceMatrix(exampleSet.size());
		Iterator<Example> r = exampleSet.iterator();
		Attribute idAttribute = exampleSet.getAttributes().getId();
		int i = 0;
		while (r.hasNext()) {
			Example e = r.next();
			double idValue = e.getValue(idAttribute);
			String idString = idAttribute.isNominal() ? idAttribute.getMapping().mapIndex((int)idValue) : idValue + "";
			matrix.setName(i, idString);
			Iterator<Example> s = exampleSet.iterator();
			int j = 0;
			while (s.hasNext()) {
				matrix.setDistance(i, j, calculateDistance(e, s.next()));
				j++;
			}
			i++;
		}
		return matrix;
	}
}
