package de.tud.inf.example.set.attributevalues;


/**
 * should be implemented by those classes, which describe the correlation between inner attributes of a complex attribute (e.g. pdfs)
 *  * @author Antje Gruner
 *
 */
public interface ComplexValue {

	/** Indicates the default number of fraction digits which is defined by the system
	 *  property rapidminer.gui.fractiondigits.numbers. */
	public static final int DEFAULT_NUMBER_OF_DIGITS = -1;
	
	/** Indicates an unlimited number of fraction digits. */
	public static final int UNLIMITED_NUMBER_OF_DIGITS = -2;
	/**
	 * 
	 * @return the valueType of the complex attribute
	 */
	public int getValueType();
	public double getDoubleValue();
	
	public String getStringRepresentation(int digits, boolean quoteWhitespace);
	
}
