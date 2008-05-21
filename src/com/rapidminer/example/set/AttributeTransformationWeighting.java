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
import com.rapidminer.example.AttributeTransformation;
import com.rapidminer.example.AttributeWeights;



/** This transformation simply returns the weight-scaled value. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: AttributeTransformationWeighting.java,v 1.2 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class AttributeTransformationWeighting implements AttributeTransformation {

	private AttributeWeights attributeWeights;
	
	public AttributeTransformationWeighting(AttributeWeights attributeWeights) {
		this.attributeWeights = attributeWeights;
	}
	
	public AttributeTransformationWeighting(AttributeTransformationWeighting other) {
		this.attributeWeights = (AttributeWeights)other.attributeWeights.clone();
	}
	
	public Object clone() {
		return new AttributeTransformationWeighting(this);
	}
	
	public void setAttributeWeights(AttributeWeights weights) {
		this.attributeWeights = weights;
	}
	
	public double inverseTransform(Attribute attribute, double value) {
		double weight = attributeWeights.getWeight(attribute.getName());
		if (!Double.isNaN(weight))
			return value / weight;
		else
			return value;
	}

	public boolean isReversable() {
		return true;
	}

	public double transform(Attribute attribute, double value) {
		double weight = attributeWeights.getWeight(attribute.getName());
		if (!Double.isNaN(weight))
			return value * weight;
		else
			return value;
	}
}
