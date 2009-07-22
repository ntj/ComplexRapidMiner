package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.operator.similarity.attributebased.uncertain.ProbabilityDensityFunction;


public abstract class UncertainAttribute extends ComplexCompositeAttribute {


	private static final long serialVersionUID = -471778020353150372L;

	UncertainAttribute(UncertainAttribute attribute) {
		super(attribute);
	}

	public UncertainAttribute(String name, int valueType, List<Attribute> innerAttributes, List<Attribute> parameters,
			String hint) {
		super(name, valueType, innerAttributes, parameters, hint);
	}

	@Override
	public abstract ProbabilityDensityFunction getComplexValue(DataRow row);
	
	protected void setValues(ProbabilityDensityFunction pdf, DataRow row){
		double[] values = new double[innerAttributes.size()];
		for(int i = 0; i< innerAttributes.size();i++)
			values[i] = innerAttributes.get(i).getValue(row);
		pdf.setValue(values);
	}

}
