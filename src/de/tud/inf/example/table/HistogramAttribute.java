package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.Histogram;

public class HistogramAttribute extends UncertainAttribute{

	private static final long serialVersionUID = 827552188427781393L;
	
	public HistogramAttribute(HistogramAttribute a){
		super(a);
	}
	public HistogramAttribute(String name, int valueType,
			List<Attribute> innerAttributes, List<Attribute> parameters,
			String hint) {
		super(name, valueType, innerAttributes, parameters, hint);
	}

	
	@Override
	public Histogram getComplexValue(DataRow row) {
		Histogram h = (Histogram)ComplexValueFactory.getComplexValueFunction(innerAttributes.size(), this.getValueType(),hint);
		
		h.setMinMax(row.getRelativeValuesFor(this.parameters.get(0).getTableIndex()));
		h.setProbabilityValues(row.getRelativeValuesFor(this.parameters.get(0).getTableIndex()));
		setValues(h,row);
		return h;
	}
	
	
	@Override
	public HistogramAttribute clone() {
		return new HistogramAttribute(this);
	}
	
	

}
