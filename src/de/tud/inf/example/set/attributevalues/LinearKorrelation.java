package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.Tools;


public class LinearKorrelation implements ComplexValue{
	
	double[] values;
	public int getValueType() {
		return Ontology.COMPLEX_VALUE;
	}
	
	public double getDoubleValue() {
		return 0;
	}
	
	
	public void setValues(double[] values){
		this.values = values;
	}

	public double[] getValues(){
		return values;	
	}
	
	
	public String getStringRepresentation() {
		String s =  Double.toString(values[0]);
		for (int i =0;i<values.length;i++)
			s += ", " + Double.toString(values[i]);			
		return s;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		String s =  Tools.formatIntegerIfPossible(values[0], digits);
		for (int i =1;i<values.length;i++)
			s += ", " +  Tools.formatIntegerIfPossible(values[i], digits);
		return s;
	}

}
