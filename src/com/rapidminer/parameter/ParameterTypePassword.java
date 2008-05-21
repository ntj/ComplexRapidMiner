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
package com.rapidminer.parameter;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.cipher.CipherException;
import com.rapidminer.tools.cipher.CipherTools;

/**
 * A parameter for passwords. The parameter is written with asteriks in the GUI
 * but can be read in process configuration file. Please make sure that noone
 * but the user can read the password from such a file.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ParameterTypePassword.java,v 2.8 2006/03/21 15:35:49
 *          ingomierswa Exp $
 */
public class ParameterTypePassword extends ParameterTypeString {

	private static final long serialVersionUID = 384977559199162363L;

	public ParameterTypePassword(String key, String description) {
		super(key, description, true);
	}

	public String getRange() {
		return "password";
	}
	
	/** This method will be invoked by the Parameters after a parameter was set
	 *  and will decrypt the given value. */
	public Object transformNewValue(Object value) {
		return decryptPassword(value.toString());
	}
	
	private String decryptPassword(String value) {
		if (CipherTools.isKeyAvailable()) {
			try {
				return CipherTools.decrypt(value);
			} catch (CipherException e) {
				LogService.getGlobal().logError("Cannot encrypt password, using non-encrypted password in XML!");
			}
		}
		return value;
	}
	
	public String getXML(String indent, String key, Object value, boolean hideDefault) {
		if ((value != null) && ((!hideDefault) || (!value.equals(getDefaultValue())))) {
			String encrypted = value.toString();
			if (CipherTools.isKeyAvailable()) {
				try {
					encrypted = CipherTools.encrypt(encrypted);
				} catch (CipherException e) {
					LogService.getGlobal().logError("Cannot encrypt password, using non-encrypted password in XML!");
				}
			}
			return (indent + "<parameter key=\"" + key + "\"\tvalue=\"" + toString(encrypted) + "\"/>" + Tools.getLineSeparator());
		} else
			return "";
	}
}
