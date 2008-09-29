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
package com.rapidminer.operator.similarity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.Ontology;

/**
 * <p>This operator creates an example set from a given similarity measure. It can either produce
 * a long table format, i.e. something like<br />
 * <br />
 * id1 id2 sim<br /> 
 * id1 id3 sim<br />
 * id1 id4 sim<br />
 * ...<br />
 * id2 id1 sim<br />
 * ...<br />
 * <br />
 * or a matrix format like here<br />
 * <br />
 * id id1 id2 id3 ...<br />
 * id1 sim sim sim...<br />
 * ...
 * <br /></p>
 *
 * @author Ingo Mierswa
 * @version $Id: Similarity2ExampleSet.java,v 1.2 2008/08/18 10:47:29 ingomierswa Exp $
 */
public class Similarity2ExampleSet extends Operator {

	public static final String PARAMETER_TABLE_TYPE = "table_type";
	
	public static final String[] TABLE_TYPES = {
		"long_table",
		"matrix"
	};
	
	public static final int TABLE_TYPE_LONG_TABLE = 0;
	
	public static final int TABLE_TYPE_MATRIX     = 1;
	
	
	public Similarity2ExampleSet(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		SimilarityMeasure similarityMeasure = getInput(SimilarityMeasure.class);
		
		ExampleSet result = null;
		if (getParameterAsInt(PARAMETER_TABLE_TYPE) == TABLE_TYPE_LONG_TABLE) {
			List<Attribute> attributes = new ArrayList<Attribute>(3);
			Attribute firstIdAttribute = AttributeFactory.createAttribute("FIRST_ID", Ontology.NOMINAL);
			attributes.add(firstIdAttribute);
			Attribute secondIdAttribute = AttributeFactory.createAttribute("SECOND_ID", Ontology.NOMINAL);
			attributes.add(secondIdAttribute);
			String name = "SIMILARITY";
			if (similarityMeasure.isDistance()) {
				name = "DISTANCE";
			}
			Attribute similarityAttribute = AttributeFactory.createAttribute(name, Ontology.REAL);
			attributes.add(similarityAttribute);
			
			MemoryExampleTable table = new MemoryExampleTable(attributes);
				
			Iterator<String> first = similarityMeasure.getIds();
			while (first.hasNext()) {
				String firstId = first.next();
				double firstIdMapping = firstIdAttribute.getMapping().mapString(firstId);
				
				Iterator<String> second = similarityMeasure.getIds();
				while (second.hasNext()) {
					String secondId = second.next();
					if (!firstId.equals(secondId)) {
						double[] data = new double[3];
						data[0] = firstIdMapping;
						data[1] = secondIdAttribute.getMapping().mapString(secondId);
						data[2] = similarityMeasure.similarity(firstId, secondId);
						table.addDataRow(new DoubleArrayDataRow(data));			
					}
				}
			}
			
			result = table.createExampleSet();
			
		} else {
			int numberOfIds = similarityMeasure.getNumberOfIds();
			List<Attribute> attributes = new ArrayList<Attribute>(numberOfIds + 1);
			Attribute idAttribute = AttributeFactory.createAttribute("ID", Ontology.NOMINAL);
			attributes.add(idAttribute);
			Iterator<String> ids = similarityMeasure.getIds();
			while (ids.hasNext()) {
				String id = ids.next();
				Attribute attribute = AttributeFactory.createAttribute(id, Ontology.REAL);
				attributes.add(attribute);
			}
			
			MemoryExampleTable table = new MemoryExampleTable(attributes);
				
			Iterator<String> first = similarityMeasure.getIds();
			while (first.hasNext()) {
				String firstId = first.next();
				double[] data = new double[numberOfIds + 1];
				data[0] = idAttribute.getMapping().mapString(firstId);
				int index = 1;
				Iterator<String> second = similarityMeasure.getIds();
				while (second.hasNext()) {
					String secondId = second.next();
					data[index++] = similarityMeasure.similarity(firstId, secondId);
				}
				table.addDataRow(new DoubleArrayDataRow(data));
			}
			
			result = table.createExampleSet(null, null, idAttribute);
		}
		
		return new IOObject[] { result };
	}

	public Class<?>[] getInputClasses() {
		return new Class[] { SimilarityMeasure.class };
	}

	public Class<?>[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeCategory(PARAMETER_TABLE_TYPE, "Indicates if the resulting table should have a matrix format or a long table format.", TABLE_TYPES, TABLE_TYPE_LONG_TABLE);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
