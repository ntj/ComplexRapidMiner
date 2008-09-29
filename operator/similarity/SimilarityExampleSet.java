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

import java.util.Iterator;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.set.AbstractExampleReader;
import com.rapidminer.example.set.AbstractExampleSet;
import com.rapidminer.example.set.MappedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DoubleArrayDataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.math.similarity.DistanceMeasure;

/**
 * This similarity based example set is used for the operator
 * {@link ExampleSet2SimilarityExampleSet}.
 * 
 * @author Ingo Mierswa
 * @version $Id: SimilarityExampleSet.java,v 1.1 2008/09/08 18:53:49 ingomierswa Exp $
 */
public class SimilarityExampleSet extends AbstractExampleSet {

	private static final long serialVersionUID = 4757975818441794105L;

	private static class IndexExampleReader extends AbstractExampleReader {

		private int index = 0;
		
		private ExampleSet exampleSet;
		
		public IndexExampleReader(ExampleSet exampleSet) {
			this.exampleSet = exampleSet;
		}
		
		public boolean hasNext() {
			return index < exampleSet.size() - 1; 
		}

		public Example next() {
			Example example = exampleSet.getExample(index);
			index++;
			return example;
		}
	}
	
	private ExampleSet parent;
	
	private Attribute parentIdAttribute;
		
	private Attributes attributes;
	
	private DistanceMeasure measure;
	
	
	public SimilarityExampleSet(ExampleSet parent, DistanceMeasure measure) {
		this.parent = parent;
		
		this.parentIdAttribute = parent.getAttributes().getId();
		this.attributes = new SimpleAttributes();		

		Attribute firstIdAttribute = null;
		Attribute secondIdAttribute = null;
		if (parentIdAttribute.isNominal()) {
			firstIdAttribute = AttributeFactory.createAttribute("FIRST_ID", Ontology.NOMINAL);
			secondIdAttribute = AttributeFactory.createAttribute("SECOND_ID", Ontology.NOMINAL);
		} else {
			firstIdAttribute = AttributeFactory.createAttribute("FIRST_ID", Ontology.NUMERICAL);
			secondIdAttribute = AttributeFactory.createAttribute("SECOND_ID", Ontology.NUMERICAL);
		}
	
		this.attributes.addRegular(firstIdAttribute);
		this.attributes.addRegular(secondIdAttribute);
		firstIdAttribute.setTableIndex(0);
		secondIdAttribute.setTableIndex(1);
		
		// copying mapping of original id attribute
		if (parentIdAttribute.isNominal()) {
			NominalMapping mapping = parentIdAttribute.getMapping();
			firstIdAttribute.setMapping(mapping);
			secondIdAttribute.setMapping(mapping);
		}
		
		String name = "SIMILARITY";
		if (measure.isDistance()) {
			name = "DISTANCE";
		}
		
		Attribute similarityAttribute = AttributeFactory.createAttribute(name, Ontology.REAL);
		this.attributes.addRegular(similarityAttribute);
		similarityAttribute.setTableIndex(2);
		
		this.measure = measure;
	}
	
    public boolean equals(Object o) {
        if (!super.equals(o))
            return false;
        if (!(o instanceof MappedExampleSet))
            return false;
        
        SimilarityExampleSet other = (SimilarityExampleSet)o;    
        if (!this.measure.getClass().equals(other.measure.getClass()))
            return false;
        return true;
    }

    public int hashCode() {
        return super.hashCode() ^ this.measure.getClass().hashCode();
    }
    
	public Attributes getAttributes() {
		return this.attributes;
	}

	public Example getExample(int index) {
		int firstIndex = index / this.parent.size();
		int secondIndex = index % this.parent.size();
		
		Example firstExample = this.parent.getExample(firstIndex);
		Example secondExample = this.parent.getExample(secondIndex);
		
		double[] data = new double[3];
		data[0] = firstExample.getValue(parentIdAttribute);
		data[1] = secondExample.getValue(parentIdAttribute);

		if (measure.isDistance())
			data[2] = measure.calculateDistance(firstExample, secondExample);
		else
			data[2] = measure.calculateSimilarity(firstExample, secondExample);
		
		return new Example(new DoubleArrayDataRow(data), this);
	}

	public Iterator<Example> iterator() {
		return new IndexExampleReader(this);
	}
	
	public ExampleTable getExampleTable() {
		return null;//this.parent.getExampleTable();
	}
	
	public int size() {
		return this.parent.size() * this.parent.size();
	}
}
