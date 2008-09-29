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
package com.rapidminer.operator.preprocessing.filter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.BinominalMapping;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.example.table.ViewAttribute;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingModel;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;

/**
 * This model maps the values of all nominal values to binary attributes. For example,
 * if a nominal attribute with name &quot;costs&quot; and possible nominal values
 * &quot;low&quot;, &quot;moderate&quot;, and &quot;high&quot; is transformed, the result
 * is a set of three binominal attributes &quot;costs = low&quot;, &quot;costs = moderate&quot;,
 * and &quot;costs = high&quot;. Only one of the values of each attribute is true for a specific
 * example, the other values are false.   
 * 
 * @author Sebastian Land
 * @version $Id: NominalToBinominalModel.java,v 1.8 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class NominalToBinominalModel extends PreprocessingModel {

	private static final long serialVersionUID = 2882937201039541604L;

	private Set<String> sourceAttributeNames;

	private Map<Attribute, Double> binominalAttributeValueMap;

	private boolean useOnlyUnderscoreInNames = false;

	public NominalToBinominalModel(ExampleSet exampleSet, boolean translateBinominals, boolean useOnlyUnderscoreInNames) {
		super(exampleSet);
		this.binominalAttributeValueMap = new LinkedHashMap<Attribute, Double>();
		this.useOnlyUnderscoreInNames = useOnlyUnderscoreInNames;
		this.sourceAttributeNames = new HashSet<String>();
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal() && (attribute.getMapping().size() > 2 || translateBinominals)) {
				sourceAttributeNames.add(attribute.getName());
			}
		}
	}

	public ExampleSet applyOnData(ExampleSet exampleSet) throws OperatorException {
		// create attributes
		Map<Attribute, Attribute> attributeSourceMap = new LinkedHashMap<Attribute, Attribute>();
		for (Attribute sourceAttribute : exampleSet.getAttributes()) {
			String sourceAttributeName = sourceAttribute.getName();
			if (sourceAttributeNames.contains(sourceAttributeName)) {
				for (String value : sourceAttribute.getMapping().getValues()) {
					// create nominal mapping
					Attribute newAttribute = AttributeFactory.createAttribute(sourceAttributeName + " = " + value, Ontology.BINOMINAL);
					NominalMapping mapping = new BinominalMapping();
					mapping.mapString("false");
					mapping.mapString("true");
					newAttribute.setMapping(mapping);
					binominalAttributeValueMap.put(newAttribute, (double) sourceAttribute.getMapping().mapString(value)); 
					attributeSourceMap.put(newAttribute, sourceAttribute);
				}
			}
		}
		// add attributes to exampleSet 
		exampleSet.getExampleTable().addAttributes(attributeSourceMap.keySet());
		Attributes attributes = exampleSet.getAttributes();
		for (Attribute attribute: attributeSourceMap.keySet())
			attributes.addRegular(attribute);

		// rebuild attribute map because of changed hashCode of exampleTableColumn
		binominalAttributeValueMap = new LinkedHashMap<Attribute, Double>(binominalAttributeValueMap);
		
		// fill new attributes with values
		for(Example example: exampleSet) {
			for (Map.Entry<Attribute, Attribute> entry: attributeSourceMap.entrySet()) {
				double sourceValue = example.getValue(entry.getValue());
				example.setValue(entry.getKey(), getValue(entry.getKey(), sourceValue));
			}
		}
		
		// remove old attributes
		Iterator<Attribute> attributeIterator = attributes.iterator();
		while (attributeIterator.hasNext()) {
			Attribute attribute = attributeIterator.next();
			if (sourceAttributeNames.contains(attribute.getName()))
				attributeIterator.remove();
		}

		return exampleSet;
	}

	public Attributes getTargetAttributes(ExampleSet applySet) {
		Attributes attributes = getSpecialAttributes(applySet);
		// add regular attributes
		for (Attribute attribute : applySet.getAttributes()) {
			if (sourceAttributeNames.contains(attribute.getName())) {
				// add binominal attributes for every value
				for (String value : attribute.getMapping().getValues()) {
					attributes.addRegular(createBinominalValueAttribute(attribute, value));
				}
			} else {
				// add original if not a sourceAttribute
				attributes.addRegular(attribute);
			}
		}
		return attributes;
	}

	public double getValue(Attribute targetAttribute, double value) {
		if (Double.compare(value, binominalAttributeValueMap.get(targetAttribute).doubleValue()) == 0) {
			return 1;
		} else {
			return 0;
		}
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Mapping Model for attributes:");
		for (String attributeName : sourceAttributeNames) {
			buffer.append(attributeName + Tools.getLineSeparator());
		}
		return buffer.toString();
	}

	private Attribute createBinominalValueAttribute(Attribute attribute, String value) {
		NominalMapping mapping = new BinominalMapping();
		mapping.mapString("false");
		mapping.mapString("true");
		// giving new attributes old name_value
		String newName = attribute.getName() + " = " + value;
		if (useOnlyUnderscoreInNames)
			newName = attribute.getName() + "_" + value;
		Attribute newAttribute = new ViewAttribute(this, attribute, newName, Ontology.BINOMINAL, mapping);
		binominalAttributeValueMap.put(newAttribute, (double) attribute.getMapping().mapString(value));
		return newAttribute;
	}

	private Attributes getSpecialAttributes(ExampleSet applySet) {
		Attributes attributes = new SimpleAttributes();
		// add special attributes to new attributes
		Iterator<AttributeRole> roleIterator = applySet.getAttributes().allAttributeRoles();
		while (roleIterator.hasNext()) {
			AttributeRole role = roleIterator.next();
			if (role.isSpecial()) {
				attributes.add(role);
			}
		}
		return attributes;
	}
}
