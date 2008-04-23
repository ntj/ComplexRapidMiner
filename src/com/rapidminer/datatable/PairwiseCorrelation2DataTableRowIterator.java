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
package com.rapidminer.datatable;

import java.util.Iterator;

import com.rapidminer.operator.visualization.CorrelationMatrix;


/**
 *  This iterator iterates over all examples of an example set and creates
 *  {@link com.rapidminer.datatable.CorrelationMatrixRow2DataTableRowWrapper} objects.
 *  
 *   @author Ingo Mierswa
 *   @version $Id: PairwiseCorrelation2DataTableRowIterator.java,v 1.1 2007/05/27 21:59:07 ingomierswa Exp $
 */
public class PairwiseCorrelation2DataTableRowIterator implements Iterator<DataTableRow> {

    private CorrelationMatrix matrix;
    
    private int firstAttribute;
    
    private int secondAttribute;
    
    /** Creates a new DataTable iterator backed up by examples. If the idAttribute is null the DataTableRows 
     *  will not be able to deliver an Id. */
    public PairwiseCorrelation2DataTableRowIterator(CorrelationMatrix matrix) {
        this.matrix = matrix;
        this.firstAttribute = 0;
        this.secondAttribute = 1;
    }
    
    public boolean hasNext() {
        return (firstAttribute < matrix.getNumberOfColumns()) && (secondAttribute < matrix.getNumberOfColumns());
    }
    
    public DataTableRow next() {
        DataTableRow row = new PairwiseCorrelation2DataTableRowWrapper(matrix, firstAttribute, secondAttribute);
        secondAttribute++;
        if (secondAttribute >= matrix.getNumberOfColumns()) {
            firstAttribute++;
            secondAttribute = firstAttribute + 1;
        }
        return row;
    }
    
    public void remove() {
        throw new RuntimeException("PairwiseCorrelation2DataTableRowIterator: removing rows is not supported!");
    }
}
