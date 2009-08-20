package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class PointListAttributeDescription extends GeometryAttributeDescription {

	public PointListAttributeDescription(int[] attIds, int[] paramIds,
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
		//ensure that relational attribute has exactly three inner attributes (x,y,z - coordinates of points)
		if (relA.getInnerAttributeCount() != 3)
			throw new IllegalArgumentException("point list attribute " +this.getName() + " must wrap a relational attribute with exactly three inner attributes");
	}
	

}
