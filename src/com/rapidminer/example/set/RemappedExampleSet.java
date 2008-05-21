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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.AttributeTransformation;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.NominalMapping;

/**
 * This example set uses the mapping given by another example set and
 * "remaps" on the fly the nominal values according to the given set.
 *
 * @author Ingo Mierswa
 * @version $Id: RemappedExampleSet.java,v 1.9 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class RemappedExampleSet extends AbstractExampleSet {

    private static final long serialVersionUID = 3460640319989955936L;

    private ExampleSet parent;
    
    private ExampleSet mappingSet;
    
    public RemappedExampleSet(ExampleSet parentSet, ExampleSet _mappingSet) {
        this.parent = (ExampleSet)parentSet.clone();
        this.mappingSet = (ExampleSet)_mappingSet.clone();

        Iterator<AttributeRole> a = this.parent.getAttributes().allAttributeRoles();
        while (a.hasNext()) {
            AttributeRole role = a.next();
            Attribute currentAttribute = role.getAttribute();
            if (currentAttribute.isNominal()) {
            	NominalMapping mapping = null;
            	mapping = currentAttribute.getMapping();
            	Attribute oldMappingAttribute = this.mappingSet.getAttributes().get(role.getAttribute().getName());
            	if ((oldMappingAttribute != null) && (oldMappingAttribute.isNominal()))
            		mapping = oldMappingAttribute.getMapping();
            	currentAttribute.addTransformation(new AttributeTransformationRemapping(mapping));
            }
        }
    }

    /** Clone constructor. */
    public RemappedExampleSet(RemappedExampleSet other) {
        this.parent = (ExampleSet)other.parent.clone();
        this.mappingSet = (ExampleSet)other.mappingSet.clone();
        
        Iterator<AttributeRole> a = this.parent.getAttributes().allAttributeRoles();
        while (a.hasNext()) {
            AttributeRole role = a.next();
            Attribute currentAttribute = role.getAttribute();
			AttributeTransformation transformation = currentAttribute.getLastTransformation();
			if (transformation != null) {
				if (transformation instanceof AttributeTransformationRemapping) {
					Attribute mappingAttribute = this.mappingSet.getAttributes().get(currentAttribute.getName());
					if (mappingAttribute != null) {
						NominalMapping oldMapping = mappingAttribute.getMapping();
						if (oldMapping != null)
							((AttributeTransformationRemapping)transformation).setNominalMapping(oldMapping);
					}
				}
			}
		}
    }

    public Attributes getAttributes() {
        return this.parent.getAttributes();
    }

    public ExampleTable getExampleTable() {
        return parent.getExampleTable();
    }

    public int size() {
        return parent.size();
    }

    public Iterator<Example> iterator() {
        return new AttributesExampleReader(parent.iterator(), this);
    }
    
    public Example getExample(int index) {
    	return this.parent.getExample(index);
    }
}
