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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.rapidminer.operator.learner.functions.kernel.KernelModel;


/**
 * This class can be used to use a kernel model as data table. The data is directly
 * read from the kernel model instead of building a copy. Please note that the method
 * for adding new rows is not supported by this type of data tables.
 * 
 * @author Ingo Mierswa
 * @version $Id: DataTableKernelModelAdapter.java,v 1.5 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class DataTableKernelModelAdapter extends AbstractDataTable {

	/** Helper class to iterated over the examples or support vectors of a {@link KernelModel}. */
	private static class KernelModelIterator implements Iterator<DataTableRow> {
		
		private int counter = 0;
		
		private DataTableKernelModelAdapter adapter;
		
		public KernelModelIterator(DataTableKernelModelAdapter adapter) {
			this.adapter = adapter;
		}
		
		public boolean hasNext() {
			return counter < adapter.getNumberOfRows();
		}
		
		public DataTableRow next() {
			DataTableRow row = adapter.getRow(counter);
			counter++;
			return row;
		}
		
		public void remove() {
			throw new RuntimeException("DataTable.KernelModelIterator: remove not supported!");
		}
	}
		
	private KernelModel kernelModel;
	
	private int[] sampleMapping = null;
	
	private Map<Integer,String> index2LabelMap = new HashMap<Integer,String>();
	private Map<String,Integer> label2IndexMap = new HashMap<String,Integer>();
	
	public DataTableKernelModelAdapter(KernelModel kernelModel) {
        super("Kernel Model Support Vectors");
		this.kernelModel = kernelModel;
		int labelCounter = 0;
		if (this.kernelModel.isClassificationModel()) {
			for (int i = 0; i < this.kernelModel.getNumberOfSupportVectors(); i++) {
				String label = this.kernelModel.getClassificationLabel(i);
				if (label2IndexMap.get(label) == null) {	
					this.label2IndexMap.put(label, labelCounter);
					this.index2LabelMap.put(labelCounter, label);
					labelCounter++;
				}
			}
		}
	}

	public int getNumberOfSpecialColumns() {
		return KernelModelRow2DataTableRowWrapper.NUMBER_OF_SPECIAL_COLUMNS;
	}
	
	public boolean isSpecial(int index) {
		return index < KernelModelRow2DataTableRowWrapper.NUMBER_OF_SPECIAL_COLUMNS;
	}
	
	public boolean isNominal(int index) {
		if (index == KernelModelRow2DataTableRowWrapper.LABEL) {
			return this.kernelModel.isClassificationModel();
		} else {
			return index == KernelModelRow2DataTableRowWrapper.SUPPORT_VECTOR;
		}
	}
	
	public String mapIndex(int column, int value) {
		if ((column == KernelModelRow2DataTableRowWrapper.LABEL) && (this.kernelModel.isClassificationModel())) {
			return index2LabelMap.get(value);
		} else if (column == KernelModelRow2DataTableRowWrapper.SUPPORT_VECTOR) {
		 	if (value == 0)
		 		return "no support vector";
		 	else
		 		return "support vector";
		} else {
			return null;
		}
	}
	
	public int mapString(int column, String value) {
		if ((column == KernelModelRow2DataTableRowWrapper.LABEL) && (this.kernelModel.isClassificationModel())) {
			return label2IndexMap.get(value);
		} else if (column == KernelModelRow2DataTableRowWrapper.SUPPORT_VECTOR) {
		 	if ("no support vector".equals(value))
		 		return 0;
		 	else
		 		return 1;
		} else {
			return -1;
		}
	}
	
	public int getNumberOfValues(int column) {
		if ((column == KernelModelRow2DataTableRowWrapper.LABEL) && (this.kernelModel.isClassificationModel())) {
			return index2LabelMap.size();
		} else if (column == KernelModelRow2DataTableRowWrapper.SUPPORT_VECTOR) {
			return 2;
		} else {
			return -1;
		}
	}
	
	public String getColumnName(int i) {
		if (i < KernelModelRow2DataTableRowWrapper.NUMBER_OF_SPECIAL_COLUMNS) {
			return KernelModelRow2DataTableRowWrapper.SPECIAL_COLUMN_NAMES[i];
		} else {
			return "attribute" + ((i - KernelModelRow2DataTableRowWrapper.NUMBER_OF_SPECIAL_COLUMNS) +1);
		}
	}

	public int getColumnIndex(String name) {
		for (int i = 0; i < KernelModelRow2DataTableRowWrapper.NUMBER_OF_SPECIAL_COLUMNS; i++)
			if (KernelModelRow2DataTableRowWrapper.SPECIAL_COLUMN_NAMES[i].equals(name))
				return i;
		if (name.startsWith("attribute")) {
			return Integer.parseInt(name.substring("attribute".length())) - 1;
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
		return kernelModel.getNumberOfAttributes() + KernelModelRow2DataTableRowWrapper.NUMBER_OF_SPECIAL_COLUMNS;
	}

	public int getNumberOfRows() {
		if (this.sampleMapping == null)
			return this.kernelModel.getNumberOfSupportVectors();
		else
			return this.sampleMapping.length;
	}

	public void add(DataTableRow row) {
		throw new RuntimeException("DataTableKernelModelAdapter: adding new rows is not supported!");		
	}

    public DataTableRow getRow(int index) {
    	if (this.sampleMapping == null) {
        	return new KernelModelRow2DataTableRowWrapper(this.kernelModel, this, index);
    	} else {
    		return new KernelModelRow2DataTableRowWrapper(this.kernelModel, this, this.sampleMapping[index]);
    	}
    }
    
	public Iterator<DataTableRow> iterator() {
		return new KernelModelIterator(this);
	}
    
    public void sample(int newSize) {
    	double ratio = (double)newSize / (double)getNumberOfRows();
    	Random random = new Random(2001);
    	List<Integer> usedRows = new LinkedList<Integer>();
    	for (int i = 0; i < getNumberOfRows(); i++) {
    		if (random.nextDouble() <= ratio) {
    			int index = i;
    			if (this.sampleMapping != null)
    				index = this.sampleMapping[index];
    			usedRows.add(index);
    		}
    	}
    	this.sampleMapping = new int[usedRows.size()];
    	int counter = 0;
    	Iterator<Integer> i = usedRows.iterator();
    	while (i.hasNext()) {
    		this.sampleMapping[counter++] = i.next();
    	}
    }
}
