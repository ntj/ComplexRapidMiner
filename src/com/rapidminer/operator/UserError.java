/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator;

import java.net.URL;
import java.text.MessageFormat;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

import com.rapidminer.NoBugError;
import com.rapidminer.tools.Tools;


/**
 * Exception class whose instances are thrown due to a user error, for example
 * missing files or wrong operator architecture. <br>
 * In order to create a UserError, do the following:
 * <ul>
 * <li>Open the file <code>UserErrorMessages.properties</code> in the
 * <code>resources</code> directory. Look for an appropriate messsage. If you
 * find one, remember its id number. If not, create a new one in the correct
 * group</li>
 * <li>The entry must include name, short message and long message. The name
 * and long message will be presented to the user litarally. The short message
 * will be parsed by <code>java.text.MessageFormat</code>. Especially, any
 * ocurrence of curly brackets will be replaced. Be careful with quotes; it
 * might be a good idea to read the ducumentation of MessageFormat first.</li>
 * <li>Create a UserError by using this id. If the UserError is created because
 * of another exception, e.g. a FileNotFoundException, this exception should be
 * passed to the UserError in the constructor.</li>
 * </ul>
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: UserError.java,v 1.3 2008/05/09 19:23:18 ingomierswa Exp $
 */
public class UserError extends OperatorException implements NoBugError {

	private static final long serialVersionUID = -8441036860570180869L;

	private static ResourceBundle messages = null;

	private static final MessageFormat formatter = new MessageFormat("");

	static {
		try {
			URL url = Tools.getResource("UserErrorMessages.properties");
			if (url != null)
				messages = new PropertyResourceBundle(url.openStream());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}

	private int code;

	private transient Operator operator;

	/**
	 * Creates a new UserError.
	 * 
	 * @param operator
	 *            The {@link Operator} in which the exception occured.
	 * @param cause
	 *            The exception that caused the user error. May be null. Using
	 *            this makes debugging a lot easier.
	 * @param code
	 *            The error code referring to a message in the file
	 *            <code>UserErrorMessages.properties</code>
	 * @param arguments
	 *            Arguments for the short message.
	 */
	public UserError(Operator operator, Throwable cause, int code, Object[] arguments) {
		super(getErrorMessage(code, arguments), cause);
		this.code = code;
		this.operator = operator;
	}

	/** Convenience constructor for messages with no arguments and cause. */
	public UserError(Operator operator, Throwable cause, int code) {
		this(operator, code, new Object[0], cause);
	}

	public UserError(Operator operator, int code, Object[] arguments) {
		this(operator, null, code, arguments);
	}

	/** Convenience constructor for messages with no arguments. */
	public UserError(Operator operator, int code) {
		this(operator, null, code, new Object[0]);
	}

	/** Convenience constructor for messages with exactly one argument. */
	public UserError(Operator operator, int code, Object argument1) {
		this(operator, null, code, new Object[] { argument1 });
	}

	/**
	 * Convenience constructor for messages with exactly one arguments and
	 * cause.
	 */
	public UserError(Operator operator, Throwable cause, int code, Object argument1) {
		this(operator, cause, code, new Object[] { argument1 });
	}

	/** Convenience constructor for messages with exactly two arguments. */
	public UserError(Operator operator, int code, Object argument1, Object argument2) {
		this(operator, null, code, new Object[] { argument1, argument2 });
	}

	public String getDetails() {
		return getResourceString(code, "long", "Description missing.");
	}

	public String getErrorName() {
		return getResourceString(code, "name", "Unnamed error.");
	}

	public int getCode() {
		return code;
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}

	public static String getErrorMessage(int code, Object[] arguments) {
		String message = getResourceString(code, "short", "No message.");
		try {
			formatter.applyPattern(message);
			String formatted = formatter.format(arguments);
			return formatted;
		} catch (Throwable t) {
			return message;
		}
	}

	/**
	 * Returns a resource message for the given error code.
	 * 
	 * @param key
	 *            one out of &quot;name&quot;, &quot;short&quot;,
	 *            &quot;long&quot;
	 */
	public static String getResourceString(int code, String key, String deflt) {
		if (messages == null)
			return deflt;
		try {
			return messages.getString("error." + code + "." + key);
		} catch (java.util.MissingResourceException e) {
			return deflt;
		}
	}

	public String getHTMLMessage() {
		return "<html>Error in: <b>" + getOperator() + "</b><br>" + Tools.escapeXML(getMessage()) + "<hr>" + Tools.escapeXML(getDetails()) + "</html>";
	}
}
