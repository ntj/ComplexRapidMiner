package com.rapidminer.operator.uncertain;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;

class SamplingThread extends Thread {
	private int from;
	private int to;
	private ExampleSet es;
	private AbstractPDFSampler pdfSampler;
	private AbstractSampleStrategy st;

	public SamplingThread(int from, int to, ExampleSet es,
			AbstractPDFSampler pdfSampler, AbstractSampleStrategy st) {
		this.from = from;
		this.to = to;
		this.es = es;
		this.st = st;
		this.pdfSampler = pdfSampler;
	}

	@Override
	public void run() {
		System.err.println("Sampling thread Started: " + this.from + "-"
				+ this.to);
		DataRow dr = es.getExampleTable().getDataRow(0);
		int dataManagement = 0;
		if (dr instanceof DoubleArrayDataRow) {
			dataManagement = DataRowFactory.TYPE_DOUBLE_ARRAY;
		}
		Attribute[] attributeArray = es.getExampleTable().getAttributes();
		DataRowFactory dataRowFactory = new DataRowFactory(
				dataManagement, '.');
		for (int j = this.from; j < this.to; j++) {
			Example e = es.getExample(j);
			st.setValue(pdfSampler.getValuesFromExample(e));
			Double[][] newExamples = st.getSamples();
			if (newExamples.length > 0) {
				System.err.println("Adding point "+j);
				for (int i = 0; i < newExamples.length; i++) {
					DataRow dataRow = dataRowFactory.create(newExamples[i],
							attributeArray);
					pdfSampler.addDataRow(dataRow, i);
				}
				pdfSampler.addOriginalPoint(dr);
			}else{
				System.err.println("point wo new samplepoints " + j);
			}
	

		}
	}
}