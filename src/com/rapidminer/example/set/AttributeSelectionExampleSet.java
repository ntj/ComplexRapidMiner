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
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;


/**
 * An implementation of ExampleSet that is only a fixed view on a selection of attributes
 * of the parent example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: AttributeSelectionExampleSet.java,v 1.7 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class AttributeSelectionExampleSet extends AbstractExampleSet {
    
	private static final long serialVersionUID = 7946137859300860625L;

	private ExampleSet parent; 
	
    /**
     * Constructs a new AttributeSelectionExampleSet. Only those attributes with
     * a true value in the selection mask will be used. If the given mask is null,
     * all regular attributes of the parent example set will be used.
     */
    public AttributeSelectionExampleSet(ExampleSet exampleSet, boolean[] selectionMask) {
    	this.parent = (ExampleSet)exampleSet.clone();
        if (selectionMask != null) {
            if (selectionMask.length != exampleSet.getAttributes().size())
                throw new IllegalArgumentException("Length of the selection mask must be equal to the parent's number of attributes.");

            int counter = 0;
            Iterator<Attribute> i = this.parent.getAttributes().iterator();
            while (i.hasNext()) {
            	i.next();
            	if (!selectionMask[counter])
                    i.remove();
                counter++;
            }
        }
    }

    /** Clone constructor. */
    public AttributeSelectionExampleSet(AttributeSelectionExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.parent.clone();
    }

    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof AttributeSelectionExampleSet))
            return false;
        return this.parent.equals(((AttributeSelectionExampleSet)o));
    }

    public int hashCode() {
        return super.hashCode() ^ this.parent.hashCode();
    }
    
    // -------------------- overridden methods --------------------
    
    /** Returns the attribute container. */
    public Attributes getAttributes() {
    	return this.parent.getAttributes();
    }

    /**
     * Creates a new example set reader.
     */
    public Iterator<Example> iterator() {
        return new AttributesExampleReader(parent.iterator(), this);
    }

    public Example getExample(int index) {
    	return this.parent.getExample(index);
    }
    
	public ExampleTable getExampleTable() {
		return parent.getExampleTable();
	}
	
	public int size() {
		return parent.size();
	}
}
