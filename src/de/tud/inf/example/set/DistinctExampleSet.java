package de.tud.inf.example.set;

import java.util.HashMap;
import java.util.Iterator;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AbstractExampleSet;
import com.rapidminer.example.table.ExampleTable;

public class DistinctExampleSet extends AbstractExampleSet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8467825676883890623L;

	private Attributes attributes;
	
	private Example[] examples;
	
	private ExampleTable exampleTable;

	public DistinctExampleSet(ExampleSet exampleSet) {
		
		this.attributes = exampleSet.getAttributes();
		
		this.buildDistinctTable(exampleSet);
		
		this.exampleTable = exampleSet.getExampleTable();
	}

	/*
	 * Clone constructor
	 */
	public DistinctExampleSet(DistinctExampleSet exampleSet) {
		
		this.examples = exampleSet.examples.clone();
		
		this.attributes = (Attributes)exampleSet.attributes.clone();
		
		this.exampleTable = exampleSet.getExampleTable();
		
	}

	public Attributes getAttributes() {

		return this.attributes;
	}

	public Example getExample(int index) {

		//return parent.getExample((Integer) exampleMapping[index]);
		return examples[index];
	}

	public ExampleTable getExampleTable() {

		return this.exampleTable;
	}

	public int size() {

		return this.examples.length;
	}

	public Iterator<Example> iterator() {

		return new DistinctExampleReader(this);
	}

	private void buildDistinctTable(ExampleSet exampleSet) {
		
		HashMap<String, Example> exampleMap = new HashMap<String, Example>(exampleSet.size(), 1);

		StringBuffer key;

		// getting the id Attribute
		Attribute id = this.getAttributes().getId();

		for (Example ex : exampleSet) {

			key = new StringBuffer();
			for (Attribute attr : this.getAttributes()) {
				
				// Id Attribute should be ignored
				if (!attr.equals(id)) {

					key.append(ex.getValue(attr));
				}

			}
			if (!exampleMap.containsKey(key.toString())) {

				exampleMap.put(key.toString(), ex);
			} 

		}

		// array of the examples
		examples = exampleMap.values().toArray(new Example[]{});
	}

}
