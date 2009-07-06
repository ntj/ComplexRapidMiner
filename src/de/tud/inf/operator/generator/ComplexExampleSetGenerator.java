package de.tud.inf.operator.generator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.example.table.ListDataRowReader;
import com.rapidminer.example.table.MemoryExampleTable;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.generator.ExampleSetGenerator;
import com.rapidminer.operator.generator.TargetFunction;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.parameter.ParameterTypeStringCategory;
import com.rapidminer.parameter.UndefinedParameterError;
import com.rapidminer.tools.Ontology;

import de.tud.inf.example.set.ComplexExampleSet;
import de.tud.inf.example.table.ComplexAttributeDescription;
import de.tud.inf.example.table.ComplexExampleTable;

/**
 * generates complex example set, provides separate definition of parameter attributes values (e.g. target function etc.), 
 * one can choose between three different example set types. They define, how many example set attributes are complex, and the number of their 
 * inner attributes
 * 
 
 *  
 * @author Antje Gruner
 *
 */
public class ComplexExampleSetGenerator extends ExampleSetGenerator{

	/** the parameter name for &quot;determine the number of complex attributes in this exampleSet&quot; */
	private static final String PARAMETER_COMPLEX_ATTRIBUTES_COUNT = "complex_attributes_count";

	/** The parameter name for &quot;determine the dimension of all inner attributes.&quot; */
	private static final String PARAMETER_INNER_ATTRIBUTE_COUNT = "inner_attribute_count";
	
	/** The parameter name for &quot;determine the type of all complex attributes&quot; */
	private static final String PARAMETER_COMPLEX_ATTRIBUTE_TYPE = "complex_attributes_type";
	
	/** implemented complex types */
	private static final String[] complexTypes =  new String[]{Ontology.VALUE_TYPE_NAMES[Ontology.UNIFORM],Ontology.VALUE_TYPE_NAMES[Ontology.COMPLEX_VALUE]};
	
	
	/** The parameter name for &quot;used value for performance tests &quot; */
	private static final String PARAMETER_PERFORMANCE_TYPE = "performance_test_type";
	
	private static final String[] perform_types = new String[]{"1_complexA_full_dimension","half_complexA_one_dim","all_complexA_one_dim"};
	
	/** The parameter name for &quot;Specifies the target function of this example set parameters&quot; */
	private static final String PARAMETER_PARAMATTS_TARGET_FUNCTION = "parameter_target_function";

	/** The parameter name for &quot;The minimum value for the parameter attributes.&quot; */
	private static final String PARAMETER_PARAMATTS_ATTRIBUTES_LOWER_BOUND = "parameter_attributes_lower_bound";

