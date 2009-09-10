package de.tud.inf.operator.io;

import java.io.PrintWriter;
import java.util.List;
import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.io.ArffExampleSetWriter;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.table.ComplexAttribute;
import de.tud.inf.example.table.RelationalAttribute;
/**
 * writes ComplexExampleSet to a file with extended ARFF syntax
 * @author Antje Gruner
 *
 */
public class ComplexArffExampleSetWriter extends ArffExampleSetWriter{

	protected static final Class[] INPUT_CLASSES = { ComplexExampleSet.class };

	protected static final Class[] OUTPUT_CLASSES = { ComplexExampleSet.class };
	public ComplexArffExampleSetWriter(OperatorDescription description) {
		super(description);
	}

	@Override
	protected void printExampleSet(ExampleSet eSet,PrintWriter out) throws OperatorException{
			ComplexExampleSet exampleSet = (ComplexExampleSet)eSet;
			//1.1 print dependency section		
	        out.println("@DATATABLE RapidMinerComplexData");
	        out.println();
	        out.println(ComplexArffDescription.depAnnotation + " dependency");
	        out.println();
	        //class attribute must be nominal and here possible nominal values are missing
	        out.println("@ATTRIBUTE " + ComplexArffDescription.depAttName + " STRING");
	        out.println("@ATTRIBUTE " + ComplexArffDescription.depClassName + " {uniform, matrix}");
	        //print possible symbols
	        
	        Attribute[] atts = exampleSet.getExampleTable().getParentTable().getAttributes();
	        
	        out.println("@ATTRIBUTE " + ComplexArffDescription.depInnerAttributesName + " RELATIONAL");
	        	out.print("   @ATTRIBUTE " + "a  {");
	        	if(atts.length>0){
	        		out.print(atts[0].getName());
	        		for(int i =1;i< atts.length;i++)
	        			out.print(", " + atts[i].getName());
	        	}
	        	out.println("}");
	        out.println("@END " + ComplexArffDescription.depInnerAttributesName);
	        
	        out.println("@ATTRIBUTE " + ComplexArffDescription.depParamName + " RELATIONAL ");
	        out.print("   @ATTRIBUTE " + "p  {");
        	if(atts.length>0){
        		out.print(atts[0].getName());
        		for(int i =1;i< atts.length;i++)
        			out.print(", " + atts[i].getName());
        	}
        	out.println("}");
	        out.println("@END " + ComplexArffDescription.depParamName);
	        
	        out.println("@ATTRIBUTE " + ComplexArffDescription.depHintName + " STRING");
	        out.println();
	        
	        //1.2 print stuff from dependency list of Cet
	        out.println("@DATA");
	        for(Attribute a: exampleSet.getAttributes()){
	        	if(a.isComplex()){
	        		//a new DATA instance of dependency section begins
	        		//name
	        		out.print("\"" + a.getName() + "\"" +", ");
	        		//symbol
	        		out.print(Ontology.VALUE_TYPE_NAMES[a.getValueType()]+", ");
	        		//inner attributes
	        		List<Attribute> innerAs = ((ComplexAttribute)a).getInnerAttributes();
	        		out.print("'");
	        		if(innerAs.size() == 0)
	        			out.print("?");
	        		else{
		        		for(int i =0;i<innerAs.size()-1;i++)
		        			out.print(innerAs.get(i).getName() + "\\n");
		        		out.print(innerAs.get(innerAs.size()-1).getName());
	        		}
	        		out.print("','");
	        		//parameter attributes
	        		innerAs = ((ComplexAttribute)a).getParameterAttributes();
	        		if((innerAs == null) || innerAs.size() == 0)
	        			out.print("?");
	        		else{
		        		for(int i =0;i<innerAs.size()-1;i++)
		        			out.print(innerAs.get(i).getName() + "\\n");
		        		out.print(innerAs.get(innerAs.size()-1).getName());
	        		}
	        		out.print("',");
	        		//hint attribute
	        		String hint = ((ComplexAttribute)a).getHint(); 
	        		if((hint == "") || (hint == null)) out.print("?");
	        		else out.print( "\"" + ((ComplexAttribute)a).getHint() + "\"") ;
	        	}
	        	out.println();
	        }
	        	
	        
			//2. create simple exampleSet and print it
			out.println();
	        super.printExampleSet(exampleSet.getExampleTable().getParentTable().createExampleSet(),out);
			out.close();
	}
	
	
	@Override
	protected void printRelationalAttribute(RelationalAttribute attribute, PrintWriter out) throws OperatorException{
		out.println("relational");
		List<Attribute> innerAtts = attribute.getInnerAttributes();
		for (Attribute a: innerAtts){
			out.print("   ");
			printAttributeData(a,out);
		}
		out.println("@end '" + attribute.getName() + "'" );
	}
	
	/**
	 * print instances of relational attribute
	 * @param attribute
	 * @param out
	 * @throws OperatorException
	 */
	@Override
	protected void printRelationalData(RelationalAttribute attribute,Example example,PrintWriter out) throws OperatorException{
    	//should somehow work with print inner attribute instance data but not possible, since instances of relational attribute are not mapped to inner examples
		boolean firstInstance = true; //to separate instances with "\n"
		boolean firstValue = true;    //to separate inner instance values with ","
		double[][] values = example.getRelativeValue(attribute);
		List<Attribute> innerAtts = attribute.getInnerAttributes();
		out.print("'");
		for(int j=0;j<values.length;j++){
			if(!firstInstance) out.print("\\n");
			firstValue = true;
			for(int i=0;i<innerAtts.size();i++){
				if(!firstValue) out.print(",");
				if(innerAtts.get(i).isNominal())
					out.print(innerAtts.get(i).getMapping().mapIndex((int)values[j][i]));
				else out.print(values[j][i]);
				firstValue = false;
			}
			firstInstance = false;
		}
		out.print("'");
    }
	

}
