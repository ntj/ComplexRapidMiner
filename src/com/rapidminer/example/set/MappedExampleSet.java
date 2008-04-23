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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.table.ExampleTable;


/**
 *  <p>This example set uses a mapping of indices to access the examples provided by the 
 *  parent example set. The mapping does not need to contain unique indices which is
 *  especially useful for sampling with replacement. For performance reasons (iterations,
 *  database access...) the mapping will be sorted during the construction of this example
 *  set (based on the parameter sort).</p>  
 *  
 *  <p>Please note that the constructor takes a boolean flag indicating if the examples
 *  from the given array are used or if the examples which are not part of this mapping
 *  should be used. This might be useful in the context of bootstrapped validation for
 *  example.</p>
 *  
 *  @author Ingo Mierswa, Martin Scholz
 *  @version $Id: MappedExampleSet.java,v 1.2 2007/07/10 18:02:03 ingomierswa Exp $
 */
public class MappedExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = -488025806523583178L;

	/** The parent example set. */
	private ExampleSet parent;
	
    /** The used mapping. */
    private int[] mapping;
    
    /** Constructs an example set based on the given mapping. */
    public MappedExampleSet(ExampleSet parent, int[] mapping) {
        this(parent, mapping, true);
    }
    
    /** Constructs an example set based on the given mapping. If the boolean flag 
     *  useMappedExamples is false only examples which are not part of the original 
     *  mapping are used. */
    public MappedExampleSet(ExampleSet parent, int[] mapping, boolean useMappedExamples) {
    	this.parent = parent;
        this.mapping = mapping; 
        Arrays.sort(this.mapping);
        
        if (!useMappedExamples) {
            List<Integer> inverseIndexList = new ArrayList<Integer>();
            int currentExample = -1;
            for (int m : mapping) {
                if (m != currentExample) {
                    for (int z = currentExample + 1; z < m; z++) {
                        inverseIndexList.add(z);
                    }
                    currentExample = m;
                }
            }
            this.mapping = new int[inverseIndexList.size()];
            Iterator<Integer> i = inverseIndexList.iterator();
            int index = 0;
            while (i.hasNext())
                this.mapping[index++] = i.next();
        }
    }

    /** Clone constructor. */
    public MappedExampleSet(MappedExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.parent.clone();
        this.mapping = exampleSet.mapping;
    }

    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof MappedExampleSet))
            return false;
        
        MappedExampleSet other = (MappedExampleSet)o;    
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
    
    /** Returns a {@link MappedExampleReader}. */
    public Iterator<Example> iterator() {
        return new MappedExampleReader(parent.iterator(), this.mapping);
    }

    /** Returns the i-th example in the mapping. */
    public Example getExample(int index) {
        if ((index < 0) || (index >= this.mapping.length)) {
            throw new RuntimeException("Given index '" + index + "' does not fit the mapped ExampleSet!");
        } else {
            return parent.getExample(this.mapping[index]);
        }
    }

    /** Counts the number of examples. */
    public int size() {
        return mapping.length;
    }
    
    public Attributes getAttributes() {
    	return parent.getAttributes();
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
	
    /** Creates a new mapping for the given example set by sampling with replacement. */
    public static int[] createBootstrappingMapping(ExampleSet exampleSet, int size, Random random) {
        int[] mapping = new int[size];
        for (int i = 0; i < mapping.length; i++)
            mapping[i] = random.nextInt(exampleSet.size());
        return mapping;
    }
    
    public static int[] createWeightedBootstrappingMapping(ExampleSet exampleSet, int size, Random random) {
        Attribute weightAttribute = exampleSet.getAttributes().getSpecial(Attributes.WEIGHT_NAME);
        exampleSet.recalculateAttributeStatistics(weightAttribute);
        double maxWeight = exampleSet.getStatistics(weightAttribute, Statistics.MAXIMUM);
        
        int[] mapping = new int[size];
        for (int i = 0; i < mapping.length; i++) {
            int index = -1;
            do {
                index = random.nextInt(exampleSet.size());
                Example example = exampleSet.getExample(index);
                double currentWeight = example.getValue(weightAttribute);
                if (random.nextDouble() > currentWeight / maxWeight) {
                    index = -1;
                }
            } while (index == -1);
            mapping[i] = index;
        }
        return mapping;
    }
}
