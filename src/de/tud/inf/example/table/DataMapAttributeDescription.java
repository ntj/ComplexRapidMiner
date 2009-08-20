package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class DataMapAttributeDescription extends GeometryAttributeDescription {

	public DataMapAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
	}

	@Override
	public void checkConstraints(ExampleTable et) {
		super.checkConstraints(et);
		
		RelationalAttribute relA=null;
		for(int i=0;i<et.getNumberOfAttributes();i++)
			if(et.getAttribute(i).getTableIndex() == this.getAttributeIndexes()[0]) {
				relA = (RelationalAttribute)et.getAttribute(i);
				break;
			}
		
		//ensure that relational attribute has exactly two inner attributes (stores key,value - pairs of this map)
		if (relA.getInnerAttributeCount() != 2)
			throw new IllegalArgumentException("data_map attribute " +this.getName() + " must wrap a relational attribute with exactly two inner attributes");
	}

	
	
}
