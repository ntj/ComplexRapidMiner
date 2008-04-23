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
package com.rapidminer.operator.learner.associations.fpgrowth;

import java.util.LinkedList;

/**
 * A frequency stack based on a list implementation.
 * 
 * @author Sebastian Land
 * @version $Id: ListFrequencyStack.java,v 1.1 2007/05/27 22:02:33 ingomierswa Exp $
 */
public class ListFrequencyStack implements FrequencyStack {

	private LinkedList<Integer> list;

	public ListFrequencyStack() {
		list = new LinkedList<Integer>();
	}

	public int getFrequency(int height) {
		if (height >= list.size()) {
			return 0;
		} else if (height == list.size() - 1) {
			return list.getLast();
		} else {
			return list.get(height);
		}
	}

	public void increaseFrequency(int stackHeight, int value) {
		if (stackHeight == list.size() - 1) {
			// int newValue = value + list.pollLast(); // IM: pollLast only
			// available in JDK 6
			int newValue = value + list.removeLast();
			list.addLast(newValue);
		} else if (stackHeight == list.size()) {
			list.addLast(value);
		}
	}

	public void popFrequency(int height) {
		if (height == list.size() - 1) {
			// list.pollLast(); // IM: pollLast only available in JDK 6
			list.removeLast();
		} else if (height < list.size() - 1) {
			list.remove(height);
		}
	}
}
