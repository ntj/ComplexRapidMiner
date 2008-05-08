package com.rapidminer.operator.uncertain;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.similarity.attributebased.uncertain.AbstractProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeFile;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.UndefinedParameterError;

public class PDFSampler extends Operator{

	private static final String SAMPLE_STRATEGY = "Sampling Strategy";
	public static final int MONTE_CARLO = 1;
	public static final int SIMPLE = 0;
	public static final int PDF = 2;
	public static final int INVERTED_PDF = 3;
	private static final String[] SAMPLING_METHODS = new String[]{"Simple","Monte Carlo","PDF","Inverted PDF"};
	private static final String SAMPLE_FREQUENCY = "Sampling Frequncy";
	private static final String GLOBAL_UNCERTAINTY = "Global Uncertainty";
	private static final String ADD_ORIGINAL_POINT = "Add original measurement";
	private static final String ABSOLUTE_ERROR = "Absolute error";

	
	public PDFSampler(OperatorDescription description) {
		super(description);
		
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		SimpleExampleSet es = (SimpleExampleSet)getInput(ExampleSet.class);
		List<Attribute> listAtt = new LinkedList<Attribute>();
		//create list 
		Iterator<Attribute> iAt = es.getAttributes().iterator();
		while(iAt.hasNext()){
			Attribute a = iAt.next();
			listAtt.add(a);
		}
		
		
		MemoryExampleTable newMT = new MemoryExampleTable(listAtt);
		AbstractSampleStrategy st = getSamplingStrategy();
		//copy to data to a new instance of the example set
		st.setSampleRate(getParameterAsInt(SAMPLE_FREQUENCY));
		
		
		
		for(Example e : es){
			st.setElement(getValues(e));
			Double[][] newExamples = st.getSamples();
			
			if(newExamples.length>0){
				Attribute[] attributeArray = es.getExampleTable().getAttributes();
				DataRow dr = es.getExampleTable().getDataRow(0);
				int dataManagement = 0;
				if (dr instanceof DoubleArrayDataRow) {
					dataManagement = DataRowFactory.TYPE_DOUBLE_ARRAY;
				}
		
				DataRowFactory dataRowFactory = new DataRowFactory(dataManagement);
				for(int i = 0;i< newExamples[0].length ; i++){
					DataRow dataRow = dataRowFactory.create(newExamples[i], attributeArray);
					if(getParameterAsBoolean(ADD_ORIGINAL_POINT)){
						newMT.addDataRow(dataRow);
					}
					
				}
				newMT.addDataRow(dr);
			}
		}
		return new IOObject[]{new SimpleExampleSet(newMT)};
		
	}
	
	private AbstractSampleStrategy getSamplingStrategy(){
		try {
			AbstractSampleStrategy st = null;
			switch(getParameterAsInt(SAMPLE_STRATEGY)){
			case MONTE_CARLO:{st =  new MonteCarloSampling(); break;}
			case SIMPLE:{st =  new SimpleSampling();break;}
			default:
			}
			AbstractProbabilityDensityFunction pdf = new SimpleProbabilityDensityFunction(getParameterAsInt(GLOBAL_UNCERTAINTY),getParameterAsBoolean(ABSOLUTE_ERROR));
			st.setPdf(pdf);
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
		ParameterType type = new ParameterTypeCategory(SAMPLE_STRATEGY, "Specifies the sampling method",SAMPLING_METHODS,1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(SAMPLE_FREQUENCY, "Specifies the sampling frequency",0,10000000,1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeInt(GLOBAL_UNCERTAINTY, "The uncertainty specification around the points",0,10000000,1);
		type.setExpert(false);
		types.add(type);
		type = new ParameterTypeBoolean(ADD_ORIGINAL_POINT, "Add the original Measurement in the sampled set",true);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeBoolean(ABSOLUTE_ERROR, "Specifies if the error is an absolute error",true);
		type.setExpert(false);
		types.add(type);
		
		return types;
	}
}
