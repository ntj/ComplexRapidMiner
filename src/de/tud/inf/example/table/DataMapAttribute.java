package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.DataMapValue;

/**
 * provides a simple Map<Key,Value> - map, data is read from an relational attribute with two inner attributes
 * since relational attributes cannot be nested, there are just atomar values possible
 * @author Antje Gruner
 *
 */
public class DataMapAttribute extends ComplexProxyAttribute{

	public DataMapAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 806365320039407341L;

	@Override
	public ComplexValue getComplexValue(DataRow row) {
		DataMapValue dMapVal = (DataMapValue)ComplexValueFactory.getComplexValueFunction(getValueType(),this.hint);
		double[][]  values = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		dMapVal.setValues(values);
		return dMapVal;
	}

	//TEST if set works
	public void setComplexValue(DataMapValue value, DataRow row){
		//this attribute must now exactly where to store which informaion in mapvalue and stores it in datarow
	}
	
	
	@Override
	public int getParameterCount() {
		return 0;
	}

}
