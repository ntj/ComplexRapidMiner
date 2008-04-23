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

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeWeights;
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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;


/**
 * Splits a <code>JMySVMModel</code> into an <code>ExampleSet</code> of the
 * support vectors and <code>AttributeWeights</code> representing the normal
 * of the hyperplane. Additionally the <code>ExampleSet</code> of the SVs
 * contain a new attribute with the alpha-values.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: SplitSVMModel.java,v 1.4 2007/06/15 18:44:37 ingomierswa Exp $
 */
public class SplitSVMModel extends Operator {


	/** The parameter name for &quot;Add the alpha values as an attribute.&quot; */
	public static final String PARAMETER_ALPHA = "alpha";

	/** The parameter name for &quot;The string representation of the classes. Note: Specify the negative class first!&quot; */
	public static final String PARAMETER_CLASSES = "classes";
	public SplitSVMModel(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		Model model = getInput(Model.class);

		while ((model instanceof ContainerModel) && (((ContainerModel) model).getNumberOfModels() > 0)) {
			model = ((ContainerModel) model).getModel(0);
		}

		if (!(model instanceof JMySVMModel)) {
			throw new OperatorException("Expected JMySVMModel instead of: " + model.getClass().getName());
		}

		JMySVMModel svmmodel = (JMySVMModel) model;

		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples modelSet = svmmodel.getExampleSet();

		double[][] atts = modelSet.atts;
		int[][] index = modelSet.index;
		double[] y = modelSet.get_ys();
		double[] alphas = modelSet.get_alphas();
		boolean addalpha = getParameterAsBoolean(PARAMETER_ALPHA);
		Map meanvariances = modelSet.getMeanVariances();
		boolean scaled = meanvariances != null;
		if (meanvariances != null) {
			scaled = !meanvariances.isEmpty();
		}

		int dim = modelSet.get_dim();
		int size = modelSet.count_examples();

		// Attribute
		// label=AttributeFactory.createAttribute("class",Ontology.REAL);
		Attribute label = AttributeFactory.createAttribute("class", Ontology.NOMINAL);
		StringTokenizer st = new StringTokenizer(getParameterAsString(PARAMETER_CLASSES), " ");
		String pos = "p", neg = "n";
		if (st.hasMoreTokens())
			pos = st.nextToken();
		if (st.hasMoreTokens())
			neg = st.nextToken();
		label.getMapping().mapString(pos);
		label.getMapping().mapString(neg);

		Attribute alpha = AttributeFactory.createAttribute("alpha", Ontology.REAL);

		// create table
		List<Attribute> attributes = new LinkedList<Attribute>();
		for (int i = 0; i < dim; i++) {
			attributes.add(AttributeFactory.createAttribute("att_" + (i + 1), Ontology.REAL));
		}
		if (addalpha)
			attributes.add(alpha);
		attributes.add(label);

		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// create data
		List<DataRow> data = new LinkedList<DataRow>();
		double[] samples;
		int nr = dim + 1;
		if (addalpha)
			nr++;

		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples.MeanVariance meanvariance;
		for (int n = 0; n < size; n++) {
			if (alphas[n] == 0.0d) {
				continue;
			}
			samples = new double[nr];
			for (int i = 0; i < dim; i++) {
				samples[i] = 0.0d;
			}

			for (int i = 0; i < atts[n].length; i++) {
				samples[index[n][i]] = atts[n][i];
			}

			if (scaled) {
				for (int i = 0; i < dim; i++) {
					meanvariance = (com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples.MeanVariance) meanvariances.get(Integer.valueOf(i));
					if (meanvariance != null) {
						samples[i] = samples[i] * Math.sqrt(meanvariance.getVariance()) + meanvariance.getMean();
					}
				}
			}

			if (addalpha)
				samples[nr - 2] = alphas[n];
			if (y[n] == 1.0d) {
				samples[nr - 1] = label.getMapping().getPositiveIndex();
			} else {
				samples[nr - 1] = label.getMapping().getNegativeIndex();
			}

			data.add(new DoubleArrayDataRow(samples));
		}

		// fill table with data
		table.readExamples(new ListDataRowReader(data.iterator()));

		ExampleSet svSet = null;
		// create example set and return it
		svSet = table.createExampleSet(label);
		svSet.recalculateAllAttributeStatistics();

		SVMInterface svm = svmmodel.createSVM();
		svm.init(svmmodel.getKernel(), svmmodel.getExampleSet());
		double[] weights = svm.getWeights();

		AttributeWeights attweights = new AttributeWeights();
		int w = 0;
		for (Attribute attribute : svSet.getAttributes()) {
			attweights.setWeight(attribute.getName(), weights[w++]);
		}

		return new IOObject[] { svSet, attweights };
	}

	public Class[] getInputClasses() {
		return new Class[] { Model.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, AttributeWeights.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_ALPHA, "Add the alpha values as an attribute.", false));
		types.add(new ParameterTypeString(PARAMETER_CLASSES, "The string representation of the classes. Note: Specify the negative class first!", "negative positive"));
		return types;
	}
}
