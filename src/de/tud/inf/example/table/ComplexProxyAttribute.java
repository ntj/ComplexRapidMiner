package de.tud.inf.example.table;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;




/**
 * this complex Attribute wraps just one inner relational attribute, which stores tuples of values
 * base class for all complex multi-instance attributes
 * 
 * @author Antje Gruner
 *
 */
public abstract class ComplexProxyAttribute extends ComplexAttribute {

	private static final long serialVersionUID = 5332926180780863779L;

	protected RelationalAttribute innerAttribute;
	
	ComplexProxyAttribute(String name, int valueType,RelationalAttribute innerAttribute, String symbol,String hint) {
		super(name, valueType,symbol,hint);
		this.innerAttribute = innerAttribute;
	}
	
	@Override
	public ComplexClassType getComplexClassType() {
		return ComplexClassType.Proxy;
	}
	
	@Override
	public void setValue(DataRow row, double value) {
		throw new UnsupportedOperationException();
	}
	
	public int getInnerAttributeCount(){
		return 1;
	}
	
	@Override
	public String checkConstraints(ExampleTable et, ComplexAttributeDescription cad){
		String messg = "";
		//1. just one attribute should be wrapped
		if(cad.getAttributeIndexes().length != 1) messg += "attribute " +cad.getName() + " must have exactly one inner attribute ";
		//2. this attribute must be relational
		RelationalAttribute relA = null;
		for(int i=0;i<et.getNumberOfAttributes();i++)
			if(et.getAttribute(i).getTableIndex() == cad.getAttributeIndexes()[0])
				if(!et.getAttribute(i).isRelational()) messg += "attribute " +cad.getName() + "'s inner attribute must be relational ";
				else relA = (RelationalAttribute)et.getAttribute(i);
		if(relA == null) messg+= "complex attribute " + cad.getName() + " must wrap one attribute which is relational";
		return messg;
	}

	@Override
	public List<Attribute> getParameterAttributes() {
		return null;
	}
	
	@Override
	public List<Attribute> getInnerAttributes() {
		ArrayList<Attribute> aList = new ArrayList<Attribute>();
		aList.add(innerAttribute);
		return aList;
	}
	
	@Override
	public double[] getComplexValueAsArray(DataRow row) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		
	}
	
	public double getValue(DataRow row){
		return innerAttribute.getValue(row);
	}
	
	
}
