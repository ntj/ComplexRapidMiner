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
package com.rapidminer.operator.features.selection;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;


/**
 * This operator selects all attributes which have a weight fulfilling a given
 * condition. For example, only attributes with a weight greater than
 * <code>min_weight</code> should be selected. This operator is also able
 * to select the k attributes with the highest weight.
 * 
 * @author Ingo Mierswa, Stefan Rueping
 * @version $Id: AttributeWeightSelection.java,v 1.6 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class AttributeWeightSelection extends Operator {

	/** The parameter name for &quot;Use this weight for the selection relation.&quot; */
	public static final String PARAMETER_WEIGHT = "weight";

	/** The parameter name for &quot;Selects only weights which fulfill this relation.&quot; */
	public static final String PARAMETER_WEIGHT_RELATION = "weight_relation";

	/** The parameter name for &quot;Number k of attributes to be selected for weight-relations 'top k' or 'bottom k'.&quot; */
	public static final String PARAMETER_K = "k";

	/** The parameter name for &quot;Percentage of attributes to be selected for weight-relations 'top p%' or 'bottom p%'.&quot; */
	public static final String PARAMETER_P = "p";

	/** The parameter name for &quot;Indicates if attributes which weight is unknown should be deselected.&quot; */
	public static final String PARAMETER_DESELECT_UNKNOWN = "deselect_unknown";

	/** The parameter name for &quot;Indicates if the absolute values of the weights should be used for comparison.&quot; */
	public static final String PARAMETER_USE_ABSOLUTE_WEIGHTS = "use_absolute_weights";
	
	private static final String[] WEIGHT_RELATIONS = { "greater", "greater equals", "equals", "less equals", "less", "top k", "bottom k", "all but top k", "all but bottom k", "top p%", "bottom p%" };

	private static final int GREATER = 0;

	private static final int GREATER_EQUALS = 1;

	private static final int EQUALS = 2;

	private static final int LESS_EQUALS = 3;

	private static final int LESS = 4;

	private static final int TOPK = 5;

	private static final int BOTTOMK = 6;

	private static final int ALLBUTTOPK = 7;

	private static final int ALLBUTBOTTOMK = 8;

	private static final int TOPPPERCENT = 9;

	private static final int BOTTOMPPERCENT = 10;

	
	public AttributeWeightSelection(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet eSet = getInput(ExampleSet.class);
		AttributeWeights weights = getInput(AttributeWeights.class);
		boolean deselectUnknown = getParameterAsBoolean(PARAMETER_DESELECT_UNKNOWN);
		double relationWeight = getParameterAsDouble(PARAMETER_WEIGHT);
		int relation = getParameterAsInt(PARAMETER_WEIGHT_RELATION);
		boolean useAbsoluteWeights = getParameterAsBoolean(PARAMETER_USE_ABSOLUTE_WEIGHTS);

        // determine which attributes have a known weight value
		boolean[] weightKnown = new boolean[eSet.getAttributes().size()];
        Vector<Attribute> knownAttributes = new Vector<Attribute>();
        int index = 0;
        for (Attribute attribute : eSet.getAttributes()) {
        	double weight = weights.getWeight(attribute.getName());
        	if (!Double.isNaN(weight)) {
        		knownAttributes.add(attribute);
        		weightKnown[index++] = true;
        	} else {
        		weightKnown[index++] = false;
        	}
        }
		
		// determine number of attributes that should be selected
		int nrAtts = knownAttributes.size();
		int k = getParameterAsInt(PARAMETER_K);
        
		if (relation == ALLBUTTOPK) {
			relation = BOTTOMK;
			k = nrAtts - k;
		}
		if (relation == ALLBUTBOTTOMK) {
			relation = TOPK;
			k = nrAtts - k;
		}
		if (relation == TOPPPERCENT) {
			relation = TOPK;
			k = (int) Math.round(nrAtts * getParameterAsDouble(PARAMETER_P));
		}
		if (relation == BOTTOMPPERCENT) {
			relation = BOTTOMK;
			k = (int) Math.round(nrAtts * getParameterAsDouble(PARAMETER_P));
		}
        
		if (k < 1)
			k = 1;
        
		if (k > nrAtts)
			k = nrAtts;

		// top k or bottom k
		if ((relation == TOPK) || (relation == BOTTOMK)) {
            int direction = AttributeWeights.INCREASING;
            if (relation == BOTTOMK)
                direction = AttributeWeights.DECREASING;
            int comparatorType = AttributeWeights.ORIGINAL_WEIGHTS;
            if (useAbsoluteWeights)
                comparatorType = AttributeWeights.ABSOLUTE_WEIGHTS;

            String[] attributeNames = new String[knownAttributes.size()];
            index = 0;
            for (Attribute attribute : knownAttributes) {
                attributeNames[index++] = attribute.getName();
            }
            weights.sortByWeight(attributeNames, direction, comparatorType);
            
            Iterator<Attribute> iterator = eSet.getAttributes().iterator();
            index = 0;
            while (iterator.hasNext()) {
                Attribute attribute = iterator.next();
                if (!weightKnown[index]) {
                	if (deselectUnknown) {
                		iterator.remove();
                	}
                } else {
                    boolean remove = true;
                    for (int i = 0; i < k; i++) {
                        if (attribute.getName().equals(attributeNames[i])) {
                            remove = false;
                            break;
                        }
                    }
                    if (remove)
                        iterator.remove();
                }
                index++;
            }
		} else { // simple relations
			Iterator<Attribute> iterator = eSet.getAttributes().iterator();
			while (iterator.hasNext()) {
				Attribute attribute = iterator.next();
				double weight = weights.getWeight(attribute.getName());
				if (useAbsoluteWeights)
					weight = Math.abs(weight);
				if (Double.isNaN(weight) && (deselectUnknown)) {
					iterator.remove();
				} else {
					switch (relation) {
						case GREATER:
							if (weight <= relationWeight)
								iterator.remove();
							break;
						case GREATER_EQUALS:
							if (weight < relationWeight)
								iterator.remove();
							break;
						case EQUALS:
							if (weight != relationWeight)
								iterator.remove();
							break;
						case LESS_EQUALS:
							if (weight > relationWeight)
								iterator.remove();
							break;
						case LESS:
							if (weight >= relationWeight)
								iterator.remove();
							break;
					}
				}
			}
		}

		return new IOObject[] { eSet };
	}

	public InputDescription getInputDescription(Class cls) {
		if (AttributeWeights.class.isAssignableFrom(cls)) {
			return new InputDescription(cls, false, true);
		} else {
			return super.getInputDescription(cls);
		}
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_WEIGHT, "Use this weight for the selection relation.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.0d);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeCategory(PARAMETER_WEIGHT_RELATION, "Selects only weights which fulfill this relation.", WEIGHT_RELATIONS, GREATER_EQUALS);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(PARAMETER_K, "Number k of attributes to be selected for weight-relations 'top k' or 'bottom k'.", 1, Integer.MAX_VALUE, 10);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(PARAMETER_P, "Percentage of attributes to be selected for weight-relations 'top p%' or 'bottom p%'.", 0.0d, 1.0d, 0.5d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_DESELECT_UNKNOWN, "Indicates if attributes which weight is unknown should be deselected.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ABSOLUTE_WEIGHTS, "Indicates if the absolute values of the weights should be used for comparison.", true));
		return types;
	}
}
