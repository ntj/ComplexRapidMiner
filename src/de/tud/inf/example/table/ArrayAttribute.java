package de.tud.inf.example.table;

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
	
	public ArrayAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
		// TODO Auto-generated constructor stub
	}

	
	@Override
	public ComplexValue getComplexValue(DataRow row) {
		ArrayValue mv = (ArrayValue)ComplexValueFactory.getComplexValueFunction(1, getValueType(), this.getHint());
		//TODO: mv.setValues()..
		return null;
	}

	@Override
	public int getParameterCount() {
		return 2;
	}

}
