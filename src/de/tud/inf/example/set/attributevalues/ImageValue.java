package de.tud.inf.example.set.attributevalues;

import com.rapidminer.tools.Ontology;

public class ImageValue implements ComplexValue{

	public double getDoubleValue() {
		return 0;
	}

	public int getValueType() {
		return Ontology.FILE;
	}

}
