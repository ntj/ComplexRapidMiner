package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AbstractAttribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.ComplexValue;

/**
 * base class for all non primitive attributes types
 * 
 * @author Antje Gruner
 * 
 */
public abstract class ComplexAttribute extends AbstractAttribute {
	
	private static final long serialVersionUID = -8546639471664353338L;
	protected final String hint;

	ComplexAttribute(ComplexAttribute attribute) {
		super(attribute);
		this.hint = attribute.hint;
	}

	ComplexAttribute(String name, int valueType, String hint) {
		super(name, valueType);
		this.hint = hint;
	}

	@Override
	public Object clone() {
		throw new UnsupportedOperationException();
	}

	public String getAsString(double value, int digits, boolean quoteWhitespace) {
		throw new UnsupportedOperationException();
	}

	public String getAsString(ComplexValue value, int digits,
			boolean quoteWhitespace) {
		return value.getStringRepresentation(digits, quoteWhitespace);
	}

	public NominalMapping getMapping() {
		throw new UnsupportedOperationException();
	}

	public boolean isComplex() {
		return true;
	}

	public boolean isNominal() {
		return false;
	}

	public boolean isNumerical() {
		return false;
	}

	public boolean isRelational() {
		return false;
	}

	public void setMapping(NominalMapping nominalMapping) {
		throw new UnsupportedOperationException();
	}

	public abstract ComplexValue getComplexValue(DataRow row);

	public abstract void setComplexValue(DataRow row, ComplexValue value);

	public abstract double[] getComplexValueAsArray(DataRow row);

	public abstract double getValue(DataRow row);

	public String getHint() {
		return hint;
	}

	public abstract int getParameterCount();

	public abstract int getInnerAttributeCount();

	public abstract List<Attribute> getInnerAttributes();

	public abstract List<Attribute> getParameterAttributes();

	public String getSymbol(){
		return Ontology.ATTRIBUTE_VALUE_TYPE.mapIndex(getValueType());
	}
	
}
