package de.tud.inf.example.table;

import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.similarity.attributebased.uncertain.GaussProbabilityDensityFunction;

import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

/**
 * 
 * @author Antje Gruner
 *
 */
public class GaussAttribute extends UncertainAttribute {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8515028651795348008L;
	
	public GaussAttribute(String name, int valueType,
			List<Attribute> innerAttributes, List<Attribute> parameters,
			String symbol, String hint) {
		super(name, valueType, innerAttributes, parameters, symbol, hint);
	}

	@Override
	public GaussProbabilityDensityFunction getComplexValue(DataRow row){
		GaussProbabilityDensityFunction pdf = (GaussProbabilityDensityFunction)ComplexValueFactory.getComplexValueFunction(innerAttributes.size(), symbol,hint);
		pdf.setCovarianceMatrix(row.getRelativeValuesFor(this.parameters.get(0).getTableIndex()));
		setValues(pdf,row);
		return pdf;
	}

	@Override
	public String checkConstraints(ExampleTable et,
			ComplexAttributeDescription cad) {
		String messg = super.checkConstraints(et, cad);
		//1. gauss expects as parameters! one relational attribute
		if(cad.getParamIndexes().length != 1)
			messg += "gauss attribute must have exactly one relational parameter attribute, which stores values of variance matrix ";
		else{
			//1.2 check if parameter attribute is relational
			int pId = cad.getParamIndexes()[0];
			for(int i=0;i<et.getNumberOfAttributes();i++)
				if(et.getAttribute(i).getTableIndex() == pId)
					if(!et.getAttribute(i).isRelational()) messg +=	"gauss attribute " +cad.getName() + 
																	"'s parameter attribute must be relational ";
					else { 
						RelationalAttribute relA = (RelationalAttribute)et.getAttribute(i);
						//test if relational parameter attribute can serve as variance matrix attribute
						if(relA.getInnerAttributeCount() == 0  || relA.getInnerAttributeCount() > 2)
							messg += "gauss attribute " +cad.getName() + 	"'s parameter attribute must have one or two inner attributes, " +
									"										which store values of covariance matrix ";
						else if ((relA.getInnerAttributeCount() == 2) && !relA.getInnerAttributeAt(0).isNumerical())
							messg += "gauss attribute " +cad.getName() + 	"'s parameter attribute first inner attribute serves as key for matrix" +
																			"entries and therefore must be numerical ";
					}
		}
		return messg;
	}
	
	

}
