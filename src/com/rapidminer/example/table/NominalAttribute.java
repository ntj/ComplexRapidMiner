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

import com.rapidminer.example.NominalStatistics;
import com.rapidminer.example.UnknownStatistics;

/**
 * This class holds all information on a single nominal attribute. In addition
 * to the generic attribute fields this class keeps information about the
 * nominal values and the value to index mappings. If one of the methods
 * designed for numerical attributes was invoked a RuntimeException will be
 * thrown.
 * 
 * It will be guaranteed that all values are mapped to indices without any
 * missing values. This could, however, be changed in future versions thus
 * operators should not rely on this fact.
 * 
 * @author Ingo Mierswa
 * @version $Id: NominalAttribute.java,v 2.12 2006/04/05 08:57:22 ingomierswa
 *          Exp $
 */
public abstract class NominalAttribute extends AbstractAttribute {

	/* pp */ NominalAttribute(String name, int valueType) {
		super(name, valueType);
        registerStatistics(new NominalStatistics());
        registerStatistics(new UnknownStatistics());
	}
	
	/* pp */ NominalAttribute(NominalAttribute other) {
		super(other);
	}
	
	public boolean isNominal() { 
		return true; 
	}
	
	/**
	 * Returns a string representation and maps the value to a string if type is
	 * nominal. The number of digits is ignored.
	 */
	public String getAsString(double value, int digits, boolean quoteWhitespace) {
		if (Double.isNaN(value)) {
			return "?";
		} else {
            try {
                String result = getMapping().mapIndex((int) value); 
                if (quoteWhitespace) {
                    if (result.indexOf(" ") >= 0) {
                        result = "\"" + result + "\"";
                    }
                }
                return result;
            } catch (Throwable e) {
                return "?";
            }
		}
	}
}
