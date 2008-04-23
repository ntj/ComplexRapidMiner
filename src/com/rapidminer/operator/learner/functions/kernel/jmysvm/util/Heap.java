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
 * Implements a Heap on n doubles and ints
 * 
 * @author Stefan Rueping
 * @version $Id: Heap.java,v 1.1 2007/06/15 18:44:30 ingomierswa Exp $
 */
public abstract class Heap {

	protected int the_size;

	protected int last;

	protected double[] heap;

	protected int[] indizes;

	public Heap() {};

	public Heap(int n) {
		the_size = 0;
		init(n);
	};

	public int size() {
		return last; // last = number of elements
	};

	public void init(int n) {
		if (the_size != n) {
			the_size = n;
			heap = new double[n];
			indizes = new int[n];
		};
		last = 0;
	};

	public void clear() {
		the_size = 0;
		last = 0;
		heap = null;
		indizes = null;
	};

	public int[] get_values() {
		return indizes;
	};

	public abstract void add(double value, int index);

	public double top_value() {
		return heap[0];
	};

	public boolean empty() {
		return (last == 0);
	};

	protected abstract void heapify(int start, int size);

};
