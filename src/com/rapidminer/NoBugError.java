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
package com.rapidminer;

/**
 * All exceptions that are no bugs (but caused by an error of the user) should
 * implement this interface in order to indicate that this should <i>not</i>
 * issue a bug report.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: NoBugError.java,v 1.1 2007/05/27 22:02:43 ingomierswa Exp $
 */
public interface NoBugError {

	/** Returns a html message. */
	public String getHTMLMessage();

	/** Returns the error details/description. */
	public String getDetails();

	/** Returns the error name. */
	public String getErrorName();

	/** Returns the error code. */
	public int getCode();

}
