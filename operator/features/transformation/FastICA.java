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
package com.rapidminer.operator.features.transformation;

import java.util.Iterator;
import java.util.List;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

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
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.RandomGenerator;
import com.rapidminer.tools.math.MathFunctions;

/**
 * This operator performs the independent componente analysis (ICA).
 * Implementation of the FastICA-algorithm of Hyvaerinen und Oja. The operator
 * outputs a <code>FastICAModel</code>. With the <code>ModelApplier</code>
 * you can transform the features.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: FastICA.java,v 1.10 2008/08/21 13:17:07 ingomierswa Exp $
 * @see FastICAModel
 */
public class FastICA extends Operator {

	/** The parameter name for &quot;Number components to be extracted (-1 number of attributes is used).&quot; */
	public static final String PARAMETER_NUMBER_OF_COMPONENTS = "number_of_components";

	/** The parameter name for &quot;If 'parallel' the components are extracted simultaneously, 'deflation' the components are extracted one at a time&quot; */
	public static final String PARAMETER_ALGORITHM_TYPE = "algorithm_type";

	/** The parameter name for &quot;The functional form of the G function used in the approximation to neg-entropy&quot; */
	public static final String PARAMETER_FUNCTION = "function";

	/** The parameter name for &quot;constant in range [1, 2] used in approximation to neg-entropy when fun="logcosh"&quot; */
	public static final String PARAMETER_ALPHA = "alpha";

	/** The parameter name for &quot;Indicates whether rows of the data matrix &quot; */
	public static final String PARAMETER_ROW_NORM = "row_norm";

	/** The parameter name for &quot;maximum number of iterations to perform&quot; */
	public static final String PARAMETER_MAX_ITERATION = "max_iteration";

