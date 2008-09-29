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

import com.rapidminer.tools.Tools;

public class DistanceMatrix implements Matrix {

	private static final long serialVersionUID = 8249533011260125345L;

	private double distance[][];

	private String name[];

	/** Copy constructor */
	private DistanceMatrix(DistanceMatrix matrix) {
		this.distance = new double[matrix.distance.length][];
		for (int i = 0; i < this.distance.length; i++) {
			this.distance[i] = new double[i];
			for (int j = 0; j < i; j++) {
				this.distance[i][j] = matrix.distance[i][j];
			}
		}
		this.name = new String[matrix.name.length];
		for (int i = 0; i < this.name.length; i++) {
			this.name[i] = matrix.name[i];
		}
	}

	public DistanceMatrix(int n) {
		distance = new double[n][];
		for (int i = 0; i < n; i++) {
			distance[i] = new double[i];
		}
		name = new String[n];
	}

	public String getName(int i) {
		return name[i];
	}

	public void setName(int i, String name) {
		this.name[i] = name;
	}

	public int getDimension() {
		return distance.length;
	}

	public double getDistance(int i, int j) {
		if (i == j)
			return 0;
		if (j < i)
			return getDistance(j, i);
		return distance[j][i];
	}

	public void setDistance(int i, int j, double d) {
		if (d < 0.0)
			throw new IllegalArgumentException("Distances must be > 0");
		if (i == j) {
			if (d != 0.0)
				throw new IllegalArgumentException("d(" + i + "," + i + ") = " + d + " != 0");
			else
				return;
		}
		if (j < i) {
			setDistance(j, i, d);
			return;
		}
		distance[j][i] = d;
	}

	/** Throws an IllegalArgumentException if the matrix is no distance measure. */
	public void verify() {
		for (int i = 0; i < distance.length; i++) {
			for (int j = 0; j < i; j++) {
				for (int k = 0; k < distance.length; k++) {
					if (getDistance(i, j) + getDistance(j, k) < getDistance(i, k)) {
						throw new IllegalArgumentException("Matrix violates triangle inequality (" + i + "," + j + "," + k + ")");
					}
				}
			}
		}
	}

	public DistanceMatrix copy() {
		return new DistanceMatrix(this);
	}

	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < distance.length; i++) {
			if (i != 0)
				result.append(Tools.getLineSeparator());
			for (int j = 0; j < distance.length; j++) {
				if (j != 0)
					result.append("\t");
				result.append(getDistance(i, j));
			}
		}
		return result.toString();
	}

	public double getValue(int i, int j) {
		return getDistance(i, j);
	}

	public void setValue(int i, int j, double v) {
		setDistance(i, j, v);
	}

	public boolean isFixed(int i, int j) {
		return (i <= j);
	}

	public String getLabel(int i) {
		return name[i];
	}
}
