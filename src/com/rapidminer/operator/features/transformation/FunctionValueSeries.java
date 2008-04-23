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
package com.rapidminer.operator.features.transformation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.ContainerModel;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.functions.kernel.JMySVMModel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.svm.SVMInterface;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * Calculates for each sample a series of function values. Therefore, the weights
 * of the given <code>JMySVMModel</code> are ordered descending by their absolute
 * value. The x-th value of the series is the function valueof the example by
 * taking the first x weights. The other weights are to zero. So the series
 * attribute count values. Additionally the user can enter nr_attributes which
 * are summerized to one value calculation. This can reduce the number of
 * calculations dramatically. The result is an <code>ExampleSet</code>
 * containing for each example a series of function values given by the
 * attribute values.
 * 
 * @author Daniel Hakenjos
 * @version $Id: FunctionValueSeries.java,v 1.1 2006/04/14 13:07:13 ingomierswa
 *          Exp $
 */
public class FunctionValueSeries extends Operator {


	/** The parameter name for &quot;The number of attributes summarized in each iteration.&quot; */
	public static final String PARAMETER_NR_ATTRIBUTES = "nr_attributes";
	private double[] weights;

	private int[] attribute_index;

	private int number_of_samples, number_atts, nr_series, nr_attributes;

	private double[] labelvalues, predvalues;

	private double[][] samples, fvalue_series;

	private com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples trainSet;

	private com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples modelSet;

