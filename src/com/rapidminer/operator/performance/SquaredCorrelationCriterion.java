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
package com.rapidminer.operator.performance;

/**
 * Computes the square of the empirical corellation coefficient 'r' between
 * label and prediction. Eith P=prediction, L=label, V=Variance, Cov=Covariance
 * we calculate r by: <br>
 * Cov(L,P) / sqrt(V(L)*V(P)). Uses the calculation of the superclass.
 * 
 * @author Ingo Mierswa
 * @version $Id: SquaredCorrelationCriterion.java,v 2.7 2006/03/21 15:35:51
 *          ingomierswa Exp $
 */
public class SquaredCorrelationCriterion extends CorrelationCriterion {

	private static final long serialVersionUID = 8751373179064203312L;

	public String getDescription() {
		return "Returns the squared correlation coefficient between the label and predicted label.";
	}

	public double getMikroAverage() {
		double r = super.getMikroAverage();
		return r * r;
	}

	public String getName() {
		return "squared_correlation";
	}
}
