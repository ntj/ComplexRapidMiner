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
package com.rapidminer.operator.preprocessing.normalization;

import java.util.HashMap;
import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.table.ViewAttribute;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.preprocessing.PreprocessingModel;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.Tupel;


/** This model performs a z-Transformation on the given example set. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: ZTransformationModel.java,v 1.7 2008/05/09 19:23:19 ingomierswa Exp $
 */
public class ZTransformationModel extends PreprocessingModel {

	private static final long serialVersionUID = 7739929307307501706L;

	private HashMap<String, Tupel<Double, Double>> attributeMeanVarianceMap;
    
	public ZTransformationModel(ExampleSet exampleSet, HashMap<String, Tupel<Double, Double>> attributeMeanVarianceMap) {
		super(exampleSet);
		this.attributeMeanVarianceMap = attributeMeanVarianceMap;
	}

	/** Performs the transformation. */
	public ExampleSet applyOnData(ExampleSet exampleSet) throws OperatorException {
		Attributes attributes = exampleSet.getAttributes();
		for (Example example: exampleSet) {
			for (Attribute attribute: attributes) {
				if (attributeMeanVarianceMap.containsKey(attribute.getName())) {
					Tupel<Double, Double> meanVarianceTupel = attributeMeanVarianceMap.get(attribute.getName());
					if (meanVarianceTupel.getSecond().doubleValue() <= 0) {
						example.setValue(attribute, 0);
					} else {
						double newValue = (example.getValue(attribute) - meanVarianceTupel.getFirst().doubleValue()) / (Math.sqrt(meanVarianceTupel.getSecond().doubleValue()));
						example.setValue(attribute, newValue);
					}
					
				}
			}
		}
        return exampleSet;
	}

	/**
	 * Returns a nicer name. Necessary since this model is defined as inner
	 * class.
	 */
	public String getName() {
		return "Z-Transformation";
	}

	/** Returns a string representation of this model. */
	public String toString() {
		StringBuffer result = new StringBuffer();
		result.append("Normalize " + attributeMeanVarianceMap.size() + " attributes to mean 0 and variance 1." + Tools.getLineSeparator() + "Using");
		int counter = 0;
		for(String name: attributeMeanVarianceMap.keySet()) {
			if (counter > 4) {
				result.append(Tools.getLineSeparator() + "... " + (attributeMeanVarianceMap.size() - 5) + " more attributes ...");
				break;
			}
			Tupel<Double, Double> meanVariance = attributeMeanVarianceMap.get(name);
			result.append(Tools.getLineSeparator() + name + " --> mean: " + meanVariance.getFirst().doubleValue() + ", variance: " + meanVariance.getSecond().doubleValue());
			counter++;
		}
		return result.toString();
	}

	public Attributes getTargetAttributes(ExampleSet viewParent) {
		SimpleAttributes attributes = new SimpleAttributes();
		// add special attributes to new attributes
		Iterator<AttributeRole> roleIterator = viewParent.getAttributes().allAttributeRoles();
		while (roleIterator.hasNext()) {
			AttributeRole role = roleIterator.next();
			if (role.isSpecial()) {
				attributes.add(role);
			}
		}
		// add regular attributes
		for (Attribute attribute: viewParent.getAttributes()) {
			if (attribute.isNominal() || !attributeMeanVarianceMap.containsKey(attribute.getName())) {
				attributes.addRegular(attribute);
			} else {
				// giving new attributes old name: connection to rangesMap
				attributes.addRegular(new ViewAttribute(this, attribute, attribute.getName(), Ontology.NUMERICAL, null));
			}
		}
		return attributes;
	}

	public double getValue(Attribute targetAttribute, double value) {
		Tupel<Double, Double> meanVarianceTupel = attributeMeanVarianceMap.get(targetAttribute.getName());
		if (meanVarianceTupel.getSecond().doubleValue() <= 0) {
			return(0);
		} else {
			 return(value - meanVarianceTupel.getFirst().doubleValue()) / (Math.sqrt(meanVarianceTupel.getSecond().doubleValue()));
		}
	}
}