	public FunctionValueSeries(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {

		ExampleSet eSet = getInput(ExampleSet.class);

		boolean haslabel = eSet.getAttributes().getLabel() != null;
		boolean haspredlabel = eSet.getAttributes().getPredictedLabel() != null;

		number_of_samples = eSet.size();
		number_atts = eSet.getAttributes().size();

		// Create Samples
		labelvalues = new double[number_of_samples];
		predvalues = new double[number_of_samples];

		nr_attributes = getParameterAsInt(PARAMETER_NR_ATTRIBUTES);

		nr_series = number_atts / nr_attributes;
		if (number_atts % nr_attributes > 0) {
			nr_series++;
		}

		fvalue_series = new double[number_of_samples][nr_series];
		samples = new double[number_atts][number_of_samples];

		Iterator<Example> reader = eSet.iterator();
		Example example;
		for (int sample = 0; sample < number_of_samples; sample++) {
			example = reader.next();
			int d = 0;
			for (Attribute attribute : eSet.getAttributes()) {
				samples[d][sample] = example.getValue(attribute);
				d++;
			}
			if (haslabel) {
				labelvalues[sample] = example.getValue(example.getAttributes().getLabel());
			}
			if (haspredlabel) {
				predvalues[sample] = example.getValue(example.getAttributes().getPredictedLabel());
			}
		}
		// create attribute index
		attribute_index = new int[number_atts];
		for (int i = 0; i < attribute_index.length; i++) {
			attribute_index[i] = i;
		}

		Model model = getInput(Model.class);

		while ((model instanceof ContainerModel) && (((ContainerModel) model).getNumberOfModels() > 0)) {
			model = ((ContainerModel) model).getModel(0);
		}

		if (!(model instanceof JMySVMModel)) {
			throw new OperatorException("Expected JMySVMModel instead of: " + model.getClass().getName());
		}

		JMySVMModel jmysvmmodel = (JMySVMModel) model;

		modelSet = jmysvmmodel.getExampleSet();
		trainSet = new com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples(eSet, eSet.getAttributes().getLabel(), modelSet.getMeanVariances());

		SVMInterface svm = jmysvmmodel.createSVM();
		svm.init(jmysvmmodel.getKernel(), jmysvmmodel.getExampleSet());
		weights = svm.getWeights();
		// attribute_index wird mitsortiert.
		orderWeights(weights);

		// ueber anzahl zu berechnender wertereihen
		for (int d = 0; d < nr_series; d++) {

			// alle Beispiele
			for (int i = 0; i < number_of_samples; i++) {
				fvalue_series[i][d] = 0.0d;

				double[] atts; // = new double[number_atts]; Unused
				int[] index = new int[number_atts];
				for (int a = 0; a < number_atts; a++) {
					index[a] = a;
				}
				com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample svm_example = trainSet.get_example(i);
				atts = svm_example.toDense(number_atts);

				for (int a = Math.min((d + 1) * nr_attributes, number_atts) - 1; a < number_atts; a++) {
					atts[attribute_index[a]] = 0.0d;
				}
				svm_example = new com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample(index, atts);

				int[] sv_index;
				double[] sv_att;
				// double[] sv_atts;
				double the_sum = modelSet.get_b();
				double[] alphas = modelSet.get_alphas();
				double alpha;
				for (int j = 0; j < modelSet.count_examples(); j++) {
					alpha = alphas[j];
					if (alpha != 0) {
						sv_index = new int[number_atts];
						for (int a = 0; a < number_atts; a++) {
							sv_index[a] = a;
						}
						sv_att = modelSet.get_example(j).toDense(number_atts);

						the_sum += alpha * jmysvmmodel.getKernel().calculate_K(sv_index, sv_att, svm_example.index, svm_example.att);
					}
				}
				fvalue_series[i][d] = the_sum;
			}
		}

		// Create ExampleSet

		// create table
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int i = 0; i < nr_series; i++) {
			attributes.add(AttributeFactory.createAttribute("fvalue_" + (i + 1), Ontology.REAL));
		}
		if (haslabel)
			attributes.add(eSet.getAttributes().getLabel());
		if (haspredlabel)
			attributes.add(eSet.getAttributes().getPredictedLabel());

		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// create data
		List<DataRow> data = new LinkedList<DataRow>();
		double[] samples;
		int nr = nr_series;
		if (haslabel) {
			nr++;
		}
		if (haspredlabel) {
			nr++;
		}

		for (int n = 0; n < number_of_samples; n++) {
			samples = new double[nr];
			System.arraycopy(fvalue_series[n], 0, samples, 0, nr_series);

			if (haslabel) {
				samples[nr_series] = labelvalues[n];
			}
			if (haspredlabel) {
				samples[nr - 1] = predvalues[n];
			}

			data.add(new DoubleArrayDataRow(samples));
		}
		// fill table with data
		table.readExamples(new ListDataRowReader(data.iterator()));

		ExampleSet resultSet = null;
		// create example set and return it
		Map<Attribute,String> specialMap = new HashMap<Attribute, String>();
		specialMap.put(eSet.getAttributes().getLabel(), Attributes.LABEL_NAME);
		specialMap.put(eSet.getAttributes().getPredictedLabel(), Attributes.PREDICTION_NAME);
		resultSet = table.createExampleSet(specialMap);
		resultSet.recalculateAllAttributeStatistics();

		return new IOObject[] { resultSet };
	}

	/**
	 * Order the subarry from index anfang to index ende with QuickSort Also the
	 * attribute_index is ordered.
	 * 
	 * @param array
	 * @param links
	 * @param rechts
	 */
	public void orderWeights(double[] array, int links, int rechts) {
		int left = links, right = rechts;
		double pivot = array[(links + rechts) >>> 1], temp;
		pivot = Math.abs(pivot);
		do {
			// Tauschpartner links suchen:
			while (Math.abs(array[left]) > pivot)
				left++;
			// Tauschpartner rechts suchen:
			while (Math.abs(array[right]) < pivot)
				right--;
			// Vertauschen:
			if (left <= right) {
				temp = array[left];
				array[left] = array[right];
				array[right] = temp;

				temp = attribute_index[left];
				attribute_index[left] = attribute_index[right];
				attribute_index[right] = (int) temp;

				left++;
				right--;
			}
		} while (!(left > right));
		if (links < right)
			orderWeights(array, links, right);
		if (left < rechts)
			orderWeights(array, left, rechts);
	}

	/**
	 * Order weights with QuickSort. Performs an ordering to the absolute value
	 */
	public void orderWeights(double[] weights) {
		orderWeights(weights, 0, weights.length - 1);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NR_ATTRIBUTES, "The number of attributes summarized in each iteration.", 1, Integer.MAX_VALUE, 1);
		types.add(type);
		return types;
	}
}
