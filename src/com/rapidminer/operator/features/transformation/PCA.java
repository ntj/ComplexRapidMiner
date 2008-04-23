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
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

/**
 * This operator performs a principal components analysis (PCA) using the
 * covariance matrix. The user can specify the amount of variance to cover in
 * the original data when retaining the best number of principal components. The
 * user can also specify manually the number of principal components. The
 * operator outputs a <code>PCAModel</code>. With the
 * <code>ModelApplier</code> you can transform the features.
 * 
 * @author Daniel Hakenjos, Ingo Mierswa
 * @version $Id: PCA.java,v 1.3 2007/07/10 18:02:03 ingomierswa Exp $
 * @see PCAModel
 */
public class PCA extends Operator {


	/** The parameter name for &quot;Keep the all components with a cumulative variance smaller than the given threshold.&quot; */
	public static final String PARAMETER_VARIANCE_THRESHOLD = "variance_threshold";

	/** The parameter name for &quot;Keep this number of components. If '-1' then keep all components.'&quot; */
	public static final String PARAMETER_NUMBER_OF_COMPONENTS = "number_of_components";
    public static final String PARAMETER_REDUCTION_TYPE = "dimensionality_reduction";
    
    public static final String[] REDUCTION_METHODS = new String[] {
        "none",
        "keep variance",
        "fixed number"
    };
    
    public static final int REDUCTION_NONE     = 0;
    public static final int REDUCTION_VARIANCE = 1;
    public static final int REDUCTION_FIXED    = 2;
    
	private double[][] data;

	private Matrix covarianceMatrix;

	private double[][] covarianceMatrixEntries;

	private double[] eigenvalues;

	private Matrix eigenvectorMatrix;

	private double[][] eigenvectors;

	public PCA(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// 1) check wether all attributes are numerical
		ExampleSet exampleSet = getInput(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();

		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal()) {
				throw new UserError(this, 104, "PCA", attribute.getName());
			}
		}

		// 2) create data and substract the mean
		data = new double[exampleSet.size()][exampleSet.getAttributes().size()];

		Iterator<Example> reader = exampleSet.iterator();
		for (int sample = 0; sample < exampleSet.size(); sample++) {
			Example example = reader.next();
			int d = 0;
			for (Attribute attribute : exampleSet.getAttributes()) {
				data[sample][d++] = example.getValue(attribute) - exampleSet.getStatistics(attribute, Statistics.AVERAGE);
			}

		}

		log("Creating the covariance matrix...");
		
		// 3) Create Covarianz-Matrix
		covarianceMatrixEntries = new double[exampleSet.getAttributes().size()][exampleSet.getAttributes().size()];

		// fill the covariance matrix
		for (int i = 0; i < exampleSet.getAttributes().size(); i++) {
			for (int j = 0; j < exampleSet.getAttributes().size(); j++) {
				double covariance = getCovarianz(i, j);
				if (i != j) {
					covarianceMatrixEntries[i][j] = covariance;
					covarianceMatrixEntries[j][i] = covariance;
				} else {
					covarianceMatrixEntries[i][j] = covariance;
				}
			}
			checkForStop();
		}
		covarianceMatrix = new Matrix(covarianceMatrixEntries);

		log("Performing the eigenvalue decomposition...");
		
		// 4) EigenVector and EigenValues of the covarianz-matrix
		EigenvalueDecomposition eigenvalueDecomposition = covarianceMatrix.eig();

		eigenvalues = eigenvalueDecomposition.getRealEigenvalues();
		eigenvectorMatrix = eigenvalueDecomposition.getV();
		eigenvectors = eigenvectorMatrix.getArray();

		PCAModel model = new PCAModel(exampleSet, eigenvalues, eigenvectors);
		
        int reductionType = getParameterAsInt(PARAMETER_REDUCTION_TYPE);
        switch (reductionType) {
            case REDUCTION_NONE:
                model.setNumberOfComponents(exampleSet.getAttributes().size());
                break;
            case REDUCTION_VARIANCE:
                model.setVarianceThreshold(getParameterAsDouble(PARAMETER_VARIANCE_THRESHOLD));
                break;
            case REDUCTION_FIXED:
                model.setNumberOfComponents(getParameterAsInt(PARAMETER_NUMBER_OF_COMPONENTS));
                break;
        }

		return new IOObject[] { exampleSet, model };
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class, Model.class };
	}

	private double getCovarianz(int dim1, int dim2) {
		double covariance = 0.0d;
		for (int sample = 0; sample < data.length; sample++) {
			covariance += data[sample][dim1] * data[sample][dim2];
		}
		covariance = covariance / (data.length - 1);
		return covariance;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> list = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_VARIANCE_THRESHOLD, "Keep the all components with a cumulative variance smaller than the given threshold.", 0, 1, 0.95);
        type.setExpert(false);
		list.add(type);
		type = new ParameterTypeCategory(PARAMETER_REDUCTION_TYPE, "Indicates which type of dimensionality reduction should be applied", REDUCTION_METHODS, REDUCTION_VARIANCE);
		list.add(type);
		type = new ParameterTypeInt(PARAMETER_NUMBER_OF_COMPONENTS, "Keep this number of components. If \'-1\' then keep all components.'", -1, Integer.MAX_VALUE, -1);
		list.add(type);
		return list;
	}
}
