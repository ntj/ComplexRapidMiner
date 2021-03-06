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
package com.rapidminer.operator.similarity.attributebased;

/**
 * Implements the Russell-Rao similarity for nominal attributes.
 * 
 * @author Michael Wurst
 * @version $Id: RussellRaoNominalSimilarity.java,v 1.5 2008/09/12 10:31:21 tobiasmalbrecht Exp $
 */
public class RussellRaoNominalSimilarity extends AbstractNominalSimilarity {

	private static final long serialVersionUID = 2355067515743317025L;

	protected double calculateSimilarity(double a, double b, double c) {
		return a / (a + b + c);
	}
}
