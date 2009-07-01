package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;


public class LinearKorrelation implements ComplexValue{
	
	double[] values;
	public int getValueType() {
		return Ontology.COMPLEX_VALUE;
	}
	
	public double getDoubleValue() {
		return 0;
	}
	
	
	public void setValues(double[] values){
		//Aufpassen objektreferenz evtl?
		this.values = values;
	}

}
