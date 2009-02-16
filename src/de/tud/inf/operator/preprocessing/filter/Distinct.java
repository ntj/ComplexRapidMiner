package de.tud.inf.operator.preprocessing.filter;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;

import de.tud.inf.example.set.DistinctExampleSet;

public class Distinct extends Operator{

	public Distinct(OperatorDescription description) {
		super(description);
		
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet inputSet = this.getInput(ExampleSet.class);
		
		// create output ExampleSet
		DistinctExampleSet outputSet = new DistinctExampleSet(inputSet);
		
		return new IOObject[] {outputSet};
	}

	@Override
	public Class<?>[] getInputClasses() {
		
		return new Class[] {ExampleSet.class};
	}

	@Override
	public Class<?>[] getOutputClasses() {

		return new Class[] {ExampleSet.class};
	}

	
}
