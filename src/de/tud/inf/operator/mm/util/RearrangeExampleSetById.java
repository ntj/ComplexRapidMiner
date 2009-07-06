/*
 *  YALE - Yet Another Learning Environment
 *
 *  Copyright (C) 2001-2006 by the class authors
 *
 *  Project administrator: Ingo Mierswa
 *
 *      YALE was mainly written by (former) members of the
 *      Artificial Intelligence Unit
 *      Computer Science Department
 *      University of Dortmund
 *      44221 Dortmund,  Germany
 *
 *  Complete list of YALE developers available at our web site:
 *
 *       http://yale.sf.net
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
package de.tud.inf.operator.mm.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.Ontology;

/**
 * This operator transposes an ExampleSet from a n x 2 - matrix to a n x m - matrix.
 * The input ExampleSet must have a target id column and a column with the target attributes.
 * Rows with the same target id will represent a row in the transposed matrix. The number of
 * attributes in the output ExampleSet depends on the maximum number of rows with the same 
 * target id. Their attribute names are generic.
 *  
 * @author Thomas Harzer, modified by Michael Conrad
 *
 */
public class RearrangeExampleSetById extends Operator {
	
	public RearrangeExampleSetById(OperatorDescription description) {
		super(description);
	}
	
	public IOObject[] apply() throws OperatorException {
		
		ExampleSet es = getInput(ExampleSet.class);
		Attributes attributes = es.getAttributes();
		Attribute targetId = attributes.get(getParameterAsString("id_column"));
		String targetPre = getParameterAsString("target_column_prefix");
		// special attributes
		Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
		specialAttributes.put(targetId, Attributes.ID_NAME);       
		Map<Double,List<Double>> myObjects = new TreeMap<Double,List<Double>>();
		Map<Double, Map<String, Double>> myAdditionalInformation = new HashMap<Double, Map<String, Double>>();
		
		boolean useAsSeries = getParameterAsBoolean("use_as_series");
				
		Iterator<Example> reader = es.iterator();
		while (reader.hasNext()) {
			Example myExample = reader.next();
			double idValue = myExample.getValue(targetId);
			Attributes myAttributes = myExample.getAttributes();
			
			// get the target attributes from a defined column prefix			
			List<Double> myAttributeList = new LinkedList<Double>();
			Iterator<Attribute> attReader = myAttributes.iterator();
			while (attReader.hasNext()){
				Attribute myAttribute = attReader.next();
				if(myAttribute.getName().startsWith(getParameterAsString("value_column_prefix"))){
					myAttributeList.add(myExample.getValue(myAttribute));
				}else if(myAttribute.getName().compareTo(getParameterAsString("id_column")) != 0){
					if (myAdditionalInformation.get(idValue) == null) {
						Map<String, Double> valueList = new HashMap<String, Double>();
						valueList.put(myAttribute.getName(), myExample.getValue(myAttribute));
						
						myAdditionalInformation.put(idValue, valueList);
					}
					else {
						Map<String, Double> myList = myAdditionalInformation.get(idValue);
						if (!myList.containsKey(myAttribute.getName())){
							myList.put(myAttribute.getName(), myExample.getValue(myAttribute));
						}
					}
				}
			}
			
			if (myObjects.get(idValue) == null) {
				List<Double> attributeList = new LinkedList<Double>();
				attributeList.addAll(myAttributeList);
				myObjects.put(idValue, attributeList);
			}
			else {
				List<Double> myList = myObjects.get(idValue);
				myList.addAll(myAttributeList);
			}
		}
		
		// get number of attributes
		int attrCounter = 0;
		Iterator<Entry<Double, List<Double>>> it = myObjects.entrySet().iterator();
		while (it.hasNext()) {
			 Entry<Double, List<Double>> pairs = it.next();
			 List<Double> currentList = myObjects.get(pairs.getKey());
			 int listSize = currentList.size();
			 if (listSize > attrCounter) {
				 attrCounter = listSize;
			 }
		}
		
		// create transposed ExampleSet
		List<Attribute> unionAttributeList = new LinkedList<Attribute>();
		unionAttributeList.add(targetId);
		if(!myAdditionalInformation.isEmpty()){
			Iterator<String> attIterator = myAdditionalInformation.entrySet().iterator().next().getValue().keySet().iterator();
			while (attIterator.hasNext()){
				Attribute newAddAttribute = AttributeFactory.createAttribute(attIterator.next().toString(), Ontology.REAL);
				unionAttributeList.add(newAddAttribute);
			}
		}
				
		for (int i = 1; i <= attrCounter; i++) {
			Attribute newAttribute = null;
			if (useAsSeries){
				if (i == 1){
					newAttribute = AttributeFactory.createAttribute(targetPre+"_" + i, Ontology.REAL, Ontology.VALUE_SERIES_START);
				}else if (i == attrCounter){
					newAttribute = AttributeFactory.createAttribute(targetPre+"_" + i, Ontology.REAL, Ontology.VALUE_SERIES_END);
				}else{
					newAttribute = AttributeFactory.createAttribute(targetPre+"_" + i, Ontology.REAL, Ontology.VALUE_SERIES);
				}
			}else{
				newAttribute = AttributeFactory.createAttribute(targetPre+"_" + i, Ontology.REAL);
			}
			unionAttributeList.add(newAttribute);
		}
		MemoryExampleTable myTable = new MemoryExampleTable(unionAttributeList);
		
		Iterator<Entry<Double, List<Double>>> it2 = myObjects.entrySet().iterator();
		// get number of attributes
		while (it2.hasNext()) {
			 Entry<Double, List<Double>> pairs = it2.next();			 
			 List<Double> currentList = myObjects.get(pairs.getKey());
			 double[] unionDataRow = new double[unionAttributeList.size()];
			 // add key
			 unionDataRow[0] = (Double)pairs.getKey();
			 // add additional columns
			 int index = 1;
			 if(!myAdditionalInformation.isEmpty()){
				 Iterator<String> attIterator2 = myAdditionalInformation.get((Double)pairs.getKey()).keySet().iterator();
				 while (attIterator2.hasNext()){
					 unionDataRow[index] = myAdditionalInformation.get((Double)pairs.getKey()).get(attIterator2.next());
					 index++;
				 }
			 }
			 // add values
			 Iterator<Double> it3 = currentList.iterator();
			 while (it3.hasNext()) {
				 double value = it3.next();
				 unionDataRow[index] = value;
				 index++;
			 }
			 myTable.addDataRow(new DoubleArrayDataRow(unionDataRow));
		}
				
		ExampleSet exampleSet = myTable.createExampleSet(specialAttributes);
		return new IOObject[] { exampleSet };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		 List<ParameterType> types = super.getParameterTypes();
		 types.add(new ParameterTypeString("id_column", "The column with the target id", ""));
		 types.add(new ParameterTypeString("value_column_prefix", "The column prefix with the target attributes", ""));
		 types.add(new ParameterTypeBoolean("use_as_series", "Indicates whether generated set will be used as Value Series", false));
		 types.add(new ParameterTypeString("target_column_prefix", "The prefix for all target columns.", "attribute"));
	     return types;
	}	
}