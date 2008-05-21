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
package com.rapidminer.operator.preprocessing.filter;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;


/** This operator creates a new, shuffled ExampleSet by making <em>a new copy</em>
 *  of the exampletable in main memory!
 *  Caution! System may run out of memory, if exampletable is too large.
 *
 *  @author Sebastian Land, Ingo Mierswa
 *  @version $Id: PermutationOperator.java,v 1.3 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class PermutationOperator extends Operator {
    
    public PermutationOperator(OperatorDescription description) {
        super(description);
    }
    
    public IOObject[] apply() throws OperatorException {
        ExampleSet exampleSet = getInput(ExampleSet.class);
        ExampleTable table = exampleSet.getExampleTable();
        
        // generate attribute list (clones)
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (Attribute attribute : exampleSet.getAttributes()) {
            attributeList.add((Attribute)attribute.clone());
        }
        int toCopy = table.size();
       
        // generate new ExampleTable of size of old table
        MemoryExampleTable shuffledTable = new MemoryExampleTable(attributeList);
        
        // copy all dataRows
        DataRow[] isCopied = new DataRow[toCopy];
        int areCopied = 0;
        DataRowReader reader = table.getDataRowReader();
        while (areCopied < toCopy) {
            int currentRow = (int)Math.round((Math.random()*(toCopy-1)));
            if (isCopied[currentRow] == null){
                isCopied[currentRow] = reader.next();
                // increase counter of copied rows
                areCopied++;
            }
            checkForStop();
        }
        for (int i=0; i < toCopy; i++) {
            shuffledTable.addDataRow(isCopied[i]);
        }
        
        return new IOObject[] { shuffledTable.createExampleSet(exampleSet.getAttributes().specialAttributes()) };
    }
    
    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }
    
    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }
}