	/** The parameter name for &quot;The maximum value for the parameter attributes.&quot; */
	private static final String PARAMETER_PARAMATTS_ATTRIBUTES_UPPER_BOUND = "parameter_attributes_upper_bound";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global).&quot; */
	private static final String PARAMETER_PARAMATTS_LOCAL_RANDOM_SEED = "parameter_local_random_seed";

	
	private int dim;
	private int paramCount = 1; //default case: uniform
	private int cCount;

	
	public ComplexExampleSetGenerator(OperatorDescription description) throws UndefinedParameterError {
		super(description);
	}

	@Override
	public IOObject[] apply() throws OperatorException {
		//determine type of complex attributes and set appropriate parameters
		setParameters();
		//generate two example tables, one with attributes and one with parameters, and merge
		//1. create default table with label attribute
		ExampleTable defaultTable = createDefaultExampleTable(getParameterAsInt(PARAMETER_NUMBER_OF_ATTRIBUTES));
		
		if(this.paramCount > 0){
			//2. initialize and create parameter table
			double lower = getParameterAsDouble(PARAMETER_PARAMATTS_ATTRIBUTES_LOWER_BOUND);
			double upper = getParameterAsDouble(PARAMETER_PARAMATTS_ATTRIBUTES_UPPER_BOUND);
			String functionName = getParameterAsString(PARAMETER_PARAMATTS_TARGET_FUNCTION);
			int seed = getParameterAsInt(PARAMETER_PARAMATTS_LOCAL_RANDOM_SEED);
			
			TargetFunction function = initializeFunction(lower, upper, functionName, getParameterAsInt(PARAMETER_NUMBER_EXAMPLES), cCount*paramCount);
			ExampleTable paramTable = createExampleTable(	cCount*paramCount, 
															getParameterAsInt(PARAMETER_NUMBER_EXAMPLES),
															function,
															seed,
															false,
															"param"
															);
			//3. merge tables
			ExampleTable result = mergeTables(paramTable,defaultTable); //since defaultTable contains label attribute, label will be last attribute in list
			
			return new IOObject[] { createExampleSet(result, label,paramTable.getNumberOfAttributes()) };
		}
		else return new IOObject[] { createExampleSet(defaultTable, label,0) }; 
	}
	
	
	public ExampleTable mergeTables(ExampleTable t1,ExampleTable t2) throws UndefinedParameterError{
		//copy attributes
		MemoryExampleTable result = new MemoryExampleTable(Arrays.asList(t1.getAttributes()));
		for(int i =0;i< t2.getNumberOfAttributes();i++){
			Attribute a = AttributeFactory.createAttribute(t2.getAttribute(i));
			result.addAttribute(a);
		}
		List<DataRow> data = new LinkedList<DataRow>();
		
		//create new DataRows
		DataRowFactory factory = new DataRowFactory(getParameterAsInt(PARAMETER_DATAMANAGEMENT),'.');
		int newLength = t1.getNumberOfAttributes() + t2.getNumberOfAttributes();
		DataRow r1, r2;
		int t1attCount = t1.getNumberOfAttributes();
		for(int e =0;e<getParameterAsInt(PARAMETER_NUMBER_EXAMPLES);e++){
			r1 = t1.getDataRow(e);
			r2 = t2.getDataRow(e);
			DataRow row =  factory.create(newLength);
			for (int i = 0; i < newLength; i++){
				Attribute set = result.getAttribute(i);
				if(i < t1attCount){
					Attribute get = t1.getAttribute(i);
					if(set.getName().equals(get.getName()))
						row.set(result.getAttribute(i), r1.get(t1.getAttribute(i)));
					else{
						System.out.println("sth wrong");
					}
				}
				else{
					Attribute get = t2.getAttribute(i - t1attCount);
					if(set.getName().equals(get.getName()))
						row.set(result.getAttribute(i), r2.get(get));
					else{
						System.out.println("sth wrong");
					}
				}
			}
            row.trim();
			data.add(row);
		}
		// fill table with data
		result.readExamples(new ListDataRowReader(data.iterator()));
		return result;
	}	
	 
	public ComplexExampleSet createExampleSet(ExampleTable table, Attribute label, int nrParams) throws OperatorException{
		List<ComplexAttributeDescription> cadList = null;
		cadList = new ArrayList<ComplexAttributeDescription>();
		for(int i=0;i< cCount;i++){
			int[] attIds = new int[dim];
			int[] paramIds = new int[paramCount];
			for(int j=0; j<dim;j++){
				attIds[j] 		= table.getAttribute(nrParams + i*dim + j).getTableIndex(); //factor
			}
			for(int k=0;k<paramCount;k++)
				paramIds[k]  	= table.getAttribute(i*paramCount + k).getTableIndex(); //offset + factor
			
			ComplexAttributeDescription cad = new ComplexAttributeDescription(	attIds,
																			 	paramIds,
																			 	complexTypes[getParameterAsInt(PARAMETER_COMPLEX_ATTRIBUTE_TYPE)],
																			 	"complAtt_"+i,"");
			cadList.add(cad);
		}
		ComplexExampleTable cet = new ComplexExampleTable(table,cadList);
		return cet.createExampleSet(cet.getAttribute(cet.getAttributeCount()-1)); //the label attribute
		
	}
	

	public void setParameters() throws OperatorException{
		int nrAttributes = getParameterAsInt(PARAMETER_NUMBER_OF_ATTRIBUTES);
	
		switch(getParameterAsInt(PARAMETER_COMPLEX_ATTRIBUTE_TYPE)){
			case 0: // uniform
				paramCount = 1;
				break;
			case 1: //default complex value 
				paramCount = 0;
				break;
		}
		
		switch(getParameterAsInt(PARAMETER_PERFORMANCE_TYPE)){
			case 0: // one complex attribute with all attributes (except label) as inner attributes
				dim = nrAttributes;
				if(dim<=0)
					cCount = 0;
				else
					cCount = 1;
				break;
			case 1: //half the attributes are complex
				dim = 1;
				cCount = nrAttributes/2;
				break;
			case 2: //all attributes are complex
				dim = 1;
				cCount = nrAttributes;
				break;
			default:
				dim = getParameterAsInt(PARAMETER_INNER_ATTRIBUTE_COUNT);
				cCount = getParameterAsInt(PARAMETER_COMPLEX_ATTRIBUTES_COUNT);
				break;
		}
	}
	
	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type;
		type = new ParameterTypeInt(PARAMETER_COMPLEX_ATTRIBUTES_COUNT, "the number of complex attributes in this example set", 0, Integer.MAX_VALUE, 0);
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeInt(PARAMETER_INNER_ATTRIBUTE_COUNT, "the number of inner attributes of each complex attributes in this example set", 1, Integer.MAX_VALUE, 1); 
		type.setExpert(false);
		types.add(type);
		
		type = new ParameterTypeCategory(PARAMETER_PERFORMANCE_TYPE,"complex example set types of implemented performance test",perform_types,0);
		type.setExpert(true);
		types.add(type);
		
		types.add(new ParameterTypeCategory(PARAMETER_COMPLEX_ATTRIBUTE_TYPE, "determines the type of complex attributes",complexTypes,0));
		
		type = new ParameterTypeStringCategory(PARAMETER_PARAMATTS_TARGET_FUNCTION, "Specifies the target function of this example set", KNOWN_FUNCTION_NAMES);
		type.setExpert(false);
		types.add(type);
		
		types.add(new ParameterTypeDouble(PARAMETER_PARAMATTS_ATTRIBUTES_LOWER_BOUND, "The minimum value for the parameter attributes.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, -10));
		types.add(new ParameterTypeDouble(PARAMETER_PARAMATTS_ATTRIBUTES_UPPER_BOUND, "The maximum value for the parameter attributes.", Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 10));
        types.add(new ParameterTypeString(PARAMETER_PARAMATTS_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global).","-1"));
		return types;
	}
	
	
}
