package com.rapidminer.operator.similarity;

import java.util.List;

import com.rapidminer.example.ExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.math.similarity.DistanceMeasure;
import com.rapidminer.tools.math.similarity.DistanceMeasures;

public class ExampleSet2SimilarityExampleSet extends Operator {

	public ExampleSet2SimilarityExampleSet(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		DistanceMeasure measure = DistanceMeasures.createMeasure(this, exampleSet);
		return new IOObject[] { new SimilarityExampleSet(exampleSet, measure) };
		
		/*
		Attribute idAttribute = exampleSet.getAttributes().getId();
		if (idAttribute == null)
			throw new UserError(this, 129);
		
		
		// constructing new memory example table
		List<Attribute> attributes = new ArrayList<Attribute>(3);
		Attribute firstIdAttribute;
		Attribute secondIdAttribute;
		if (idAttribute.isNominal()) {
			firstIdAttribute = AttributeFactory.createAttribute("FIRST_ID", Ontology.NOMINAL);
			secondIdAttribute = AttributeFactory.createAttribute("SECOND_ID", Ontology.NOMINAL);
		} else {
			firstIdAttribute = AttributeFactory.createAttribute("FIRST_ID", Ontology.NUMERICAL);
			secondIdAttribute = AttributeFactory.createAttribute("SECOND_ID", Ontology.NUMERICAL);
		}
		attributes.add(firstIdAttribute);
		attributes.add(secondIdAttribute);
		String name = "SIMILARITY";
		if (measure.isDistance()) {
			name = "DISTANCE";
		}
		Attribute similarityAttribute = AttributeFactory.createAttribute(name, Ontology.REAL);
		attributes.add(similarityAttribute);
		
		MemoryExampleTable table = new MemoryExampleTable(attributes);

		// copying mapping of original id attribute
		if (idAttribute.isNominal()) {
			NominalMapping mapping = idAttribute.getMapping();
			firstIdAttribute.setMapping(mapping);
			secondIdAttribute.setMapping(mapping);
		}
		
		// creating new datarows with ids and measure value
		boolean isDistance = measure.isDistance();
		for (int i = 0; i < exampleSet.size(); i++) {
			Example firstExample = exampleSet.getExample(i);
			for (int j = i + 1; j < exampleSet.size(); j++) {
				double[] data = new double[3];
				Example secondExample = exampleSet.getExample(j);
				data[0] = firstExample.getValue(idAttribute);
				data[1] = secondExample.getValue(idAttribute);

				if (isDistance)
					data[2] = measure.calculateDistance(firstExample, secondExample);
				else
					data[2] = measure.calculateSimilarity(firstExample, secondExample);
				
				table.addDataRow(new DoubleArrayDataRow(data));			
			}
		}
		
		return new IOObject[] { table.createExampleSet() };
		*/
	}

	public Class<?>[] getInputClasses() {
		return new Class<?>[] {ExampleSet.class};
	}

	public Class<?>[] getOutputClasses() {
		return new Class<?>[] {ExampleSet.class};
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.addAll(DistanceMeasures.getParameterTypes(this));
		return types;
	}
}
