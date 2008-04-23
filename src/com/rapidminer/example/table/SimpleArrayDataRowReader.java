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

import com.rapidminer.example.Attribute;


/**
 * Creates a data row reader which uses an iterator over SimpleArrayData.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimpleArrayDataRowReader.java,v 2.7 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class SimpleArrayDataRowReader extends AbstractDataRowReader {

	private Iterator<SimpleArrayData> simpleData;

	private Attribute[] attributes;

	public SimpleArrayDataRowReader(DataRowFactory factory, Attribute[] attributes, Iterator<SimpleArrayData> simpleData) {
		super(factory);
		this.attributes = attributes;
		this.simpleData = simpleData;
	}

	public boolean hasNext() {
		return simpleData.hasNext();
	}

	public DataRow next() {
		return getFactory().create((simpleData.next()).getData(), attributes);
	}
}
