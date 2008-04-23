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

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.tools.LogService;


/**
 * Implements a partition. A partition is used to divide an example set into
 * different parts of arbitrary sizes without actually make a copy of the data.
 * Partitions are used by {@link SplittedExampleSet}s. Partition numbering
 * starts at 0.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: Partition.java,v 1.2 2007/05/28 21:23:34 ingomierswa Exp $
 */
public class Partition implements Cloneable, Serializable {

	private static final long serialVersionUID = 6126334515107973287L;

	/** Mask for the selected partitions. */
	private boolean[] mask;

	/** Size of the individual partitions. */
	private int[] partitionSizes;

	/** Maps every example to its partition index. */
	private int[] splitPartition;

	/**
	 * Maps every example index to the true index of the data row in the example
	 * table.
	 */
	private int[] tableIndexMap = null;

	/**
	 * Creates a new partition of a given size consisting of
	 * <tt>ratio.length</tt> sets. The set <i>i</i> will be of size of
	 * <i>size x ratio[i]</i>, i.e. the sum of all <i>ratio[i]</i> must be 1.
	 * Initially all partitions are selected.
	 */
	public Partition(double ratio[], int size, PartitionBuilder builder) {
		init(ratio, size, builder);
	}

	/**
	 * Creates a new partition of a given size consisting of <i>noPartitions</i>
	 * equally sized sets. Initially all partitions are selected.
	 */
	public Partition(int noPartitions, int size, PartitionBuilder builder) {
		double[] ratio = new double[noPartitions];
		for (int i = 0; i < ratio.length; i++) {
			ratio[i] = 1 / (double) noPartitions;
		}
		init(ratio, size, builder);
	}

	/** Creates a partition from the given one. Partition numbering starts at 0. */
	public Partition(int[] splitPartition, int numberOfPartitions) {
		init(splitPartition, numberOfPartitions);
	}

	/** Clone constructor. */
	private Partition(Partition p) {
		this.partitionSizes = p.partitionSizes.clone();
		this.mask = p.mask.clone();
		this.splitPartition = p.splitPartition.clone();
		recalculateTableIndices();
	}

	/**
	 * Creates a partition from the given ratios. The partition builder is used
	 * for creation.
	 */
	private void init(double[] ratio, int size, PartitionBuilder builder) {
		LogService.getGlobal().log("Create new partition using a '" + builder.getClass().getName() + "'.", LogService.STATUS);
		splitPartition = builder.createPartition(ratio, size);
		init(splitPartition, ratio.length);
	}

	/** Private initialization method used by constructors. */
	private void init(int[] elements, int noOfPartitions) {
		LogService.getGlobal().log("Create new partition with " + elements.length + " elements and " + noOfPartitions + " partitions.", LogService.STATUS);
		partitionSizes = new int[noOfPartitions];
		splitPartition = elements;
		for (int i = 0; i < splitPartition.length; i++)
			if (splitPartition[i] >= 0)
				partitionSizes[splitPartition[i]]++;

		mask = new boolean[noOfPartitions];
		for (int i = 0; i < mask.length; i++)
			mask[i] = true;
		recalculateTableIndices();
	}

    public boolean equals(Object o) {
        if (!(o instanceof Partition))
            return false;
        
        Partition other = (Partition)o;
        
        for (int i = 0; i < mask.length; i++)
            if (this.mask[i] != other.mask[i])
                return false;

        for (int i = 0; i < splitPartition.length; i++)
            if (this.splitPartition[i] != other.splitPartition[i])
                return false;
                
        return true;
    }

    public int hashCode() {
        int hc = 17;
        int hashMultiplier = 59;
        
        hc = hc * hashMultiplier + this.mask.length;
        for (int i = 1; i < mask.length; i <<= 1) {
           hc = hc * hashMultiplier + Boolean.valueOf(this.mask[i]).hashCode();
        }
        
        hc = hc * hashMultiplier + this.splitPartition.length;
        for (int i = 1; i < splitPartition.length; i <<= 1) {
           hc = hc * hashMultiplier + Integer.valueOf(this.splitPartition[i]).hashCode();
        }
        
        return hc; 
    }
    
	/** Clears the selection, i.e. deselects all subsets. */
	public void clearSelection() {
		this.mask = new boolean[mask.length];
		recalculateTableIndices();
	}

	public void invertSelection() {
		for (int i = 0; i < mask.length; i++)
			mask[i] = !mask[i];
		recalculateTableIndices();
	};

	/** Marks the given subset as selected. */
	public void selectSubset(int i) {
		this.mask[i] = true;
		recalculateTableIndices();
	}

	/** Marks the given subset as deselected. */
	public void deselectSubset(int i) {
		this.mask[i] = false;
		recalculateTableIndices();
	}

	/** Returns the number of subsets. */
	public int getNumberOfSubsets() {
		return partitionSizes.length;
	}

	/** Returns the number of selected elements. */
	public int getSelectionSize() {
		int s = 0;
		for (int i = 0; i < partitionSizes.length; i++)
			if (mask[i])
				s += partitionSizes[i];
		return s;
	}

	/** Returns the total number of examples. */
	public int getTotalSize() {
		return splitPartition.length;
	}

	/**
	 * Returns true iff the example with the given index is selected according
	 * to the current selection mask.
	 */
	public boolean isSelected(int index) {
		return mask[splitPartition[index]];
	}

	/**
	 * Recalculates the example table indices of the currently selected
	 * examples.
	 */
	private void recalculateTableIndices() {
		List<Integer> indices = new LinkedList<Integer>();
		for (int i = 0; i < splitPartition.length; i++) {
			if (mask[splitPartition[i]]) {
				indices.add(i);
			}
		}
		tableIndexMap = new int[indices.size()];
		Iterator<Integer> i = indices.iterator();
		int counter = 0;
		while (i.hasNext()) {
			tableIndexMap[counter++] = i.next();
		}
	}

	/**
	 * Returns the actual example table index of the i-th example of the
	 * currently selected subset.
	 */
	public int mapIndex(int index) {
		return tableIndexMap[index];
	}

	public String toString() {
		StringBuffer str = new StringBuffer("(");
		for (int i = 0; i < partitionSizes.length; i++)
			str.append((i != 0 ? "/" : "") + partitionSizes[i]);
		str.append(")");
		return str.toString();
	}

	public Object clone() {
		return new Partition(this);
	}
}
