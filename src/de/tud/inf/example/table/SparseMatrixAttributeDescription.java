package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.Ontology;

public class SparseMatrixAttributeDescription extends MatrixAttributeDescription{

	public SparseMatrixAttributeDescription(int[] attIds, int[] paramIds,
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
		
		//sparse matrix inner relational attributes 
		if (relA.getInnerAttributeCount() != 2)
			throw new IllegalArgumentException("sparse matrix attribute " +this.getName() + " must wrap relational attribute with exactly two inner attributes");
		else if(!Ontology.ATTRIBUTE_VALUE_TYPE.isA(relA.getInnerAttributeAt(0).getValueType(),Ontology.NUMERICAL)) 
			throw new IllegalArgumentException("sparse matrix attribute " +this.getName() + " must wrap relational attribute which inner first attribute serves as key and therefore must be numerical");
	}
}
