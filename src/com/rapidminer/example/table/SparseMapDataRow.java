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
package com.rapidminer.example.table;

import java.util.HashMap;
import java.util.Map;

import com.rapidminer.example.Tools;


/**
 * Implementation of DataRow that is backed by a HashMap. Usually using the
 * {@link DoubleSparseArrayDataRow} should be more efficient.
 * 
 * @author Ingo Mierswa
 * @version $Id: SparseMapDataRow.java,v 2.15 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class SparseMapDataRow extends DataRow {

	private static final long serialVersionUID = -7452459295368606029L;
	
	/** Maps the indices of attributes to the data. */
	private Map<Integer, Double> data = new HashMap<Integer, Double>();

	/** Returns the desired data for the given index. */
	protected double get(int index, double defaultValue) {
		Double value = data.get(index);
		if (value != null)
			return value.doubleValue();
		else
			return defaultValue;
	}

	/** Sets the given data for the given index. */
	protected void set(int index, double value, double defaultValue) {
		if (Tools.isDefault(defaultValue, value))
			data.remove(index);
		else
			data.put(index, value);
	}

	/** Does nothing. */
	protected void ensureNumberOfColumns(int numberOfColumns) {}

	/** Does nothing. */
	public void trim() {}

	/** Returns a string representation of the data row. */
	public String toString() {
		return data.toString();
	}
}
