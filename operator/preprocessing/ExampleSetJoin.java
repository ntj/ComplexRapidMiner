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
package com.rapidminer.operator.preprocessing;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;


/**
 * <p>
 * Build the join of two example sets using the id attributes of the sets, i.e. both example sets must have an id attribute where the same id indicate the same examples. If examples are missing an
 * exception will be thrown. The result example set will consist of the same number of examples but the union set or the union list (depending on parameter setting double attributes will be removed or
 * renamed) of both feature sets. In case of removing double attribute the attribute values must be the same for the examples of both example set, otherwise an exception will be thrown.
 * </p>
 * <p>
 * Please note that this check for double attributes will only be applied for regular attributes. Special attributes of the second input example set which do not exist in the first example set will
 * simply be added. If they already exist they are simply skipped.
 * </p>
 * 
 * @author Ingo Mierswa
 * @version $Id: ExampleSetJoin.java,v 1.4 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class ExampleSetJoin extends AbstractExampleSetJoin {

    public ExampleSetJoin(OperatorDescription description) {
        super(description);
    }

    protected MemoryExampleTable joinData(ExampleSet es1, ExampleSet es2, List<AttributeSource> originalAttributeSources, List<Attribute> unionAttributeList) throws OperatorException {
        es1.remapIds();
        es2.remapIds();
        Attribute id1 = es1.getAttributes().getId();
        Attribute id2 = es2.getAttributes().getId();
        
        MemoryExampleTable unionTable = new MemoryExampleTable(unionAttributeList);
        Iterator<Example> reader = es1.iterator();
        while (reader.hasNext()) {
            Example example1 = reader.next();
            double id1Value = example1.getValue(id1);
            Example example2 = null;
            if (id1.isNominal())
                example2 = es2.getExampleFromId(id2.getMapping().getIndex(id1.getMapping().mapIndex((int) id1Value)));
            else
                example2 = es2.getExampleFromId(id1Value);
            if (example2 == null)
                throw new UserError(this, 130, "'" + example1.getValueAsString(id1) + "'");

            double[] unionDataRow = new double[unionAttributeList.size()];
            Iterator<AttributeSource> a = originalAttributeSources.iterator();
            int index = 0;
            while (a.hasNext()) {
                AttributeSource source = a.next();
                if (source.getSource() == AttributeSource.FIRST_SOURCE) {
                    unionDataRow[index] = example1.getValue(source.getAttribute());
                } else if (source.getSource() == AttributeSource.SECOND_SOURCE) {
                    unionDataRow[index] = example2.getValue(source.getAttribute());
                }
                index++;
            }
            unionTable.addDataRow(new DoubleArrayDataRow(unionDataRow));
            checkForStop();
        }
        return unionTable;
    }

    protected boolean isIdNeeded() {
        return true;
    }
}
