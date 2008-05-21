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
package com.rapidminer.tools;

/**
 * This class can be used to build pairs of typed objects and sort them.
 * 
 * @author Sebastian Land
 * @version $Id: Tupel.java,v 1.5 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class Tupel<T1 extends Comparable<T1>, T2> implements Comparable<Tupel<T1, T2>> {
	
	private T1 t1;

	private T2 t2;

	public Tupel(T1 t1, T2 t2) {
		this.t1 = t1;
		this.t2 = t2;
	}

	public T1 getFirst() {
		return t1;
	}

	public T2 getSecond() {
		return t2;
	}

	public int compareTo(Tupel<T1, T2> o) {
		return t1.compareTo(o.getFirst());
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof Tupel))
			return false;
		Tupel a = (Tupel) o;
		if (!this.t1.equals(a.t1))
			return false;
		return true;
	}
	
    public int hashCode() {
    	return this.t1.hashCode();
    }
}
