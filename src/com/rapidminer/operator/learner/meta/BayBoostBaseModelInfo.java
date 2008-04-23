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
package com.rapidminer.operator.learner.meta;

import java.io.Serializable;

import com.rapidminer.operator.Model;

/**
 * Stores a base model together with its contingency matrix, which offerers a
 * more convenient access in the context of ensemble classification.
 * 
 * @author Martin Scholz
 * @version $Id: BayBoostBaseModelInfo.java,v 1.3 2006/04/14 15:04:22
 *          ingomierswa Exp $
 */
public class BayBoostBaseModelInfo implements Serializable {

	private static final long serialVersionUID = 2818741267629650262L;

	// all fields are final, in particular ContingencyMatrix returns clones of
	// the original matrix
	private final Model model;

	private final ContingencyMatrix matrix;

	public BayBoostBaseModelInfo(Model model, ContingencyMatrix matrix) {
		this.model = model;
		this.matrix = matrix;
	}

	public Model getModel() {
		return this.model;
	}

	public ContingencyMatrix getContingencyMatrix() {
		return this.matrix;
	}

	public double getLiftRatio(int trueLabel, int predictedLabel) {
		return matrix.getLiftRatio(trueLabel, predictedLabel);
	}

}
