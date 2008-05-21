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

import java.util.List;

import Jama.EigenvalueDecomposition;
import Jama.Matrix;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
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
import com.rapidminer.tools.math.matrix.CovarianceMatrix;

/**
 * This operator performs a principal components analysis (PCA) using the
 * covariance matrix. The user can specify the amount of variance to cover in
 * the original data when retaining the best number of principal components. The
 * user can also specify manually the number of principal components. The
 * operator outputs a <code>PCAModel</code>. With the
 * <code>ModelApplier</code> you can transform the features.
 * 
 * @author Ingo Mierswa
 * @version $Id: PCA.java,v 1.8 2008/05/09 19:22:51 ingomierswa Exp $
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
    

	public PCA(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		
		// 1) check whether all attributes are numerical
		ExampleSet exampleSet = getInput(ExampleSet.class);
		exampleSet.recalculateAllAttributeStatistics();

		for (Attribute attribute : exampleSet.getAttributes()) {
			if (attribute.isNominal()) {
				throw new UserError(this, 104, "PCA", attribute.getName());
			}
		}
		
		// 2) create covariance matrix
		log("Creating the covariance matrix...");
		Matrix covarianceMatrix = CovarianceMatrix.getCovarianceMatrix(exampleSet);
		
		// 3) EigenVector and EigenValues of the covariance matrix
		log("Performing the eigenvalue decomposition...");
		EigenvalueDecomposition eigenvalueDecomposition = covarianceMatrix.eig();

		// 4) create and deliver results
		double[] eigenvalues = eigenvalueDecomposition.getRealEigenvalues();
		Matrix eigenvectorMatrix = eigenvalueDecomposition.getV();
		double[][] eigenvectors = eigenvectorMatrix.getArray();

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
