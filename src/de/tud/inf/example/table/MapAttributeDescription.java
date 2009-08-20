package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class MapAttributeDescription extends GeometryAttributeDescription {

	public MapAttributeDescription(int[] attIds, int[] paramIds, String symbol,
			String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
	}

	public void checkConstraints(ExampleTable et) {
		super.checkConstraints(et);
		
		RelationalAttribute relA=null;
		for(int i=0;i<et.getNumberOfAttributes();i++)
			if(et.getAttribute(i).getTableIndex() == this.getAttributeIndexes()[0]) {
				relA = (RelationalAttribute)et.getAttribute(i);
				break;
			}
		//ensure that relational attribute has exactly one inner attribute (map values)
		if (relA.getInnerAttributeCount() != 1)
			throw new IllegalArgumentException("map attribute " +this.getName() + " must wrap a relational attribute with exactly one inner attribute");
		//ensure that there are six parameters: origin,dimension,stepSize of x and y dimensions
		if (this.getParamIndexes().length != 6)
			throw new IllegalArgumentException("map attribute " +this.getName() + " needs six parameter attributes");		
	}
	
}
