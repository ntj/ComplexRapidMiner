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
package com.rapidminer.datatable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.rapidminer.operator.visualization.SymmetricalMatrix;


/**
 * This class can be used to use all pairs (entries) of a correlation matrix as data table. The data is directly
 * read from the correlation matrix instead of building a copy. Please note that the method
 * for adding new rows is not supported by this type of data tables.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataTablePairwiseMatrixExtractionAdapter.java,v 1.2 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class DataTablePairwiseMatrixExtractionAdapter extends AbstractDataTable {

    private SymmetricalMatrix matrix;
    
    private String[] index2NameMap;
    
    private Map<String,Integer> name2IndexMap = new HashMap<String,Integer>();

    private String[] tableColumnNames;
    
    public DataTablePairwiseMatrixExtractionAdapter(SymmetricalMatrix matrix, String[] columnNames, String[] tableColumnNames) {
        super("Pairwise Correlation Table");
        this.matrix = matrix;
        this.index2NameMap = columnNames;
        for (int i = 0; i < this.index2NameMap.length; i++)
            this.name2IndexMap.put(this.index2NameMap[i], i);
        this.tableColumnNames = tableColumnNames;
        if ((this.tableColumnNames == null) || (this.tableColumnNames.length != 3))
        	throw new RuntimeException("Cannot create pairwise matrix extraction data table with other than 3 table column names.");
    }

    public int getNumberOfSpecialColumns() {
        return 0;
    }
    
    public boolean isSpecial(int index) {
        return false;
    }
    
    public boolean isNominal(int index) {
        return (index <= 1);
    }
    
    public String mapIndex(int column, int value) {
        return index2NameMap[value];
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
        return index2NameMap.length;
    }
    
    public String getColumnName(int i) {
    	return tableColumnNames[i];
    }

    public int getColumnIndex(String name) {
    	for (int i = 0; i < tableColumnNames.length; i++) {
    		if (tableColumnNames[i].equals(name))
    			return i;
    	}
    	return -1;
    }

    public boolean isSupportingColumnWeights() {
        return false;
    }
    
    public double getColumnWeight(int column) {
        return Double.NaN;
    }
    
    public int getNumberOfColumns() {
        return tableColumnNames.length;
    }

    public void add(DataTableRow row) {
        throw new RuntimeException("DataTablePairwiseCorrelationMatrixAdapter: adding new rows is not supported!");     
    }

    public DataTableRow getRow(int rowIndex) {
        int firstAttribute = 0;
        int secondAttribute = 1;
        for (int i = 0; i < rowIndex; i++) {
            secondAttribute++;
            if (secondAttribute >= matrix.getNumberOfColumns()) {
                firstAttribute++;
                secondAttribute = firstAttribute + 1;
            }
        }
        return new PairwiseCorrelation2DataTableRowWrapper(this.matrix, firstAttribute, secondAttribute);
    }
    
    public Iterator<DataTableRow> iterator() {
        return new PairwiseCorrelation2DataTableRowIterator(this.matrix);
    }

    public int getNumberOfRows() {
        return ((index2NameMap.length * index2NameMap.length) - index2NameMap.length) / 2;
    }
    
    /** Not implemented!!! Please use this class only for plotting purposes if you can ensure 
     *  that the number of columns / rows is small. */
    public void sample(int newSize) {}
}
