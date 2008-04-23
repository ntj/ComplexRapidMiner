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

import com.rapidminer.operator.OperatorDescription;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

/**
 * A dimensionality reduction method based on Singular Value Decomposition. TODO: see super class
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: SVDReduction.java,v 1.1 2007/05/27 22:02:50 ingomierswa Exp $
 * 
 */
public class SVDReduction extends JamaDimensionalityReduction {

	public SVDReduction(OperatorDescription description) {
		super(description);
	}

	protected Matrix callMatrixMethod(Matrix in) {
		SingularValueDecomposition svd = new SingularValueDecomposition(in);
		Matrix u = svd.getU().getMatrix(0, es.size() - 1, 0, dimensions - 1);
		return u;
	}
}
