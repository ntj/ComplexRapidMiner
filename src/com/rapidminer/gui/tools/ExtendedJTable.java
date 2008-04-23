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
 * @version $Id: ExtendedJTable.java,v 1.4 2007/06/20 12:30:03 ingomierswa Exp $
 */
public class ExtendedJTable extends JTable {

    private static final long serialVersionUID = 4840252601155251257L;

    private boolean sortable = true;
    
    private transient ColoredTableCellRenderer renderer = new ColoredTableCellRenderer();
    
    
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
        super();
        this.sortable = sortable;
        
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
            TableSorter sorter = new TableSorter(model);
            sorter.setTableHeader(getTableHeader());
            super.setModel(sorter);
        } else {
            super.setModel(model);
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
    	Color color = getCellColor(row, col);
    	if (color != null)
    		renderer.setColor(color);
    	return renderer;
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
}
