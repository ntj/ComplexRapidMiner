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
package com.rapidminer.operator.learner.functions.kernel;

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.kernel.Kernel;
import com.rapidminer.operator.learner.functions.kernel.jmysvm.svm.SVMInterface;


/**
 * The abstract superclass for the SVM models by Stefan Rueping.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractMySVMModel.java,v 1.11 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public abstract class AbstractMySVMModel extends KernelModel {

	private com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples model;

	private Kernel kernel;

	public AbstractMySVMModel(ExampleSet exampleSet, com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples model, Kernel kernel, int kernelType) {
		super(exampleSet);
		this.model = model;
		this.kernel = kernel;
	}

	/** Creates a new SVM for prediction. */
	public abstract SVMInterface createSVM();
	
	public boolean isClassificationModel() {
		return getLabel().isNominal();
	}
	
	public double getBias() {
		return model.get_b();
	}

	/** This method must divide the alpha by the label since internally the alpha value is already multiplied with y. */
	public SupportVector getSupportVector(int index) {
		return new SupportVector(model.get_example(index).toDense(getNumberOfAttributes()), model.get_y(index), getAlpha(index) / model.get_y(index));
	}
	
	public double getAlpha(int index) {
		return model.get_alpha(index);
	}
	
	public String getId(int index) {
		return model.getId(index);
	}
	
	public int getNumberOfSupportVectors() {
		return model.count_examples(); 
	}
	
	public int getNumberOfAttributes() {
		return model.get_dim();
	}
	
	public double getAttributeValue(int exampleIndex, int attributeIndex) {
		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExample sVMExample = model.get_example(exampleIndex);
		double value = 0.0d;
		try {
			value = sVMExample.toDense(getNumberOfAttributes())[attributeIndex];
		} catch (ArrayIndexOutOfBoundsException e) {
			// dense array to short --> use default value
		}
		return value;
	}
	
	public String getClassificationLabel(int index) {
		double y = model.get_y(index);
		if (y < 0)
			return getLabel().getMapping().getNegativeString();
		else
			return getLabel().getMapping().getPositiveString();
	}
	
	public double getRegressionLabel(int index) {
		return model.get_y(index);
	}
	
	public double getFunctionValue(int index) {
		SVMInterface svm = createSVM();
		svm.init(kernel, model);
		return svm.predict(model.get_example(index));
	}
	
	/** Gets the kernel. */
	public Kernel getKernel() {
		return kernel;
	}

	/** Gets the model, i.e. an SVM example set. */
	public com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples getExampleSet() {
		return model;
	}
	
	/**
	 * Sets the correct prediction to the example from the result value of the
	 * SVM.
	 */
	public abstract void setPrediction(Example example, double prediction);

	public ExampleSet performPrediction(ExampleSet exampleSet, Attribute predictedLabelAttribute) throws OperatorException {
		com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples toPredict = new com.rapidminer.operator.learner.functions.kernel.jmysvm.examples.SVMExamples(exampleSet, exampleSet.getAttributes().getPredictedLabel(), model.getMeanVariances());

		SVMInterface svm = createSVM();
		svm.init(kernel, model);
		svm.predict(toPredict);

		// set predictions from toPredict
		Iterator<Example> reader = exampleSet.iterator();
		int k = 0;
		while (reader.hasNext()) {
			setPrediction(reader.next(), toPredict.get_y(k++));
		}
		
		return exampleSet;
	}
}
