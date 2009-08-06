package de.tud.inf.example.table;

import com.rapidminer.example.table.ExampleTable;

public class GaussAttributeDescription extends PDFAttributeDescription{

	public GaussAttributeDescription(int[] attIds, int[] paramIds,
			String symbol, String name, String hint) {
		super(attIds, paramIds, symbol, name, hint);
		
	}

	@Override
	public void checkConstraints(ExampleTable et) {
		
		super.checkConstraints(et);
		
		if(this.getParamIndexes().length != 1)
			throw new IllegalArgumentException("gauss attribute must have one relational parameter attribute, which stores values of variance matrix");
		else{
			//1.2 check if parameter attribute is relational
			int pId = this.getParamIndexes()[0];
			for(int i=0;i<et.getNumberOfAttributes();i++)
				if(et.getAttribute(i).getTableIndex() == pId)
					if(!et.getAttribute(i).isRelational()) 
						throw new IllegalArgumentException("gauss attribute " +this.getName() + "'s parameter attribute must be relational");
					else { 
						RelationalAttribute relA = (RelationalAttribute)et.getAttribute(i);
						//test if relational parameter attribute can serve as variance matrix attribute
						if(relA.getInnerAttributeCount() == 0  || relA.getInnerAttributeCount() > 2)
							throw new IllegalArgumentException("gauss attribute " +this.getName() + "'s parameter attribute must have one or two inner attributes, which store values of covariance matrix");
						else if (!relA.getInnerAttributeAt(0).isNumerical())
							throw new IllegalArgumentException("gauss attribute " +this.getName() + "'s parameter attribute first inner attribute serves as key for matrix entries and therefore must be numerical");
						break;
					}
		}
	}
	
	

}
