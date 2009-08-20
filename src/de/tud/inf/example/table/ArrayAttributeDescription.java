package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class ArrayAttributeDescription extends GeometryAttributeDescription {

	public ArrayAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void checkConstraints(ExampleTable et) {
		super.checkConstraints(et);
		//
	}

}
