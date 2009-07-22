package de.tud.inf.example.set;

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
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.ComplexAttributeInstantiationException;
import de.tud.inf.example.table.ComplexAttributeDescription;
import de.tud.inf.example.table.ComplexExampleTable;
import de.tud.inf.example.table.RelationalAttribute;

/**
 * ExampleSet, which contains atomar and complex attributes
 * @author Antje Gruner
 * 
 */
public class ComplexExampleSet extends AbstractExampleSet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2979045598526927487L;

	private ComplexExampleTable exampleTable;
	
	private Attributes attributes;
	
	private int nrWrappedAttributes =0;
	private int nrAtomarAttributes =0;
	public ComplexExampleSet(ComplexExampleTable exampleTable) {
		this(exampleTable, null);
	}

	public ComplexExampleSet(ComplexExampleTable exampleTable, Map<Attribute, String> specialAttributes) {
		//create ComplexAttributes from Attributes in exampleTable + dependency information
		this.exampleTable = exampleTable;
		attributes = new SimpleAttributes();
		List<Attribute> regularList = new LinkedList<Attribute>();
		List<Attribute> innerAttributes;
		List<Attribute> parameters;
		
		//when innerAttribute/parameter found, store its table index here, all attributes, which index does not appear in this list are default primitive attributes
		List<Integer> dependExampleTableAtt = new LinkedList<Integer>();
		//build complex Attributes from Dependency information
		for(int d = 0;d < exampleTable.getDependencyCount();d++){
			ComplexAttributeDescription dep = exampleTable.getDependencyAt(d);
			int[] depAttIndexes = dep.getAttributeIndexes();
			int[] depParamIndexes = dep.getParamIndexes();
			innerAttributes = new LinkedList<Attribute>();
			parameters = new LinkedList<Attribute>();
			for (int i = 0; i < exampleTable.getNumberOfAttributes(); i++) {
				Attribute attribute = exampleTable.getAttribute(i);
				if (attribute != null){
					//collect those inner attributes which tableIndex is an element of dependency table index list
					for(int j = 0;j<depAttIndexes.length;j++)
						if (depAttIndexes[j] == attribute.getTableIndex()){
							innerAttributes.add(attribute);
							dependExampleTableAtt.add(i);
						}
					//collect those parameter attributes which tableIndex is an element of parameter table index list
					if(depParamIndexes != null){
						for(int j = 0;j<depParamIndexes.length;j++)
							if (depParamIndexes[j] == attribute.getTableIndex()){
								parameters.add(attribute);
								dependExampleTableAtt.add(i);
							}
					}
				}
			}
			if(innerAttributes.size() == dep.getAttributeIndexes().length){
				//build complex Attribute
				int type = Ontology.ATTRIBUTE_VALUE_TYPE.mapName(dep.getSymbol());
				Attribute a = null;
				//test whether proxy attribute
				if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(type, Ontology.GEOMETRY))
					if((innerAttributes.size() == 1) && (innerAttributes.get(0).isRelational())){
						a = AttributeFactory.createProxyAttribute(dep.getName(),type,
											(RelationalAttribute)innerAttributes.get(0),parameters,dep.getSymbol(),dep.getHint());
					}
					else throw new ComplexAttributeInstantiationException("geometry attribute '" + dep.getName() +"' can only be instantiated with one relational inner attribute");
				else if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(type, Ontology.COMPLEX_VALUE)){
					a =  AttributeFactory.createCompositeAttribute(dep.getName(),type,innerAttributes,parameters,dep.getHint());
				}
				if(a != null)
					regularList.add(a);
			}
			else
				throw new ComplexAttributeInstantiationException("complex attribute '" + dep.getName() +"' should be build from existing  attributes");
			nrWrappedAttributes += innerAttributes.size();
		}
		
		
		//add those attributes, which are not involved in complex attribute stuff
		for (int i = 0; i < exampleTable.getNumberOfAttributes(); i++)
			if(!dependExampleTableAtt.contains(i)&&(exampleTable.getAttribute(i) != null)){
				regularList.add(exampleTable.getAttribute(i));
				nrAtomarAttributes++;
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
	
	public Attributes getAttributes() {
		return attributes;
	}
	
	// --- examples ---

	public ComplexExampleTable getExampleTable() {
		return exampleTable;
	}
	
	public int size() {
		return exampleTable.size();
	}

	public Example getExample(int index) {
		//change
		DataRow dataRow = getExampleTable().getDataRow(index);
		if (dataRow == null)
			return null;
		else
			return new Example(dataRow, this);
	}
	
	public Iterator<Example> iterator() {
		return new SimpleExampleReader(getExampleTable().getDataRowReader(), this);
	}
	
	/**
	 * get the number of attributes, which are hidden, i.e they are inner attributes of complex attributes (no parameter attributes),
	 * (so attribute, which are wrapped more than once will be counted more than once) 
	 * @return
	 */
	public int getNrWrappedAttributes(){
		return nrWrappedAttributes;
	}
	
	/**
	 * get the number of attributes, which are not complex
	 * @return
	 */
	public int getNrAtomarAttributes(){
		return nrAtomarAttributes;
	}
}
