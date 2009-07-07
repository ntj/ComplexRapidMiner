package de.tud.inf.example.table;


import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.LinearKorrelation;


/**
 * Complex Attribute, which manages a list of inner attributes
 * @author Antje Gruner
 *
 */
public class ComplexCompositeAttribute extends ComplexAttribute {
	
	private static final long serialVersionUID = 1L;
	protected List<Attribute> innerAttributes = null;
	protected List<Attribute> parameters = null;
	
	
	public ComplexCompositeAttribute(ComplexCompositeAttribute attribute) {
		
		this(attribute.getName(),
			 attribute.getValueType(),
			 attribute.innerAttributes,
			 attribute.parameters,
			 attribute.symbol,
			 attribute.hint);
	}
	

	public ComplexCompositeAttribute(String name, int valueType, List<Attribute> innerAtts, List<Attribute>params,String symbol,String hint) {
		super(name, valueType,symbol,hint);
		if(innerAtts !=null){
			innerAttributes = new ArrayList<Attribute>();
			for(int i =0;i<innerAtts.size();i++)
				innerAttributes.add(innerAtts.get(i));
		}
		if(params !=null){
			parameters = new ArrayList<Attribute>();
			for(int i =0;i<params.size();i++)
				parameters.add(params.get(i));
		}
	}
	
	
	@Override
	public double getValue(DataRow row) {
		return this.getComplexValue(row).getDoubleValue();
	}
	
	
	public  ComplexValue getComplexValue(DataRow row){
		LinearKorrelation lk = (LinearKorrelation)ComplexValueFactory.getComplexValueFunction(symbol,hint);
		double[] values = new double[innerAttributes.size()];
		for(int i = 0; i< innerAttributes.size();i++)
			values[i] = innerAttributes.get(i).getValue(row);
		lk.setValues(values);
		return lk;
	}
	
	public List<Attribute> getInnerAttributes(){
		return innerAttributes;	
	}
	
	public List<Attribute> getParameterAttributes(){
		return parameters;	
	}
	
	
	public Attribute getInnerAttribute(String attributeName){
		for(Attribute a:this.innerAttributes)
			if(a.getName().equals(attributeName)) return a;
		return null;
	}
	
	@Override
	public ComplexClassType getComplexClassType() {
		return ComplexClassType.Composite;
	}
	
	/**
	 * dummy function
	 */
	@Override
	public void setValue(DataRow row, double value) {
		for(Attribute a: this.innerAttributes)
			a.setValue(row, value);
	}

	@Override
	public int getInnerAttributeCount(){
		return innerAttributes.size();
		
	}

	@Override
	public int getParameterCount() {
		return parameters.size();
	}


	@Override
	public String checkConstraints(ExampleTable et, ComplexAttributeDescription cad) {
		String messg = "";
		if(cad.getAttributeIndexes() == null || cad.getAttributeIndexes().length == 0)
			messg +=  "attribute " +cad.getName()+ " must contain at least one inner attribute";
		return messg;
	}


	@Override
	public double[] getComplexValueAsArray(DataRow row) {
		double[] values = new double[innerAttributes.size()];
		for(int i =0;i<innerAttributes.size();i++){
			values[i] = innerAttributes.get(i).getValue(row);
		}
		return values;
	}


	@Override
	public String getAsString(ComplexValue value, int digits,
			boolean quoteWhitespace) {
		return value.getStringRepresentation(digits,quoteWhitespace);
	}


	

}
