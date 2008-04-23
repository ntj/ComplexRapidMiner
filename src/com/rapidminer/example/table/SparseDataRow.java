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
package com.rapidminer.example.table;

import com.rapidminer.example.FastExample2SparseTransform;

/**
 * This interface defines methods for sparse data rows which might be
 * implemented to allow more efficient querying of the non-default values.
 * Please refer to {@link FastExample2SparseTransform} for further information.
 * 
 * @author Ingo Mierswa
 * @version $Id: SparseDataRow.java,v 1.1 2007/05/27 22:01:18 ingomierswa Exp $
 */
public interface SparseDataRow {

	/**
	 * Returns an array of all attribute indices with corresponding non-default
	 * values.
	 */
	public int[] getNonDefaultIndices();

	/** Returns an array of all non-default attribute values. */
	public double[] getNonDefaultValues();
}
