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
package com.rapidminer.operator.preprocessing.transformation;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.InputDescription;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;


/**
 * @author Tobias Malbrecht
 * @version $Id: ExampleSetTransformationOperator.java,v 1.2 2008/07/17 14:40:26 tobiasmalbrecht Exp $
 */
public abstract class ExampleSetTransformationOperator extends Operator {
	public ExampleSetTransformationOperator(OperatorDescription description) {
		super(description);
	}

    /** Indicates that the consumption of example sets can be user defined (default: no consumption). */
    public InputDescription getInputDescription(Class cls) {
        if (ExampleSet.class.isAssignableFrom(cls)) {
            return new InputDescription(cls, false, true);
        } else {
            return super.getInputDescription(cls);
        }
    }
	
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
}