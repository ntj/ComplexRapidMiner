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

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Tests the data of AttributeWeights
 * 
 * @author Christian Lohmann
 * @version $Id: AttributeWeightsDataSampleTest.java,v 1.2 2007/06/13 17:15:13 meltac Exp $
 */
public class AttributeWeightsDataSampleTest extends OperatorDataSampleTest {

	private double[] sampledata;
	private String[] attributes;

	public AttributeWeightsDataSampleTest(String file, String[] attributes,double[] sampledata) {
		super(file);
		this.attributes = attributes;
		this.sampledata = sampledata;
	}
	
	@Override
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		AttributeWeights attributeweights = output.get(AttributeWeights.class);
		assertNotNull(attributeweights);
		for(int i = 0; i < sampledata.length; i++) {
			assertEquals(sampledata[i], attributeweights.getWeight(attributes[i]));
		}
	}
}
