package de.tud.inf.example.table;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.DataMapValue;

/**
 * provides a simple Map<Key,Value> - map, data is read from an relational attribute with two inner attributes
 * since relational attributes cannot be nested, there are just atomar values possible
 * @author Antje Gruner
 *
 */
public class DataMapAttribute extends ComplexProxyAttribute{

	public DataMapAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	public DataMapAttribute(String name, int valueType, String hint){
		super(name,valueType,hint);
		innerAttribute = (RelationalAttribute)AttributeFactory.createAttribute(Ontology.ATTRIBUTE_VALUE_TYPE.RELATIONAL);
		List<Attribute> iList = new LinkedList<Attribute>();
		//key attribute: check whether string or numeric key
		if(valueType == Ontology.ATTRIBUTE_VALUE_TYPE.DATA_MAP_STRING)
			iList.add(AttributeFactory.createAttribute(name + "_key",Ontology.ATTRIBUTE_VALUE_TYPE.STRING));
		else
			iList.add(AttributeFactory.createAttribute(name + "_key",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		//value attribute
		iList.add(AttributeFactory.createAttribute(name + "_value",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		
		innerAttribute.setInnerAttributes(iList);
		
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 806365320039407341L;

	@Override
	public ComplexValue getComplexValue(DataRow row) {
		DataMapValue dMapVal = null;
		dMapVal = (DataMapValue)ComplexValueFactory.getComplexValueFunction(getValueType(),this.hint);
		double[][]  values = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		//build map
		if(innerAttribute.getInnerAttributeAt(0).isNominal())
			dMapVal.setValues(values,innerAttribute.getInnerAttributeAt(0).getMapping());		
		else 		dMapVal.setValues(values);
		return dMapVal;
	}

	//TEST if set works
	public void setComplexValue(DataMapValue value, DataRow row){
		//this attribute must now exactly where to store which information in mapValue and stores it in dataRow
	}
	
	
	@Override
	public int getParameterCount() {
		return 0;
	}

}
