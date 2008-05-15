package com.rapidminer.operator.uncertain;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;
import com.rapidminer.parameter.UndefinedParameterError;

public class PDFSamplerNominalDistribution extends AbstractPDFSampler{

	public PDFSamplerNominalDistribution(OperatorDescription description) {
		super(description);
		
	}

	@Override
	protected AbstractProbabilityDensityFunction getPDF()
			throws UndefinedParameterError {
		
		return null;
	}

}
