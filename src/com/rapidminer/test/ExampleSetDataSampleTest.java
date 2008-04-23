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
package com.rapidminer.test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Tests for the Data of an ExampleSet
 *
 * @author Marcin Skirzynski, Tobias Beckers
 * @version $Id: ExampleSetDataSampleTest.java,v 1.3 2007/06/26 13:22:48 marcin_skir Exp $
 */
public class ExampleSetDataSampleTest extends OperatorDataSampleTest {
	
	private String attributeName;

	private double[] values;
	
	private String[] stringValues;
	
	private boolean isDouble;

	public ExampleSetDataSampleTest(String file, String attributeName, double[] values) {
		super(file);
		this.attributeName = attributeName;
		this.values = values;
		this.isDouble = true;
	}
	
	public ExampleSetDataSampleTest(String file, String attributeName, String[] stringValues) {
		super(file);
		this.attributeName = attributeName;
		this.stringValues = stringValues;
		this.isDouble = false;
	}

	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		ExampleSet exampleSet = output.get(ExampleSet.class);
		Attribute attribute = exampleSet.getAttributes().get(attributeName);
		assertNotNull(attribute);
		int counter = 0;
		if (isDouble) {
			double value = 0;
			
			for (int i=0;i<values.length;i++) {
				value = exampleSet.getExample(i).getValue(attribute);
				assertEquals(value, values[counter++]);
			}
		}
		else {
			String value = "";
			
			for (int i=0;i<stringValues.length;i++) {
				value = exampleSet.getExample(i).getValueAsString(attribute);
				assertEquals(value, stringValues[counter++]);
			}
		}
	}
}
