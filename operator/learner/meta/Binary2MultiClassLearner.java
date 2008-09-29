/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2008 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see http://www.gnu.org/licenses/.
 */
package com.rapidminer.operator.learner.meta;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.Model;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.learner.LearnerCapability;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;

/**
 * A metaclassifier for handling multi-class datasets with 2-class classifiers. This class
 * supports several strategies for multiclass classification including procedures which are 
 * capable of using error-correcting output codes for increased accuracy.  
 * 
 * @author Helge Homburg
 * @version $Id: Binary2MultiClassLearner.java,v 1.11 2008/05/09 19:22:46 ingomierswa Exp $
 */
public class Binary2MultiClassLearner extends AbstractMetaLearner {

	/** The parameter name for &quot;What strategy should be used for multi class classifications?&quot; */
	public static final String PARAMETER_CLASSIFICATION_STRATEGIES = "classification_strategies";

	/** The parameter name for &quot;A multiplier regulating the codeword length in random code modus.&quot; */
	public static final String PARAMETER_RANDOM_CODE_MULTIPLICATOR = "random_code_multiplicator";

	/** The parameter name for &quot;Use the given random seed instead of global random numbers (-1: use global)&quot; */
	public static final String PARAMETER_LOCAL_RANDOM_SEED = "local_random_seed";
	
	private static final String[] STRATEGIES = { "1 against all", "1 against 1", "exhaustive code (ECOC)", "random code (ECOC)" };
	
	private static final int ONE_AGAINST_ALL = 0;

	private static final int ONE_AGAINST_ONE = 1;
	
	private static final int EXHAUSTIVE_CODE = 2;
	
	private static final int RANDOM_CODE = 3;	
	
	/** This List stores a short description for the generated models. */
	private LinkedList<String> modelNames = new LinkedList<String>();
	
	/**
	 * A class which stores all necessary information to train a series of models
	 * according to a certain classification strategy.
	 */	 
	private static class CodePattern {
	
		String[][] data;
		boolean[][] partitionEnabled;		
		
		public CodePattern(int numberOfClasses, int numberOfFunctions) { 
			data = new String[numberOfClasses][numberOfFunctions];
			partitionEnabled = new boolean[numberOfClasses][numberOfFunctions];
			for (int i = 0; i < numberOfClasses; i++) {
				for (int j = 0; j < numberOfFunctions; j++) {
					partitionEnabled[i][j] = true; 
				}
			}		
		}
	}
	
	public Binary2MultiClassLearner(OperatorDescription description) {
		super(description);
	}		
		
	private SplittedExampleSet constructClassPartitionSet(ExampleSet inputSet){
		
		Attribute classLabel = inputSet.getAttributes().getLabel();
		int numberOfClasses = classLabel.getMapping().size();
		int[] examples = new int[inputSet.size()]; 
		Iterator<Example> exampleIterator = inputSet.iterator();
		int i = 0;
		while (exampleIterator.hasNext()) {
			Example e = exampleIterator.next();
			examples[i] = (int)e.getValue(classLabel);  
			i++;
		}			
		Partition separatedClasses = new Partition(examples, numberOfClasses);
		return new SplittedExampleSet((ExampleSet)inputSet.clone(), separatedClasses);		
	}
	
	/**
	 * Trains a series of models depending on the classification method specified by a
	 * certain code pattern.
	 */
	private Model[] applyCodePattern(SplittedExampleSet seSet, Attribute classLabel, CodePattern cP) throws OperatorException {		
		int numberOfClasses = classLabel.getMapping().size();
		int numberOfFunctions = cP.data[0].length;
		Model[] models = new Model[numberOfFunctions];
		
        // Hash maps are used for addressing particular class values using indices without relying 
		// upon a consistent index distribution of the corresponding substructure.
		HashMap<Integer, Integer>  classIndexMap = new HashMap<Integer, Integer> (numberOfClasses);		
		
		for (int currentFunction = 0; currentFunction < numberOfFunctions; currentFunction++) {
			// 1. Configure a split example set and add a temporary label.
			int counter = 0;			
			seSet.clearSelection();
			
			for (String currentClass : classLabel.getMapping().getValues()) {							
				classIndexMap.put(classLabel.getMapping().mapString(currentClass), counter);				
				if (cP.partitionEnabled[counter][currentFunction]) {
					seSet.selectAdditionalSubset(classLabel.getMapping().mapString(currentClass));
				}				
				counter++;
			}			
			Attribute workingLabel = AttributeFactory.createAttribute("multiclass_working_label", Ontology.BINOMINAL);
			seSet.getExampleTable().addAttribute(workingLabel);
			seSet.getAttributes().addRegular(workingLabel);
			int currentIndex = 0;
			
			Iterator<Example> iterator = seSet.iterator();
			while (iterator.hasNext()) {
				Example e = iterator.next();					
				currentIndex = classIndexMap.get((int)e.getValue(classLabel));
				
				if (cP.partitionEnabled[currentIndex][currentFunction]) {
					e.setValue(workingLabel, workingLabel.getMapping().mapString(cP.data[currentIndex][currentFunction]));					
				}
			}				
			seSet.getAttributes().remove(workingLabel);
			seSet.getAttributes().setLabel(workingLabel);
			
			// 2. Apply the example set to the inner learner.
			models[currentFunction] = applyInnerLearner(seSet);
	        inApplyLoop();	            
	        
	        // 3. Clean up for the next run.
	        seSet.getAttributes().setLabel(classLabel);
	        seSet.getExampleTable().removeAttribute(workingLabel);		
		}
		return models;		
	}
	
