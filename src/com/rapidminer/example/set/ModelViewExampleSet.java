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

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.ViewModel;

/**
 * This is a generic example set (view on the view stack of the data) which
 * can be used to apply any preprocessing model and create a view from it.
 *
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: ModelViewExampleSet.java,v 1.10 2008/05/09 19:22:49 ingomierswa Exp $
 */
public class ModelViewExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = -6443667708498013284L;

	private ExampleSet parent;
	
	private Attributes attributes;

	
	public ModelViewExampleSet(ExampleSet parent, ViewModel model) {
		this.parent = (ExampleSet)parent.clone();
		this.attributes = model.getTargetAttributes(parent);
	}
	
	/** Clone constructor.*/
	public ModelViewExampleSet(ModelViewExampleSet other) {
		this.parent = (ExampleSet)other.parent.clone();
		this.attributes = other.attributes;

		if (other.attributes != null)
			this.attributes = (Attributes) other.attributes.clone();
	}
	
	public Attributes getAttributes() {
		return this.attributes;
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
		return this.parent.getExampleTable();
	}

	public int size() {
		return this.parent.size();
	}
}
