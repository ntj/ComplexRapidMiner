package com.rapidminer.operator.uncertain;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;

public abstract class AbstractPDFSampler extends Operator {

	protected static final String SAMPLE_STRATEGY = "Sampling Strategy";
	protected static final int MONTE_CARLO = 1;
	protected static final int SIMPLE = 0;
	protected static final int PDF = 2;
	protected static final int INVERTED_PDF = 3;
	protected static final String[] SAMPLING_METHODS = new String[] { "Simple",
			"Monte Carlo", "PDF", "Inverted PDF" };
	protected static final String[] SAMPLE_DISTRIBUTION = new String[] {
			"One ExampleSet", "Round Robin Distribution", "Random Distribution" };
	protected static final String SAMPLE_FREQUENCY = "Sampling Frequncy";
	protected static final String GLOBAL_UNCERTAINTY = "Global Uncertainty";
	protected static final String ADD_ORIGINAL_POINT = "Add original measurement";
	protected static final String ABSOLUTE_ERROR = "Absolute error";
	protected static final String SPLIT_TO_NEW_EXAMPLE_SETS = "Splitt";
	protected static final String NUM_THREADS = "Num Concurrent Threads for sampling";
	private MemoryExampleTable newMT;
	private AbstractSampleStrategy st;
	private MemoryExampleTable[] resultMT;

	public AbstractPDFSampler(OperatorDescription description) {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		SimpleExampleSet es = (SimpleExampleSet) getInput(ExampleSet.class);
		List<Attribute> listAtt = new LinkedList<Attribute>();
		// create list
		Iterator<Attribute> iAt = es.getAttributes().iterator();
		while (iAt.hasNext()) {
			Attribute a = iAt.next();
			listAtt.add(a);
		}

		newMT = new MemoryExampleTable(listAtt);
		st = getSamplingStrategy();
		// copy to data to a new instance of the example set
		st.setSampleRate(getParameterAsInt(SAMPLE_FREQUENCY));

		resultMT = new MemoryExampleTable[getParameterAsInt(SAMPLE_FREQUENCY)];

		for (int i = 0; i < getParameterAsInt(SAMPLE_FREQUENCY); i++) {
			resultMT[i] = new MemoryExampleTable(listAtt);
		}
		getValues();
		LinkedList<SamplingThread> ll = new LinkedList<SamplingThread>();
		int size = es.size();
		int sizePerThread = size / getParameterAsInt(NUM_THREADS);
		for (int i = 1; i <= getParameterAsInt(NUM_THREADS); i++) {
			// create the threads
			if (i == getParameterAsInt(NUM_THREADS)) {
				ll.add(new SamplingThread(sizePerThread * (i - 1), size, es,
						this, st));
			} else {
				ll.add(new SamplingThread(sizePerThread * (i - 1),
						sizePerThread * (i), es, this, st));
			}
		}
		System.err.println("Starting Sampling threads started");
		for (SamplingThread ct : ll) {
			ct.start();
		}
		System.err.println("All Sampling threads started");
		for (SamplingThread ct : ll) {
			try {
				ct.join();
			} catch (InterruptedException e) {

				e.printStackTrace();
			}
		}
		System.err.println("All threads for sampling joined.");
		IOObject[] result = new IOObject[getParameterAsInt(SAMPLE_FREQUENCY)];
		for (int i = 0; i < getParameterAsInt(SAMPLE_FREQUENCY); i++) {
			result[i] = new SimpleExampleSet(resultMT[i]);
		}

		return result;
	}

	protected abstract AbstractProbabilityDensityFunction getPDF()
			throws UndefinedParameterError;

	private AbstractSampleStrategy getSamplingStrategy() {
		try {
			AbstractSampleStrategy st = null;
			switch (getParameterAsInt(SAMPLE_STRATEGY)) {
			case MONTE_CARLO: {
				st = new MonteCarloSampling();
				break;
			}
			case SIMPLE: {
				st = new SimpleSampling();
				break;
			}
			default:
			}

			st.setPdf(this.getPDF());
			return st;
		} catch (UndefinedParameterError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class[] getInputClasses() {
		Class[] input = new Class[1];
		input[0] = ExampleSet.class;
		return input;
	}

	@Override
	public Class[] getOutputClasses() {
		Class[] output = new Class[1];
		output[0] = ExampleSet.class;
		return output;
	}

	private double[] getValues(Example e) {
		if (e == null)
			return null;
		double[] values = new double[e.getAttributes().size()];
		int index = 0;
		for (Attribute attribute : e.getAttributes())
			values[index++] = e.getValue(attribute);
		return values;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(SAMPLE_STRATEGY,
				"Specifies the sampling method", SAMPLING_METHODS, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(SAMPLE_FREQUENCY,
				"Specifies the sampling frequency", 0, 10000000, 1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(NUM_THREADS,
				"Specifies the number of concurrent threadsfor sampling", 1, 4,
				1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeDouble(GLOBAL_UNCERTAINTY,
				"The uncertainty specification around the points", 0, 10000000,
				1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(ADD_ORIGINAL_POINT,
				"Add the original Measurement in the sampled set", true);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeBoolean(SPLIT_TO_NEW_EXAMPLE_SETS,
				"Specifies if the ", true);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeBoolean(ABSOLUTE_ERROR,
				"Specifies if the error is an absolute error", true);
		type.setExpert(false);
		types.add(type);

		type = new ParameterTypeCategory("Sample Distribution", "",
				SAMPLE_DISTRIBUTION, 2);
		type.setExpert(false);
		types.add(type);

		return types;
	}

	public void addDataRow(DataRow dataRow, int i) {
		if (getParameterAsBoolean(SPLIT_TO_NEW_EXAMPLE_SETS)) {
			resultMT[i].addDataRow(dataRow);
		} else {
			newMT.addDataRow(dataRow);
		}

	}

	public void addOriginalPoint(DataRow dr) {
		if (getParameterAsBoolean(ADD_ORIGINAL_POINT)) {
			newMT.addDataRow(dr);
		}
	}

	public double[] getValuesFromExample(Example e) {
		return getValues(e);
	}
}
