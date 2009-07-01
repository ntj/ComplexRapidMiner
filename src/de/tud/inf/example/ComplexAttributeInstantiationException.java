package de.tud.inf.example;

/**
 * this Exception is thrown when semantic constraints, which are necessary when building complex attributes from
 * simple attributes are not fulfilled
 * @author Antje Gruner
 *
 */
public class ComplexAttributeInstantiationException extends RuntimeException{

	
	private static final long serialVersionUID = 675131367663617733L;

	public ComplexAttributeInstantiationException(String message) {
		super(message);
	}
}
