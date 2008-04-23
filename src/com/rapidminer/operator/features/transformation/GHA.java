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

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;

import Jama.Matrix;

/**
 * Generalized Hebbian Algorithm (GHA) is an iterative method to compute
 * principal components. From a computational point of view, it can be
 * advantageous to solve the eigenvalue problem by iterative methods which do
 * not need to compute the covariance matrix directly. This is useful when the
 * ExampleSet contains many Attributes (hundreds, thousands). The operator
 * outputs a <code>GHAModel</code>. With the <code>ModelApplier</code> you
 * can transform the features.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: GHA.java,v 1.4 2007/07/13 22:52:14 ingomierswa Exp $
 */
public class GHA extends Operator {


	/** The parameter name for &quot;Number of components to compute. If '-1' nr of attributes is taken.'&quot; */
	public static final String PARAMETER_NUMBER_OF_COMPONENTS = "number_of_components";

	/** The parameter name for &quot;Number of Iterations to apply the update rule.&quot; */
	public static final String PARAMETER_NUMBER_OF_ITERATIONS = "number_of_iterations";

	/** The parameter name for &quot;The learning rate for GHA (small)&quot; */
	public static final String PARAMETER_LEARNING_RATE = "learning_rate";

	/** The parameter name for &quot;The local random seed for this operator, uses global random number generator if -1.&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	private double learningRate;

	private int numberOfComponents;

	private int numberOfIterations;

	private double[] means;

	private double[][] data;

	private Matrix W;

    private RandomGenerator random;
    
    
	public GHA(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
        this.random = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		exampleSet.recalculateAllAttributeStatistics();

		// 1) check wether all attributes are numerical
		means = new double[exampleSet.getAttributes().size()];
		int a = 0;
		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal()) {
				throw new UserError(this, 104, "GHA", attribute.getName());
			}
			means[a] = exampleSet.getStatistics(attribute, Statistics.AVERAGE);
			a++;
		}

		log("Initialising the weight matrix...");
        
		// 2) create data and substract the mean
		data = new double[exampleSet.size()][exampleSet.getAttributes().size()];

		Iterator<Example> reader = exampleSet.iterator();
		Example example;
		for (int sample = 0; sample < exampleSet.size(); sample++) {
			example = reader.next();
			int d = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				data[sample][d] = example.getValue(attribute) - means[d];
				d++;
			}
			checkForStop();
		}

		// init
		learningRate = getParameterAsDouble(PARAMETER_LEARNING_RATE);
		numberOfComponents = getParameterAsInt(PARAMETER_NUMBER_OF_COMPONENTS);
		if (numberOfComponents < 0)
			numberOfComponents = exampleSet.getAttributes().size();
		numberOfIterations = getParameterAsInt(PARAMETER_NUMBER_OF_ITERATIONS);

        double[][] randomMatrix = new double[numberOfComponents][exampleSet.getAttributes().size()];
        for (int i = 0; i < randomMatrix.length; i++) {
            for (int j = 0; j < randomMatrix[i].length; j++) {
                randomMatrix[i][j] = random.nextDouble();
            }            
        }
        W = new Matrix(randomMatrix);
		W.timesEquals(0.1d);

		log("Training with learning rate: " + learningRate);
		train();

		log("Creating the model...");
		// compute eigenvalues
		// --> create covariancematrix
		// --> multiply with eigenvector
		// --> calculate eigenvalue

		double[][] covarianceMatrixEntries = new double[exampleSet.getAttributes().size()][exampleSet.getAttributes().size()];

		// fill the covariancematrix
		double covariance;
		for (int i = 0; i < exampleSet.getAttributes().size(); i++) {
			for (int j = 0; j < exampleSet.getAttributes().size(); j++) {
				covariance = getCovariance(i, j);
				if (i != j) {
					covarianceMatrixEntries[i][j] = covariance;
					covarianceMatrixEntries[j][i] = covariance;
				} else {
					covarianceMatrixEntries[i][j] = covariance;
				}
				checkForStop();
			}
		}

		Matrix covarianceMatrix = new Matrix(covarianceMatrixEntries);
		Matrix tmp = W.times(covarianceMatrix);

		double[][] weights = W.getArray();
		double[][] tmparray = tmp.getArray();

		double[] eigenvalues = new double[numberOfComponents];

		for (int i = 0; i < weights.length; i++) {
			double nr = 0;
			eigenvalues[i] = 0.0d;
			for (int j = 0; j < weights[0].length; j++) {
				tmparray[i][j] = tmparray[i][j] / weights[i][j];
				if (tmparray[i][j] > 0.0d) {
					nr += 1.0d;
					eigenvalues[i] += tmparray[i][j];
				}
			}
			nr = Math.max(nr, 1.0d);
			eigenvalues[i] = eigenvalues[i] / nr;
		}

		GHAModel model = new GHAModel(exampleSet, eigenvalues, W.getArray(), means);

		return new IOObject[] { exampleSet, model };
	}

	private double getCovariance(int dim1, int dim2) {
		double cov = 0.0d;
		for (int sample = 0; sample < data.length; sample++) {
			cov += data[sample][dim1] * data[sample][dim2];
		}
		cov = cov / (data.length - 1);
		return cov;
	}

	private void train() throws OperatorException {
		int sample;
		Matrix x;
		Matrix y;

		int iterlog = 1;
		while ((numberOfIterations / iterlog > 10) && (numberOfIterations / (iterlog * 10) >= 3)) {
			iterlog *= 10;
		}

        
		for (int iter = 1; iter <= numberOfIterations; iter++) {
			if (iter % iterlog == 0) {
				log("Iteration " + iter);
			}

			sample = (int) (random.nextDouble() * data.length);

			// sample as matrix
			x = new Matrix(data[sample], data[sample].length);

			// create output y
			// y = W'*x;
			y = W.times(x);

			// double[rows][columns]
			double[][] yyT = y.times(y.transpose()).getArray();
			// lower triangular

			for (int row = 0; row < yyT.length; row++) {
				for (int col = row + 1; col < yyT.length; col++) {
					yyT[row][col] = 0.0d;
				}
			}

			// the lower triangular matrix
			Matrix LT = new Matrix(yyT);

			// W = W + beta*(x*y' - W*tril(y*y'));
			// beta = options.rate*options.annealfunc(iter);
			Matrix tmp1 = y.times(x.transpose());
			Matrix tmp2 = LT.times(W);
			tmp1 = tmp1.minus(tmp2);
			tmp1.timesEquals(learningRate);

			W.plusEquals(tmp1);

			double[][] w = W.getArray();
			for (int i = 0; i < w.length; i++) {
				for (int j = 0; j < w[0].length; j++) {
					if ((Double.isInfinite(w[i][j])) || (Double.isNaN(w[i][j]))) {
						throw new OperatorException("Lost convergence at iterator " + (iter + 1) + ". Lower learning rate?");
					}
				}
			}
			checkForStop();
			// all ok continue iteration
		}
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_COMPONENTS, "Number of components to compute. If \'-1\' nr of attributes is taken.'", -1, Integer.MAX_VALUE, -1);
        type.setExpert(false);
        list.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_OF_ITERATIONS, "Number of Iterations to apply the update rule.", 0, Integer.MAX_VALUE, 10);
		list.add(type);
		type = new ParameterTypeDouble(PARAMETER_LEARNING_RATE, "The learning rate for GHA (small)", 0.0d, Double.POSITIVE_INFINITY, 0.01d);
		list.add(type);
        list.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "The local random seed for this operator, uses global random number generator if -1.", -1, Integer.MAX_VALUE, -1));
		return list;
	}
}
