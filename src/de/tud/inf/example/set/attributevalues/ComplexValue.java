package de.tud.inf.example.set.attributevalues;


/**
 * should be implemented by those classes, which describe the correlation between inner attributes of a complex attribute (e.g. pdfs)
 *  * @author Antje Gruner
 *
 */
public interface ComplexValue {

	/**
	 * 
	 * @return the valueType of the complex attribute
	 */
	public int getValueType();
	public double getDoubleValue();

}
