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
 * Creates a simple non-shuffled partition for an example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimplePartitionBuilder.java,v 2.5 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class SimplePartitionBuilder implements PartitionBuilder {

	/** Returns a simple non-shuffled partition for an example set. */
	public int[] createPartition(double[] ratio, int size) {
		// determine partition starts
		int[] startNewP = new int[ratio.length + 1];
		startNewP[0] = 0;
		double ratioSum = 0;
		for (int i = 1; i < startNewP.length; i++) {
			ratioSum += ratio[i - 1];
			startNewP[i] = (int) Math.round(size * ratioSum);
		}

		// create a simple partition
		int p = 0;
		int[] part = new int[size];
		for (int i = 0; i < part.length; i++) {
			if (i >= startNewP[p + 1])
				p++;
			part[i] = p;
		}

		return part;
	}
}
