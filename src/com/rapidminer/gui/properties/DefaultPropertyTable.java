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


/**
 * This abstract class implements some methods which should be sufficient in
 * almost all cases of the RapidMiner GUI.
 * 
 * @author Ingo Mierswa
 * @version $Id: DefaultPropertyTable.java,v 1.2 2008/07/31 17:43:41 ingomierswa Exp $
 */
public abstract class DefaultPropertyTable extends PropertyTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6290460979115818689L;

	public boolean isCellEditable(int row, int col) {
		return (col == 1);
	}

	public void setValue(int row, Object value) {
		getOperator(row).getParameters().setParameterWithoutCheck(getParameterType(row).getKey(), value);
	}
}
