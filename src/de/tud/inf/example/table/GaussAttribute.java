package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.operator.similarity.attributebased.uncertain.GaussProbabilityDensityFunction;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

/**
 * 
 * @author Antje Gruner
 *
 */
public class GaussAttribute extends UncertainAttribute {

	private static final long serialVersionUID = -8515028651795348008L;
	
	public GaussAttribute(GaussAttribute a){
		super(a);
	}
	
	public GaussAttribute(String name, int valueType,
			List<Attribute> innerAttributes, List<Attribute> parameters,
			String hint) {
		super(name, valueType, innerAttributes, parameters, hint);
	}

	@Override
	public GaussProbabilityDensityFunction getComplexValue(DataRow row){
		GaussProbabilityDensityFunction pdf = (GaussProbabilityDensityFunction)ComplexValueFactory.getComplexValueFunction(innerAttributes.size(), this.getValueType(),hint);
		pdf.setCovarianceMatrix(row.getRelativeValuesFor(this.parameters.get(0).getTableIndex()));
		setValues(pdf,row);
		return pdf;
	}
	

	@Override
	public Object clone() {
		return new GaussAttribute(this);
	}
	
	


}
