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
package com.rapidminer.datatable;

/**
 * A data list that contains Object arrays that record process results or
 * other data. Each row can consist of an id and an object array which represents 
 * the data.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimpleDataTableRow.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class SimpleDataTableRow implements DataTableRow {

	private double[] row;

	private String id;

	public SimpleDataTableRow(double[] row) {
		this(row, null);
	}
	
	public SimpleDataTableRow(double[] row, String id) {
		this.row = row;
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public double getValue(int index) {
		return row[index];
	}
	
	public int getNumberOfValues() {
		return row.length;
	}
}
