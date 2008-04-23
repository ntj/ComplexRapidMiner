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

import java.util.Iterator;


/**
 * Iterates over a list of DataRows. Actually a misnomer because this class does
 * not use a list but an iterator over an arbitrary collection.
 * 
 * @author Ingo Mierswa
 * @version $Id: ListDataRowReader.java,v 2.7 2006/03/21 15:35:39 ingomierswa
 *          Exp $
 */
public class ListDataRowReader implements DataRowReader {

	private Iterator<DataRow> iterator;

	public ListDataRowReader(Iterator<DataRow> i) {
		this.iterator = i;
	}

	public boolean hasNext() {
		return iterator.hasNext();
	}

	public DataRow next() {
		return iterator.next();
	}

	/** Will throw a new {@link UnsupportedOperationException} since {@link DataRowReader} does not have
	 *  to implement remove. */
	public void remove() {
		throw new UnsupportedOperationException("The method 'remove' is not supported by DataRowReaders!");
	}
}
