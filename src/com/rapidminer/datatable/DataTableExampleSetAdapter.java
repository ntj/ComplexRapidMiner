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
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SplittedExampleSet;


/**
 * This class can be used to use an example set as data table. The data is directly
 * read from the example set instead of building a copy. Please note that the method
 * for adding new rows is not supported by this type of data tables.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataTableExampleSetAdapter.java,v 1.2 2007/06/10 23:18:53 ingomierswa Exp $
 */
public class DataTableExampleSetAdapter extends AbstractDataTable {

	private ExampleSet exampleSet;
	
	private List<Attribute> allAttributes = new ArrayList<Attribute>();
	
	private int numberOfRegularAttributes = 0;
	
    private AttributeWeights weights = null;
    
    private Attribute idAttribute;
    
    
	public DataTableExampleSetAdapter(ExampleSet exampleSet, AttributeWeights weights) {
        super("Example Set");
		this.exampleSet = exampleSet;
        this.weights = weights;
        
        for (Attribute attribute : exampleSet.getAttributes()) {
        	allAttributes.add(attribute);
		}
        
        this.idAttribute = exampleSet.getAttributes().getId();
        Iterator<AttributeRole> s = exampleSet.getAttributes().specialAttributes();
        while (s.hasNext()) {
        	Attribute specialAttribute = s.next().getAttribute();
        	if ((idAttribute == null) || (!idAttribute.equals(specialAttribute)))
        		allAttributes.add(specialAttribute);
        }
        
        this.numberOfRegularAttributes = exampleSet.getAttributes().size();
	}

	public int getNumberOfSpecialColumns() {
		return allAttributes.size() - numberOfRegularAttributes;
	}
	
	public boolean isSpecial(int index) {
		return index >= numberOfRegularAttributes;
	}
	
	public boolean isNominal(int index) {
		return allAttributes.get(index).isNominal();
	}
	
	public String mapIndex(int column, int value) {
		return allAttributes.get(column).getMapping().mapIndex(value);
	}
	
	public int mapString(int column, String value) {
		return allAttributes.get(column).getMapping().mapString(value);
	}
	
	public int getNumberOfValues(int column) {
		return allAttributes.get(column).getMapping().size();
	}
	
	public String getColumnName(int i) {
		return allAttributes.get(i).getName();
	}

	public int getColumnIndex(String name) {
		for (int i = 0; i < allAttributes.size(); i++)
			if (allAttributes.get(i).getName().equals(name))
				return i;
		return -1;
	}

    public boolean isSupportingColumnWeights() {
        return weights != null;
    }
    
    public double getColumnWeight(int column) {
        if (weights == null)
            return Double.NaN;
        else
            return weights.getWeight(getColumnName(column));
    }
    
	public int getNumberOfColumns() {
		return this.allAttributes.size();
	}

	public void add(DataTableRow row) {
		throw new RuntimeException("DataTableExampleSetAdapter: adding new rows is not supported!");		
	}

    public DataTableRow getRow(int index) {
        return new Example2DataTableRowWrapper(exampleSet.getExample(index), allAttributes, idAttribute);
    }
    
	public Iterator<DataTableRow> iterator() {
		return new Example2DataTableRowIterator(exampleSet.iterator(), allAttributes, idAttribute);
	}

	public int getNumberOfRows() {
		return this.exampleSet.size();
	}
    
    public void sample(int newSize) {
    	double ratio = (double)newSize / (double)getNumberOfRows(); 
        this.exampleSet = new SplittedExampleSet(exampleSet, ratio, SplittedExampleSet.SHUFFLED_SAMPLING, -1);
        ((SplittedExampleSet)this.exampleSet).selectSingleSubset(0);
    }
}