	/**
	 * Builds a code pattern according to the "1 against all" classification scheme.
	 */
	private CodePattern buildCodePattern_ONE_VS_ALL(Attribute classLabel) {
		int numberOfClasses = classLabel.getMapping().size();		
		CodePattern cP = new CodePattern(numberOfClasses, numberOfClasses); //, ONE_AGAINST_ALL); 
		Iterator<String> classIt = classLabel.getMapping().getValues().iterator();	
		modelNames.clear();
		
		for (int i = 0; i < numberOfClasses; i++) {
			for (int j = 0; j < numberOfClasses; j++) { 
				if (i == j) {
					String currentClass = classIt.next();
					modelNames.add(currentClass+" vs. all other");
					cP.data[i][j] = currentClass;
				} else {
					cP.data[i][j] = "all_other_classes";
				}
			}
		}
		return cP;
	}
	
	/**
	 * Builds a code pattern according to the "1 against 1" classification scheme.
	 */
	private CodePattern buildCodePattern_ONE_VS_ONE(Attribute classLabel) {
		int numberOfClasses = classLabel.getMapping().size();	
		int numberOfCombinations = (numberOfClasses * (numberOfClasses -1)) / 2;
		String[] classIndexMap = new String[numberOfClasses];
		CodePattern cP = new CodePattern(numberOfClasses, numberOfCombinations); //, ONE_AGAINST_ONE);
		modelNames.clear();
		
		for (int i = 0; i < numberOfClasses; i++) {
			for (int j = 0; j < numberOfCombinations; j++) {
				cP.partitionEnabled[i][j] = false;
			}
		}		
		int classIndex = 0;		
		
		for (String className : classLabel.getMapping().getValues()) {
			classIndexMap[classIndex] = className;
			classIndex++;
		}		
		int currentClassA = 0, currentClassB = 1;		
		for (int counter = 0; counter < numberOfCombinations; counter++) {
						
			if (currentClassB > (numberOfClasses - 1) ) {
				currentClassA++;
				currentClassB = currentClassA + 1;
			}
			if (currentClassA > (numberOfClasses - 2) ) {
				break;
			}									
			cP.partitionEnabled[currentClassA][counter] = true;
			cP.partitionEnabled[currentClassB][counter] = true;
			String currentClassNameA = classIndexMap[currentClassA];
			String currentClassNameB = classIndexMap[currentClassB];
			cP.data[currentClassA][counter] = currentClassNameA;
			cP.data[currentClassB][counter] = currentClassNameB;			
			
			modelNames.add(currentClassNameA+" vs. "+currentClassNameB);
			
			currentClassB++;			
		}
		return cP;
	}
	
	/**
	 * Builds a code pattern according to the "exhaustive code" classification scheme.
	 */
	private CodePattern buildCodePattern_EXHAUSTIVE_CODE(Attribute classLabel) {
		int numberOfClasses = classLabel.getMapping().size();	
		int numberOfFunctions = (int)Math.pow(2, numberOfClasses - 1) - 1;		
		CodePattern cP = new CodePattern(numberOfClasses, numberOfFunctions); //, EXHAUSTIVE_CODE);
		
		for (int i = 0; i < numberOfFunctions; i++) {			
			cP.data[0][i] = "true";
		}
		for (int i = 1; i < numberOfClasses; i++) {
			int currentStep = (int)Math.pow(2, numberOfClasses - (i + 1));
			for (int j = 0; j < numberOfFunctions; j++) {
				cP.data[i][j] = ""+(((j / currentStep) % 2) > 0);				
			}
		}
		return cP;
	}
	
