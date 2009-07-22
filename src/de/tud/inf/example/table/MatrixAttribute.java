package de.tud.inf.example.table;

import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.attributevalues.AbstractMatrixValue;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;

public class MatrixAttribute extends ComplexProxyAttribute{

	private static final long serialVersionUID = 493080292487472506L;

	public MatrixAttribute(String name, int valueType,
			RelationalAttribute innerAttribute, String hint) {
		super(name, valueType, innerAttribute, hint);
	}

	@Override
	public AbstractMatrixValue getComplexValue(DataRow row) {
		//all matrices are instantiated with one relational attribute, which can have different number of inner attributes,
		//but checking in complexArffChecker
		double[][]  values = row.getRelativeValuesFor(this.innerAttribute.getTableIndex());
		AbstractMatrixValue m = (AbstractMatrixValue)ComplexValueFactory.getComplexValueFunction(getValueType(),this.hint);
		if(m != null)
			m.setValues(values);
		return m;
	}

	@Override
	public Object clone() {
		return null;
	}

	@Override
	public int getParameterCount() {
		return 0;
	}

	@Override
	public String checkConstraints(ExampleTable et, ComplexAttributeDescription cad) {
		String messg = super.checkConstraints(et, cad);
		String[] pList = cad.getHint().split(ComplexValueFactory.getParameterSep());
		if(pList.length != 2)
			messg += "Hint of matrix attribute "+ cad.getName() +" is not valid, must be 'rows_columns'. ";
		else{
			try{
				Integer.parseInt(pList[0]);
				Integer.parseInt(pList[1]);
			}catch (NumberFormatException e){
				messg += messg += "Hint of matrix attribute "+ cad.getName() +" is not valid, must be 'rows_columns'. ";
			}
		}
		RelationalAttribute relA = null;
		for(int i=0;i<et.getNumberOfAttributes();i++)
			if(et.getAttribute(i).getTableIndex() == cad.getAttributeIndexes()[0])
				if(!et.getAttribute(i).isRelational()) messg += "attribute " +cad.getName() + "'s inner attribute must be relational. ";
				else relA = (RelationalAttribute)et.getAttribute(i);
		if(relA != null){
			if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(getValueType(), Ontology.SIMPLE_MATRIX)){
				if (relA.getInnerAttributeCount() != 1)
					messg += "matrix attribute " +cad.getName() + " must wrap a relational attribute with exactly one inner attribute. ";
			}
			else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(getValueType(), Ontology.SPARSE_MATRIX)){
				//sparse matrix inner relational attributes 
				if (relA.getInnerAttributeCount() != 2)
					messg += "sparse matrix attribute " +cad.getName() + " must wrap relational attribute with exactly two inner attributes. ";
				else if(!Ontology.ATTRIBUTE_VALUE_TYPE.isA(relA.getInnerAttributeAt(0).getValueType(),Ontology.NUMERICAL)) 
					messg += "sparse matrix attribute " +cad.getName() + " must wrap relational attribute which inner first attribute serves as key and therefore must be numerical. ";
			}
			else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(getValueType(), Ontology.SPARSE_BINARY_MATRIX)){
				if (relA.getInnerAttributeCount() != 1)
					messg += "sparse matrix attribute " +cad.getName() + " must wrap relational attribute with exactly one inner attribute. ";
				else if(!Ontology.ATTRIBUTE_VALUE_TYPE.isA(relA.getInnerAttributeAt(0).getValueType(),Ontology.NUMERICAL)) 
					messg += "sparse matrix attribute " +cad.getName() + " must wrap a relational attribute which inner attribute serves as key and therefore must be numerical. ";
			}
		}
		return messg;
	}
}
