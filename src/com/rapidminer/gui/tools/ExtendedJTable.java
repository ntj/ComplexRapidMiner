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
package com.rapidminer.gui.tools;

import java.awt.Color;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import com.rapidminer.tools.Tableable;
import com.rapidminer.tools.Tools;

/**
 * <p>This class extends a JTable in a way that editing is handled like it is expected, i.e. 
 * editing is properly stopped during focus losts, resizing, or column movement. The current
 * value is then set to the model. The only way to abort the value change is by pressing
 * the escape key.</p>
 * 
 * <p>The extended table is sortable per default. Developers should note that this feature 
 * might lead to problems if the columns contain different class types end different editors.
 * In this case one of the constructors should be used which set the sortable flag to false.
 * </p>
 *   
 * @author Ingo Mierswa
 * @version $Id: ExtendedJTable.java,v 1.11 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class ExtendedJTable extends JTable implements Tableable {

    private static final long serialVersionUID = 4840252601155251257L;

    private boolean sortable = true;
    
    private boolean useColoredCellRenderer = true;
        
    private transient ColoredTableCellRenderer renderer = new ColoredTableCellRenderer();
    
    private TableSorter tableSorter = null;
    
    
    public ExtendedJTable() {
        this(null, true);
    }

    public ExtendedJTable(boolean sortable) {
        this(null, sortable);
    }
    
    public ExtendedJTable(TableModel model, boolean sortable) {
        this(model, sortable, true);
    }

    public ExtendedJTable(TableModel model, boolean sortable, boolean columnMovable) {
    	this(model, sortable, columnMovable, true);
    }
    
    public ExtendedJTable(TableModel model, boolean sortable, boolean columnMovable, boolean autoResize) {
    	this(model, sortable, columnMovable, autoResize, true);
    }
    
    public ExtendedJTable(TableModel model, boolean sortable, boolean columnMovable, boolean autoResize, boolean useColoredCellRenderer) {
        super();
        this.sortable = sortable;
        this.useColoredCellRenderer = useColoredCellRenderer;
       
        // allow all kinds of selection (e.g. for copy and paste)
        setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        setColumnSelectionAllowed(true);
        setRowSelectionAllowed(true);
        
        setRowHeight(getRowHeight() + SwingTools.TABLE_ROW_EXTRA_HEIGHT);
        getTableHeader().setReorderingAllowed(columnMovable);
        
        // necessary in order to fix changes after focus was lost
        putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        
        // auto resize?
        if (!autoResize)
        	setAutoResizeMode(AUTO_RESIZE_OFF);
        
        if (model != null) {
            setModel(model);
        }
    }
    
    protected Object readResolve() {
    	this.renderer = new ColoredTableCellRenderer();
    	return this;
    }
    
    protected TableSorter getTableSorter() {
    	return this.tableSorter;
    }
    
    /** Subclasses might overwrite this method. The returned color will be used for the cell renderer. 
     *  The default method implementation returns {@link SwingTools#LIGHTEST_BLUE} and white for 
     *  alternating rows. If no colors should be used at all, return null. */
    public Color getCellColor(int row, int column) {
        if (row % 2 == 0)
            return Color.WHITE;
        else
            return SwingTools.LIGHTEST_BLUE;
    }
    
    public void setSortable(boolean sortable) {
        this.sortable = sortable;
    }
    
    public boolean isSortable() {
        return sortable;
    }
    
    public void setModel(TableModel model) {
        if (sortable) {
            this.tableSorter = new TableSorter(model);
            this.tableSorter.setTableHeader(getTableHeader());
            super.setModel(this.tableSorter);
        } else {
            super.setModel(model);
            this.tableSorter = null;
        }
    }
    
    /** Necessary to properly stopping the editing when a column is moved (dragged). */
    public void columnMoved(TableColumnModelEvent e) {
        if (isEditing()) {
            cellEditor.stopCellEditing();
        }
        super.columnMoved(e);
    }

    /** Necessary to properly stopping the editing when a column is resized. */
    public void columnMarginChanged(ChangeEvent e) {
        if (isEditing()) {
            cellEditor.stopCellEditing();
        }
        super.columnMarginChanged(e);
    }
    
    public TableCellRenderer getCellRenderer(int row, int col) {
    	if (useColoredCellRenderer) {
    		Color color = getCellColor(row, col);
    		if (color != null)
    			renderer.setColor(color);
    		return renderer;
    	} else {
    		return super.getCellRenderer(row, col);
    	}
    }
    
    /** This method ensures that the correct tool tip for the current table cell is delivered. */
    public String getToolTipText(MouseEvent e) {
        Point p = e.getPoint();
        int colIndex = columnAtPoint(p);
        int realColumnIndex = convertColumnIndexToModel(colIndex);
        int rowIndex = rowAtPoint(p);
        Object value = getModel().getValueAt(rowIndex, realColumnIndex);
        if (value != null)
            return SwingTools.transformToolTipText(value.toString());
        else
            return super.getToolTipText();
    }

	public String getCell(int row, int column) {
		String text = null;
		if (getTableHeader() != null) {
			if (row == 0) {
				// titel row
				return getTableHeader().getColumnModel().getColumn(column).getHeaderValue().toString();
			} else {
				row--;
			}
		}
		// data area
		Object value = getModel().getValueAt(row, column);
		if (value instanceof Number) {
			Number number = (Number)value;
			double numberValue = number.doubleValue();
			text = Tools.formatIntegerIfPossible(numberValue);
		} else {
			if (value != null)
				text = value.toString();
			else
				text = "?";
		}
		return text;
	}

	public int getColumnNumber() {
		return getColumnCount();
	}

	public int getRowNumber() {
		if (getTableHeader() != null) {
			return getRowCount() + 1;
		} else {
			return getRowCount();
		}
	}
	
	public int getModelIndex(int rowIndex) {
		return tableSorter.modelIndex(rowIndex);
	}
}
