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
package com.rapidminer.example.table;

import java.io.Serializable;

import com.rapidminer.example.Attribute;


/**
 * This interface defines methods for all entries of ExampleTable
 * implementations. It provides a set and get method for the data. Subclasses
 * may use a double array, a sparse representation, a file or a database.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: DataRow.java,v 1.1 2007/05/27 22:01:19 ingomierswa Exp $
 */
public abstract class DataRow implements Serializable {
	
	/** Returns the value for the given index. */
	/* pp */ abstract double get(int index, double defaultValue);

	/** Sets the given data for the given index. */
	/* pp */ abstract void set(int index, double value, double defaultValue);

	/**
	 * Ensures that neither <code>get(i)</code> nor <code>put(i,v)</code>
	 * throw a runtime exception for all <i>0 <= i <= numberOfColumns</i>.
	 */
	/* pp */ abstract void ensureNumberOfColumns(int numberOfColumns);

	/** Trims the number of columns to the actually needed number. */
	public abstract void trim();
	
	/** Returns a string representation for this data row. */
	public abstract String toString();
	
	/** Returns the value stored at the given {@link Attribute}'s index. 
	 *  Returns Double.NaN if the given attribute is null. */
	public double get(Attribute attribute) {
		if (attribute == null)
			return Double.NaN;
		else
			return attribute.getValue(this);
	}
	
	/** Sets the value of the {@link Attribute} to <code>value</code>. */
	public void set(Attribute attribute, double value) {
		attribute.setValue(this, value);
	}
}
