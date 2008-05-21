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
package com.rapidminer.example.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.tools.LogService;


/**
 * This class is the core data supplier for example sets. Several example sets
 * can use the same data and access the attribute values by reference. In this
 * case the data is hold in the main memory during the process.
 * 
 * @author Ingo Mierswa
 * @version $Id: MemoryExampleTable.java,v 2.23 2006/03/23 17:48:24 ingomierswa
 *          Exp $
 */
public class MemoryExampleTable extends AbstractExampleTable {

	private static final long serialVersionUID = -3000023475208774934L;

	/** List of {@link DataRow}s. */
	private List<DataRow> dataList = new ArrayList<DataRow>();

	/** Number of columns. */
	private int columns;

	/** Number of columns to add when new columns are allocated. */
	private static final int INCREMENT = 10;

	/**
	 * Creates a new instance of MemoryExampleTable.
	 * 
	 * @param attributes
	 *            List of {@link Attribute} containing the attributes of the
	 *            columns. None of these must be null.
	 */
	public MemoryExampleTable(List<Attribute> attributes) {
		super(attributes);
		this.columns = attributes.size();
	}

	/**
	 * Creates a new instance of MemoryExampleTable.
	 * 
	 * @param attributes
	 *            List of {@link Attribute} containing the attributes of the
	 *            columns. None of these must be null.
	 * @param size
	 *            initial size of this example table. All values will be
	 *            Double.NaN.
	 */
	public MemoryExampleTable(List<Attribute> attributes, DataRowFactory factory, int size) {
		this(attributes);
		dataList = new ArrayList<DataRow>(size);
		for (int i = 0; i < size; i++) {
			DataRow dataRow = factory.create(attributes.size());
			for (Attribute attribute : attributes) {
				dataRow.set(attribute, Double.NaN);
			}
			dataList.add(dataRow);
		}
	}

	/**
	 * Creates an empty memory example table and fills it with the data rows
	 * read from i.
	 */
	public MemoryExampleTable(List<Attribute> attributes, DataRowReader i) {
		this(attributes, i, false);
	}

	/**
	 * Creates an empty memory example table and fills it with the data rows
	 * read from i.
	 */
	public MemoryExampleTable(List<Attribute> attributes, DataRowReader i, boolean permutate) {
		this(attributes);
		readExamples(i, permutate);
	}

	/**
	 * Reads the examples into memory in the order they are delivered by the
	 * given reader. Removes all old data rows first.
	 */
	public void readExamples(DataRowReader i) {
		readExamples(i, false);
	}

    /**
     * Reads the examples into memory and permutates the order. Removes all old
     * data rows first.
     */
    public void readExamples(DataRowReader i, boolean permutate) {
        readExamples(i, false, null);
    }
    
	/**
	 * Reads the examples into memory and permutates the order. Removes all old
	 * data rows first.
	 */
	public void readExamples(DataRowReader i, boolean permutate, Random random) {
		dataList.clear();
		while (i.hasNext()) {
			if (permutate) {
				int index = random.nextInt(dataList.size() + 1);
				dataList.add(index, i.next());
			} else {
				dataList.add(i.next());
			}
		}
	}

	/** Returns a new data row reader. */
	public DataRowReader getDataRowReader() {
		return new ListDataRowReader(dataList.iterator());
	}

	/** Returns the data row with the given index. */
	public DataRow getDataRow(int index) {
		return dataList.get(index);
	}

	/** Returns the size of this example table, i.e. the number of data rows. */
	public int size() {
		return dataList.size();
	}

	/**
	 * Convenience method allowing the adding of data rows without a data row
	 * reader.
	 */
	public void addDataRow(DataRow dataRow) {
		dataList.add(dataRow);
	}

	/** Convenience method for removing data rows. */
	public boolean removeDataRow(DataRow dataRow) {
		return dataList.remove(dataRow);
	}
	
	/** Convenience method for removing data rows. */
	public DataRow removeDataRow(int index) {
		return dataList.remove(index);
	}
	
	/** Clears the table. */
	public void clear() {
		dataList.clear();
	}
	
	/**
	 * Adds a new attribute to this example table by invoking the super method.
	 * If the number of attribues reaches a threshold, the number of attributes
	 * is increased by INCREMENT attributes. This avoids a large number of array
	 * copies in cases like automatic feature construction etc.
	 */
	public int addAttribute(Attribute attribute) {
		int index = super.addAttribute(attribute);
		if (dataList == null)
			return index;
		int n = getNumberOfAttributes();
		if (n <= columns)
			return index;
		int newSize = n + INCREMENT;
		LogService.getGlobal().log("Resizing example table from " + columns + " to " + newSize + " columns.", LogService.STATUS);
		columns = newSize;

		if (dataList != null) {
			Iterator<DataRow> i = dataList.iterator();
			while (i.hasNext())
				i.next().ensureNumberOfColumns(columns);
		}
		return index;
	}
    
	public static MemoryExampleTable createCompleteCopy(ExampleTable oldTable) {
        MemoryExampleTable table = new MemoryExampleTable(Arrays.asList(oldTable.getAttributes()));
        DataRowReader reader = oldTable.getDataRowReader();
        while (reader.hasNext()) {
            DataRow dataRow = reader.next();
            double[] newDataRowData = new double[oldTable.getNumberOfAttributes()];
            for (int a = 0; a < oldTable.getNumberOfAttributes(); a++) {
                Attribute attribute = oldTable.getAttribute(a);
                if (attribute != null) {
                    newDataRowData[a] = dataRow.get(attribute);
                } else {
                    newDataRowData[a] = Double.NaN;
                }
            }
            table.addDataRow(new DoubleArrayDataRow(newDataRowData));
        }
        return table;
    }
}
