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
package com.rapidminer.operator.preprocessing.transformation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeList;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;


/**
 * This operator converts an example set by dividing examples
 * which consist of multiple observations (at different times)
 * into multiple examples, where each example covers on point
 * in time. An index attribute is added, which contains denotes
 * the actual point in time the example belongs to after the
 * transformation. The parameter <tt>keep_missings</tt> specifies
 * whether examples should be kept, even if if exhibits missing
 * values for all series at a certain point in time.
 * 
 * @author Tobias Malbrecht
 * @version $Id: Attribute2ExamplePivoting.java,v 1.1 2008/09/08 18:57:13 tobiasmalbrecht Exp $
 */
public class Attribute2ExamplePivoting extends ExampleSetTransformationOperator {

	public static final String PARAMETER_ATTRIBUTE_NAME_REGEX = "attributes";
	
	public static final String PARAMETER_SERIES = "attribute_name";
	
	public static final String PARAMETER_INDEX_ATTRIBUTE = "index_attribute";
	
	public static final String PARAMETER_KEEP_MISSINGS = "keep_missings";

	public Attribute2ExamplePivoting(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		List seriesList = getParameterList(PARAMETER_SERIES);

		int numberOfSeries = seriesList.size();
		
        String[] seriesNames = new String[numberOfSeries];
        Pattern[] seriesPatterns = new Pattern[numberOfSeries];
        ArrayList<Vector<Attribute>> seriesAttributes = new ArrayList<Vector<Attribute>>(numberOfSeries);
        int[] attributeTypes = new int[numberOfSeries];
		Iterator iterator = seriesList.iterator();
		int j = 0;
		while (iterator.hasNext()) {
			Object[] pair = (Object[]) iterator.next();
			seriesNames[j] = (String) pair[0];
			seriesPatterns[j] = Pattern.compile((String) pair[1]);
			seriesAttributes.add(j, new Vector<Attribute>());
			attributeTypes[j] = Ontology.ATTRIBUTE_VALUE;
			j++;
		}

		Vector<Attribute> newAttributes = new Vector<Attribute>();
		Vector<Attribute> constantAttributes = new Vector<Attribute>();
		
        // identify series attributes and check attribute types
        for (Attribute attribute : exampleSet.getAttributes()) {
        	boolean matched = false;
        	for (int i = 0; i < numberOfSeries; i++) {
        		Matcher matcher = seriesPatterns[i].matcher(attribute.getName());
        		if (matcher.matches()) {
                	matched = true;
                    seriesAttributes.get(i).add(attribute);
                	if (attributeTypes[i] != Ontology.ATTRIBUTE_VALUE) {
                		if (attribute.getValueType() != attributeTypes[i]) {
                			throw new OperatorException("attributes have different value types: no conversion is performed");
                		}
                	} else {
                		attributeTypes[i] = attribute.getValueType();
                	}
                	break;
        		}
    		}
        	if (!matched) {
            	Attribute attributeCopy = AttributeFactory.createAttribute(attribute.getName(), attribute.getValueType());
            	if (attribute.isNominal()) {
            		attributeCopy.setMapping((NominalMapping) attribute.getMapping().clone());
            	}
    			newAttributes.add(attributeCopy);
        		constantAttributes.add(attribute);
    		}
        }

        // check series length
        int seriesLength = 0;
        if (numberOfSeries >= 1) {
        	seriesLength = seriesAttributes.get(0).size();
            for (int i = 0; i < numberOfSeries - 1; i++) {
            	seriesLength = seriesAttributes.get(i).size();
            	if (seriesLength != seriesAttributes.get(i+1).size()) {
            		throw new OperatorException("series must have the same length: no conversion is performed");
            	}
            }
        }

        // index attributes
        Attribute indexAttribute = AttributeFactory.createAttribute(getParameterAsString(PARAMETER_INDEX_ATTRIBUTE), Ontology.INTEGER);
        newAttributes.add(indexAttribute);

        // series attribtues
        for (int i = 0; i < numberOfSeries; i++) {
        	Attribute seriesAttribute = AttributeFactory.createAttribute(seriesNames[i], attributeTypes[i]);
        	newAttributes.add(seriesAttribute);
        }

		MemoryExampleTable table = new MemoryExampleTable(newAttributes);

		for (Example example : exampleSet) {
			int l = 0;
			for (int k = 0; k < seriesLength; k++) {
				l++;
				double[] data = new double[newAttributes.size()];
				for (int i = 0; i < data.length; i++) {
					data[i] = Double.NaN;
				}
				
				// set constant attribute values
				for (int i = 0; i < constantAttributes.size(); i++) {
					data[i] = example.getValue(constantAttributes.get(i));
				}
				
				// set index attribute value
				data[data.length - numberOfSeries - 1] = l;
				
				// set series attribute values
				boolean onlyMissings = true;
				for (int i = 0; i < numberOfSeries; i++) {
					Attribute seriesAttribute = seriesAttributes.get(i).get(k);
					double seriesValue = example.getValue(seriesAttribute);
					double newValue = Double.NaN;
					if (!Double.isNaN(seriesValue)) {
						if (seriesAttribute.isNominal()) {
							newValue = newAttributes.get(newAttributes.size() - numberOfSeries + i).getMapping().mapString(seriesAttribute.getMapping().mapIndex((int) seriesValue));
						} else {
							newValue = seriesValue;
						}
						onlyMissings = false;
					}
					data[data.length - numberOfSeries + i] = newValue;
				}
                checkForStop();
				if (!getParameterAsBoolean(PARAMETER_KEEP_MISSINGS) && onlyMissings) {
					continue;
				} else {
					table.addDataRow(new DoubleArrayDataRow(data));
				}
			}
		}


		// create and deliver example set
		ExampleSet result = table.createExampleSet();
		result.recalculateAllAttributeStatistics();
		return new IOObject[] { result };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType attributeNames = new ParameterTypeString(PARAMETER_ATTRIBUTE_NAME_REGEX, "Attributes that forms series.", false);
		ParameterType type = new ParameterTypeList(PARAMETER_SERIES, "Name of resulting attribute.", attributeNames);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeString(PARAMETER_INDEX_ATTRIBUTE, "Name of index attribute.", false);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(PARAMETER_KEEP_MISSINGS, "Keep missing values.", false);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
