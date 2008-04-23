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
package com.rapidminer.operator.learner.functions.kernel.jmysvm.util;

/**
 * Implements a MaxHeap on n doubles and ints
 * 
 * @author Stefan Rueping
 * @version $Id: MaxHeap.java,v 1.1 2007/06/15 18:44:31 ingomierswa Exp $
 */
public class MaxHeap extends Heap {

	public MaxHeap(int n) {
		the_size = 0;
		init(n);
	};

	public final void add(double value, int index) {
		if (last < the_size) {
			heap[last] = value;
			indizes[last] = index;
			last++;
			if (last == the_size) {
				for (int j = last; j > 0; j--) {
					heapify(j - 1, last + 1 - j);
				};
			};
		} else if (value >= heap[0]) {
			heap[0] = value;
			indizes[0] = index;
			heapify(0, last);
		};
	};

	protected final void heapify(int start, int size) {
		double[] my_heap = heap;
		boolean running = true;
		int pos = 1;
		int left, right, largest;
		double dummyf;
		int dummyi;
		start--; // other variables counted from 1
		while (running) {
			left = 2 * pos;
			right = left + 1;
			if ((left <= size) && (my_heap[left + start] < my_heap[start + pos])) {
				largest = left;
			} else {
				largest = pos;
			};
			if ((right <= size) && (my_heap[start + right] < my_heap[start + largest])) {
				largest = right;
			};
			if (largest == pos) {
				running = false;
			} else {
				dummyf = my_heap[start + pos];
				dummyi = indizes[start + pos];
				my_heap[start + pos] = my_heap[start + largest];
				indizes[start + pos] = indizes[start + largest];
				my_heap[start + largest] = dummyf;
				indizes[start + largest] = dummyi;
				pos = largest;
			};
		};
	};
};
