package de.tud.inf.example.table;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.NominalMapping;
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
		innerAttribute = (RelationalAttribute)AttributeFactory.createAttribute(Ontology.RELATIONAL);
		List<Attribute> iList = new LinkedList<Attribute>();
		//key attribute: check whether string or numeric key
		if(valueType == Ontology.DATA_MAP_STRING)
			iList.add(AttributeFactory.createAttribute(name + "_key",Ontology.STRING));
		else
			iList.add(AttributeFactory.createAttribute(name + "_key",Ontology.NUMERICAL));
		//value attribute
		iList.add(AttributeFactory.createAttribute(name + "_value",Ontology.NUMERICAL));
		
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

	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		DataMapValue dmValue = (DataMapValue)value;
		
		//get attribute keyMapping
		NominalMapping attrMapping = innerAttribute.getInnerAttributeAt(0).getMapping();
		//get object key mapping
		NominalMapping objMapping = dmValue.getKeyMapping();
		
		Map<String,Integer> kvMap = dmValue.getMap();
		
		double[][] rValues = new double[kvMap.size()][2];
		//TODO: set data map values here
		/*
		for(int i=0;i<rValues.length;i++){
			//get string value at position i from object mapping, map that string to an index (via attribute mapping)
			//store resulting index into dataRow
			rValues[i][0] =  attrMapping.mapString(objMapping.mapIndex((int)values[i]));
			rValues[i][0] =  attrMapping.mapString(objMapping.mapIndex((int)values[i]));
		}		
		row.setRelationalValues(innerAttribute.getTableIndex(), rValues);
		*/
	}

}
