package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.Histogram;

public class HistogramAttribute extends UncertainAttribute{

	public HistogramAttribute(String name, int valueType,
			List<Attribute> innerAttributes, List<Attribute> parameters,
			String symbol, String hint) {
		super(name, valueType, innerAttributes, parameters, symbol, hint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 827552188427781393L;

	@Override
	public Histogram getComplexValue(DataRow row) {
		Histogram h = (Histogram)ComplexValueFactory.getComplexValueFunction(innerAttributes.size(), symbol,hint);
		
		h.setMinMax(row.getRelativeValuesFor(this.parameters.get(0).getTableIndex()));
		h.setProbabilityValues(row.getRelativeValuesFor(this.parameters.get(0).getTableIndex()));
		setValues(h,row);
		return h;
	}

}