	/** The parameter name for &quot;A positive scalar giving the tolerance at which &quot; */
	public static final String PARAMETER_TOLERANCE = "tolerance";
	
	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	
	private static final Class[] INPUT_CLASSES = new Class[] { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = new Class[] { ExampleSet.class, Model.class };

	private static final String[] ALGORITHM_TYPE = new String[] { "deflation", "parallel" };

	private static final String[] FUNCTION = new String[] { "logcosh", "exp" };

	private int algorithmType;

	private int function;

	private int numberOfComponents;

	private double tolerance;

	private double alpha;

	private boolean rowNorm;

	private int maxIteration;

	private int numberOfSamples, numberOfAttributes;

	private Attribute[] attributes;

	private double[] means;

	private double[][] data;

	private double[][] wInit;

    
	public FastICA(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// get the ExampleSet
		ExampleSet set = getInput(ExampleSet.class);
		set.recalculateAllAttributeStatistics();
		numberOfSamples = set.size();
		numberOfAttributes = set.getAttributes().size();

		// all attributes numerical
		attributes = new Attribute[numberOfAttributes];
		means = new double[numberOfAttributes];
		int i = 0;
		Iterator<Attribute> atts = set.getAttributes().iterator();
		while (atts.hasNext()) {
			attributes[i] = atts.next();
			if (!attributes[i].isNumerical()) {
				throw new UserError(this, 104, new Object[] { "FastICA", attributes[i].getName() });
			}
			means[i] = set.getStatistics(attributes[i], Statistics.AVERAGE);
			i++;
		}

		// get the parameter
		algorithmType = getParameterAsInt(PARAMETER_ALGORITHM_TYPE);
		function = getParameterAsInt(PARAMETER_FUNCTION);
		tolerance = getParameterAsDouble(PARAMETER_TOLERANCE);
		alpha = getParameterAsDouble(PARAMETER_ALPHA);
		rowNorm = getParameterAsBoolean(PARAMETER_ROW_NORM);
		maxIteration = getParameterAsInt(PARAMETER_MAX_ITERATION);
		numberOfComponents = getParameterAsInt(PARAMETER_NUMBER_OF_COMPONENTS);
        
		if (numberOfComponents < 1) {
			numberOfComponents = numberOfAttributes;
		}
		if (numberOfComponents > numberOfAttributes) {
			numberOfComponents = numberOfAttributes;
			logWarning("The parameter 'number_of_components' is too large! Set to number of attributes.");
		}

		// get the centered data
		data = new double[numberOfSamples][numberOfAttributes];
		Iterator<Example> reader = set.iterator();
		Example example;

		for (int sample = 0; sample < numberOfSamples; sample++) {
			example = reader.next();
			for (int d = 0; d < numberOfAttributes; d++) {
				data[sample][d] = example.getValue(attributes[d]) - means[d];
			}
		}

		log("Initializing the weights...");
		// init the weight matrix
		// w.init <- matrix(rnorm(n.comp^2),n.comp,n.comp)
		wInit = new double[numberOfComponents][numberOfComponents];
		// init w randomly
		RandomGenerator randomGenerator = RandomGenerator.getRandomGenerator(getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED));
		for (i = 0; i < numberOfComponents; i++) {
			for (int j = 0; j < numberOfComponents; j++) {
				wInit[i][j] = randomGenerator.nextDouble() * 2 - 1;
			}
		}

		// row normalization
		// Scaling is done by dividing the rows of the data
		// by their root-mean-square. The root-mean-square for a row
		// is obtained by computing the
		// square-root of the sum-of-squares of the values in the
		// row divided by the number of values minus one.
		if (rowNorm) {
			log("Scaling the data now.");
			double rms_row;
			for (int row = 0; row < numberOfSamples; row++) {
				// compute root mean square for the row
				rms_row = 0.0d;
				for (int d = 0; d < numberOfAttributes; d++) {
					rms_row += data[row][d] * data[row][d];
				}
				rms_row = Math.sqrt(rms_row) / Math.max(1, numberOfAttributes - 1);

				for (int d = 0; d < numberOfAttributes; d++) {
					data[row][d] = data[row][d] / rms_row;
				}
			}
		}

		Matrix X = new Matrix(data);
		X = X.transpose();

		// Whitening
		log("Whitening the data now.");
		// V <- X %*% t(X)/n
		// V nrow=nr_atts ncol=nr_atts
		Matrix V = X.times(X.transpose().timesEquals(1.0d / numberOfSamples));

		// s <- La.svd(V, method="dgesdd")
		SingularValueDecomposition svd = V.svd();

		// D <- diag(c(1/sqrt(s$d)))
		Matrix D = svd.getS();
		double[][] singularvalue = D.getArray();

		for (i = 0; i < singularvalue.length; i++) {
			singularvalue[i][i] = 1.0d / Math.sqrt(singularvalue[i][i]);
		}
		D = new Matrix(singularvalue);

		// K <- D %*% t(s$u)
		Matrix K = D.times(svd.getU().transpose());
		// K <- matrix(K[1:n.comp, ], n.comp, p)
		K = new Matrix(K.getArray(), numberOfComponents, numberOfAttributes);
		// X1 <- K %*% X
		Matrix X1 = K.times(X);
		// end Whitening

		Matrix a;
		if (algorithmType == 0) {
			a = deflation(X1);
		} else {
			a = parallel(X1);
		}

		log("Creating the model...");
		// w <- a %*% K
		Matrix w = a.times(K);
		// S <- w %*% X
		// Matrix S = w.times(X);	// Unused. Shevek
		// A <- t(w) %*% solve(w %*% t(w))
		Matrix W2 = w.times(w.transpose());
		Matrix A = w.transpose().times(W2.inverse());

		/*
		 * return(list(X = t(X), K = t(K), W = t(a), A = t(A), S = t(S)))
		 */
		Matrix W;
		X = X.transpose();
		K = K.transpose();
		W = a.transpose();
		A = A.transpose();
		// S = S.transpose();	// Unused. Shevek

		// X pre-processed data matrix
		// X(nr_samples,nr_atts)

		// K pre-whitening matrix that projects data onto th first n.comp
		// principal components.
		// K(nr_atts,nr_components)

		// W estimated un-mixing matrix
		// W(nr_components,nr_components)

		// A estimated mixing matrix
		// A(nr_components,nr_atts)

		// S estimated source matrix
		// S(nr_samples,nr_components)

		FastICAModel model = new FastICAModel(set, numberOfComponents, means, rowNorm, K, W, A);

		return new IOObject[] { set, model };
	}

