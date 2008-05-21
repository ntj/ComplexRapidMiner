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

import java.util.Iterator;
import java.util.LinkedList;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.ViewAttribute;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingModel;
import com.rapidminer.operator.preprocessing.PreprocessingOperator;
import com.rapidminer.tools.Ontology;


/**
 * This operator maps all non numeric attributes to real valued attributes.
 * Nothing is done for numeric attributes, binary attributes are mapped to 0 and
 * 1.
 * 
 * For nominal attributes one of the following calculations will be done:
 * <ul>
 * <li>Dichotomization, i.e. one new attribute for each value of the nominal
 * attribute. The new attribute which corresponds to the actual nominal value
 * gets value 1 and all other attributes gets value 0.</li>
 * <li>Alternatively the values of nominal attributes can be seen as equally
 * ranked, therefore the nominal attribute will simply be turned into a real
 * valued attribute, the old values results in equidistant real values.</li>
 * </ul>
 * 
 * At this moment the same applies for ordinal attributes, in a future release
 * more appropriate values based on the ranking between the ordinal values may
 * be included.
 * 
 * @author Ingo Mierswa, Sebastian Land
 * @version $Id: NominalToNumeric.java,v 1.15 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class NominalToNumeric extends PreprocessingOperator {

	private static class NominalToNumericModel extends PreprocessingModel {

		private static final long serialVersionUID = -4203775081616082145L;

		protected NominalToNumericModel(ExampleSet exampleSet) {
			super(exampleSet);
		}

		public ExampleSet applyOnData(ExampleSet exampleSet) throws OperatorException {
			// selecting transformation attributes and creating new numeric attributes
			LinkedList<Attribute> nominalAttributes = new LinkedList<Attribute>();
			LinkedList<Attribute> transformedAttributes = new LinkedList<Attribute>();
			for (Attribute attribute : exampleSet.getAttributes()) {
				if (attribute.isNominal()) {
					nominalAttributes.add(attribute);
  				 	// creating new attributes for nominal attributes
					transformedAttributes.add(AttributeFactory.createAttribute(attribute.getName(), Ontology.NUMERICAL));
				}
			}
			
			// ensuring capacity in exampletable
			exampleSet.getExampleTable().addAttributes(transformedAttributes);
			
			// copying values
			for (Example example: exampleSet) {
				Iterator<Attribute> target = transformedAttributes.iterator();
				for (Attribute attribute: nominalAttributes) {
					example.setValue(target.next(), example.getValue(attribute));
				}
			}
			
			// removing nominal attributes from example Set
			Attributes attributes = exampleSet.getAttributes();
			for(Attribute attribute: exampleSet.getAttributes()) {
				if (attribute.isNominal())
					attributes.replace(attribute, transformedAttributes.poll());
			}
			return exampleSet;
		}

		public Attributes getTargetAttributes(ExampleSet parentSet) {
			SimpleAttributes attributes = new SimpleAttributes();
			// add special attributes to new attributes
			Iterator<AttributeRole> specialRoles = parentSet.getAttributes().specialAttributes();
			while (specialRoles.hasNext()) {
				attributes.add(specialRoles.next());
			}
			
			// add regular attributes
			Iterator<AttributeRole> i = parentSet.getAttributes().allAttributeRoles();
			while (i.hasNext()) {
				AttributeRole attributeRole = i.next();
				if (!attributeRole.isSpecial()) {
					Attribute attribute = attributeRole.getAttribute();
					if (attribute.isNominal()) {
						attributes.addRegular(new ViewAttribute(this, attribute, attribute.getName(), Ontology.INTEGER, null));
					} else {
						attributes.add(attributeRole);
					}
				}
			}
			return attributes;
		}

		public double getValue(Attribute targetAttribute, double value) {
			return value;
		}
		
	}
	
	public NominalToNumeric(OperatorDescription description) {
		super(description);
	}
	
	public Model createPreprocessingModel(ExampleSet exampleSet) throws OperatorException {
		return new NominalToNumericModel(exampleSet);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
}
