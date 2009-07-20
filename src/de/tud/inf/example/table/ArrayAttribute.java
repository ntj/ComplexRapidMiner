package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValue;

/**
 * manages 2-dimensional arrays where dimensions can be changed for each example
 * @author Antje Gruner
 *
 */
public class ArrayAttribute extends ComplexProxyAttribute{

	ArrayAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String symbol, String hint) {
		super(name, valueType, innerAttribute, symbol, hint);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -3660839071790466716L;

	@Override
	public ComplexValue getComplexValue(DataRow row) {
		
		return null;
	}

	@Override
	public int getParameterCount() {
		return 2;
	}

}
