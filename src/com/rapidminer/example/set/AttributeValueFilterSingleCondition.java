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
package com.rapidminer.example.set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.Tools;

/**
 * The condition is fulfilled if an attribute has a value equal to, not equal to,
 * less than, ... a given value.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeValueFilterSingleCondition.java,v 1.8 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class AttributeValueFilterSingleCondition implements Condition {
    
    private static final long serialVersionUID = 1537763901048986863L;

	private static final String[] COMPARISON_TYPES = { "<=", ">=", "!=", "<>", "=", "<", ">" };

    public static final int LEQ = 0;

    public static final int GEQ = 1;

    public static final int NEQ1 = 2;
    
    public static final int NEQ2 = 3;

    public static final int EQUALS = 4;

    public static final int LESS = 5;

    public static final int GREATER = 6;

    private int comparisonType = EQUALS;

    private Attribute attribute;

    private double numericalValue;

    private String nominalValue;
    
    /**
     * Creates a new AttributeValueFilter. If attribute is not nominal, value
     * must be a number.
     */
    public AttributeValueFilterSingleCondition(Attribute attribute, int comparisonType, String value) {
        this.attribute = attribute;
        this.comparisonType = comparisonType;
        setValue(value);
    }

    /**
     * Constructs an AttributeValueFilter for a given {@link ExampleSet} from a
     * parameter string
     * 
     * @param parameterString
     *            Must be of the form attribute R value, where R is one out of =,
     *            !=, &lt&, &gt;, &lt;=, and &gt;=.
     */
    public AttributeValueFilterSingleCondition(ExampleSet exampleSet, String parameterString) {
        if ((parameterString == null) || (parameterString.length() == 0))
            throw new IllegalArgumentException("Parameter string must not be empty!");

        int compIndex = -1;
        for (comparisonType = 0; comparisonType < COMPARISON_TYPES.length; comparisonType++) {
            compIndex = parameterString.indexOf(COMPARISON_TYPES[comparisonType]);
            if (compIndex != -1)
                break;
        }
        if (compIndex == -1)
            throw new IllegalArgumentException("Parameter string must have the form 'attribute {=|<|>|<=|>=|!=} value'");
        String attName = parameterString.substring(0, compIndex).trim();
        String valueStr = parameterString.substring(compIndex + COMPARISON_TYPES[comparisonType].length()).trim();
        if ((attName.length() == 0) || (valueStr.length() == 0))
            throw new IllegalArgumentException("Parameter string must have the form 'attribute {=|<|>|<=|>=|!=} value'");

        this.attribute = exampleSet.getAttributes().get(attName);

        if (this.attribute == null) {
            throw new IllegalArgumentException("Unknown attribute: '" + attName + "'");
        }
        setValue(valueStr);
    }

    private void setValue(String value) {
        if (attribute.isNominal()) {
            if ((comparisonType != EQUALS) && (comparisonType != NEQ1 && comparisonType != NEQ2))
                throw new IllegalArgumentException("For nominal attributes only '=' and '!=' or '<>' is allowed!");
            this.nominalValue = value;
        } else {
            try {
                this.numericalValue = Double.parseDouble(value);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Value for attribute '" + attribute.getName() + "' must be numerical!");
            }
        }
    }
    
    /**
     * Since the condition cannot be altered after creation we can just return
     * the condition object itself.
     * 
     * @deprecated Conditions should not be able to be changed dynamically and hence there is no need for a copy
     */
    @Deprecated
    public Condition duplicate() {
        return this;
    }
    
    public String toString() {
        return 
        	attribute.getName() + " " + 
        	COMPARISON_TYPES[comparisonType] + " " + 
        	(attribute.isNominal() ? nominalValue : "" + numericalValue);
    }

    /** Returns true if the condition is fulfilled for the given example. */
    public boolean conditionOk(Example e) {
        if (attribute.isNominal()) {
            switch (comparisonType) {
                case NEQ1:
                case NEQ2:
                    return !e.getNominalValue(attribute).equals(nominalValue);
                case EQUALS:
                    return e.getNominalValue(attribute).equals(nominalValue);
                default:
                    return false;
            }   
        } else {
            switch (comparisonType) {
                case LEQ:
                    return Tools.isLessEqual(e.getNumericalValue(attribute), numericalValue);
                case GEQ:
                    return Tools.isGreaterEqual(e.getNumericalValue(attribute), numericalValue);
                case NEQ1:
                case NEQ2:
                    return Tools.isNotEqual(e.getNumericalValue(attribute), numericalValue);
                case EQUALS:
                    return Tools.isEqual(e.getNumericalValue(attribute), numericalValue);
                case LESS:
                    return Tools.isLess(e.getNumericalValue(attribute), numericalValue);
                case GREATER:
                    return Tools.isGreater(e.getNumericalValue(attribute), numericalValue);
                default:
                    return false;
            }
        }
    }
}
