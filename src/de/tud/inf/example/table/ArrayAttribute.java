package de.tud.inf.example.table;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ArrayValue;
import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

/**
 * 
 * manages (2)-dimensional arrays where inner and outer dimensions can change for each example
 * @author Antje Gruner
 *
 */
public class ArrayAttribute extends ComplexProxyAttribute{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3660839071790466716L;
	
	private Attribute dim1;
	private Attribute dim2;
	
	public ArrayAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint, Attribute dim1, Attribute dim2) {
		super(name, valueType, innerAttribute, hint);
		this.dim1 = dim1;
		this.dim2 = dim2;
	}

	
	@Override
	public ComplexValue getComplexValue(DataRow row) {
		ArrayValue mv = (ArrayValue)ComplexValueFactory.getComplexValueFunction(1, getValueType(), this.getHint());
		double[][]  values = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		int x = (int)row.get(dim1);
		int y = (int)row.get(dim2);

		mv.setValues( x,y,values);
		return null;
	}

	@Override
	public int getParameterCount() {
		return 2;
	}


	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		// TODO Auto-generated method stub
		
	}

}
