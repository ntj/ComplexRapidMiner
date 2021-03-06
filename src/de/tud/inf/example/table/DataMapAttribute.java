package de.tud.inf.example.table;

import java.util.Map;

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

	public DataMapAttribute(DataMapAttribute a){
		super(a);
	}
	
	public DataMapAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	public DataMapAttribute(String name, int valueType, String hint){
		super(name,valueType,hint);
		innerAttribute = (RelationalAttribute)AttributeFactory.createAttribute(Ontology.RELATIONAL);
		
		//key attribute: check whether string or numeric key
		if(valueType == Ontology.DATA_MAP_STRING)
			innerAttribute.addInnerAttribute(AttributeFactory.createAttribute(name + "_key",Ontology.STRING));
		else
			innerAttribute.addInnerAttribute(AttributeFactory.createAttribute(name + "_key",Ontology.NUMERICAL));
		//value attribute
		innerAttribute.addInnerAttribute(AttributeFactory.createAttribute(name + "_value",Ontology.NUMERICAL));
	}
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 806365320039407341L;

	@Override
	public DataMapValue getComplexValue(DataRow row) {
		DataMapValue dMapVal = (DataMapValue)ComplexValueFactory.getComplexValueFunction(getValueType(),this.hint);
		double[][]  values = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		//build map
		if(innerAttribute.getInnerAttributeAt(0).isNominal())
			dMapVal.setValues(values,innerAttribute.getInnerAttributeAt(0).getMapping());		
		else 		dMapVal.setValues(values);
		return dMapVal;
	}

	
	@Override
	public int getParameterCount() {
		return 0;
		
	}

	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		DataMapValue dmValue = (DataMapValue)value;	
		double[][] rValues = new double[dmValue.size()][2];
		int count =0;
		if(innerAttribute.getInnerAttributeAt(0).isNominal()){
			NominalMapping attrMapping = innerAttribute.getInnerAttributeAt(0).getMapping();
			Map<String,Double> kvMap = dmValue.getStringMap();  
			for (Map.Entry<String, Double> entry: kvMap.entrySet()) {
				//store key
				rValues[count][0] = attrMapping.mapString(entry.getKey());
				//store value
	            rValues[count][1] = entry.getValue();
	            count++;
			}
		}
		else{
			Map<Integer,Double> kvMap = dmValue.getMap();  
			for (Map.Entry<Integer, Double> entry: kvMap.entrySet()) {
				rValues[count][0] = entry.getKey();
	            rValues[count][1] = entry.getValue();
	            count++;
			}
		}
		row.setRelationalValues(innerAttribute.getTableIndex(), rValues);
	}

	@Override
	public DataMapAttribute clone() {
		return new DataMapAttribute(this);
	}

}