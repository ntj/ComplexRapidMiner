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

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.tools.Tools;


/**
 * Returns a new data table with as many dimensions as the size of the given data table
 * list plus 1. Traverses the xAxis of all given data tables for the next points and 
 * adds for the missing places the last y-Value.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataTableMerger.java,v 1.1 2007/05/27 21:59:06 ingomierswa Exp $
 */
public class DataTableMerger {

    public DataTableMerger() {}
    
    /** Returns a new data table with as many dimensions as the size of the given data table
     *  list plus 1. Traverses the xAxis of all given data tables for the next points and 
     *  adds for the missing places the last y-Value. */
    public DataTable getMergedTables(List<DataTable> dataTables, String xColumnName, int xColumn, int yColumn) {
        String[] columnNames = new String[dataTables.size() + 1];
        columnNames[0] = xColumnName;
        int counter = 1;
        for (DataTable dataTable : dataTables)
            columnNames[counter++] = dataTable.getName();
        
        SimpleDataTable resultTable = new SimpleDataTable("ROC Plots", columnNames);
        
        int[] currentTableRows = new int[dataTables.size()];
        double[] lastYValues = new double[currentTableRows.length];
        while (true) {
            // check for finish
            boolean finished = true;
            counter = 0;
            for (DataTable dataTable : dataTables) {
                if (currentTableRows[counter++] < dataTable.getNumberOfRows()) {
                    finished = false;
                    break;
                }
            }
            if (finished)
                break;
            
            // find next minimum x
            counter = 0;
            double minX = Double.POSITIVE_INFINITY;
            for (DataTable dataTable : dataTables) {
                if (currentTableRows[counter] < dataTable.getNumberOfRows()) {
                    DataTableRow row = dataTable.getRow(currentTableRows[counter++]);
                    double currentX = row.getValue(xColumn);
                    if (currentX < minX) {
                        minX = currentX;
                    }
                }
            }
            
            // create new data row
            counter = 0;
            double[] newValues = new double[columnNames.length];
            newValues[0] = minX;
            List<Integer> increaseRowCounters = new LinkedList<Integer>();
            for (DataTable dataTable : dataTables) {
                double currentY = lastYValues[counter];
                if (currentTableRows[counter] < dataTable.getNumberOfRows()) {
                    DataTableRow row = dataTable.getRow(currentTableRows[counter]);
                    double currentX = row.getValue(xColumn);
                    if (Tools.isEqual(currentX, minX)) {
                        currentY = row.getValue(yColumn);
                        lastYValues[counter] = currentY;
                        increaseRowCounters.add(counter);
                    }
                }
                newValues[counter + 1] = currentY;
                counter++;
            }
            SimpleDataTableRow newDataRow = new SimpleDataTableRow(newValues);
            resultTable.add(newDataRow);
            
            // increase row counters
            for (int r : increaseRowCounters)
                currentTableRows[r] += 1;
        }
        return resultTable;
    }
}