	private Matrix deflation(Matrix X) throws OperatorException {
		log("Deflation FastICA using " + FUNCTION[function] + " approx. to neg-entropy function");

		// X(nr_components,nr_samples)
		// n <- nrow(X)
		// p <- ncol(X)
		// int n=X.getRowDimension();
		// int p=X.getColumnDimension();

		// W <- matrix(0, n.comp, n.comp)
		Matrix W = new Matrix(numberOfComponents, numberOfComponents, 0.0d);

		Matrix w, t, Wu, wx, gwx, xgwx, g_wx;
		double k, rss, lim, value;
		int iter;
		int iterlog = 1;
		while ((maxIteration / iterlog > 10) && (maxIteration / (iterlog * 10) >= 3)) {
			iterlog *= 10;
		}
		for (int i = 0; i < numberOfComponents; i++) {
			log("Component " + (i + 1));

			// w <- matrix(w.init[i,], n.comp, 1)
			w = new Matrix(wInit[i], wInit[i].length);

			if (i > 0) {
				// t <- w
				// t[1:length(t)] <- 0
				t = new Matrix(wInit[i].length, 1, 0.0d);

				// for (u in 1:(i - 1)) {
				for (int u = 1; u <= i; u++) {
					// W[u, ]
					// Wu(1,nr_components)
					Wu = W.getMatrix(u - 1, u - 1, 0, numberOfComponents - 1);
					// k <- sum(w * W[u, ])
					// k(1,1)
					k = w.transpose().times(Wu.transpose()).getArray()[0][0];
					// t <- t + k * W[u, ]
					t.plusEquals(Wu.times(k).transpose());
				}
				// w <- w - t
				w.minusEquals(t);
			}

			// sqrt(sum(w^2))
			rss = Math.sqrt(w.times(w.transpose()).getArray()[0][0]);
			// w <- w/sqrt(sum(w^2))
			// w(nr_components,1)
			w.timesEquals(1.0d / rss);

			// lim <- rep(1000, maxit)
			lim = 1000.0d;
			// it <- 1
			iter = 1;

			// while (lim[it] > tol && it < maxit) {
			while (lim > tolerance && iter <= maxIteration) {
				// wx <- t(w) %*% X
				// wx(1,nr_samples)
				wx = w.transpose().times(X);
				double[][] wxarray = wx.getArray();
				if (function == 0) {
					// logcosh function
					// gwx <- tanh(alpha * wx)
					for (int j = 0; j < wxarray[0].length; j++) {
						wxarray[0][j] = MathFunctions.tanh(alpha * wxarray[0][j]);
					}
				} else {
					// exp function
					// gwx <- wx * exp(-(wx^2)/2)
					for (int j = 0; j < wxarray[0].length; j++) {
						wxarray[0][j] = wxarray[0][j] * Math.exp(-0.5d * wxarray[0][j] * wxarray[0][j]);
					}
				}
				// gwx(1,nr_samples)
				gwx = new Matrix(wxarray);

				// gwx <- matrix(gwx, n.comp, p, byrow = TRUE)
				// gwx(nr_components,nr_samples)
				// xgwx <- X * gwx
				// gwx(nr_components,nr_samples)
				double[][] gwxarray = gwx.getArray();
				double[][] Xarray = X.getArray();

				for (int row = 0; row < Xarray.length; row++) {
					for (int col = 0; col < Xarray[0].length; col++) {
						Xarray[row][col] = Xarray[row][col] * gwxarray[0][col];
					}
				}
				// xgwx(nr_components,nr_samples)
				xgwx = new Matrix(Xarray);

				// v1 <- apply(xgwx, 1, FUN = mean)
				// calculates the mean of each row
				Matrix v1 = new Matrix(numberOfComponents, 1, 0.0d);
				double mean;
				for (int row = 0; row < numberOfComponents; row++) {
					mean = 0;
					for (int col = 0; col < numberOfSamples; col++) {
						mean += xgwx.get(row, col);
					}
					mean = mean / numberOfSamples;
					v1.set(row, 0, mean);
				}

				// g_wx(1,nr_samples)
				g_wx = wx.copy();
				mean = 0.0d;
				if (function == 0) {
					// logcosh function
					// g.wx <- alpha * (1 - (tanh(alpha * wx))^2)
					for (int j = 0; j < wxarray[0].length; j++) {
						value = MathFunctions.tanh(alpha * g_wx.get(0, j));
						value = alpha * (1.0d - value * value);
						mean += value;
						g_wx.set(0, j, value);
					}
				} else {
					// exp function
					// g.wx <- (1 - wx^2) * exp(-(wx^2)/2)
					for (int j = 0; j < wxarray[0].length; j++) {
						value = g_wx.get(0, j);
						value = (1.0d - value * value) * Math.exp(-0.5d * value * value);
						mean += value;
						g_wx.set(0, j, value);
					}
				}
				mean /= numberOfSamples;

				// v2 <- mean(g.wx) * w
				// v2(nr_components,1)
				Matrix v2 = w.copy();
				v2.timesEquals(mean);

				// w1 <- v1 - v2
				// w1 <- matrix(w1, n.comp, 1)
				// w1(nr_components,1)
				Matrix w1 = v1.minus(v2);

				if (i > 0) {
					// t <- w1
					// t[1:length(t)] <- 0
					t = new Matrix(w1.getRowDimension(), w1.getColumnDimension(), 0.0d);

					// for (u in 1:(i - 1)) {
					for (int u = 1; u <= i; u++) {
						// W[u, ]
						// Wu(1,nr_components)
						Wu = W.getMatrix(u - 1, u - 1, 0, numberOfComponents - 1);

						// k <- sum(w1 * W[u, ])
						// k(1,1)
						k = w1.transpose().times(Wu.transpose()).getArray()[0][0];
						// t <- t + k * W[u, ]
						t.plusEquals(Wu.times(k).transpose());
					}
					// w1 <- w1 - t
					w1.minusEquals(t);
				}

				// w1 <- w1/sqrt(sum(w1^2))
				rss = Math.sqrt(w1.transpose().times(w1).getArray()[0][0]);
				w1.timesEquals(1.0d / rss);

				// lim[it] <- Mod(Mod(sum((w1 * w))) - 1)
				lim = Math.abs(Math.abs(w1.transpose().times(w).getArray()[0][0]) - 1.0d);

				if ((iter % iterlog == 0) || (lim <= tolerance)) {
				    log("Iteration " + (iter) + ", tolerance = " + lim);
				}
				iter++;

				// w <- matrix(w1, n.comp, 1)
				w = w1.copy();
			}

			// W[i, ] <- w
			for (int col = 0; col < numberOfComponents; col++) {
				W.set(i, col, w.get(col, 0));
			}
			checkForStop();
		}
		return W;
	}

