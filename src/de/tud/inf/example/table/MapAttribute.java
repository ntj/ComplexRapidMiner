package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
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
			RelationalAttribute innerAttribute, List<Attribute> parameters, String symbol, String hint) {
		super(name, valueType, innerAttribute, symbol, hint);
		this.parameters = parameters;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -8243720102763289432L;

	@Override
	public MapValue getComplexValue(DataRow row) {
		MapValue mv = (MapValue)ComplexValueFactory.getComplexValueFunction(1, this.getSymbol(), this.getHint());
		double[] origin  = {parameters.get(0).getValue(row), parameters.get(1).getValue(row)};
		double[] spacing = {parameters.get(2).getValue(row), parameters.get(3).getValue(row)}; 
		int[]    extent  = {(int)parameters.get(4).getValue(row), (int)parameters.get(5).getValue(row)};
		List<Attribute> aList = this.getInnerAttributes();
		double[][] values = row.getRelativeValuesFor(aList.get(0).getTableIndex());
		double[] zValues = new double[values.length];
		for(int i =0;i<values.length;i++)
			zValues[i] = values[i][0];
		mv.setValues(zValues,origin,spacing,extent);
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

}
