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
package com.rapidminer.gui.viewer;

import javax.swing.table.AbstractTableModel;

import com.rapidminer.tools.Tools;


/** The model for the {@link com.rapidminer.gui.viewer.ConfusionMatrixViewerTable}. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: ConfusionMatrixViewerTableModel.java,v 1.3 2007/07/15 22:06:25 ingomierswa Exp $
 */
public class ConfusionMatrixViewerTableModel extends AbstractTableModel {
    
    private static final long serialVersionUID = 1206988933244249851L;
    
	private String[] classNames;
    private int[][] counter;
    private int[] rowSums;
    private int[] columnSums;
    
    public ConfusionMatrixViewerTableModel(String[] classNames, int[][] counter) {
        this.classNames = classNames;
        this.counter = counter;
        this.rowSums    = new int[classNames.length];
        this.columnSums = new int[classNames.length];
        for (int i = 0; i < classNames.length; i++) {
        	for (int j = 0; j < classNames.length; j++) {
        		this.columnSums[i] += counter[i][j];
        		this.rowSums[i] += counter[j][i];
        	}
        }
    }
    
    public int getRowCount() {
        return classNames.length + 2;
    }

    public int getColumnCount() {
        return classNames.length + 2;
    }
    
    public Object getValueAt(int row, int col) {
    	if (row == 0) {
    		if (col == 0) {
    			return "";
    		} else if (col == getColumnCount() - 1) {
    			return "class precision";
    		} else {
    			return "true " + classNames[col - 1];
    		}
    	} else if (row == getRowCount() - 1) {
    		if (col == 0) {
    			return "class recall";
    		} else if (col == getColumnCount() - 1) {
    			return "";
    		} else {
    			double recall = counter[col - 1][col - 1] / (double)columnSums[col - 1];
    			return Tools.formatPercent(recall);
    		}	
    	} else {
    		if (col == 0) {
    			if (row - 1 >= 0)
    				return "pred. " + classNames[row - 1];
    			else 
    				return "";
    		} else if (col == getColumnCount() - 1) {
    			double precision = counter[row - 1][row - 1] / (double)rowSums[row - 1];
    			return Tools.formatPercent(precision);
    		} else {
    			if ((col - 1 >= 0) && (row - 1 >= 0))
    				return counter[col - 1][row - 1];
    			else
    				return "";
    		}	    		
    	}
    }
}
