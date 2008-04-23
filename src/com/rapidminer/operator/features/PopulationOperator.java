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
package com.rapidminer.operator.features;

/**
 * An operator that modifies populations.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: PopulationOperator.java,v 2.11 2006/03/21 15:35:42 ingomierswa
 *          Exp $
 */
public interface PopulationOperator {

	/** Modifies the population. */
	public void operate(Population pop) throws Exception;

	/**
	 * Indicates if the operation should be performed in the given generation.
	 * Allows pop ops which works only in a part of the generations.
	 */
	public boolean performOperation(int generation);
}
