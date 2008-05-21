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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.clustering.IdUtils;
import com.rapidminer.operator.similarity.SimilarityAdapter;


/**
 * An adaptar for similarities defined on (contineous) values.
 * 
 * @author Michael Wurst
 * @version $Id: AbstractValueBasedSimilarity.java,v 1.8 2008/05/09 19:22:56 ingomierswa Exp $
 */
public abstract class AbstractValueBasedSimilarity extends SimilarityAdapter implements ExampleBasedSimilarityMeasure {

	private ExampleSet es = null;

	private Set<String> ids;

	/**
	 * Get the real valued similarity between examples.
	 * 
	 * @param e1
	 *            first example
	 * @param e2
	 *            second example
	 * @return double
	 */
	public abstract double similarity(double[] e1, double[] e2);

	private double[] getValues(Example e) {
		if (e == null)
			return null;
		double[] values = new double[e.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : e.getAttributes())
			values[index++] = e.getValue(attribute);
		return values;
	}

	public void init(ExampleSet exampleSet) throws OperatorException {
		this.es = exampleSet;
		this.es.remapIds();
		ids = new HashSet<String>();
		Iterator<Example> er = es.iterator();
		while (er.hasNext())
			ids.add(IdUtils.getIdFromExample(er.next()));
	}

	public Iterator<String> getIds() {
		return ids.iterator();
	}

	public boolean isSimilarityDefined(String x, String y) {
		if (ids.contains(x) && ids.contains(y))
			return true;
		else
			return false;
	}

	public double similarity(String x, String y) {
		if (!isSimilarityDefined(x, y))
			return java.lang.Double.NaN;
		Example ex = IdUtils.getExampleFromId(es, x);
		Example ey = IdUtils.getExampleFromId(es, y);
		double[] e1 = getValues(ex);
		double[] e2 = getValues(ey);
		return similarity(e1, e2);
	}

	public double similarity(Example x, Example y) {
		double[] e1 = getValues(x);
		double[] e2 = getValues(y);
		return similarity(e1, e2);
	}
    
    public int getNumberOfIds() {
        return ids.size();
    }
}
