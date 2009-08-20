package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

public class UniformAttribute extends UncertainAttribute {

	public UniformAttribute(String name, int valueType,
			List<Attribute> innerAttributes, List<Attribute> parameters,
			String hint) {
		super(name, valueType, innerAttributes, parameters, hint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7884722287410317438L;

	@Override
	public SimpleProbabilityDensityFunction getComplexValue(DataRow row) {
		SimpleProbabilityDensityFunction pdf = (SimpleProbabilityDensityFunction)ComplexValueFactory.getComplexValueFunction(getValueType(),hint);
		pdf.setUncertainty(parameters.get(0).getValue(row));
		setValues(pdf,row);
		return pdf;
	}

}
