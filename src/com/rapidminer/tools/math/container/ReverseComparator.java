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
package com.rapidminer.tools.math.container;

import java.util.Comparator;

/**
 * This comparator reverses the sort direction for a given comparator.
 * 
 * @author Sebastian Land
 * @version $Id: ReverseComparator.java,v 1.4 2008/07/13 23:25:24 ingomierswa Exp $
 * @param <F> the type of the comparable
 */
public class ReverseComparator<F> implements Comparator<F> {
	
	private Comparator<? super F> comparator;
	
	public ReverseComparator(Comparator<? super F> comp) {
		this.comparator = comp;
	}
	
	public int compare(F o1, F o2) {
		return -comparator.compare(o1, o2);
	}
}
