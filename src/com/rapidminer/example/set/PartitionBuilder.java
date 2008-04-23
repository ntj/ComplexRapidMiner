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

/**
 * Creates partitions from ratio arrays. Subclasses might shuffle the examples
 * before building the partition or apply stratification. The delivered
 * partitions consist of an array of integer values with the same length as the
 * given size. For each element the integer defines the number of the partition
 * for this element. Numbering starts with 0.
 * 
 * @author Ingo Mierswa
 * @version $Id: PartitionBuilder.java,v 1.1 2007/05/27 21:59:00 ingomierswa Exp $
 */
public interface PartitionBuilder {

	/**
	 * Creates a partition from the given ratios. Size is the number of
	 * elements, i.e. the number of examples.
	 */
	public int[] createPartition(double[] ratio, int size);
}
