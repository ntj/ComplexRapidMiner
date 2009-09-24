package de.tud.inf.example.table;

import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.ConstantArrayValue;

/**
 * array attribute, where dimensions could be set only once for all array values
 * @author Antje Gruner
 *
 */
public class ConstantArrayAttribute extends ComplexProxyAttribute{

	public ConstantArrayAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}
	
	public ConstantArrayAttribute(String name, int valueType,String hint) {
		super(name,valueType,hint);
		innerAttribute = (RelationalAttribute)AttributeFactory.createAttribute(Ontology.RELATIONAL);
		innerAttribute.addInnerAttribute(AttributeFactory.createAttribute(Ontology.REAL));
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 8150227211379984011L;

	@Override
	public ComplexValue getComplexValue(DataRow row) {
		ConstantArrayValue aValue = (ConstantArrayValue)ComplexValueFactory.getComplexValueFunction(getValueType(), getHint());
		double[][] values = row.getRelativeValuesFor(innerAttribute.getTableIndex());
		aValue.setValues(values);
		return aValue;
	}

	@Override
	public int getParameterCount() {
		return 0;
	}

	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		double[][] values = ((ConstantArrayValue)value).getValues(); 
		double[][] relValues = new double[values.length * values[0].length][1];
		for (int i=0;i<values.length;i++)
			for (int j=0;j<values[0].length;j++)
				relValues[i*values[0].length + j][0] = values[i][j];
		row.setRelationalValues(this.innerAttribute.getTableIndex(),relValues);	
	}

}
