package de.tud.inf.example.table;

import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AbstractAttribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.NominalMapping;

public class RelationalAttribute extends AbstractAttribute{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2873557998011692767L;
	
	private List<Attribute> innerAttributes = new LinkedList<Attribute>();
	
	RelationalAttribute(RelationalAttribute attribute) {
		super(attribute);
	}

	public RelationalAttribute(String name, int valueType) {
		super(name, valueType);
	}

	@Override
	public Object clone() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAsString(double value, int digits, boolean quoteWhitespace) {
		return "NA";
	}

	public NominalMapping getMapping() {
		return null;
	}

	public boolean isComplex() {
		return false;
	}

	public boolean isNominal() {
		return false;
	}

	public boolean isNumerical() {
		return false;
	}

	public boolean isRelational() {
		return true;
	}

	public void setInnerAttributes(List<Attribute> innerAttributes){
		this.innerAttributes = innerAttributes;
	}
	
	public Attribute getInnerAttributeAt(int id){
		return innerAttributes.get(id);
	}
	
	public List<Attribute> getInnerAttributes(){
		return innerAttributes;
	}
	
	public int getInnerAttributeCount(){
		return innerAttributes.size();
	}
	
	/**
	 * creates one tuple instance with values = value and stores it in relationalValueMap of this DataRow
	 */
	public void setValue(DataRow row, double value) {
		double[][] values = new double[1][this.innerAttributes.size()];
		for(int i =0;i<values.length;i++)
			values[0][i] = value;
		row.setRelationalValues(getTableIndex(), values);
	}
	
	/**
	 * this method is never used at the moment
	 * @param row
	 * @return
	 */
	public double[][] getRelationalValue(DataRow row){
		return row.getRelativeValuesFor(this.getTableIndex());
	}
	
	/**
	 * 
	 * @param row
	 * @return
	 */
	public void setRelationalValue(DataRow row, double[][] values){
		row.setRelationalValues(getTableIndex(),values);
	}

	
	public void setMapping(NominalMapping nominalMapping) {
		throw new UnsupportedOperationException();	
	}
	
	public void addInnerAttribute(Attribute att){
		this.innerAttributes.add(att);
	}
	
	
}
