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
package com.rapidminer.gui.plotter;

/**
 *  Collects all necessary information about tool tips for plotters.
 *
 *  @author Ingo Mierswa
 *  @version $Id: ToolTip.java,v 1.1 2007/05/27 21:59:04 ingomierswa Exp $
 */
public class ToolTip {

	private String text;

	private int xPos;

	private int yPos;

	public ToolTip(String toolTip, int xPos, int yPos) {
		this.text = toolTip;
		this.xPos = xPos;
		this.yPos = yPos;
	}
	
	public String getText() { return text; }
	
	public int getX() { return xPos; }
	
	public int getY() { return yPos; }
}
