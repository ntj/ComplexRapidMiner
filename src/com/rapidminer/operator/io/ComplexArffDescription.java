package com.rapidminer.operator.io;

/**
 * this class contains all key names of the complexArff - file specification
 * @author Antje Gruner
 *
 */
public class ComplexArffDescription {
	
	/**
	 * the name of the complex attributes
	 */
	public static final String depAttName = "name";
	/**
	 * the name of the relational attributes which stores information about inner attributes 
	 */
	public static final String depInnerAttributesName = "attributes";
	/**
	 * the name of the relational attributes which stores information about parameter attributes
	 */
	public static final String depParamName = "parameters";
	/**
	 * the name of the symbol attribute
	 */
	public static final String depClassName = "className";
	public static final String depHintName = "hints";
	public static final String depAnnotation = "@DESCRIPTION";

}
