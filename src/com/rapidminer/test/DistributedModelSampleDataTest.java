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

import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.ContainerModel;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.learner.bayes.DistributionModel;

/**
 * Tests for the Data of an DistributedModel. Expected a i-double array for i examples in the experiment.
 *
 * @author Marcin Skirzynski
 * @version $Id: DistributedModelSampleDataTest.java,v 1.2 2007/06/19 17:09:46 marcin_skir Exp $
 */
public class DistributedModelSampleDataTest extends OperatorDataSampleTest {
	
	private double[] expectedValue;
	
	public DistributedModelSampleDataTest(String file, double[] expectedValue) {
		super(file);
		this.expectedValue = expectedValue;
	}
	

	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		ContainerModel containerModel = output.get(ContainerModel.class);
		DistributionModel distributionModel = containerModel.getModel(DistributionModel.class);
		ExampleSet exampleSet = output.get(ExampleSet.class);

		int counter = 0;
		for (Example e : exampleSet) {
			assertEquals(distributionModel.getProbabilityForClass(0, e),expectedValue[counter++]);
		}
			
	}
}
