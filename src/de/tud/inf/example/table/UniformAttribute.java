package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.similarity.attributebased.uncertain.SimpleProbabilityDensityFunction;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

public class UniformAttribute extends UncertainAttribute {

	public UniformAttribute(String name, int valueType,
			List<Attribute> innerAttributes, List<Attribute> parameters,
			String symbol, String hint) {
		super(name, valueType, innerAttributes, parameters, symbol, hint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 7884722287410317438L;

	@Override
	public SimpleProbabilityDensityFunction getComplexValue(DataRow row) {
		SimpleProbabilityDensityFunction pdf = (SimpleProbabilityDensityFunction)ComplexValueFactory.getComplexValueFunction(symbol,hint);
		pdf.setUncertainty(parameters.get(0).getValue(row));
		setValues(pdf,row);
		return pdf;
	}

	@Override
	public String checkConstraints(ExampleTable et,
			ComplexAttributeDescription cad) {
		String messg = super.checkConstraints(et, cad);
		if (cad.getParamIndexes().length !=1)
			messg += "uncertain value with uniform pdf expects exactly one parameter (uncertainty)";
		//TODO: check if parameter attribute is NUMERIC
		return messg;
	}
	



}
