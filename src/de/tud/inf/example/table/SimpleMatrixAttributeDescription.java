package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class SimpleMatrixAttributeDescription extends MatrixAttributeDescription{

	public SimpleMatrixAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
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
		
		if (relA.getInnerAttributeCount() != 1)
			throw new IllegalArgumentException("matrix attribute " +this.getName() + " must wrap a relational attribute with exactly one inner attribute");
				
	}
}
