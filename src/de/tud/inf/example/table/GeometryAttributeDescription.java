package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

/**
 * base description class for all complex attributes, which consists of multi-instance values, e.g. matrices, maps, trees etc.
 * 
 * @author Antje Gruner
 *
 */
public abstract class GeometryAttributeDescription extends
		ComplexAttributeDescription {

	public GeometryAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
	}
	
	
public void checkConstraints(ExampleTable et) {
	super.checkConstraints(et);
	//ensure that there is exactly one inner attribute, which is relational
	if(this.getAttributeIndexes().length != 1)
		throw new IllegalArgumentException("attribute " +this.getName() + " must have exactly one inner attribute ");
	for(int i=0;i<et.getNumberOfAttributes();i++)
		if(et.getAttribute(i).getTableIndex() == this.getAttributeIndexes()[0]) {
			if(!et.getAttribute(i).isRelational())
				throw new IllegalArgumentException("attribute " +this.getName() + "'s inner attribute must be relational ");
			break;
		}
	}

}
