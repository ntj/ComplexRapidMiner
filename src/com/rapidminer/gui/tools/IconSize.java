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
package com.rapidminer.gui.tools;

/** This enumeration hold the information about all available icon sizes (mainly used for 
 *  actions). Currently, only icon sizes 24 and 32 are supported. 32 should be used for 
 *  tool bars and 24 in menus. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: IconSize.java,v 1.4 2008/05/09 19:22:58 ingomierswa Exp $
 */
public enum IconSize {

	SMALL(24), MIDDLE(32), LARGE(48);
	
	private int size;
	
	private IconSize(int size) {
		this.size = size;
	}
	
	public int getSize() { 
		return size; 
	}
}