	/**
	 * Builds a code pattern according to the "random code" classification scheme.
	 */
	private CodePattern buildCodePattern_RANDOM_CODE(Attribute classLabel) throws OperatorException {
		double multiplicator = getParameterAsDouble(PARAMETER_RANDOM_CODE_MULTIPLICATOR);
		int randomSeed = getParameterAsInt(PARAMETER_LOCAL_RANDOM_SEED);
		
		int numberOfClasses = classLabel.getMapping().size();	
		CodePattern cP = new CodePattern(numberOfClasses, (int)(numberOfClasses * multiplicator)); //, RANDOM_CODE);
		
		Random randomGenerator = RandomGenerator.getRandomGenerator(randomSeed);		
		
		for (int i = 0; i < cP.data.length; i++) {
			for (int j = 0; j < cP.data[0].length; j++) {
				cP.data[i][j] = ""+randomGenerator.nextBoolean();
			}			
		}		
		
		//TODO: Improve random codeword quality
		
		// Ensure that each column shows at least one occurrence of "1" (true) or "0" (false),
		// otherwise the following two-class classification procedure fails.		
		for (int i = 0; i < cP.data[0].length; i++) {				
			boolean containsNoOne = true, containsNoZero = true;
			for (int j = 0; j < cP.data.length; j++) {					
				if ("true".equals(cP.data[j][i])) {
					containsNoOne = false;					
				} else {
					containsNoZero = false;						
				}					
			} 
			if (containsNoOne) {
				cP.data[(int)(randomGenerator.nextDouble()*(cP.data.length - 1))][i] = "true";					
			}
			if (containsNoZero) {
				cP.data[(int)(randomGenerator.nextDouble()*(cP.data.length - 1))][i] = "false";					
			}
		}		
		return cP;
	}
		
	public Model learn(ExampleSet inputSet) throws OperatorException {
		Attribute classLabel = inputSet.getAttributes().getLabel();		
		
		if (classLabel.getMapping().size() == 2) {
			return applyInnerLearner(inputSet);
		}
		
		int classificationStrategy = getParameterAsInt(PARAMETER_CLASSIFICATION_STRATEGIES);		
		CodePattern cP;		
		Model[] models;		
		
		SplittedExampleSet seSet = constructClassPartitionSet(inputSet);
		
		switch (classificationStrategy) {
		
			case ONE_AGAINST_ALL: {
				log("Binary2MultiCLassLearner set to <<1-vs-all>>");
					
				cP = buildCodePattern_ONE_VS_ALL(classLabel);
				models = applyCodePattern(seSet, classLabel, cP);		
				
				return new Binary2MultiClassModel(inputSet, models, classificationStrategy, modelNames);		
			}
			
			case ONE_AGAINST_ONE: {
				log("Binary2MultiCLassLearner set to <<1-vs-1>>");
				
				cP = buildCodePattern_ONE_VS_ONE(classLabel);
				models = applyCodePattern(seSet, classLabel, cP);			
				
				return new Binary2MultiClassModel(inputSet, models, classificationStrategy, modelNames);
			}
			
			case EXHAUSTIVE_CODE: {
				log("Binary2MultiCLassLearner set to <<exhaustive code>>");
					
				cP = buildCodePattern_EXHAUSTIVE_CODE(classLabel);
				models = applyCodePattern(seSet, classLabel, cP);		
				
				return new Binary2MultiClassModel(inputSet, models, classificationStrategy, cP.data);		
			}
			
			case RANDOM_CODE: {
				log("Binary2MultiCLassLearner set to <<random code>>");
				
				cP = buildCodePattern_RANDOM_CODE(classLabel);
				models = applyCodePattern(seSet, classLabel, cP);			
				
				return new Binary2MultiClassModel(inputSet, models, classificationStrategy, cP.data);
			}
			
			default: {			
				throw new OperatorException("Binary2MultiCLassLearner: Unknown classification strategy selected");
			}
		
		}		
	}
	
    public boolean supportsCapability(LearnerCapability capability) {
        if (capability == com.rapidminer.operator.learner.LearnerCapability.POLYNOMINAL_CLASS)
            return true;
        else
        	return super.supportsCapability(capability);
    }
    
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeCategory(PARAMETER_CLASSIFICATION_STRATEGIES, "What strategy should be used for multi class classifications?", STRATEGIES, ONE_AGAINST_ALL));
		types.add(new ParameterTypeDouble(PARAMETER_RANDOM_CODE_MULTIPLICATOR, "A multiplicator regulating the codeword length in random code modus.", 1.0d, Double.POSITIVE_INFINITY, 2.0d));
		types.add(new ParameterTypeInt(PARAMETER_LOCAL_RANDOM_SEED, "Use the given random seed instead of global random numbers (-1: use global)", -1, Integer.MAX_VALUE, -1));
		return types;
	}

}
