package de.tud.inf.example.table;


import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;

import de.tud.inf.example.set.attributevalues.ComplexValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.LinearKorrelation;


/**
 * Complex Attribute, which manages a list of inner attributes without any parameter attributes
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
			 attribute.hint);
	}
	

	public ComplexCompositeAttribute(String name, int valueType, List<Attribute> innerAtts, List<Attribute>params, String hint) {
		super(name, valueType,hint);
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
		LinearKorrelation lk = (LinearKorrelation)ComplexValueFactory.getComplexValueFunction(this.getValueType(),hint);
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
	public double[] getComplexValueAsArray(DataRow row) {
		double[] values = new double[innerAttributes.size()];
		for(int i =0;i<innerAttributes.size();i++){
			values[i] = innerAttributes.get(i).getValue(row);
		}
		return values;
	}


	@Override
	public void setComplexValue(DataRow row, ComplexValue value) {
		throw new UnsupportedOperationException();
	}


	@Override
	public Object clone() {
		return new ComplexCompositeAttribute(this);
	}

	
}
