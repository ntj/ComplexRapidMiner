package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.AbstractMatrixValue;
import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

public class MatrixAttribute extends ComplexProxyAttribute{

	private static final long serialVersionUID = 493080292487472506L;

	public MatrixAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	@Override
	public AbstractMatrixValue getComplexValue(DataRow row) {
		//all matrices are instantiated with one relational attribute, which can have different number of inner attributes,
		//but checking in complexArffChecker
		double[][]  values = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		AbstractMatrixValue m = (AbstractMatrixValue)ComplexValueFactory.getComplexValueFunction(getValueType(),this.hint);
		if(m != null)
			m.setValues(values);
		return m;
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	public int getParameterCount() {
		return 0;
	}

	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		throw new UnsupportedOperationException();
	}
}
