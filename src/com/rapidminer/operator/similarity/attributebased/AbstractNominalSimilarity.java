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
package com.rapidminer.operator.similarity.attributebased;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.operator.OperatorException;


/**
 * Abstract base class for nominal similarities that are based on counts of agreeing and disagreeing values in both examples.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractNominalSimilarity.java,v 1.3 2008/05/09 19:22:56 ingomierswa Exp $
 */
public abstract class AbstractNominalSimilarity extends AbstractValueBasedSimilarity {

	private boolean[] binominal;

	private double[] zeroIndex;

	/**
	 * Calculate a similarity given the number of attributes for which both examples agree/disagree.
	 * 
	 * @param a
	 *            the number of attributes for which both examples are equal and non-zero
	 * @param b
	 *            the number of attributes for which both examples have unequal values
	 * @param c
	 *            the number of attributes for which both examples have zero values
	 * @return the similarity
	 */
	protected abstract double calculateSimilarity(double a, double b, double c);

	public double similarity(double[] e1, double[] e2) {
		// Count cases
		int counterA = 0;
		int counterB = 0;
		int counterC = 0;
		for (int i = 0; i < e1.length; i++) {
			if (e1[i] == e2[i])
				if (binominal[i]) {
					if (e1[i] == zeroIndex[i])
						counterC++;
					else
						counterA++;
				} else
					counterA++;
			else {
				counterB++;
			}
		}
		return calculateSimilarity(counterA, counterB, counterC);
	}

	public boolean isDistance() {
		return false;
	}

	public void init(ExampleSet exampleSet) throws OperatorException {
		super.init(exampleSet);
		Tools.onlyNominalAttributes(exampleSet, "nominal similarities");
		binominal = new boolean[exampleSet.getAttributes().size()];
		zeroIndex = new double[exampleSet.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			binominal[index] = attribute.isNominal() && attribute.getMapping().size() == 2;
			if (binominal[index])
				zeroIndex[index] = attribute.getMapping().getNegativeIndex();
			else
				zeroIndex[index] = Double.NaN;
			index++;
		}
	}
}
