package com.rapidminer.operator.uncertain;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;

public class PDFSampler extends Operator{

	private static final String SAMPLE_STRATEGY = "Sampling Frequency";
	public static final int MONTE_CARLO = 1;
	public static final int SIMPLE = 0;
	public static final int PDF = 2;
	public static final int INVERTED_PDF = 3;
	private static final String[] SAMPLING_METHODS = new String[]{"Simple","Monte Carlo","PDF","Inverted PDF"};

	
	public PDFSampler(OperatorDescription description) {
		super(description);
		
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		ExampleSet es = getInput(ExampleSet.class);
		SampleStrategy st = getSamplingStrategy();
		
		for(Example e : es){
			st.setElement(getValues(e));
			Double[][] newExamples = st.getSamples();
			
			Attribute[] attributeArray = es.getExampleTable().getAttributes();
			DataRow dr = es.getExampleTable().getDataRow(0);
			int dataManagement = 0;
			if (dr instanceof DoubleArrayDataRow) {
				dataManagement = DataRowFactory.TYPE_DOUBLE_ARRAY;
			}
			
			DataRowFactory dataRowFactory = new DataRowFactory(dataManagement);
			for(int i = 0;i< newExamples.length ; i++){
				
				DataRow dataRow = dataRowFactory.create(newExamples[i], attributeArray);
				
				((MemoryExampleTable)es.getExampleTable()).addDataRow(dataRow);
			}
		}
		
		return new IOObject[]{es};
		
	}
	
	private SampleStrategy getSamplingStrategy(){
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
		ParameterType type = new ParameterTypeCategory(SAMPLE_STRATEGY, "Specifies the sampling frequency",SAMPLING_METHODS,1);
		type.setExpert(false);
		return types;
	}
}
