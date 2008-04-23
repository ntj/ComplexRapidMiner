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
package com.rapidminer.parameter;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.tools.Tools;


/**
 * A parameter type for parameter lists. Operators ask for the list of the
 * specified values with
 * {@link com.rapidminer.operator.Operator#getParameterList(String)}.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterTypeList.java,v 2.14 2006/04/05 08:57:27 ingomierswa
 *          Exp $
 */
public class ParameterTypeList extends ParameterType {

	private static final long serialVersionUID = -6101604413822993455L;

	private List defaultList = new LinkedList();

	private ParameterType valueType;

	public ParameterTypeList(String key, String description, ParameterType valueType) {
		this(key, description, valueType, new LinkedList());
	}

	public ParameterTypeList(String key, String description, ParameterType valueType, List defaultList) {
		super(key, description);
		this.defaultList = defaultList;
		this.valueType = valueType;
		if (valueType.getDescription() == null)
			valueType.setDescription(description);
	}

	public ParameterType getValueType() {
		return valueType;
	}

	public Object checkValue(Object value) {
		Iterator i = ((List) value).iterator();
		List<Object[]> newList = new LinkedList<Object[]>();
		while (i.hasNext()) {
			Object[] current = (Object[]) i.next();
			newList.add(new Object[] { (String) current[0], valueType.checkValue(current[1]) });
		}
		return newList;
	}

	public Object getDefaultValue() {
		return defaultList;
	}
	
	public void setDefaultValue(Object defaultValue) {
		this.defaultList = (List)defaultValue;
	}

	/** Returns false. */
	public boolean isNumerical() { return false; }
	
	public String getXML(String indent, String key, Object value, boolean hideDefault) {
		StringBuffer result = new StringBuffer();
		result.append(indent + "<list key=\"" + key + "\">" + Tools.getLineSeparator());
		Iterator i = ((List) value).iterator();
		while (i.hasNext()) {
			Object[] current = (Object[]) i.next();
			result.append(valueType.getXML(indent + "  ", (String) current[0], current[1], false));
		}
		result.append(indent + "</list>" + Tools.getLineSeparator());
		return result.toString();
	}

	public String getRange() {
		return "list";
	}
}
