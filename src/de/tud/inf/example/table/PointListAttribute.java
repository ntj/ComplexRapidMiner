package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.PointListValue;

/**
 * manages objects, which contain sets of 3-dimensional points
 * @author Antje Gruner
 *
 */
public class PointListAttribute extends ComplexProxyAttribute{

	public PointListAttribute(PointListAttribute a){
		super(a);
	}
	
	public PointListAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4789324727532173295L;

	@Override
	public ComplexValue getComplexValue(DataRow row) {
		PointListValue listValue =  (PointListValue) ComplexValueFactory.getComplexValueFunction(getValueType(),getHint());
		listValue.setValues(row.getRelativeValuesFor(this.getInnerAttribute().getTableIndex()));
		return listValue;
	}

	@Override
	public int getParameterCount() {
		return 0;
	}

	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object clone() {
		return new PointListAttribute(this);
	}
	
	

}
