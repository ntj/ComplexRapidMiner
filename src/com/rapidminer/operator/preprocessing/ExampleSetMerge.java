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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;


/** 
 * <p>This operator merges two or more given example sets by adding all examples in 
 * one example table containing all data rows. Please note that the new example table
 * is built in memory and this operator might therefore not be applicable for merging
 * huge data set tables from a database. In that case other preprocessing tools should
 * be used which aggregates, joins, and merges tables into one table which is then used
 * by RapidMiner.</p>
 * 
 * <p>All input example sets must provide the same attribute signature. That means that
 * all examples sets must have the same number of (special) attributes and attribute names.
 * If this is true this operator simply merges all example sets by adding all examples of all
 * table into a new set which is then returned.</p>
 *  
 * @author Ingo Mierswa
 * @version $Id: ExampleSetMerge.java,v 1.6 2008/05/09 19:22:54 ingomierswa Exp $
 */
public class ExampleSetMerge extends Operator {
    

	/** The parameter name for &quot;Determines, how the data is represented internally.&quot; */
	public static final String PARAMETER_DATAMANAGEMENT = "datamanagement";
    public ExampleSetMerge(OperatorDescription description) {
        super(description);
    }

    public Class[] getInputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public Class[] getOutputClasses() {
        return new Class[] { ExampleSet.class };
    }

    public IOObject[] apply() throws OperatorException {
        // collect all input example sets
        List<ExampleSet> allExampleSets = new LinkedList<ExampleSet>();
        boolean found = true;
        while (found) {
            try {
                ExampleSet exampleSet = getInput(ExampleSet.class);
                allExampleSets.add(exampleSet);
            } catch (MissingIOObjectException e) {
                found = false;
            }
        }
        
        // throw error if no example sets were available
        if (allExampleSets.size() == 0) 
            throw new MissingIOObjectException(ExampleSet.class);
        
        // checks if all example sets have the same signature
        checkForCompatibility(allExampleSets);
        
        // create new example table
        ExampleSet firstSet = allExampleSets.get(0);
        List<Attribute> attributeList = new LinkedList<Attribute>();
        Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
        Iterator<AttributeRole> a = firstSet.getAttributes().allAttributeRoles();
        while (a.hasNext()) {
        	AttributeRole role = a.next();
        	Attribute attributeClone = (Attribute)role.getAttribute().clone();
        	attributeList.add(attributeClone);
        	if (role.isSpecial()) {
        		specialAttributes.put(attributeClone, role.getSpecialName());
        	}
        }
        MemoryExampleTable exampleTable = new MemoryExampleTable(attributeList);
        
        Iterator<ExampleSet> i = allExampleSets.iterator();
        DataRowFactory factory = new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT), '.');
        while (i.hasNext()) {
            ExampleSet currentExampleSet = i.next();
            Iterator<Example> e = currentExampleSet.iterator();
            while (e.hasNext()) {
                DataRow dataRow = e.next().getDataRow();
                String[] newData = new String[attributeList.size()];
                //Iterator<Attribute> oldAttributes = currentExampleSet.getAttributes().allAttributes();
                Iterator<Attribute> newAttributes = attributeList.iterator();
                int counter = 0;
                while (newAttributes.hasNext()) {
                    //Attribute oldAttribute = oldAttributes.next();
                    Attribute newAttribute = newAttributes.next();
                	Attribute oldAttribute = currentExampleSet.getAttributes().get(newAttribute.getName());
                    double oldValue = dataRow.get(oldAttribute);
                    if (newAttribute.isNominal()) {
                        newData[counter] = oldAttribute.getMapping().mapIndex((int)oldValue);
                    } else {
                        newData[counter] = oldValue + "";
                    }
                    counter++;
                }
                exampleTable.addDataRow(factory.create(newData, exampleTable.getAttributes()));
                checkForStop();
            }
        }
        
        // create result example set        
        ExampleSet resultSet = exampleTable.createExampleSet(specialAttributes);
        return new IOObject[] { resultSet };
    }
    
    private void checkForCompatibility(List<ExampleSet> allExampleSets) throws OperatorException {
        ExampleSet first = allExampleSets.get(0);
        Iterator<ExampleSet> i = allExampleSets.iterator();
        while (i.hasNext()) {
            checkForCompatibility(first, i.next());
        }
    }
    
    private void checkForCompatibility(ExampleSet first, ExampleSet second) throws OperatorException {
        if (first.getAttributes().allSize() != second.getAttributes().allSize()) {
            throw new UserError(this, 925, "numbers of attributes are different");
        }
        
        Iterator<Attribute> firstIterator  = first.getAttributes().allAttributes();
        while (firstIterator.hasNext()) {
        	Attribute firstAttribute  = firstIterator.next();
        	Attribute secondAttribute = second.getAttributes().get(firstAttribute.getName());
        	if (secondAttribute == null)
                throw new UserError(this, 925, "attribute with name '" + firstAttribute.getName() + "' is not part of second example set.");
        }      
    }
    
    public List<ParameterType> getParameterTypes() {
        List<ParameterType> types = super.getParameterTypes();
        types.add(new ParameterTypeCategory(PARAMETER_DATAMANAGEMENT, "Determines, how the data is represented internally.", DataRowFactory.TYPE_NAMES, DataRowFactory.TYPE_DOUBLE_ARRAY));
        return types;
    }
}
