package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

public class MatrixAttributeDescription extends ComplexAttributeDescription{

	public MatrixAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
		
	}

	
	public void checkConstraints(ExampleTable et) {
		
		if(this.getAttributeIndexes().length != 1)
			throw new IllegalArgumentException("attribute " +this.getName() + " must have exactly one inner attribute ");
		for(int i=0;i<et.getNumberOfAttributes();i++)
			if(et.getAttribute(i).getTableIndex() == this.getAttributeIndexes()[0]) {
				if(!et.getAttribute(i).isRelational())
					throw new IllegalArgumentException("attribute " +this.getName() + "'s inner attribute must be relational ");
				break;
			}
		
		String[] pList = this.getHint().split(ComplexValueFactory.getParameterSep());
		if(pList.length != 2)
			throw new IllegalArgumentException("Hint of matrix attribute "+ this.getName() +" is not valid, must be 'rows_columns' ");
		else{
			try{
				Integer.parseInt(pList[0]);
				Integer.parseInt(pList[1]);
			}catch (NumberFormatException e){
				throw new IllegalArgumentException("Hint of matrix attribute "+ this.getName() +" is not valid, must be 'rows_columns' ");
			}
		}
	}

}
