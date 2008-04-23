/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.parameter;

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
}
