package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

public class ArrayValue implements ComplexValue{

	public double getDoubleValue() {
		// TODO Auto-generated method stub
		return 0;
	}

	public String getStringRepresentation(int digits, boolean quoteWhitespace) {
		return "NA";
	}

	public int getValueType() {
		return Ontology.ATTRIBUTE_VALUE_TYPE.MAP_SYNOPSIS;
	}
	

}
