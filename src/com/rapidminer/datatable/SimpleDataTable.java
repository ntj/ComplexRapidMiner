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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * A simple data table implementation which stores the data itself. 
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: SimpleDataTable.java,v 1.2 2007/06/07 17:12:23 ingomierswa Exp $
 */
public class SimpleDataTable extends AbstractDataTable {
    
	private List<DataTableRow> data = new ArrayList<DataTableRow>();

	private String[] columns;
	
    private double[] weights;
    
	private boolean[] specialColumns;
	
	private Map<Integer, Map<Integer,String>> index2StringMap = new HashMap<Integer,Map<Integer,String>>();
	private Map<Integer, Map<String,Integer>> string2IndexMap = new HashMap<Integer,Map<String,Integer>>();
	

    public SimpleDataTable(String name, String[] columns) {
        this(name, columns, null);
    }
    
	public SimpleDataTable(String name, String[] columns, double[] weights) {
	    super(name);
		this.columns = columns;
        this.weights = weights;
		this.specialColumns = new boolean[columns.length];
		for (int i = 0; i < this.specialColumns.length; i++)
			this.specialColumns[i] = false;
	}
	
	public int getNumberOfSpecialColumns() {
		int counter = 0;
		for (boolean b : specialColumns)
			if (b) counter++;
		return counter;
	}
	
	public boolean isSpecial(int index) {
		return specialColumns[index];
	}
	
	public void setSpecial(int index, boolean special) {
		this.specialColumns[index] = special;
	}
	
	public boolean isNominal(int column) {
		return (index2StringMap.get(column) != null);
	}
	
	public String mapIndex(int column, int index) {
		Map<Integer,String> columnIndexMap = index2StringMap.get(column);
		return columnIndexMap.get(index);
	}
	
	public int mapString(int column, String value) {
		Map<String,Integer> columnValueMap = string2IndexMap.get(column);
		if (columnValueMap == null) {
			columnValueMap = new HashMap<String,Integer>();
			columnValueMap.put(value, 0);
			string2IndexMap.put(column, columnValueMap);
			Map<Integer,String> columnIndexMap = new HashMap<Integer,String>();
			columnIndexMap.put(0, value);
			index2StringMap.put(column, columnIndexMap);
			return 0;
		} else {
			Integer result = columnValueMap.get(value);
			if (result != null) {
				return result.intValue();
			} else {
				int newIndex = columnValueMap.size();
				columnValueMap.put(value, newIndex);
				Map<Integer,String> columnIndexMap = index2StringMap.get(column);
				columnIndexMap.put(newIndex, value);
				return newIndex;
			}
		}
	}
	
	public int getNumberOfValues(int column) {
		return index2StringMap.get(column).size();
	}
	
    public boolean isSupportingColumnWeights() {
        return weights != null;
    }
    
    public double getColumnWeight(int column) {
        if (weights == null)
            return Double.NaN;
        else
            return weights[column];
    }
    
	public String getColumnName(int i) {
		return columns[i];
	}

	public int getColumnIndex(String name) {
		for (int i = 0; i < columns.length; i++) {
			if (columns[i].equals(name))
				return i;
		}
		return -1;
	}

	public int getNumberOfColumns() {
		return columns.length;
	}

	public String[] getColumnNames() {
		return columns;
	}
    
	public synchronized void add(DataTableRow row) {
		synchronized (data) {
			data.add(row);
			fireEvent();
		}
	}

    public DataTableRow getRow(int index) {
        return data.get(index);
    }
    
	public synchronized Iterator<DataTableRow> iterator() {
		Iterator<DataTableRow> i = null;
		synchronized (data) {
			i = data.iterator();
		}
		return i;
	}

	public int getNumberOfRows() {
		int result = 0;
		synchronized (data) {
			result = data.size();
		}
		return result;
	}

	public void clear() {
		data.clear();
		fireEvent();
	}
    
    public synchronized void sample(int newSize) {
    	// must be a usual random since otherwise plotting would change the rest of 
    	// the process during a breakpoint result viewing 
        Random random = new Random();
        while (getNumberOfRows() > newSize) {
        	int index = random.nextInt(getNumberOfRows());
        	data.remove(index);
        }
    }
}
