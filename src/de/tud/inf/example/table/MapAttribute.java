package de.tud.inf.example.table;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.NominalAttribute;
import com.rapidminer.example.table.NominalMapping;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.MapValue;
/**
 * Map z = f(x,y), where x and y are equidistant
   inner relational attribute contains z values, there are six parameter attributes (origin, spacing, extension of x-/y-dimension)
 * @author Antje Gruner
 *
 */
public class MapAttribute extends ComplexProxyAttribute{

	List<Attribute> parameters;
	
	public MapAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, List<Attribute> parameters, String hint) {
		super(name, valueType, innerAttribute, hint);
		this.parameters = parameters;
	}
	
	public MapAttribute(String name, int valueType, String hint){
		super(name,valueType,hint);
		innerAttribute = (RelationalAttribute)AttributeFactory.createAttribute(Ontology.ATTRIBUTE_VALUE_TYPE.RELATIONAL);
		//(maybe TODO switch inner attribute type depending on valueType of MapAttribute (Map_STRING, MAP_DOUBLE))
		//DEFAULT: create map, where z-values are numeric
		List<Attribute> iList = new LinkedList<Attribute>();
		iList.add(AttributeFactory.createAttribute(name + "_zValues",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		//set inner relational attributes
		innerAttribute.setInnerAttributes(iList);
		
		parameters = new LinkedList<Attribute>();
		parameters.add(AttributeFactory.createAttribute(name + "_oX",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		parameters.add(AttributeFactory.createAttribute(name + "_oY",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		parameters.add(AttributeFactory.createAttribute(name + "_sX",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		parameters.add(AttributeFactory.createAttribute(name + "_sY",Ontology.ATTRIBUTE_VALUE_TYPE.NUMERICAL));
		parameters.add(AttributeFactory.createAttribute(name + "_dX",Ontology.ATTRIBUTE_VALUE_TYPE.INTEGER));
		parameters.add(AttributeFactory.createAttribute(name + "_dY",Ontology.ATTRIBUTE_VALUE_TYPE.INTEGER));
	}
	
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8243720102763289432L;

	@Override
	public MapValue getComplexValue(DataRow row) {
		MapValue mv = (MapValue)ComplexValueFactory.getComplexValueFunction(1, getValueType(), this.getHint());
		double[] origin  = {parameters.get(0).getValue(row), parameters.get(1).getValue(row)};
		double[] spacing = {parameters.get(2).getValue(row), parameters.get(3).getValue(row)}; 
		int[]    extent  = {(int)parameters.get(4).getValue(row), (int)parameters.get(5).getValue(row)}; 
		double[][] values = row.getRelativeValuesFor(getInnerAttribute().getTableIndex());
		double[] zValues = new double[values.length];
		NominalMapping nm = null;
		if( innerAttribute.getInnerAttributeAt(0).isNominal())
			nm = innerAttribute.getInnerAttributeAt(0).getMapping();
		for(int i =0;i<values.length;i++)
				zValues[i] = values[i][0];
		mv.setValues(zValues,origin,spacing,extent,nm);
		return mv;
	}

	@Override
	public int getParameterCount() {
		return 6;
	}
	
	@Override
	public List<Attribute> getParameterAttributes() {
		return parameters;
	}
	
	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		MapValue mv = (MapValue)value;
		//set origin parameters
		parameters.get(0).setValue(row, mv.getOrigin()[0]);
		parameters.get(1).setValue(row, mv.getOrigin()[1]);
		//set spacing parameters
		parameters.get(2).setValue(row, mv.getSpacing()[0]);
		parameters.get(3).setValue(row, mv.getSpacing()[1]);
		//set dimension parameters
		parameters.get(4).setValue(row, mv.getDimension()[0]);
		parameters.get(5).setValue(row, mv.getDimension()[1]);
		
		//set z values
		double[] values = mv.getZValues();
		//create 2d-double array
		double[][] rValues = new double[values.length][1];
		for(int i=0;i<rValues.length;i++){
			rValues[i][0] = values[i];
		}
		row.setRelationalValues(innerAttribute.getTableIndex(), rValues);
	}
	

}
