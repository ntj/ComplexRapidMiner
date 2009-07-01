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
package com.rapidminer.example.table;

import java.text.DateFormat;
import java.util.Date;

import com.rapidminer.example.MinMaxStatistics;
import com.rapidminer.example.UnknownStatistics;
import com.rapidminer.tools.Ontology;

/**
 * This class holds all information on a single date attribute. In addition
 * to the information of the superclass, this is some statistics data like
 * minimum, maximum and average of the values.
 * 
 * @author Ingo Mierswa
 * @version $Id: DateAttribute.java,v 1.3 2008/08/05 10:45:20 tobiasmalbrecht Exp $
 */
public class DateAttribute extends AbstractAttribute {

	private static final long serialVersionUID = -685655991653799960L;

	/**
	 * Creates a simple attribute which is not part of a series and does not
	 * provide a unit string.
	 */
	/* pp */ DateAttribute(String name) {
		this(name, Ontology.DATE);
	}

	/**
	 * Creates a simple attribute which is not part of a series and does not
	 * provide a unit string.
	 */
	/* pp */ DateAttribute(String name, int valueType) {
		super(name, valueType);
		registerStatistics(new MinMaxStatistics());
        registerStatistics(new UnknownStatistics());
	}
	
	/**
	 * Clone constructor.
	 */
	private DateAttribute(DateAttribute a) {
		super(a);
	}

	@Override
	public Object clone() {
		return new DateAttribute(this);
	}

	public String getAsString(double value, int digits, boolean quoteWhitespace) {
		if (Double.isNaN(value)) {
			return "?";
		} else {
			long milliseconds = (long)value;
			String result = null;
			if (getValueType() == Ontology.DATE) {
				DateFormat format = DateFormat.getDateInstance();
				result = format.format(new Date(milliseconds));
			} else if (getValueType() == Ontology.TIME) {
				DateFormat format = DateFormat.getTimeInstance();
				result = format.format(new Date(milliseconds));
			} else if (getValueType() == Ontology.DATE_TIME) {
				DateFormat format = DateFormat.getDateTimeInstance();
				result = format.format(new Date(milliseconds));
			}
			if (quoteWhitespace) {
				result = "\"" + result + "\"";
			}
			return result;
		}
	}

	/** Returns null. */
	public NominalMapping getMapping() {
		return null;
	}

	/** Returns false. */
	public boolean isNominal() {
		return false;
	}
	
	public boolean isNumerical() { 
		return false; 
	}

	/** Do nothing. */
	public void setMapping(NominalMapping nominalMapping) {}

	
	public boolean isComplex() {
		return false;
	}

	public boolean isRelational() {
		// TODO Auto-generated method stub
		return false;
	}
}
