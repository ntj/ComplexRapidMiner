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
package com.rapidminer.gui.renderer;

import java.awt.Component;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.table.TableModel;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.report.Reportable;
import com.rapidminer.report.Tableable;
import com.rapidminer.tools.Tools;

/**
 * This is the abstract renderer superclass for all renderers which
 * should be a table based on a given {@link TableModel}.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractTableModelTableRenderer.java,v 1.5 2008/08/19 15:40:53 homburg Exp $
 */
public abstract class AbstractTableModelTableRenderer extends NonGraphicalRenderer {

	public static final String RENDERER_NAME = "Table View";
	
	public static final String PARAMETER_MIN_ROW    = "min_row";
	
	public static final String PARAMETER_MAX_ROW    = "max_row";
	
	public static final String PARAMETER_MIN_COLUMN = "min_column";
	
	public static final String PARAMETER_MAX_COLUMN = "max_column";
	
	
	public static class DefaultTableable implements Tableable {

		private TableModel model;
		
		private int minRow = 0;
		
		private int maxRow = Integer.MAX_VALUE;
		
		private int minColumn = 0;
		
		private int maxColumn = Integer.MAX_VALUE;
		
		public DefaultTableable(TableModel model, Renderer renderer) {
			this.model = model;
			
			try {
				Object minRowO = renderer.getParameter(PARAMETER_MIN_ROW);
				if (minRowO != null) {
					minRow = Integer.valueOf(minRowO.toString()) - 1;
				} else {
					minRow = 0;
				}
			} catch (UndefinedParameterError e) {
				minRow = 0;
			}
			
			try {
				Object maxRowO = renderer.getParameter(PARAMETER_MAX_ROW);
				if (maxRowO != null) {
					maxRow = Integer.valueOf(maxRowO.toString()) - 1;
				} else {
					maxRow = Integer.MAX_VALUE;
				}
			} catch (UndefinedParameterError e) {
				maxRow = Integer.MAX_VALUE;
			}
			
			try {
				Object minColO = renderer.getParameter(PARAMETER_MIN_COLUMN);
				if (minColO != null) {
					minColumn = Integer.valueOf(minColO.toString()) - 1;
				} else {
					minColumn = 0;
				}
			} catch (UndefinedParameterError e) {
				minColumn = 0;
			}
			
			try {
				Object maxColO = renderer.getParameter(PARAMETER_MAX_COLUMN);
				if (maxColO != null) {
					maxColumn = Integer.valueOf(maxColO.toString()) - 1;
				} else {
					maxColumn = 0;
				}
			} catch (UndefinedParameterError e) {
				maxColumn = 0;
			}
		}
		
		public String getColumnName(int columnIndex) {
			return model.getColumnName(columnIndex + minColumn);
		}
		
		public String getCell(int row, int column) {
			String value = model.getValueAt(row + minRow, column + minColumn).toString();
			if (Number.class.isAssignableFrom(model.getColumnClass(column))) {
				return Tools.formatIntegerIfPossible(Double.valueOf(value));
			} else {
				return value;	
			}
		}

		public int getColumnNumber() {
			int maxC = maxColumn;
			if (maxColumn >= model.getColumnCount()) {
				maxC = model.getColumnCount() - 1;
			}
			return maxC - minColumn + 1;
		}

		public int getRowNumber() {
			int maxR = maxRow;
			if (maxRow >= model.getRowCount()) {
				maxR = model.getRowCount() - 1;
			}
			return maxR - minRow + 1;
		}
	}
	
	public String getName() {
		return RENDERER_NAME;
	}

	public abstract TableModel getTableModel(Object renderable, IOContainer ioContainer);
	
	public boolean isSortable() {
		return true;
	}
	
	public boolean isColumnMovable() {
		return true;
	}
	
	public boolean isAutoresize() {
		return true;
	}
	
	public Component getVisualizationComponent(Object renderable, IOContainer ioContainer) {
		TableModel tableModel = getTableModel(renderable, ioContainer);
		if (tableModel != null) {
			return new ExtendedJScrollPane(new ExtendedJTable(getTableModel(renderable, ioContainer), isSortable(), isColumnMovable(), isAutoresize()));
		} else {
			return new JLabel("No visualization possible for table.");
		}
	}

	public Reportable createReportable(Object renderable, IOContainer ioContainer) {
		TableModel tableModel = getTableModel(renderable, ioContainer);
		if (tableModel != null)
			return new DefaultTableable(tableModel, this);
		return null;
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_MIN_ROW, "Indicates the first row number which should be rendered.", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeInt(PARAMETER_MAX_ROW, "Indicates the last row number which should be rendered.", 1, Integer.MAX_VALUE, Integer.MAX_VALUE));
		types.add(new ParameterTypeInt(PARAMETER_MIN_COLUMN, "Indicates the first column number which should be rendered.", 1, Integer.MAX_VALUE, 1));
		types.add(new ParameterTypeInt(PARAMETER_MAX_COLUMN, "Indicates the last column number which should be rendered.", 1, Integer.MAX_VALUE, Integer.MAX_VALUE));
		return types;
	}
}
