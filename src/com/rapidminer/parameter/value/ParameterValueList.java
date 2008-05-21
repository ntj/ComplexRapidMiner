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
package com.rapidminer.parameter.value;

import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterType;


/**
 * A list of parameter values.
 * 
 * @author Tobias Malbrecht
 * @version $Id: ParameterValueList.java,v 1.2 2008/05/09 19:23:26 ingomierswa Exp $
 */
public class ParameterValueList extends ParameterValues implements Iterable {
	List<String> values;

	public ParameterValueList(Operator operator, ParameterType type) {
		this(operator, type, new LinkedList<String>());
	}
	
	public ParameterValueList(Operator operator, ParameterType type, String[] valuesArray) {
		super(operator, type);
		this.values = new LinkedList<String>();
		for (int i = 0; i < valuesArray.length; i++) {
			values.add(valuesArray[i]);
		}
	}
	
	public ParameterValueList(Operator operator, ParameterType type, List<String> values) {
		super(operator, type);
		this.values = values;
	}
	
	public List<String> getValues() {
		return values;
	}

	public String[] getValuesArray() {
		String[] valuesArray = new String[values.size()]; 
		values.toArray(valuesArray);
		return valuesArray;
	}
	
	public void add(String value) {
		values.add(value);
	}
	
	public boolean contains(String value) {
		return values.contains(value);
	}
	
	public void remove(String value) {
		values.remove(value);
	}
	
	public Iterator iterator() {
		return values.iterator();
	}

	public int getNumberOfValues() {
		return values.size();
	}
	
	public String getValuesString() {
		StringBuffer valuesStringBuffer = new StringBuffer();
		boolean first = true;
		for (String value : values) {
			if (!first) {
				valuesStringBuffer.append(",");
			}
			first = false;
			valuesStringBuffer.append(value);
		}
		return valuesStringBuffer.toString();
	}
	
	public String toString() {
		return "list: " + getValuesString();
	}
}
