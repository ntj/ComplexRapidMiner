package de.tud.inf.operator.meta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.AttributeRole;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.ExampleTable;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.condition.AllInnerOperatorCondition;
import com.rapidminer.operator.condition.InnerOperatorCondition;
import com.rapidminer.parameter.ParameterType;

import de.tud.inf.example.set.UpdateableExampleSet;

public class ExampleSetGrower extends OperatorChain {
	IOContainer innerResult = null;
	// IOObject innerExampleSet = null;
	
	public ExampleSetGrower(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		// get the input
		ExampleSet completeExampleSet = getInput(ExampleSet.class);
		// copy the input
		ExampleSet completeExampleSetCopy = (ExampleSet) completeExampleSet.copy();
		// get the example table
		ExampleTable completeExampleTable = completeExampleSetCopy.getExampleTable();
		// create the mapping file for the new example set 
		Map<Attribute, String> specialAttributes = new HashMap<Attribute, String>();
		// get the info on the special attributes in the passed example set
		Iterator<AttributeRole> attribRoles = completeExampleSet.getAttributes().specialAttributes();
		// build the map 
		while(attribRoles.hasNext()) {
			AttributeRole curr = attribRoles.next();
			specialAttributes.put(curr.getAttribute(), curr.getSpecialName());
		}
		// iterator over all attributes of the example set
		Iterator<Attribute> allAttribs = completeExampleSetCopy.getAttributes().allAttributes();
		// list of all attributes of the example set
		List<Attribute> regularAttributes = new ArrayList<Attribute>();
		while (allAttribs.hasNext()) {
			regularAttributes.add(allAttribs.next());
		}
		
		UpdateableExampleSet incrementalExampleSet = 
			new UpdateableExampleSet(completeExampleTable, regularAttributes, specialAttributes);
		
		incrementalExampleSet.clear();
		innerResult = new IOContainer(new IOObject[0]);
		Iterator<Example> iter = completeExampleSet.iterator();
		while (iter.hasNext()) {
			Example example = iter.next();
			incrementalExampleSet.addExample(example);
			
			IOContainer input = new IOContainer(new IOObject[] { (IOObject) incrementalExampleSet});
			IOContainer output = getOperator(0).apply(input); 
			
			//innerResult = innerResult.append(output.getIOObjects());
			
			innerResult = new IOContainer(output.getIOObjects());
			// innerExampleSet = (IOObject) incrementalExampleSet;
			inApplyLoop();
		}
		
		return innerResult.getIOObjects();
	}
	
	@Override
	protected IOContainer getIOContainerForInApplyLoopBreakpoint() {
		return innerResult;
	}

	@Override
	public InnerOperatorCondition getInnerOperatorCondition() {
		return new AllInnerOperatorCondition(new Class[] { ExampleSet.class }, new Class[0]);
	}

	@Override
	public int getMaxNumberOfInnerOperators() {
		return 1;
	}

	@Override
	public int getMinNumberOfInnerOperators() {
		return 1;
	}

	@Override
	public Class<?>[] getOutputClasses() {
		/* return new Class[] { Model.class }; */
		return new Class[0];
	}

	@Override
	public Class<?>[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	@Override
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();

		return types;
	}
}
