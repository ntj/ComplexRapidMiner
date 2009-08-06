package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class PDFAttributeDescription extends ComplexAttributeDescription{

	public PDFAttributeDescription(int[] attIds, int[] paramIds, String symbol,
			String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
		
	}

	
	public void checkConstraints(ExampleTable et) {
		
		super.checkConstraints(et);
		
		
	}
	
	

}
