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
package com.rapidminer.operator.features.transformation;

import com.rapidminer.example.AttributeWeights;
import com.rapidminer.operator.OperatorException;


/**
 * This is an interface for models holding several components for feature
 * transformation. For each component the <code>AttributeWeights</code> have
 * to be available.
 * 
 * @author Daniel Hakenjos
 * @version $Id: ComponentWeightsCreatable.java,v 1.1 2006/04/14 13:07:13
 *          ingomierswa Exp $
 */
public interface ComponentWeightsCreatable {

	public AttributeWeights getWeightsOfComponent(int component) throws OperatorException;

}