	private Matrix parallel(Matrix X) throws OperatorException {

		log("Symmetric FastICA using " + FUNCTION[function] + " approx. to neg-entropy function");

		// X(nr_components,nr_samples)
		// n <- nrow(X)
		// p <- ncol(X)
		// int n=X.getRowDimension();
		int p = X.getColumnDimension();

		// W <- w.init
		Matrix W = new Matrix(wInit);

		// sW <- La.svd(W, method = "dgesdd")
		SingularValueDecomposition svd = W.svd();

		// diag(1/sW$d)
		double[] svalues = svd.getSingularValues();
		Matrix svaluesMatrix = new Matrix(svalues.length, svalues.length, 0.0d);
		for (int i = 0; i < svalues.length; i++) {
			svalues[i] = 1 / svalues[i];
			svaluesMatrix.set(i, i, svalues[i]);
		}
		// W <- sW$u %*% diag(1/sW$d) %*% t(sW$u) %*% W
		// W(nr_components,nr_components)
		W = svd.getU().times(svaluesMatrix).times(svd.getU().transpose()).times(W);
		// W1 <- W
		Matrix W1 = W.copy();

		// lim <- rep(1000, maxit)
		double lim = 1000.0d;

		int iter = 1;
		int iterlog = 1;
		while ((maxIteration / iterlog > 10) && (maxIteration / (iterlog * 10) >= 3)) {
			iterlog *= 10;
		}

		Matrix wx, gwx, v1, g_wx, diagmean, v2;
		double value, mean;
		// while (lim[it] > tol && it < maxit) {
		while (lim > tolerance && iter <= maxIteration) {
			// wx <- W %*% X
			// wx(nr_components,nr_samples)
			wx = W.times(X);

			// gwx(nr_components,nr_samples)
			gwx = wx.copy();
			if (function == 0) {
				// logcosh function
				// gwx <- tanh(alpha * wx)
				for (int row = 0; row < numberOfComponents; row++) {
					for (int col = 0; col < numberOfSamples; col++) {
						value = gwx.get(row, col);
						value = MathFunctions.tanh(alpha * value);
						gwx.set(row, col, value);
					}
				}
			} else {
				// exp function
				// gwx <- wx * exp(-(wx^2)/2)
				for (int row = 0; row < numberOfComponents; row++) {
					for (int col = 0; col < numberOfSamples; col++) {
						value = gwx.get(row, col);
						value = value * Math.exp(-0.5d * value * value);
						gwx.set(row, col, value);
					}
				}
			}

			// v1 <- gwx %*% t(X)/p
			// v1(nr_components,nr_components)
			v1 = gwx.times(X.transpose()).times(p);

			// g_wx(nr_components,nr_samples)
			g_wx = gwx.copy();
			// diag(apply(g.wx, 1, FUN = mean))
			diagmean = new Matrix(numberOfComponents, numberOfComponents, 0.0d);
			if (function == 0) {
				// logcosh funtion
				// g.wx <- alpha * (1 - (gwx)^2)
				for (int row = 0; row < numberOfComponents; row++) {
					mean = 0.0d;
					for (int col = 0; col < numberOfSamples; col++) {
						value = g_wx.get(row, col);
						value = alpha * (1.0d - value * value);
						g_wx.set(row, col, value);
						mean += value;
					}
					mean = mean / numberOfSamples;
					diagmean.set(row, row, mean);
				}
			} else {
				// exp function
				// g.wx <- (1 - wx^2) * exp(-(wx^2)/2)
				g_wx = wx.copy();
				for (int row = 0; row < numberOfComponents; row++) {
					mean = 0.0d;
					for (int col = 0; col < numberOfSamples; col++) {
						value = g_wx.get(row, col);
						value = (1.0d - value * value) * Math.exp(-0.5d * value * value);
						g_wx.set(row, col, value);
						mean += value;
					}
					mean = mean / numberOfSamples;
					diagmean.set(row, row, mean);
				}
			}

			// v2 <- diag(apply(g.wx, 1, FUN = mean)) %*% W
			// v2(nr_components,nr_components)
			v2 = diagmean.times(W);

			// W1 <- v1 - v2
			// W1(nr_components,nr_components)
			W1 = v1.minus(v2);

			// sW1 <- La.svd(W1, method = "dgesdd")
			svd = W1.svd();

			// diag(1/sW1$d)
			svalues = svd.getSingularValues();
			svaluesMatrix = new Matrix(svalues.length, svalues.length, 0.0d);
			for (int i = 0; i < svalues.length; i++) {
				svalues[i] = 1 / svalues[i];
				svaluesMatrix.set(i, i, svalues[i]);
			}
			// W1 <- sW1$u %*% diag(1/sW1$d) %*% t(sW1$u) %*% W1
			// W1(nr_components,nr_components)
			W1 = svd.getU().times(svaluesMatrix).times(svd.getU().transpose()).times(W1);

			// lim[it + 1] <- max(Mod(Mod(diag(W1 %*% t(W))) - 1))
			double[][] diag = W1.times(W.transpose()).getArray();
			value = Double.NEGATIVE_INFINITY;
			for (int row = 0; row < numberOfComponents; row++) {
				value = Math.max(value, Math.abs(Math.abs(diag[row][row]) - 1.0d));
			}
			lim = value;

			// W <- W1
			W = W1.copy();

			if ((iter % iterlog == 0) || (lim <= tolerance)) {
				log("Iteration " + (iter) + ", tolerance = " + lim);
			}
			iter++;
		}
		return W;
	}

