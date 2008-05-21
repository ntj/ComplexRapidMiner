package com.rapidminer.operator.uncertain;

import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.parameter.UndefinedParameterError;

public class PDFSampler extends AbstractPDFSampler {

	public PDFSampler(OperatorDescription description) {
		super(description);

	}

	@Override
	protected AbstractProbabilityDensityFunction getPDF()
			throws UndefinedParameterError {

		return new SimpleProbabilityDensityFunction(
				getParameterAsDouble(GLOBAL_UNCERTAINTY),
				getParameterAsBoolean(ABSOLUTE_ERROR));
	}
}
