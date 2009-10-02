package de.tud.inf.example.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowReader;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.att.AttributeSet;

import de.tud.inf.example.set.ComplexExampleSet;

/**
 * wraps an exampleTable which stores underlying primitive attributes and has a list of complex attribute descriptions
 * @author Antje Gruner
 * 
 */
public class ComplexExampleTable implements ExampleTable{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8667757311263418568L;
	private ExampleTable parent;
	private List<ComplexAttributeDescription> dependencies;
	
	
	public ComplexExampleTable(ExampleTable parent, List<ComplexAttributeDescription> etDep) {
		this.parent = parent;
		this.dependencies = etDep;
	}
	
	public ComplexExampleTable(ExampleTable parent){
		this(parent,new ArrayList<ComplexAttributeDescription>());
	}
	
	public ComplexExampleSet createExampleSet(AttributeSet attributeSet) {
		Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
		Iterator<String> i = attributeSet.getSpecialNames().iterator();
		while (i.hasNext()) {
			String name = i.next();
			specialAttributes.put(attributeSet.getSpecialAttribute(name), name);
		}
		return createExampleSet(specialAttributes);
	}

	public ComplexExampleSet createExampleSet() {
		return createExampleSet(new HashMap<Attribute, String>());
	}

	
	public ComplexExampleSet createExampleSet(Map<Attribute, String> specialAttributes) {
		return new ComplexExampleSet(this,specialAttributes);
	}
	

	/**
	 * Returns a new example set with all attributes switched on. The 
	 * given attributes will be used as a special label attribute for learning,
	 * as (example) weight attribute, and as id attribute.
	 */
	public ComplexExampleSet createExampleSet(Attribute labelAttribute, Attribute weightAttribute, Attribute idAttribute) {
		Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
		if (labelAttribute != null)
			specialAttributes.put(labelAttribute, Attributes.LABEL_NAME);
		if (weightAttribute != null)
			specialAttributes.put(weightAttribute, Attributes.WEIGHT_NAME);
		if (idAttribute != null)
			specialAttributes.put(idAttribute, Attributes.ID_NAME);
		return new ComplexExampleSet(this, specialAttributes);
	}
	
	/**
	 * Returns a new example set with all attributes switched on. The 
	 * given attribute will be used as a special label attribute for learning.
	 */
	public ComplexExampleSet createExampleSet(Attribute labelAttribute) {
		return createExampleSet(labelAttribute, null, null);
	}
	
	public DataRow getDataRow(int index) {
		return parent.getDataRow(index);
	}

	
	public DataRowReader getDataRowReader() {
		return parent.getDataRowReader();
	}

	
	public int size() {
		return parent.size();
	}
	
	
	public int getDependencyCount() {
		return dependencies.size();
	}	
	
	
	public ComplexAttributeDescription getDependencyAt(int i){
		return dependencies.get(i);
	}

	public int addAttribute(Attribute a) {
			return parent.addAttribute(a);
	}

	public void addAttributes(Collection<Attribute> newAttributes) {
		parent.addAttributes(newAttributes);	
	}

	public Attribute findAttribute(String name) throws OperatorException {
		return parent.findAttribute(name);
	}

	public Attribute getAttribute(int i) {
		return parent.getAttribute(i);
	}

	public int getAttributeCount() {
		return parent.getAttributeCount();
	}

	public Attribute[] getAttributes() {
		return parent.getAttributes();
	}

	public int getNumberOfAttributes() {
		return parent.getNumberOfAttributes();
	}

	public void removeAttribute(Attribute attribute) {
		parent.removeAttribute(attribute);
	}

	public void removeAttribute(int index) {
		parent.removeAttribute(index);
	}

	public String toDataString() {
		return parent.toDataString();
	}

	public ExampleSet createExampleSet(
			Iterator<AttributeRole> newSpecialAttributes) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
	}

	public ExampleTable getParentTable(){
		return parent;
	}
	
	public void addComplexAttributeDescription(ComplexAttributeDescription cad){
		dependencies.add(cad);
	}
	
	public void addComplexAttribute(ComplexAttribute ca) {
		//check if ca is an multiinstance attribute -> if so, dataRow must store relational values
		if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(ca.getValueType(),Ontology.GEOMETRY)){
			//test if exampleSet already contains multiinstance attributes,
			//(if not then dataRow needs to initialize the map which stores relational values)
			boolean first = true;
			for(ComplexAttributeDescription cad: dependencies){
				int type = Ontology.ATTRIBUTE_VALUE_TYPE.mapName(cad.getSymbol());
				if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(type,Ontology.GEOMETRY)){
					first = false;
					break;
				}
			}
			if (first){
				DataRowReader dReader = this.getDataRowReader();
				while (dReader.hasNext())
					dReader.next().initRelationalMap();
			}
		}
		//add atomar attributes and parameter attributes
		List<Attribute> innerAtts = ca.getInnerAttributes();
		int[] innerIds = new int[ca.getInnerAttributeCount()];
		for(int i=0; i<innerAtts.size(); i++)
			innerIds[i] = this.addAttribute(innerAtts.get(i));	
		List<Attribute> paramAtts = ca.getParameterAttributes();
		int[] paramIds = new int[ca.getParameterCount()];
		if(paramAtts != null){
			for(int i=0; i<paramAtts.size(); i++)
				paramIds[i] = this.addAttribute(paramAtts.get(i));
		}
		//create new complex attribute description and add it to table
		this.addComplexAttributeDescription(ComplexAttributeFactory.createAttributeDescription(innerIds, paramIds, ca.getSymbol(), ca.getName(),ca.getHint()));		
	}
	
}	
