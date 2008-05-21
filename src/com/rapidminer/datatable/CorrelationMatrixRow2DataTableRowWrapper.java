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

import com.rapidminer.operator.visualization.SymmetricalMatrix;

/**
 *  This class allows to use the rows of a {@link com.rapidminer.operator.visualization.SymmetricalMatrix} 
 *  as basis for {@link com.rapidminer.datatable.DataTableRow}.
 *  
 *   @author Ingo Mierswa
 *   @version $Id: CorrelationMatrixRow2DataTableRowWrapper.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class CorrelationMatrixRow2DataTableRowWrapper implements DataTableRow {

	private SymmetricalMatrix matrix;
	
	private int rowIndex;
	
	/** Creates a new wrapper. If the Id Attribute is null, the DataTableRow will not contain an Id. */
	public CorrelationMatrixRow2DataTableRowWrapper(SymmetricalMatrix matrix, int rowIndex) {
		this.matrix = matrix;
		this.rowIndex = rowIndex;
	}
	
	public String getId() { 
		return null;
	} 
	
	public double getValue(int index) {
		if (index == 0)
			return rowIndex;
		else
			return this.matrix.getValue(rowIndex, index - 1);
	}
	
	public int getNumberOfValues() {
		return matrix.getNumberOfColumns() + 1;
	}
}
