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
package com.rapidminer.test;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.performance.PerformanceVector;
import com.rapidminer.operator.similarity.attributebased.EuclideanDistance;
import com.rapidminer.operator.similarity.clustermodel.TreeDistance;

/**
 * Tests for Similarity in Learner/Unsupervised/Clustering/Similarity
 *
 * @author Marcin Skirzynski
 * @version $Id: SimilaritySampleDataTest.java,v 1.7 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class SimilaritySampleDataTest extends OperatorDataSampleTest {
	
	private double[] expectedValues;
	private String[] first;
	private String[] second;
	private String similarity;


	public SimilaritySampleDataTest(String file, String similarity, String[] first, String[] second, double[] expectedValues) {
		super(file);
		this.expectedValues = expectedValues;
		this.first = first;
		this.second = second;
		this.similarity = similarity;
	}
	
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		
		if (similarity.equals("Tree")) {
			TreeDistance treedistance = output.get(TreeDistance.class);
			for (int i=0;i<expectedValues.length;i++) {
				assertEquals(treedistance.similarity(first[i],second[i]), expectedValues[i]);
			}
		}
		if (similarity.equals("Euclidean")) {
			EuclideanDistance euclideandistance = output.get(EuclideanDistance.class);
			for (int i=0;i<expectedValues.length;i++) {
				assertEquals(euclideandistance.similarity(first[i],second[i]), expectedValues[i]);
			}
		}
		if (similarity.equals("Comparator")) {
			PerformanceVector performancevector = output.get(PerformanceVector.class);
			assertEquals(expectedValues[0],performancevector.getCriterion("similarity").getAverage(), 0.00001);
			
		}
	}
}
