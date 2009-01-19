package de.tud.inf.example.set;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.SimpleAttributes;
import com.rapidminer.example.set.AbstractExampleSet;
import com.rapidminer.example.set.SimpleExampleReader;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;

import de.tud.inf.example.table.UpdateableExampleTable;
import de.tud.inf.example.table.UpdateableMemoryExampleTable;

public class UpdateableExampleSet extends AbstractExampleSet {
	private static final long serialVersionUID = -5037226934269983307L;
	
	/** The table used for reading the examples from. */
	private UpdateableExampleTable exampleTable;

	/** Holds all information about the attributes. */
	private Attributes attributes = new SimpleAttributes();
	
	public UpdateableExampleSet(ExampleTable exampleTable) {
		this(exampleTable, null, null);
	}

	public UpdateableExampleSet(ExampleTable exampleTable, List<Attribute> regularAttributes) {
		this(exampleTable, regularAttributes, null);
	}

	public UpdateableExampleSet(ExampleTable exampleTable, Map<Attribute, String> specialAttributes) {
		this(exampleTable, null, specialAttributes);
	}

	public UpdateableExampleSet(ExampleTable exampleTable, List<Attribute> regularAttributes, Map<Attribute, String> specialAttributes) {
		List<Attribute> regularList = regularAttributes;
		
		Attribute[] tableAttributesArray = exampleTable.getAttributes();
		List<Attribute> tableAttributes = new ArrayList<Attribute>();
		
		for(Attribute attrib : tableAttributesArray) {
			tableAttributes.add(attrib);
		}
		
		this.exampleTable  = new UpdateableMemoryExampleTable(tableAttributes, exampleTable.getDataRowReader());
		
		if (regularList == null) {
			regularList = new LinkedList<Attribute>();
			for (int a = 0; a < exampleTable.getNumberOfAttributes(); a++) {
				Attribute attribute = exampleTable.getAttribute(a);
				if (attribute != null)
					regularList.add(attribute);	
			}
		}
		
		for (Attribute attribute : regularList) {
			if ((specialAttributes == null) || (specialAttributes.get(attribute) == null))
				getAttributes().add(new AttributeRole(attribute));
		}
		
		if (specialAttributes != null) {
			Iterator<Map.Entry<Attribute, String>> s = specialAttributes.entrySet().iterator();
			while (s.hasNext()) {
				Map.Entry<Attribute, String> entry = s.next();
				getAttributes().setSpecialAttribute(entry.getKey(), entry.getValue());
			}
		}
	}
	
	/** Clone constructor. */
	public UpdateableExampleSet(UpdateableExampleSet exampleSet) {
		this.attributes = (Attributes) exampleSet.getAttributes().clone();
		
		Attribute[] attribs = exampleSet.getExampleTable().getAttributes();
		List<Attribute> attribList = new LinkedList<Attribute>();
		for (Attribute attribute : attribs) {
			attribList.add(attribute);
		}
		
		this.exampleTable = new UpdateableMemoryExampleTable(attribList, exampleSet.getExampleTable().getDataRowReader());
	}
	
	public Attributes getAttributes() {
		return attributes;
	}

	public ExampleTable getExampleTable() {
		return exampleTable;
	}
	
	public int size() {
		return exampleTable.size();
	}

	public Example getExample(int index) {
		DataRow dataRow = getExampleTable().getDataRow(index);
		if (dataRow == null)
			return null;
		else
			return new Example(dataRow, this);
	}
	
	public void addExample(Example example) {
		exampleTable.addDataRow(example.getDataRow());
	}
	
	public void clear() {
		exampleTable.clear();
	}
	
	public Iterator<Example> iterator() {
		return new SimpleExampleReader(getExampleTable().getDataRowReader(), this);
	}
}
