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

import java.io.Serializable;

/**
 * Item the base class for itemsets and provide all necessary frequency information.
 * 
 * @author Sebastian Land
 * @version $Id: Item.java,v 1.2 2007/06/22 15:31:44 ingomierswa Exp $
 */
public interface Item extends Comparable<Item>, Serializable {

	/**
	 * This method returns the frequency of this item
	 * 
	 * @return the frequency of this item
	 */
	public abstract int getFrequency();

	/**
	 * This method adds one to the frequency of this item
	 */
	public abstract void increaseFrequency();

	/**
	 * This method increases the frequency of this item by value
	 * 
	 * @param value
	 *            is added to the frequency
	 */
	public abstract void increaseFrequency(int value);

	/**
	 * This method compares this Item with the given one. A class cast exception may be thrown if arg0 is not of same class as this item.
	 */
	public abstract int compareTo(Item arg0);

	/**
	 * This method returns a human readable String representation of this item.
	 * 
	 * @return the representing string
	 */
	public abstract String toString();
	
}
