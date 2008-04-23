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
package com.rapidminer.gui.dialog.boxviewer;

import java.awt.Dimension;
import java.awt.Paint;

import com.rapidminer.gui.tools.SwingTools;


/**
 * An operator renderer for wrapper, i.e. all non-simple operator chains. This
 * renderer is used in the box view of an process definition.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: WrapperRenderer.java,v 1.2 2007/06/07 17:12:20 ingomierswa Exp $
 */
public class WrapperRenderer extends ChainRenderer {

	public Paint getBackground(Dimension d) {
		return SwingTools.makeYellowPaint(d.getWidth(), d.getHeight());
	}
}
