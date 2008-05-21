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

import java.util.BitSet;
import java.util.Random;
import java.util.Vector;

/**
 * The RandomDataContainer is an implementation of the KohonenTrainingsData interface, and therefor provides examples of
 * data for a KohonenNet. The data is returned to the KohonenNet via an iterator, which shuffels the data examples.
 * 
 * @author Sebastian Land
 * @version $Id: RandomDataContainer.java,v 1.3 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class RandomDataContainer implements KohonenTrainingsData {

	private Vector<double[]> data = new Vector<double[]>();

	private Random generator;

	private BitSet flag;

	public void addData(double[] data) {
		this.data.add(data);
	}

	public int countData() {
		return data.size();
	}

	public double[] getNext() {
		int chosen = -1;
		while (chosen < 0) {
			int dice = generator.nextInt(data.size());
			if (!flag.get(dice)) {
				flag.set(dice);
				return (data.elementAt(dice));
			}
		}
		return null;
	}

	public void reset() {
		this.flag = new BitSet(data.size());
	}

	public void setRandomGenerator(Random generator) {
		this.generator = generator;
	}

	public double[] get(int index) {
		return data.elementAt(index);
	}
}
