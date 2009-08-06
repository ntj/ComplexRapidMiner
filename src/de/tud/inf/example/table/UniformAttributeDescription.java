package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class UniformAttributeDescription extends PDFAttributeDescription{

	public UniformAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
		
	}

	@Override
	public void checkConstraints(ExampleTable et) {
		
		super.checkConstraints(et);
		
		if(this.getParamIndexes().length >1)
			throw new IllegalArgumentException("uncertain value with uniform pdf expects exactly one parameter (uncertainty)");
	}
}
