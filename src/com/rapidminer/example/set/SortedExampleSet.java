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
package com.rapidminer.example.set;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;


/**
 *  <p>This example set uses a mapping of indices to access the examples provided by the 
 *  parent example set. In contrast to the mapped example set, where the sorting would 
 *  have been disturbed for performance reasons this class simply use the given mapping.
 *  A convenience constructor exist to create a view based on the sorting based on a
 *  specific attribute.</p>
 *  
 *  <p>
 *  Please note that this implementation is quite inefficient on databases and other
 *  non-memory example tables and should therefore only be used for small data sets.
 *  </p>
 *  
 *  @author Ingo Mierswa
 *  @version $Id: SortedExampleSet.java,v 1.3 2007/07/13 22:52:12 ingomierswa Exp $
 */
public class SortedExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = 3937175786207007275L;

	public static final String[] SORTING_DIRECTIONS = {
		"increasing",
		"decreasing"
	};
	
	public static final int INCREASING = 0;
	public static final int DECREASING = 1;

	private static class SortingIndex implements Comparable<SortingIndex> {
		
		private Object key;
		private int index;
		
		public SortingIndex(Object key, int index) {
			this.key   = key;
			this.index = index;
		}

		public int hashCode() {
			if (key instanceof Double) {
				return ((Double)key).hashCode();
			} else if (key instanceof String) {
				return ((String)key).hashCode();
			} else {
				return 42;
			}
		}
		
		public boolean equals(Object other) {
			if (!(other instanceof SortingIndex))
				return false;
			SortingIndex o = (SortingIndex)other;
			if (key instanceof Double) {
				return ((Double)key).equals(o.key);
			} else if (key instanceof String) {
				return ((String)key).equals(o.key);
			}
			return true;
		}
		
		public int compareTo(SortingIndex o) {
			if (key instanceof Double) {
				return ((Double)key).compareTo((Double)o.key);
			} else if (key instanceof String) {
				return ((String)key).compareTo((String)o.key);
			}
			return 0;
		}
		
		public int getIndex() { return index; }
		
		public String toString() { return key + " --> " + index; }
	}
	
	
	/** The parent example set. */
	private ExampleSet parent;
	
    /** The used mapping. */
    private int[] mapping;
    
    public SortedExampleSet(ExampleSet parent, Attribute sortingAttribute, int sortingDirection) {
    	this.parent = parent;
		List<SortingIndex> sortingIndex = new ArrayList<SortingIndex>(parent.size());
		
		int counter = 0;
		Iterator<Example> i = parent.iterator();
		while (i.hasNext()) {
			Example example = i.next();
			if (sortingAttribute.isNominal()) {
				sortingIndex.add(new SortingIndex(example.getNominalValue(sortingAttribute), counter));
			} else {
				sortingIndex.add(new SortingIndex(Double.valueOf(example.getNumericalValue(sortingAttribute)), counter));
			}
			counter++;
		}
		
		Collections.sort(sortingIndex);
		
		int[] mapping = new int[parent.size()];
		counter = 0;
		Iterator<SortingIndex> k = sortingIndex.iterator();
		while (k.hasNext()) {
			int index = k.next().getIndex();
			if (sortingDirection == INCREASING) {
				mapping[counter] = index;
			} else {
				mapping[parent.size() - 1 - counter] = index;
			}
			counter++;
		}
		
		this.mapping = mapping;
    }
    
    /** Constructs an example set based on the given sort mapping. If the boolean flag 
     *  useMappedExamples is false only examples which are not part of the original 
     *  mapping are used. */
    public SortedExampleSet(ExampleSet parent, int[] mapping) {
    	this.parent = parent;
        this.mapping = mapping; 
    }

    /** Clone constructor. */
    public SortedExampleSet(SortedExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.parent.clone();
        this.mapping = exampleSet.mapping;
    }

    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof SortedExampleSet))
            return false;
        
        SortedExampleSet other = (SortedExampleSet)o;    
        if (this.mapping.length != other.mapping.length)
            return false;
        for (int i = 0; i < this.mapping.length; i++) 
            if (this.mapping[i] != other.mapping[i])
                return false;
        return true;
    }

    public int hashCode() {
        return super.hashCode() ^ this.mapping.hashCode();
    }
    
    /** Returns a {@link SortedExampleReader}. */
    public Iterator<Example> iterator() {
        return new SortedExampleReader(this.parent, this.mapping);
    }

    /** Returns the i-th example in the mapping. */
    public Example getExample(int index) {
        if ((index < 0) || (index >= this.mapping.length)) {
            throw new RuntimeException("Given index '" + index + "' does not fit the mapped ExampleSet!");
        } else {
            return this.parent.getExample(this.mapping[index]);
        }
    }

    /** Counts the number of examples. */
    public int size() {
        return mapping.length;
    }

	public Attributes getAttributes() {
		return this.parent.getAttributes();
	}

    /**
     * Returns the example with the given index.
     */
    public Example getExampleFromId(double id) {
        return new Example(parent.getExampleFromId(id).getDataRow(), this);
    }

	public ExampleTable getExampleTable() {
		return parent.getExampleTable();
	}

	public void remapIds() {
		parent.remapIds();
	}
}
