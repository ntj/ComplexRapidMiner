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
import com.rapidminer.example.AttributeTypeException;
import com.rapidminer.example.table.NominalMapping;

/** This transformation returns the remapped value.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: AttributeTransformationRemapping.java,v 1.3 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class AttributeTransformationRemapping implements AttributeTransformation {
	
    private NominalMapping overlayedMapping;
    
	public AttributeTransformationRemapping(NominalMapping overlayedMapping) {
		this.overlayedMapping = overlayedMapping;
	}

	public AttributeTransformationRemapping(AttributeTransformationRemapping other) {
		this.overlayedMapping = (NominalMapping)other.overlayedMapping.clone();
	}
	
	public Object clone() {
		return new AttributeTransformationRemapping(this);
	}
	
	public void setNominalMapping(NominalMapping mapping) {
		this.overlayedMapping = mapping;
	}
	
	public double transform(Attribute attribute, double value) {
		if (Double.isNaN(value))
			return value;
        if (attribute.isNominal()) {
        	try {
        		String nominalValue = attribute.getMapping().mapIndex((int)value);
        		int index = overlayedMapping.getIndex(nominalValue);
        		if (index < 0) {
        			return value;
        		} else {
        			return index;
        		}
        	} catch (AttributeTypeException e) {
        		throw new AttributeTypeException("Attribute '" + attribute.getName() + "': " + e.getMessage());
        	}
        } else {
            return value;
        }
	}

	public double inverseTransform(Attribute attribute, double value) {
		if (Double.isNaN(value))
			return value;
        if (attribute.isNominal()) {
        	try {
        		String nominalValue = overlayedMapping.mapIndex((int)value);
        		int newValue = attribute.getMapping().getIndex(nominalValue);
        		if (newValue < 0) {
        			return value;
        		} else {
        			return newValue;
        		}
        	} catch (AttributeTypeException e) {
        		throw new AttributeTypeException("Attribute '" + attribute.getName() + "': " + e.getMessage());
        	}
        } else {
            return value;
        }
	}

	public boolean isReversable() {
		return true;
	}
	
}
