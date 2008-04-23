/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.example.set;

import java.util.Iterator;

import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;


/**
 * Hides {@link Example}s that do not fulfill a given {@link Condition}. Uses
 * a {@link ConditionExampleReader}. Please note that the method
 * <code>updateCondition()<code> must be invoked each time the condition changed its
 *  acceptance behaviour!
 *
 *  @author Ingo Mierswa
 *  @version $Id: ConditionedExampleSet.java,v 1.1 2007/05/27 21:59:00 ingomierswa Exp $
 */
public class ConditionedExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = 877488093216198777L;

	private ExampleSet parent;
	
	private Condition condition;

	private int size = -1;

	private int[] mapping;

    private boolean inverted = false;
    
	/**
	 * Creates a new example which used only examples fulfilling the given
	 * condition.
	 */
	public ConditionedExampleSet(ExampleSet parent, Condition condition) { 
        this(parent, condition, false);
    }
        
	/**
	 * Creates a new example which used only examples fulfilling the given
	 * condition.
	 */
	public ConditionedExampleSet(ExampleSet parent, Condition condition, boolean inverted) {
		this.parent = parent;
		if (condition == null)
			throw new IllegalArgumentException("Condition must not be null!");
		this.condition = condition;
        this.inverted = inverted;
		updateCondition();
	}

	/** Clone constructor. */
	public ConditionedExampleSet(ConditionedExampleSet exampleSet) {
    	this.parent = (ExampleSet)exampleSet.parent.clone();
		this.condition = exampleSet.condition.duplicate();
		this.size = exampleSet.size;
		this.mapping = new int[exampleSet.mapping.length];
        this.inverted = exampleSet.inverted;
		System.arraycopy(exampleSet.mapping, 0, this.mapping, 0, exampleSet.mapping.length);
	}

    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof ConditionedExampleSet))
            return false;
        return this.condition.equals(((ConditionedExampleSet)o).condition);
    }

    public int hashCode() {
        return super.hashCode() ^ condition.hashCode();
    }
    
	/**
	 * This method should be invoked after changing the condition, i.e. if the
	 * acceptance behavior of the condition has changed. This is not necessary
	 * for most conditions but might be for conditions which are dynamically
	 * changed.
	 */
	public void updateCondition() {
		// recalculate size
		Iterator<Example> reader = iterator();
		this.size = 0;
		while (reader.hasNext()) {
			this.size++;
			reader.next();
		}

		// create mapping
		this.mapping = new int[size];
		int exampleCounter = 0;
		int conditionCounter = 0;
		reader = parent.iterator();
		while (reader.hasNext()) {
			Example example = reader.next();
            if (!inverted) {
                if (this.condition.conditionOk(example))
                    mapping[conditionCounter++] = exampleCounter;
            } else {
                if (!this.condition.conditionOk(example))
                    mapping[conditionCounter++] = exampleCounter;                
            }
			exampleCounter++;
		}
	}

	/** Returns a {@link ConditionExampleReader}. */
	public Iterator<Example> iterator() {
		return new ConditionExampleReader(parent.iterator(), condition, inverted);
	}

	/** Returns the i-th example fulfilling the condition. */
	public Example getExample(int index) {
		if ((index < 0) || (index >= this.mapping.length))
			throw new RuntimeException("Given index '" + index + "' does not fit the filtered ExampleSet!");
		else
			return parent.getExample(this.mapping[index]);
	}

	/** Counts the number of examples which fulfills the condition. */
	public int size() {
		return size;
	}

    public Attributes getAttributes() {
    	return parent.getAttributes();
    }

    /**
     * Returns the example with the given index.
     */
    public Example getExampleFromId(double id) {
        return new Example(parent.getExampleFromId(id).getDataRow(), this);
    }

	public ExampleTable getExampleTable() {
		return parent.getExampleTable();
	}

	public void remapIds() {
		parent.remapIds();
	}
}
