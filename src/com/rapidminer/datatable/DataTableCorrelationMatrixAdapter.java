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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rapidminer.operator.visualization.CorrelationMatrix;


/**
 * This class can be used to use a correlation matrix as data table. The data is directly
 * read from the correlation matrix instead of building a copy. Please note that the method
 * for adding new rows is not supported by this type of data tables.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataTableCorrelationMatrixAdapter.java,v 1.1 2007/05/27 21:59:06 ingomierswa Exp $
 */
public class DataTableCorrelationMatrixAdapter extends AbstractDataTable {

	private CorrelationMatrix matrix;
    
	private String[] index2NameMap;
	
    private Map<String,Integer> name2IndexMap = new HashMap<String,Integer>();
    
	public DataTableCorrelationMatrixAdapter(CorrelationMatrix matrix, String[] columnNames) {
        super("Correlation Matrix");
		this.matrix = matrix;
		this.index2NameMap = columnNames;
        for (int i = 0; i < this.index2NameMap.length; i++)
            this.name2IndexMap.put(this.index2NameMap[i], i);
	}

	public int getNumberOfSpecialColumns() {
		return 0;
	}
	
	public boolean isSpecial(int index) {
		return false;
	}
	
	public boolean isNominal(int index) {
		return index == 0;
	}
	
	public String mapIndex(int column, int value) {
		return this.index2NameMap[value];
	}
	
    /** Please note that this method does not map new strings but is only able to deliver strings which
     *  where already known during construction. */
	public int mapString(int column, String value) {
        Integer result = this.name2IndexMap.get(value);
        if (result == null)
            return -1;
        else
            return result;
	}
	
	public int getNumberOfValues(int column) {
		return this.index2NameMap.length;
	}
	
	public String getColumnName(int i) {
		if (i == 0)
			return "Attributes";
		else
			return this.index2NameMap[i - 1];
	}

	public int getColumnIndex(String name) {
		if (name.equals("Attributes")) {
			return 0;
		} else {
            return mapString(0, name) + 1;
		}
	}

    public boolean isSupportingColumnWeights() {
        return false;
    }
    
    public double getColumnWeight(int column) {
    	return Double.NaN;
    }
    
	public int getNumberOfColumns() {
		return this.index2NameMap.length + 1;
	}

	public void add(DataTableRow row) {
		throw new RuntimeException("DataTableCorrelationMatrixAdapter: adding new rows is not supported!");		
	}

    public DataTableRow getRow(int index) {
        return new CorrelationMatrixRow2DataTableRowWrapper(this.matrix, index);
    }
    
	public Iterator<DataTableRow> iterator() {
		return new CorrelationMatrixRow2DataTableRowIterator(this.matrix);
	}

	public int getNumberOfRows() {
		return this.index2NameMap.length;
	}
    
	/** Not implemented!!! Please use this class only for plotting purposes if you can ensure 
	 *  that the number of columns / rows is small. */
    public void sample(int newSize) {}
}
