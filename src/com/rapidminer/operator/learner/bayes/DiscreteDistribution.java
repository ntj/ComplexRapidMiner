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
package com.rapidminer.operator.learner.bayes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Tools;


/**
 * DiscreteDistribution is an distribution for nominal values. For probability calculation it counts the weight/frequency of all values and returns this
 * number of the given value divided by the total weight of all examples.
 * If one or more values have never been counted, a total mass of totalWeight / (numberOfValues)^2 will be
 * equally distributed over this missing values to prevent the probability from getting 0. This weight mass is added to
 * the total weight.  
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: DiscreteDistribution.java,v 1.10 2008/05/10 18:28:58 stiefelolm Exp $
 */
public class DiscreteDistribution implements Distribution {

	private static final long serialVersionUID = 7573474548080998479L;

	private HashMap<Double, Double> valueWeights = new HashMap<Double, Double>();
	private double totalWeight;
	private Attribute attribute;
	private NominalMapping mapping; 
	
	public DiscreteDistribution(Attribute attribute, HashMap<Double, Double> valueWeights, double totalWeight) {
		this.attribute = attribute;
		this.valueWeights = valueWeights;
		this.totalWeight = totalWeight;
		this.mapping = attribute.getMapping();
		
		// ensuring that every value is possible! Values not seen in sample get 1% of weight
		double numberOfZeros = 0;
		for (Entry<Double, Double> entry: valueWeights.entrySet()) {
			if (entry.getValue() == 0)
				numberOfZeros++;
			
		}
		double zeroWeight = totalWeight / (Math.pow(this.valueWeights.size(),2) * numberOfZeros);
		for (Entry<Double, Double> entry: valueWeights.entrySet()) {
			if (entry.getValue() == 0)
				entry.setValue(zeroWeight);
			
		}
		this.totalWeight += totalWeight / Math.pow(this.valueWeights.size(),2);
	}

	public double getProbability(double x) {
		Double weight = valueWeights.get(x);
		if (weight != null) {
			return weight / totalWeight;
		}
		return 0;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		NominalMapping mapping = attribute.getMapping();
		for (Double valueKey: valueWeights.keySet()) {
			String valueName;
			if (Double.isNaN(valueKey))
				valueName = "unkown";
			else
				valueName = mapping.mapIndex(valueKey.intValue());
			buffer.append(valueName + "\t");
		}
		buffer.append(Tools.getLineSeparator());
		for (Double valueKey : valueWeights.keySet()) {
			Double weightObject = valueWeights.get(valueKey);
			if (weightObject != null)
				buffer.append(Tools.formatIntegerIfPossible(weightObject.doubleValue() / totalWeight) + "\t");
			else
				buffer.append("?\t");
		}
		return buffer.toString();
	}

	public double getLowerBound() {
		return Double.NaN;
	}

	public double getUpperBound() {
		return Double.NaN;
	}

	public Collection<Double> getValues() {
		return valueWeights.keySet();
	}
	public double getTotalWeight() {
		return this.totalWeight;
	}
	public String mapValue(double value) {
		return mapping.mapIndex((int)value);
	}
}
