package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.TensorValue;

/**
 * manages complex tensor values
 * @author Antje Gruner
 *
 */
public class TensorAttribute extends ComplexProxyAttribute{

	private static final long serialVersionUID = 773973406424862179L;
	
	public TensorAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	public TensorAttribute(TensorAttribute a){
		super(a);
	}
	
	@Override
	public TensorValue getComplexValue(DataRow row) {
		//all matrices are instantiated with one relational attribute, which can have different number of inner attributes,
		//but checking in complexArffChecker
		double[][]  values    = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		TensorValue t = (TensorValue)ComplexValueFactory.getComplexValueFunction(getValueType(),this.hint);
		if(t != null)
			t.setValues(values);
		return t;
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
	public TensorAttribute clone() {
		return new TensorAttribute(this);
	}

}
