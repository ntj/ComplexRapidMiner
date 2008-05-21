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
package com.rapidminer.tools.math.som;

/**
 * This class represents a node in a KohonenNet. This node contains its current weight, which is randomly set initially,
 * but adopted during the trainingsphase.
 * 
 * @author Sebastian Land
 * @version $Id: KohonenNode.java,v 1.3 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class KohonenNode {

	private double[] weight;

	public KohonenNode(double[] weightInit) {
		weight = weightInit.clone();
	}

	public double[] getWeights() {
		return weight;
	}

	public void setWeights(double[] weights) {
		this.weight = weights.clone();
	}
}
