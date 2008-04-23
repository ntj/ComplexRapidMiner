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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Tools;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * Abstract class representing some common functionality of dimensionality reduction methods. 
 * 
 * TODO: either remove or extend this class and fit it to the the rest of the package
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: DimensionalityReducer.java,v 1.3 2007/06/15 16:58:38 ingomierswa Exp $
 */
public abstract class DimensionalityReducer extends Operator {


	/** The parameter name for &quot;the number of dimensions in the result representation&quot; */
	public static final String PARAMETER_DIMENSIONS = "dimensions";
	protected ExampleSet es;

	protected int dimensions;

	protected double[][] p;

	public DimensionalityReducer(OperatorDescription description) {
		super(description);
	}

	/**
	 * Perform the actual dimensionality reduction.
	 */
	protected abstract void dimensionalityReduction();

	public IOObject[] apply() throws OperatorException {
		es = getInput(ExampleSet.class);
		dimensions = getParameterAsInt(PARAMETER_DIMENSIONS);

		Tools.onlyNumericalAttributes(es, "dimensionality reduction");
		Tools.isNonEmpty(es);
		Tools.checkAndCreateIds(es);

		dimensionalityReduction();

		ExampleSet result = createNewExampleSet();
		return new IOObject[] { result };
	}

	private ExampleSet createNewExampleSet() {
		List<Attribute> attributes = new ArrayList<Attribute>();
		Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
		for (int i = 0; i < dimensions; i++) {
			Attribute att = AttributeFactory.createAttribute("d" + i, Ontology.REAL);
			attributes.add(att);
		}

		Iterator<AttributeRole> s = es.getAttributes().specialAttributes();
		while (s.hasNext()) {
			AttributeRole role = s.next();
			Attribute att = (Attribute) role.getAttribute().clone();
			specialAttributes.put(att, role.getSpecialName());
			attributes.add(att);
		}
		MemoryExampleTable et = new MemoryExampleTable(attributes);

		// Apply the measures and build the instances
		List<DataRow> dataRows = new ArrayList<DataRow>();

		// Apply the measures and build the instances
		int i = 0;
		for (Example oldExample : es) {
			DataRow dr = new DoubleArrayDataRow(new double[dimensions + specialAttributes.size()]);
			for (int j = 0; j < dimensions; j++)
				dr.set(attributes.get(j), p[i][j]);

			Iterator<Attribute> sa = specialAttributes.keySet().iterator();

            while (sa.hasNext()) {
                Attribute att = sa.next();
                dr.set(att, oldExample.getValue(es.getAttributes().getSpecial(att.getName())));
            }
            
			dataRows.add(dr);
			i++;
		}

		et.readExamples(new ListDataRowReader(dataRows.iterator()));
		ExampleSet examples = et.createExampleSet(specialAttributes);
		return examples;
	}

	protected void setResult(double[][] result) {
		p = result;
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeInt(PARAMETER_DIMENSIONS, "the number of dimensions in the result representation", 1, Integer.MAX_VALUE, 2);
		type.setExpert(false);
		types.add(type);
		return types;
	}
}
