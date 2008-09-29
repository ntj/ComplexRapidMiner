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
package com.rapidminer.operator.preprocessing.discretization;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.PolynominalMapping;
import com.rapidminer.example.table.ViewAttribute;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingModel;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.Tupel;

/**
 * The generic discretization model.
 * 
 * @author Sebastian Land
 * @version $Id: DiscretizationModel.java,v 1.14 2008/07/01 13:20:38 stiefelolm Exp $
 */
public class DiscretizationModel extends PreprocessingModel {

	private static final long serialVersionUID = -8732346419946567062L;

	private HashMap<String, SortedSet<Tupel<Double, String>>> rangesMap;

	private Set<String> attributeNames;

	private boolean removeUseless = true;

	protected DiscretizationModel(ExampleSet exampleSet) {
		this(exampleSet, true);
	}
	
	protected DiscretizationModel(ExampleSet exampleSet, boolean removeUseless) {
		super(exampleSet);
		attributeNames = new HashSet<String>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNumerical()) {
				attributeNames.add(attribute.getName());
			}
		}
		this.removeUseless = removeUseless;
	}

	public ExampleSet applyOnData(ExampleSet exampleSet) throws OperatorException {
		// creating new nominal attributes for numerical ones
		Map<Attribute, Attribute> replacementMap = new LinkedHashMap<Attribute, Attribute>();
		ExampleTable table = exampleSet.getExampleTable();
		for (Attribute attribute: exampleSet.getAttributes()) {
			if (attribute.isNumerical()) {
				Attribute newAttribute = AttributeFactory.createAttribute(attribute.getName(), Ontology.NOMINAL);
				replacementMap.put(attribute, newAttribute);
			}
		}
		Set<Entry<Attribute, Attribute>> replacements = replacementMap.entrySet();

		// creating mapping and adding to table and exampleSet
		for (Entry<Attribute, Attribute> replacement: replacements) {
			SortedSet<Tupel<Double, String>> ranges = rangesMap.get(replacement.getKey().getName());
			Attribute newAttribute = replacement.getValue();
			table.addAttribute(newAttribute);
			exampleSet.getAttributes().addRegular(newAttribute);
			if (ranges != null) {
				for (Tupel<Double, String> rangePair : ranges) {
					newAttribute.getMapping().mapString(rangePair.getSecond());
				}
			}

		}
		
		// copying data
		for (Example example: exampleSet) {
			for (Entry<Attribute, Attribute> replacement: replacements) {
				Attribute originalAttribute = replacement.getKey();
				Attribute newAttribute = replacement.getValue();
				SortedSet<Tupel<Double, String>> ranges = rangesMap.get(originalAttribute.getName());
				if (ranges != null) {
					double value = example.getValue(originalAttribute);
					int b = 0;
					for (Tupel<Double, String> rangePair : ranges) {
						if (Tools.isLessEqual(value, rangePair.getFirst().doubleValue())) {
							example.setValue(newAttribute, b);
							break;
						}
						b++;
					}
				}
			}
		}
		
		// removing old attributes
		for (Attribute originalAttribute: replacementMap.keySet()) {
			exampleSet.getAttributes().remove(originalAttribute);
		}


		// removing useless nominal attributes
		if (removeUseless) {
			Iterator<Attribute> iterator = exampleSet.getAttributes().iterator();
			while (iterator.hasNext()) {
				Attribute attribute = iterator.next();
				if (attribute.isNominal()) {
					if (attribute.getMapping().size() < 2) {
						iterator.remove();
					}
				}
			}
		}
		return exampleSet;
	}

	public void setRanges(HashMap<Attribute, double[]> rangesMap, String rangeName, boolean longRangeNames) {
		this.rangesMap = new HashMap<String, SortedSet<Tupel<Double, String>>>();
		Iterator<Map.Entry<Attribute, double[]>> r = rangesMap.entrySet().iterator();
		while (r.hasNext()) {
			Map.Entry<Attribute, double[]> entry = r.next();
			Attribute attribute = entry.getKey();
			TreeSet<Tupel<Double, String>> ranges = new TreeSet<Tupel<Double, String>>();
			int i = 1;
			String lastLimit = Tools.formatIntegerIfPossible(Double.NEGATIVE_INFINITY);
			for (double rangeValue : entry.getValue()) {
				String newLimit = Tools.formatIntegerIfPossible(rangeValue);
				String usedRangeName = rangeName + i;
				if (longRangeNames) {
					 usedRangeName += " [" + lastLimit + " - " + newLimit + "]";
				}
				ranges.add(new Tupel<Double, String>(rangeValue, usedRangeName));
				i++;
				lastLimit = newLimit;
			}
			this.rangesMap.put(attribute.getName(), ranges);
		}
	}

	public void setRanges(HashMap<String, SortedSet<Tupel<Double, String>>> rangesMap) {
		this.rangesMap = rangesMap;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (String attributeName : rangesMap.keySet()) {
			buffer.append(Tools.getLineSeparator());
			buffer.append(Tools.getLineSeparator());
			buffer.append(attributeName);
			buffer.append(Tools.getLineSeparator());
			SortedSet<Tupel<Double, String>> set = rangesMap.get(attributeName);
			boolean first = true;
			buffer.append(Double.NEGATIVE_INFINITY + " <= ");
			for (Tupel<Double, String> tupel : set) {
				if (first) {
					first = false;
					buffer.append(tupel.getSecond() + " <= " + tupel.getFirst());
				} else {
					buffer.append(" <= " + tupel.getSecond() + " <= " + tupel.getFirst());
				}
			}
		}
		return buffer.toString();
	}

	public Attributes getTargetAttributes(ExampleSet parentSet) {
		SimpleAttributes attributes = new SimpleAttributes();
		// add special attributes to new attributes
		Iterator<AttributeRole> specialRoles = parentSet.getAttributes().specialAttributes();
		while (specialRoles.hasNext()) {
			attributes.add(specialRoles.next());
		}
		
		// add regular attributes
		for (Attribute attribute : parentSet.getAttributes()) {
			if (!attribute.isNumerical() || !attributeNames.contains(attribute.getName())) {
				attributes.addRegular(attribute);
			} else {
				// create nominal mapping
				SortedSet<Tupel<Double, String>> ranges = rangesMap.get(attribute.getName());
				if (ranges.size() > 1) {
					NominalMapping mapping = new PolynominalMapping();
					for (Tupel<Double, String> rangePair : ranges) {
						mapping.mapString(rangePair.getSecond());
					}
					// giving new attributes old name: connection to rangesMap
					attributes.addRegular(new ViewAttribute(this, attribute, attribute.getName(), Ontology.POLYNOMINAL, mapping));
				}
			}
		}
		return attributes;
	}

	public double getValue(Attribute targetAttribute, double value) {
		SortedSet<Tupel<Double, String>> ranges = rangesMap.get(targetAttribute.getName());
		if (ranges != null) {
			int b = 0;
			for (Tupel<Double, String> rangePair : ranges) {
				if (Tools.isLessEqual(value, rangePair.getFirst().doubleValue())) {
					return b;
				}
				b++;
			}
			return Double.NaN;
		} else {
			return value;
		}
	}
}
