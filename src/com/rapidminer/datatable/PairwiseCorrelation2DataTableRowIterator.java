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
package com.rapidminer.datatable;

import java.util.Iterator;

import com.rapidminer.operator.visualization.SymmetricalMatrix;


/**
 *  This iterator iterates over all examples of an example set and creates
 *  {@link com.rapidminer.datatable.CorrelationMatrixRow2DataTableRowWrapper} objects.
 *  
 *   @author Ingo Mierswa
 *   @version $Id: PairwiseCorrelation2DataTableRowIterator.java,v 1.4 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class PairwiseCorrelation2DataTableRowIterator implements Iterator<DataTableRow> {

    private SymmetricalMatrix matrix;
    
    private int firstAttribute;
    
    private int secondAttribute;
    
    /** Creates a new DataTable iterator backed up by examples. If the idAttribute is null the DataTableRows 
     *  will not be able to deliver an Id. */
    public PairwiseCorrelation2DataTableRowIterator(SymmetricalMatrix matrix) {
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
