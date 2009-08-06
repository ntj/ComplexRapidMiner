package de.tud.inf.operator.clustering.similarity;

import java.util.List;
import java.util.TreeSet;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.similarity.SimilarityMeasure;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.math.Averagable;

public class KDistanceEvaluator extends Operator{

	final String K = "K";
	
	public KDistanceEvaluator(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		SimilarityMeasure sim = getInput(SimilarityMeasure.class);
		TreeSet ts = new TreeSet();
	
		
		return new IOObject[]{new KDistance(0)};
	}

	@Override
	public Class<?>[] getInputClasses() {
		
		return new Class[]{SimilarityMeasure.class};
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[]{Averagable.class};
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		
		ParameterTypeInt k = new ParameterTypeInt(
				K, 
				"maximum number of members in the ensemble", 
				1, 
				Integer.MAX_VALUE, 
				5);
		k.setExpert(false);
		types.add(k);
		
		return types;
	}

}
