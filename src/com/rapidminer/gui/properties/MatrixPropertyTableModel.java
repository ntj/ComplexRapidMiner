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
package com.rapidminer.gui.properties;

import java.util.Vector;

import javax.swing.table.DefaultTableModel;

/**
 * A table model model used by {@link MatrixPropertyTable}. This model is 
 * necessary to support proper column removal. 
 * 
 * @see com.rapidminer.gui.properties.MatrixPropertyTable
 * @author Helge Homburg
 * @version $Id: MatrixPropertyTableModel.java,v 1.3 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class MatrixPropertyTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 0L;
	
	public MatrixPropertyTableModel(int rows, int columns) {
		super(rows, columns);
	}
	
	public Vector getColumnIdentifiers() {
		return columnIdentifiers;
	}
	
	public String getColumnName(int column) {
		if (column > 0) {
			return " True Class " + column + " ";
		} else {
			return "Cost Matrix";
		}
	}
}
