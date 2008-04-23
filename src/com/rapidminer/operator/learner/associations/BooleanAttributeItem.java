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
package com.rapidminer.operator.learner.associations;

import com.rapidminer.example.Attribute;

/**
 * This is an {@link Item} based on boolean attributes.
 * 
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: BooleanAttributeItem.java,v 1.4 2007/06/24 14:30:54 ingomierswa Exp $
 */
public class BooleanAttributeItem implements Item {

	private static final long serialVersionUID = -7963677912091349984L;

	private int frequency = 0;

	private String name;

	public BooleanAttributeItem(Attribute item) {
		this.name = item.getName();
	}

	public int getFrequency() {
		return this.frequency;
	}

	public void increaseFrequency() {
		this.frequency++;
	}

	public void increaseFrequency(double value) {
		this.frequency += value;
	}

	public boolean equals(Object other) {
		if (!(other instanceof BooleanAttributeItem))
			return false;
		BooleanAttributeItem o = (BooleanAttributeItem)other;
		return (this.name.equals(o.name)) && (this.frequency == o.frequency);
	}
	
	public int hashCode() {
		return this.name.hashCode() ^ Double.valueOf(this.frequency).hashCode();
	}
	
	public int compareTo(Item arg0) {
		Item comparer = arg0;
		// Collections.sort generates ascending order. Descending needed,
		// therefore invert return values!
		if (comparer.getFrequency() == this.getFrequency()) {
			return (-1 * this.name.compareTo(arg0.toString()));
		} else if (comparer.getFrequency() < this.getFrequency()) {
			return -1;
		} else {
			return 1;
		}
	}

	public String getName() {
		return name;
	}
	
	public String toString() {
		return this.name;
	}

	public void increaseFrequency(int value) {
		frequency += value;
	}
}
