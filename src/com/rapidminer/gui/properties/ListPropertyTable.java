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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeList;


/**
 * For {@link com.rapidminer.parameter.ParameterTypeList} the
 * parameter values are parameter lists themselves. Hence, the key must be
 * editable, too (not only the value). That is what this implementation of
 * PropertyTable is good for.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ListPropertyTable.java,v 2.10 2006/04/05 08:57:23 ingomierswa
 *          Exp $
 */
public class ListPropertyTable extends PropertyTable {

	private static final long serialVersionUID = -4547732551646588939L;

	private transient ParameterTypeList type;

	private transient Operator operator;

	public ListPropertyTable(ParameterTypeList type, List<Object[]> parameterList, Operator operator) {
		super(new String[] { type.getKey(), type.getValueType().getKey() });
		this.type = type;
		this.operator = operator;
		updateTableData(parameterList.size());
		updateEditorsAndRenderers(this);
		Iterator<Object[]> i = parameterList.iterator();
		int j = 0;
		while (i.hasNext()) {
			Object[] keyValue = i.next();
			getModel().setValueAt(keyValue[0], j, 0);
			getModel().setValueAt(keyValue[1], j, 1);
			j++;
		}
	}

	public void addRow() {
		getDefaultModel().addRow(new Object[] { "", type.getValueType().getDefaultValue() });
		updateEditorsAndRenderers(this);
		
		// necessary to use default values (without changes)
		int lastIndex = getRowCount() - 1;
		getModel().setValueAt(getKeyEditor(lastIndex).getCellEditorValue(), lastIndex, 0);
	}

	public void removeSelected() {
		int[] selectedRow = getSelectedRows();
		for (int i = selectedRow.length - 1; i >= 0; i--) {
			getDefaultModel().removeRow(selectedRow[i]);
		}
		getDefaultModel().fireTableStructureChanged();
	}

	public void getParameterList(List<Object[]> list) {
		list.clear();
		for (int i = 0; i < getModel().getRowCount(); i++) {
			list.add(new Object[] { getModel().getValueAt(i, 0), getModel().getValueAt(i, 1) });
		}
	}

	public Operator getOperator(int row) {
		return operator;
	}

	public ParameterType getParameterType(int row) {
		return type.getValueType();
	}

	public boolean isCellEditable(int row, int col) {
		return true;
	}
}
