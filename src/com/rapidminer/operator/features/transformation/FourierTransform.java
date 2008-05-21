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
package com.rapidminer.operator.features.transformation;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.SimpleExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.Complex;
import com.rapidminer.tools.math.FastFourierTransform;
import com.rapidminer.tools.math.Peak;
import com.rapidminer.tools.math.SpectrumFilter;
import com.rapidminer.tools.math.WindowFunction;


/**
 * Creates a new example set consisting of the result of a fourier
 * transformation for each attribute of the input example set.
 * 
 * @author Ingo Mierswa
 * @version $Id: FourierTransform.java,v 1.13 2006/04/05 08:57:27 ingomierswa
 *          Exp $
 */
public class FourierTransform extends Operator {

	private static final Class[] INPUT_CLASSES = { ExampleSet.class };

	private static final Class[] OUTPUT_CLASSES = { ExampleSet.class };

	public FourierTransform(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// init
		ExampleSet exampleSet = getInput(ExampleSet.class);

		// create new example table
		int numberOfNewExamples = FastFourierTransform.getGreatestPowerOf2LessThan(exampleSet.size()) / 2;
		ExampleTable exampleTable = new MemoryExampleTable(new LinkedList<Attribute>(), new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'), numberOfNewExamples);

		// create frequency attribute (for frequency)
		Attribute frequencyAttribute = AttributeFactory.createAttribute("frequency", Ontology.REAL);
		exampleTable.addAttribute(frequencyAttribute);
		DataRowReader drr = exampleTable.getDataRowReader();
		int k = 0;
		while (drr.hasNext()) {
			DataRow dataRow = drr.next();
			dataRow.set(frequencyAttribute, FastFourierTransform.convertFrequency(k++, numberOfNewExamples, exampleSet.size()));
		}

		// create fft values
		List<Attribute> attributes = new LinkedList<Attribute>();
        attributes.add(frequencyAttribute);
		Attribute label = exampleSet.getAttributes().getLabel();
        
        // add FFT values
		FastFourierTransform fft = new FastFourierTransform(WindowFunction.BLACKMAN_HARRIS);
		SpectrumFilter filter = new SpectrumFilter(SpectrumFilter.NONE);
		for (Attribute current : exampleSet.getAttributes()) {
			if (!current.isNominal()) {
				Complex[] result = fft.getFourierTransform(exampleSet, label, current);
				Peak[] spectrum = filter.filter(result, exampleSet.size());
				// create new attribute and fill table with values
				Attribute newAttribute = AttributeFactory.createAttribute("fft(" + current.getName() + ")", Ontology.REAL);
				exampleTable.addAttribute(newAttribute);
				attributes.add(newAttribute);
				fillTable(exampleTable, newAttribute, spectrum);
			}
		}
        
		ExampleSet resultSet = new SimpleExampleSet(exampleTable);
        
		return new IOObject[] { resultSet };
	}

	/**
	 * Fills the table with the length of the given complex numbers in the
	 * column of the attribute.
	 */
	private void fillTable(ExampleTable table, Attribute attribute, Peak[] values) throws OperatorException {
		DataRowReader reader = table.getDataRowReader();
		int k = 0;
		while (reader.hasNext()) {
			DataRow dataRow = reader.next();
			dataRow.set(attribute, values[k++].getMagnitude());
			checkForStop();
		}
	}

	public Class[] getInputClasses() {
		return INPUT_CLASSES;
	}

	public Class[] getOutputClasses() {
		return OUTPUT_CLASSES;
	}

}
