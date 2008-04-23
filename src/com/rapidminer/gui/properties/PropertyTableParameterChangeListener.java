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
package com.rapidminer.gui.properties;

import com.rapidminer.Process;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeStringCategory;

/**
 * The parameter selection change listener used by {@link PropertyTable}s.
 *
 * @author Ingo Mierswa
 * @version $Id: PropertyTableParameterChangeListener.java,v 1.2 2007/06/07 17:12:24 ingomierswa Exp $
 */
public class PropertyTableParameterChangeListener implements ParameterChangeListener {

	private PropertyTable propertyTable;
	
	public PropertyTableParameterChangeListener(PropertyTable propertyTable) {
		this.propertyTable = propertyTable;
	}
	
	public void parameterSelectionChanged(String operatorName, String parameterName, int row) {
		Operator operator = propertyTable.getOperator(row);
		if (operator != null) {
			Process process = operator.getProcess();
			if (process != null) {
				Operator paramOp = process.getOperator(operatorName);
				if (paramOp != null) {
					ParameterType parameterType = paramOp.getParameterType(parameterName);
					if (parameterType != null) {
						String range = parameterType.getDefaultValue().toString();
						if (parameterType instanceof ParameterTypeBoolean) {
							range = "true, false";
						} else if (parameterType instanceof ParameterTypeCategory) {
							ParameterTypeCategory categoryType = (ParameterTypeCategory)parameterType;
							StringBuffer rangeBuffer = new StringBuffer();
							for (int i = 0; i < categoryType.getNumberOfCategories(); i++) {
								if (i != 0)
									rangeBuffer.append(", ");
								rangeBuffer.append(categoryType.getCategory(i));
							}
							range = rangeBuffer.toString();
						} else if (parameterType instanceof ParameterTypeStringCategory) {
							ParameterTypeStringCategory categoryType = (ParameterTypeStringCategory)parameterType;
							boolean first = true;
							StringBuffer rangeBuffer = new StringBuffer();
							for (String category : categoryType.getValues()) {
								if (!first)
									rangeBuffer.append(", ");
								rangeBuffer.append(category);
								first = false;
							}
							range = rangeBuffer.toString();
						}
						propertyTable.getModel().setValueAt(range, row, 1);
					}
				}
			}
		}
	}
}
