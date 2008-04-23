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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;


/**
 * A simple implementation of ExampleSet containing a list of attributes and a
 * special attribute map. The data is queried from an example table which
 * contains the data (example sets actually are only views on this table and
 * does not keep any data). This simple example set implementation usually is
 * the basic example set of the multi-layered data view.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: SimpleExampleSet.java,v 2.41 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public class SimpleExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = 9163340881176421801L;

	/** The table used for reading the examples from. */
	private ExampleTable exampleTable;

	/** Holds all information about the attributes. */
	private Attributes attributes = new SimpleAttributes();

	/** Maps the id values on the line index in the example table. */
	private Map<Double, Integer> idMap = new HashMap<Double, Integer>();
	
	/**
	 * Constructs a new SimpleExampleSet backed by the given example table. The
	 * example set initially does not have any special attributes but all attributes
	 * from the given table will be used as regular attributes.
	 */
	public SimpleExampleSet(ExampleTable exampleTable) {
		this(exampleTable, null, null);
	}

	/**
	 * Constructs a new SimpleExampleSet backed by the given example table. The
	 * example set initially does not have any special attributes but all attributes
	 * from the given table will be used as regular attributes.
	 */
	public SimpleExampleSet(ExampleTable exampleTable, List<Attribute> regularAttributes) {
		this(exampleTable, regularAttributes, null);
	}
	
	/**
	 * Constructs a new SimpleExampleSet backed by the given example table. All
	 * attributes in the table apart from the special attributes become normal (regular)
	 * attributes. The special attributes are specified by the given map.
	 */
	public SimpleExampleSet(ExampleTable exampleTable, Map<Attribute, String> specialAttributes) {
		this(exampleTable, null, specialAttributes);
	}
	
	/**
	 * Constructs a new SimpleExampleSet backed by the given example table. All
	 * attributes in the table defined in the regular attribute list apart from those 
	 * (also) defined the special attributes become normal (regular) attributes. 
	 * The special attributes are specified by the given map.
	 */
	public SimpleExampleSet(ExampleTable exampleTable, List<Attribute> regularAttributes, Map<Attribute, String> specialAttributes) {
		this.exampleTable = exampleTable;
		List<Attribute> regularList = regularAttributes;
		if (regularList == null) {
			regularList = new LinkedList<Attribute>();
			for (int a = 0; a < exampleTable.getNumberOfAttributes(); a++)
				regularList.add(exampleTable.getAttribute(a));	
		}
		
		for (Attribute attribute : regularList) {
			if ((specialAttributes == null) || (specialAttributes.get(attribute) == null))
				getAttributes().add(new AttributeRole(attribute));
		}
		
		if (specialAttributes != null) {
			Iterator<Map.Entry<Attribute, String>> s = specialAttributes.entrySet().iterator();
			while (s.hasNext()) {
				Map.Entry<Attribute, String> entry = s.next();
				getAttributes().setSpecialAttribute(entry.getKey(), entry.getValue());
			}
		}
		remapIds();
	}
	
	/** Clone constructor. The example table is copied by reference, the attributes are 
	 *  copied by a deep clone. */
	public SimpleExampleSet(SimpleExampleSet exampleSet) {
		this.exampleTable = exampleSet.exampleTable;
		this.attributes = (Attributes)exampleSet.getAttributes().clone();
		this.idMap.putAll(exampleSet.idMap);
	}

	// --- attributes ---
	
	public Attributes getAttributes() {
		return attributes;
	}
	
	// --- examples ---

	public ExampleTable getExampleTable() {
		return exampleTable;
	}
	
	public int size() {
		return exampleTable.size();
	}

	public Iterator<Example> iterator() {
		return new SimpleExampleReader(getExampleTable().getDataRowReader(), this);
	}
	
	public Example getExampleFromId(double id) {
		return createExample(getDataRowFromId(id));
	}
	
	public Example getExample(int index) {
		return createExample(getExampleTable().getDataRow(index));
	}

	/**
	 * Creates an example for the given data row. Returns null if the given data
	 * is null.
	 */
	private Example createExample(DataRow dataRow) {
		if (dataRow == null)
			return null;
		return new Example(dataRow, this);
	}
	
	/**
	 * This method can be used by subclasses to determine the data row with a
	 * given id value. This data row is used to construct the desired example.
	 * This method returns null if no data row with the given id exists.
	 */
	private DataRow getDataRowFromId(double id) {
		Integer indexObject = idMap.get(id);
		if (indexObject != null) {
			return getExampleTable().getDataRow(indexObject.intValue());
		} else {
			return null;
		}
	}

	/**
	 * Remaps all ids. This method should be invoked after Id tagging or example
	 * set loading.
	 */
	public void remapIds() {
		idMap.clear();
		Attribute idAttribute = getAttributes().getSpecial(Attributes.ID_NAME);
		if (idAttribute != null) {
			DataRowReader reader = getExampleTable().getDataRowReader();
			int index = 0;
			while (reader.hasNext()) {
				DataRow dataRow = reader.next();
				idMap.put(dataRow.get(idAttribute), index);
				index++;
			}
		}
	}
}
