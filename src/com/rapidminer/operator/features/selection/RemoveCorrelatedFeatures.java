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
package com.rapidminer.operator.features.selection;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.Value;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.tools.Tools;


/**
 * Removes (un-) correlated features due to the selected filter relation. The
 * procedure is quadratic in number of attributes. In order to get more stable 
 * results, the original, random, and reverse order of attributes is available.
 * 
 * Please note that this operator might fail in some cases when the attributes
 * should be filtered out, e.g. it might not be able to remove for example all 
 * negative correlated features. The reason for this behaviour seems to be that 
 * for the complete m x m - matrix of correlations (for m attributes) the 
 * correlations will not be recalculated and hence not checked if one of the 
 * attributes of the current pair was already marked for removal. That means 
 * for three attributes a1, a2, and a3 that it might be that a2 was already ruled 
 * out by the negative correlation with a1 and is now not able to rule out a3 any 
 * longer. 
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: RemoveCorrelatedFeatures.java,v 1.3 2006/04/05 08:57:27
 *          ingomierswa Exp $
 */
public class RemoveCorrelatedFeatures extends Operator {


	/** The parameter name for &quot;Use this correlation for the filter relation.&quot; */
	public static final String PARAMETER_CORRELATION = "correlation";

	/** The parameter name for &quot;Removes one of two features if their correlation fulfill this relation.&quot; */
	public static final String PARAMETER_FILTER_RELATION = "filter_relation";

	/** The parameter name for &quot;The algorithm takes this attribute order to calculate correlation and filter.&quot; */
	public static final String PARAMETER_ATTRIBUTE_ORDER = "attribute_order";

	/** The parameter name for &quot;Indicates if the absolute value of the correlations should be used for comparison.&quot; */
	public static final String PARAMETER_USE_ABSOLUTE_CORRELATION = "use_absolute_correlation";
	private static final String[] ORDER = new String[] { "original", "random", "reverse" };

	private static final int ORDER_ORIGINAL = 0;

	private static final int ORDER_RANDOM = 1;

	private static final int ORDER_REVERSE = 2;
	

	private static final String[] FILTER_RELATIONS = new String[] { "greater", "greater equals", "equals", "less equals", "less" };

	private static final int GREATER = 0;

	private static final int GREATER_EQUALS = 1;

	private static final int EQUALS = 2;

	private static final int LESS_EQUALS = 3;

	private static final int LESS = 4;
	
	/** The number of removed features (for logging as value, see constructor.)*/
	private int removedFeatures = 0;

