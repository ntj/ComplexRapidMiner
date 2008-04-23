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
package com.rapidminer.operator.preprocessing.filter;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;


/**
 * Converts all numerical attributes to binary ones. If the value of an
 * attribute is between the specified minimal and maximal value, it becomes <em>false</em>, 
 * otherwise <em>true</em>. If the value is missing, the new value will be missing. The default
 * boundaries are both set to 0, thus only 0.0 is mapped to false and all other values are 
 * mapped to true.
 * 
 * @author Sebastian Land, Ingo Mierswa, Shevek
 * @version $Id: NumericToBinominal.java,v 1.3 2007/07/06 20:37:23 ingomierswa Exp $
 */
public class NumericToBinominal extends NumericToNominal {
       
	/** The parameter name for &quot;The minimal value which is mapped to false (included).&quot; */
	public static final String PARAMETER_MIN = "min";

	/** The parameter name for &quot;The maximal value which is mapped to false (included).&quot; */
	public static final String PARAMETER_MAX = "max";
	
    public NumericToBinominal(OperatorDescription description) {
        super(description);
    }

    protected void setValue(Example example, Attribute newAttribute, double value) throws OperatorException {
        double min = getParameterAsDouble(PARAMETER_MIN);
        double max = getParameterAsDouble(PARAMETER_MAX);
        if (Double.isNaN(value)) {
            example.setValue(newAttribute, Double.NaN);
        } else if ((value < min) || (value > max)) {
            example.setValue(newAttribute, newAttribute.getMapping().mapString("true"));
        } else {
            example.setValue(newAttribute, newAttribute.getMapping().mapString("false"));
        }        
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeDouble(PARAMETER_MIN, "The minimal value which is mapped to false (included).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d));
        types.add(new ParameterTypeDouble(PARAMETER_MAX, "The maximal value which is mapped to false (included).", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0.0d));
        return types;
    }
}
