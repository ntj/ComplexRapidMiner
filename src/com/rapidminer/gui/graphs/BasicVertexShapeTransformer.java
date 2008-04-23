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
package com.rapidminer.gui.graphs;

import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import org.apache.commons.collections15.Transformer;

/**
 * The basic vertex shaper for the {@link GraphViewer}.
 *
 * @author Ingo Mierswa
 * @version $Id: BasicVertexShapeTransformer.java,v 1.1 2007/06/19 00:14:07 ingomierswa Exp $
 */
public class BasicVertexShapeTransformer<V> implements Transformer<V, Shape> {
	
	private Shape shape = new Ellipse2D.Float(-10.0f, -10.0f, 20.0f, 20.0f);
	
	public Shape transform(V object) {
		return shape;
	}
}
