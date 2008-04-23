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
package com.rapidminer.gui.tools;

/** This enumeration hold the information about all available icon sizes (mainly used for 
 *  actions). Currently, only icon sizes 24 and 32 are supported. 32 should be used for 
 *  tool bars and 24 in menus. 
 *  
 *  @author Ingo Mierswa
 *  @version $Id: IconSize.java,v 1.1 2007/05/27 21:59:31 ingomierswa Exp $
 */
public enum IconSize {

	SMALL(24), MIDDLE(32);
	
	private int size;
	
	private IconSize(int size) {
		this.size = size;
	}
	
	public int getSize() { 
		return size; 
	}
}
