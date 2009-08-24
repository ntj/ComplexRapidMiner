package de.tud.inf.operator.complexobjects;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.similarity.attributebased.uncertain.GaussProbabilityDensityFunction;
import com.rapidminer.operator.similarity.attributebased.uncertain.ProbabilityDensityFunction;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.set.attributevalues.ComplexValueFactory;
import de.tud.inf.example.set.attributevalues.ConstantArrayValue;
import de.tud.inf.example.set.attributevalues.MapValue;
import de.tud.inf.example.set.attributevalues.MatrixValue;
import de.tud.inf.example.set.attributevalues.TensorValue;
import de.tud.inf.example.table.ComplexAttribute;
import de.tud.inf.example.table.ConstantArrayAttribute;
import de.tud.inf.example.table.MapAttribute;
import de.tud.inf.example.table.UncertainAttribute;

public class ComplexValueTestOperator extends Operator{

	public ComplexValueTestOperator(OperatorDescription description) {
		super(description);
	}
	
	@Override
	public IOObject[] apply() throws OperatorException {
		System.out.println("\n\n--------------------------------------------");
		System.out.println("[ComplexValueTestOperator.apply() start]");
		System.out.println("--------------------------------------------");
		ComplexExampleSet es = getInput(ComplexExampleSet.class);
		Example e;
		MatrixValue mv;
		TensorValue tv;
		for(Attribute a: es.getAttributes()){
			System.out.println("--------------------------------------------");
			System.out.println("attribute name:   "+a.getName());
			if(a.isComplex())
				System.out.println("attribute hint:   "+((ComplexAttribute)a).getHint());
			for(int i =0;i< es.size();i++){
				System.out.println("\nrow " + i);
				e = es.getExample(i);
				if(a.isComplex()){
					if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.MATRIX)){
						mv = (MatrixValue)e.getComplexValue(a);
						System.out.print("Matrix: ");
						mv.print(5, 2);
					}
					else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.TENSOR)){
						tv = (TensorValue)e.getComplexValue(a);
						//System.out.println("Dimension of tensor: " + tv.getDimension());
						double[] key = new double[]{0,0,0};
						System.out.println("first tensor value: " + tv.getValueAt(key));		
					}
					//uniform specific stuff
					else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.UNIFORM)){
						ProbabilityDensityFunction sPdf = (ProbabilityDensityFunction)e.getComplexValue(a);
						System.out.println("Uncertainty of pdf at row "+i+": " +sPdf.getUncertainty());
					}
					//gauss specific stuff
					else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.GAUSS)){
						GaussProbabilityDensityFunction gPdf = (GaussProbabilityDensityFunction)e.getComplexValue(a);
						System.out.print("Covariance Matrix: ");
						gPdf.getCovarianceMatrix().print(4, 2);
					}
					//general uncertain stuff
					if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.UNCERTAIN)){
						ProbabilityDensityFunction pdf = (ProbabilityDensityFunction)e.getComplexValue(a);
						int dim = ((UncertainAttribute)a).getInnerAttributeCount();
						System.out.print("min values: ");
						for(int d=0;d<dim;d++){
							System.out.print(pdf.getMinValue(d) + "   ");
						}
						System.out.println("");
					}
				}
				else{ //simple value stuff
					if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.NUMERICAL))
						System.out.println("value: " + e.getValue(a));
					if(Ontology.ATTRIBUTE_VALUE_TYPE.isA(a.getValueType(),Ontology.NOMINAL))
						System.out.println("value: " + e.getNominalValue(a));
				}
			}
		}
		System.out.println("Test creating new Attributes");
		MapAttribute mapAtt =  (MapAttribute)AttributeFactory.createAttribute("my first complex attribute added from an operator",Ontology.MAP);
		double[] origin = new double[2];
		double[] spacing = new double[2];
		int[] dim = new int[2];
		dim[0] = 3;
		dim[1] = 3;	
		double[] values = new double[5];
		MapValue mValue = new MapValue(origin,spacing,dim,values);
		es.addComplexAttribute(mapAtt);
		for(int i=0;i<es.size();i++)
			es.getExample(i).setComplexValue(mapAtt,mValue);
		
		//create constant array attribute and set two constant array values
		if(es.size() >1){
			ConstantArrayAttribute arrayAttr =  (ConstantArrayAttribute)AttributeFactory.createAttribute("complex array attribute",Ontology.ARRAY,"2_2");
			es.addComplexAttribute(arrayAttr);
			ConstantArrayValue arrayValue = (ConstantArrayValue)ComplexValueFactory.getComplexValueFunction(arrayAttr);
			
			double[] vals = new double[]{1,2,3,4,5};
			arrayValue.setValues(vals);
			es.getExample(0).setComplexValue(arrayAttr, arrayValue);
			
			double[] vals2 = new double[]{4,6,7,9};
			arrayValue.setValues(vals2);
			es.getExample(1).setComplexValue(arrayAttr, arrayValue);
		}
		System.out.println("\n\n[ComplexValueTestOperator.apply() finished]");
		return new IOObject[] {es};
	}

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] {ComplexExampleSet.class};
	}

	@Override
	public Class<?>[] getOutputClasses() {
		return new Class[] {ExampleSet.class};
	}

}
