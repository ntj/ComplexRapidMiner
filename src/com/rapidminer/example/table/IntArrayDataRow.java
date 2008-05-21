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


/**
 * Implementation of DataRow that is backed by an integer array.
 * 
 * @author Ingo Mierswa
 * @version $Id: DoubleArrayDataRow.java,v 2.11 2006/03/21 15:35:39 ingomierswa
 *          Exp $
 */
public class IntArrayDataRow extends DataRow {
	
	private static final long serialVersionUID = -8089560930865510003L;
	
	/** Holds the data for all attributes. */
	private int[] data;

	/** Creates a new data row backed by an primitive array. */
	public IntArrayDataRow(int[] data) {
		this.data = data;
	}
	
	protected double get(int index, double defaultValue) {
		return data[index];
	}

	/** Sets the given data for the given index. */
	protected void set(int index, double value, double defaultValue) {
		data[index] = (int)value;
	}

	/**
	 * Creates a new array of the given size if necessary and copies the data
	 * into the new array.
	 */
	protected void ensureNumberOfColumns(int numberOfColumns) {
		if (data.length >= numberOfColumns)
			return;
		int[] newData = new int[numberOfColumns];
		System.arraycopy(data, 0, newData, 0, data.length);
		data = newData;
	}

	/** Does nothing. */
	public void trim() {}

	/** Returns a string representation of the data row. */
	public String toString() {
		StringBuffer result = new StringBuffer();
		for (int i = 0; i < data.length; i++)
			result.append((i == 0 ? "" : ",") + data[i]);
		return result.toString();
	}
}
