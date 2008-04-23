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
package com.rapidminer.example.set;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DelegateAttribute;

/**
 * This attribute scales the data row values by its weight.
 * 
 * @author Ingo Mierswa
 * @version $Id: WeightedAttribute.java,v 1.1 2007/07/14 12:31:38 ingomierswa Exp $
 */
public class WeightedAttribute extends DelegateAttribute {

	private static final long serialVersionUID = 1167054050312730345L;
	
	private double weight = 1.0d;
	
	public WeightedAttribute(Attribute parent, double weight) {
		super(parent);
		this.weight = weight;
	}
	
	public double getValue(DataRow row) {
		if (Double.isNaN(weight)) {
			return super.getValue(row);
		} else {
			return super.getValue(row) * weight;
		}
	}
}
