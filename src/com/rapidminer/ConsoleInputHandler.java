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
package com.rapidminer;

import java.io.IOException;

/**
 * Reads input from console, e.g. a password in case of database reading.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ConsoleInputHandler.java,v 2.9 2006/03/21 15:35:36 ingomierswa
 *          Exp $
 */
public class ConsoleInputHandler implements InputHandler {

	public String inputPassword(String messageText) {
		try {
			System.out.println(messageText);
			StringBuffer password = new StringBuffer();
			while (true) {
				char character = (char) System.in.read();
				if ((character == 0x0a) || (character == 0x0d))
					return password.toString();
				password.append(character);
			}
		} catch (IOException e) {
			return null;
		}
	}
}
