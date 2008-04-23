/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.datatable;

import com.rapidminer.operator.visualization.CorrelationMatrix;

/**
 *  This class allows to use the rows of a {@link com.rapidminer.operator.visualization.CorrelationMatrix} 
 *  as basis for {@link com.rapidminer.datatable.DataTableRow}.
 *  
 *   @author Ingo Mierswa
 *   @version $Id: CorrelationMatrixRow2DataTableRowWrapper.java,v 1.1 2007/05/27 21:59:06 ingomierswa Exp $
 */
public class CorrelationMatrixRow2DataTableRowWrapper implements DataTableRow {

	private CorrelationMatrix matrix;
	
	private int rowIndex;
	
	/** Creates a new wrapper. If the Id Attribute is null, the DataTableRow will not contain an Id. */
	public CorrelationMatrixRow2DataTableRowWrapper(CorrelationMatrix matrix, int rowIndex) {
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
