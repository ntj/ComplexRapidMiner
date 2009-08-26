package de.tud.inf.example.table;

import java.util.ArrayList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;




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
	
	ComplexProxyAttribute(String name, int valueType,RelationalAttribute innerAttribute, String hint) {
		super(name, valueType,hint);
		this.innerAttribute = innerAttribute;
	}
	
	ComplexProxyAttribute(String name, int valueType, String hint) {
		super(name, valueType,hint);
		this.innerAttribute = null;
	}
	
	
	

	@Override
	public void setValue(DataRow row, double value) {
		throw new UnsupportedOperationException();
	}
	
	public int getInnerAttributeCount(){
		return 1;
	}
	
	

	@Override
	public List<Attribute> getParameterAttributes() {
		return null;
	}

	public Attribute getInnerAttribute() {
		return innerAttribute;
	}
	
	public List<Attribute> getInnerAttributes() {
		List<Attribute> list = new ArrayList<Attribute>();
		list.add(innerAttribute);
		return list;
	}
	
	@Override
	public double[] getComplexValueAsArray(DataRow row) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException();
		
	}
	
	/**
	 * returns a default value (e.g. average of matrix entries)
	 */
	public double getValue(DataRow row){
		return 0;
	}
		

}