	public Class<?>[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class<?>[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		
		ParameterType type = new ParameterTypeInt(PARAMETER_NUMBER_OF_COMPONENTS, "Number components to be extracted (-1 number of attributes is used).", -1, Integer.MAX_VALUE, -1);
        type.setExpert(false);
		list.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_ALGORITHM_TYPE, "If 'parallel' the components are extracted simultaneously, 'deflation' the components are extracted one at a time", ALGORITHM_TYPE, 0);
		list.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_FUNCTION, "The functional form of the G function used in the approximation to neg-entropy", FUNCTION, 0);
		list.add(type);
		
		type = new ParameterTypeDouble(PARAMETER_ALPHA, "constant in range [1, 2] used in approximation to neg-entropy when fun=\"logcosh\"", 1.0d, 2.0d, 1.0d);
		list.add(type);
		
		type = new ParameterTypeBoolean(PARAMETER_ROW_NORM, "Indicates whether rows of the data matrix " + "should be standardized beforehand.", false);
		list.add(type);
		
		type = new ParameterTypeInt(PARAMETER_MAX_ITERATION, "maximum number of iterations to perform", 0, Integer.MAX_VALUE, 200);
		list.add(type);
		
		type = new ParameterTypeDouble(PARAMETER_TOLERANCE, "A positive scalar giving the tolerance at which " + "the un-mixing matrix is considered to have converged.", 0.0d, Double.POSITIVE_INFINITY, 1e-4);
		list.add(type);
		
        type = new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1);
        list.add(type);
        
		return list;
	}
}
