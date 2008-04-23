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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;


/**
 * This ExampleReader skips all examples that do not fulfil a specified
 * {@link Condition}.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ConditionExampleReader.java,v 2.17 2006/03/21 15:35:39
 *          ingomierswa Exp $
 */
public class ConditionExampleReader extends AbstractExampleReader {

	/** Array of short names for the known conditions. */
	public static final String[] KNOWN_CONDITION_NAMES = { 
        "all", 
        "correct_predictions", 
        "wrong_predictions", 
        "no_missing_attributes", 
        "missing_attributes", 
        "no_missing_labels", 
        "missing_labels", 
        "attribute_value_filter" 
	};

	/**
	 * Array of fully qualified classnames of implementations of
	 * {@link Condition} that are useful independently of special applications.
	 * All conditions given here must provide a construtor with arguments
	 * (ExampleSet data, String parameters).
	 */
	private static final String[] KNOWN_CONDITION_IMPLEMENTATIONS = { 
        AcceptAllCondition.class.getName(), 
        CorrectPredictionCondition.class.getName(), 
        WrongPredictionCondition.class.getName(), 
        NoMissingAttributesCondition.class.getName(), 
        MissingAttributesCondition.class.getName(),
        NoMissingLabelsCondition.class.getName(), 
        MissingLabelsCondition.class.getName(), 
        AttributeValueFilter.class.getName() 
	};

	/** The example reader that provides a complete example set. */
	private Iterator<Example> parent;

	/** The used condition. */
	private Condition condition;

	/** The example that will be returned by the next invocation of next(). */
	private Example currentExample;

    /** Indicates if the inverted condition should be fulfilled. */
    private boolean inverted = false;
    
	/**
	 * Constructs a new ConditionExampleReader the next() method of which
	 * returns only examples that fulfil a specified condition.
	 */
	public ConditionExampleReader(Iterator<Example> parent, Condition condition, boolean inverted) {
		this.parent = parent;
		this.currentExample = null;
		this.condition = condition;
        this.inverted = inverted;
	}

	public boolean hasNext() {
		while (currentExample == null) {
			if (!parent.hasNext())
				return false;
			Example e = parent.next();
            if (!inverted) {
                if (condition.conditionOk(e))
                    currentExample = e;
            } else {
                if (!condition.conditionOk(e))
                    currentExample = e;                
            }
		}
		return true;
	}

	public Example next() {
		hasNext();
		Example dummy = currentExample;
		currentExample = null;
		return dummy;
	}

	/**
	 * Checks if the given name is the short name of a known condition and
	 * creates it. If the name is not known, this method creates a new instance
	 * of className which must be an implementation of {@link Condition} by
	 * calling its two argument constructor passing it the example set and the
	 * parameter string
	 */
	public static Condition createCondition(String name, ExampleSet exampleSet, String parameterString) throws ConditionCreationException {
		String className = name;
		for (int i = 0; i < KNOWN_CONDITION_NAMES.length; i++) {
			if (KNOWN_CONDITION_NAMES[i].equals(name)) {
				className = KNOWN_CONDITION_IMPLEMENTATIONS[i];
				break;
			}
		}
		try {
			Class<?> clazz = com.rapidminer.tools.Tools.classForName(className);
			if (!Condition.class.isAssignableFrom(clazz))
				throw new ConditionCreationException("'" + className + "' does not implement Condition!");
			java.lang.reflect.Constructor constructor = clazz.getConstructor(new Class[] { ExampleSet.class, String.class });
			return (Condition) constructor.newInstance(new Object[] { exampleSet, parameterString });
		} catch (ClassNotFoundException e) {
			throw new ConditionCreationException("Cannot find class '" + className + "'. Check your classpath.");
		} catch (NoSuchMethodException e) {
			throw new ConditionCreationException("'" + className + "' must implement two argument constructor " + className + "(ExampleSet, String)!");
		} catch (IllegalAccessException e) {
			throw new ConditionCreationException("'" + className + "' cannot access two argument constructor " + className + "(ExampleSet, String)!");
		} catch (InstantiationException e) {
			throw new ConditionCreationException(className + ": cannot create condition (" + e.getMessage() + ").");
		} catch (Throwable e) {
			throw new ConditionCreationException(className + ": cannot invoke condition (" + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()) + ").");
		}
	}
}