	public RemoveCorrelatedFeatures(OperatorDescription description) {
		super(description);
		addValue(new Value("features_removed", "Number of removed features") {
			public double getValue() {
				return removedFeatures;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = this.getInput(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();

		double[] means = new double[exampleSet.getAttributes().size()];
		double[] deviations = new double[exampleSet.getAttributes().size()];
		boolean[] removeFeature = new boolean[exampleSet.getAttributes().size()];
		int[] attributeIndex = new int[exampleSet.getAttributes().size()];

		int index = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			means[index] = exampleSet.getStatistics(attribute, Statistics.AVERAGE);
			deviations[index] = Math.sqrt(exampleSet.getStatistics(attribute, Statistics.VARIANCE));
			removeFeature[index] = false;
			attributeIndex[index] = index;
			index++;
		}

		double[][] samples = new double[exampleSet.size()][exampleSet.getAttributes().size()];

		int counter = 0;
		for (Example example : exampleSet) {
			int d = 0;
			for (Attribute attribute : example.getAttributes()) {
				samples[counter][d++] = example.getValue(attribute);
			}
			counter++;
		}

		// attribute order
		int order = getParameterAsInt(PARAMETER_ATTRIBUTE_ORDER);
		if (order == ORDER_ORIGINAL) {
			for (int i = 0; i < exampleSet.getAttributes().size(); i++)
				attributeIndex[i] = i;
		} else if (order == ORDER_RANDOM) {
			// random attributes
			Vector<Integer> vector = new Vector<Integer>();
			for (int i = 0; i < exampleSet.getAttributes().size(); i++) {
				vector.add(i);
			}

			int vindex;
			for (int i = 0; i < exampleSet.getAttributes().size(); i++) {
				vindex = (int) Math.floor(Math.random() * vector.size());
				attributeIndex[i] = vector.remove(vindex).intValue();
			}
		} else if (order == ORDER_REVERSE) {
			for (int i = 0; i < exampleSet.getAttributes().size(); i++) {
				attributeIndex[i] = exampleSet.getAttributes().size() - 1 - i;
			}
		}

		// absolute value
		boolean absolute = getParameterAsBoolean(PARAMETER_USE_ABSOLUTE_CORRELATION);

		// filter relation
		int relation = getParameterAsInt(PARAMETER_FILTER_RELATION);

		// filtering
		double threshold = getParameterAsDouble(PARAMETER_CORRELATION);
		if (absolute && threshold < 0.0d) {
			threshold = Math.abs(threshold);
			logWarning("Correlation value is lower zero. Setting to absolute: " + threshold);
		}

		Attribute[] allAttributes = exampleSet.getAttributes().createRegularAttributeArray();
		for (int i = 0; i < exampleSet.getAttributes().size() - 1; i++) {
			if (removeFeature[attributeIndex[i]] == true) {
				continue;
			}
			for (int j = i + 1; j < exampleSet.getAttributes().size(); j++) {
				if (removeFeature[attributeIndex[j]] == true) {
					continue;
				}
				
				double correlation = getCorrelation(samples, means, deviations, attributeIndex[i], attributeIndex[j]);
				if (absolute) {
					correlation = Math.abs(correlation);
				}

				if (fulfillRelation(correlation, threshold, relation)) {
					removeFeature[attributeIndex[j]] = true;
					String first  = allAttributes[attributeIndex[i]].getName();
					String second = allAttributes[attributeIndex[j]].getName();
					log("Removed Attribute : " + second + " --> correlation(" + first + "," + second + ")=" + correlation);
				}
			}
		}

		// actual removal (and counter)
		this.removedFeatures = 0;
		index = 0;
		Iterator<Attribute> iterator = exampleSet.getAttributes().iterator();
		while (iterator.hasNext()) {
			iterator.next();
			if (removeFeature[index]) {
				iterator.remove();
				this.removedFeatures++;
			}
			index++;
		}

		log("Removed " + this.removedFeatures + "features." + Tools.getLineSeparator() + "ExampleSet has now " + exampleSet.getAttributes().size() + " features.");
		return new IOObject[] { exampleSet };
	}

	private boolean fulfillRelation(double correlation, double threshold, int relation) {
		switch (relation) {
			case GREATER:
				return (correlation > threshold);
			case GREATER_EQUALS:
				return (correlation >= threshold);
			case EQUALS:
				return (correlation == threshold);
			case LESS_EQUALS:
				return (correlation <= threshold);
			case LESS:
				return (correlation < threshold);
		}
		return false;
	}

	/**
	 * Calculates the correlation between the two features
	 * 
	 * @param att1
	 *            index of feature 1
	 * @param att2
	 *            index of feature 2
	 * @return the correlation in (-1.0,1.0)
	 */
	private double getCorrelation(double[][] samples, double[] means, double[] deviations, int att1, int att2) {
		// calculate covariance
		double covariance = 0.0d;
		for (int j = 0; j < samples.length; j++) {
			covariance += (samples[j][att1] - means[att1]) * (samples[j][att2] - means[att2]);
		}
		covariance = covariance / (samples.length - 1);

		// calculate correlation
		double correlation = 0.0d;
		correlation = (deviations[att1] * deviations[att2]);
		if (correlation == 0.0d) {
			correlation = covariance;
		} else {
			correlation = covariance / correlation;
		}

		return correlation;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_CORRELATION, "Use this correlation for the filter relation.", -1.0d, 1.0d, 0.95d);
		list.add(type);
		type = new ParameterTypeCategory(PARAMETER_FILTER_RELATION, "Removes one of two features if their correlation fulfill this relation.", FILTER_RELATIONS, 0);
		list.add(type);
		type = new ParameterTypeCategory(PARAMETER_ATTRIBUTE_ORDER, "The algorithm takes this attribute order to calculate correlation and filter.", ORDER, 0);
		list.add(type);
		type = new ParameterTypeBoolean(PARAMETER_USE_ABSOLUTE_CORRELATION, "Indicates if the absolute value of the correlations should be used for comparison.", true);
		list.add(type);
		return list;
	}
}
