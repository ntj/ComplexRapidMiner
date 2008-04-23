/*
 *  RapidMiner
 *
 *  Copyright (C) 2001-2007 by Rapid-I and the contributors
 *
 *  Complete list of developers available at our web site:
 *
 *       http://rapid-i.com
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as 
 *  published by the Free Software Foundation; either version 2 of the
 *  License, or (at your option) any later version. 
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 *  USA.
 */
package com.rapidminer.operator.features.construction;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.generator.AbsoluteValueGenerator;
import com.rapidminer.generator.BasicArithmeticOperationGenerator;
import com.rapidminer.generator.ExponentialFunctionGenerator;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.generator.FloorCeilGenerator;
import com.rapidminer.generator.MinMaxGenerator;
import com.rapidminer.generator.PowerGenerator;
import com.rapidminer.generator.ReciprocalValueGenerator;
import com.rapidminer.generator.SquareRootGenerator;
import com.rapidminer.generator.TrigonometricFunctionGenerator;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;


/**
 * This operator applies a set of functions on all features of the input example
 * set. Applicable functions include +, -, *, /, norm, sin, cos, tan, atan, exp,
 * log, min, max, floor, ceil, round, sqrt, abs, and pow. Features with two
 * arguments will be applied on all pairs. Non commutative functions will also
 * be applied on all permutations.
 * 
 * @see com.rapidminer.generator.FeatureGenerator
 * @author Ingo Mierswa
 * @version $Id: CompleteFeatureGenerationOperator.java,v 1.4 2006/04/05
 *          08:57:27 ingomierswa Exp $
 */
public class CompleteFeatureGenerationOperator extends Operator {


	/** The parameter name for &quot;If set to true, all the original attributes are kept, otherwise they are removed from the example set.&quot; */
	public static final String PARAMETER_KEEP_ALL = "keep_all";

	/** The parameter name for &quot;Generate sums.&quot; */
	public static final String PARAMETER_USE_PLUS = "use_plus";

	/** The parameter name for &quot;Generate differences.&quot; */
	public static final String PARAMETER_USE_DIFF = "use_diff";

	/** The parameter name for &quot;Generate products.&quot; */
	public static final String PARAMETER_USE_MULT = "use_mult";

	/** The parameter name for &quot;Generate quotients.&quot; */
	public static final String PARAMETER_USE_DIV = "use_div";

	/** The parameter name for &quot;Generate reciprocal values.&quot; */
	public static final String PARAMETER_USE_RECIPROCALS = "use_reciprocals";

	/** The parameter name for &quot;Generate square root values.&quot; */
	public static final String PARAMETER_USE_SQUARE_ROOTS = "use_square_roots";

	/** The parameter name for &quot;Generate the power of one attribute and another.&quot; */
	public static final String PARAMETER_USE_POWER_FUNCTIONS = "use_power_functions";

	/** The parameter name for &quot;Generate sinus.&quot; */
	public static final String PARAMETER_USE_SIN = "use_sin";

	/** The parameter name for &quot;Generate cosinus.&quot; */
	public static final String PARAMETER_USE_COS = "use_cos";

	/** The parameter name for &quot;Generate tangens.&quot; */
	public static final String PARAMETER_USE_TAN = "use_tan";

	/** The parameter name for &quot;Generate arc tangens.&quot; */
	public static final String PARAMETER_USE_ATAN = "use_atan";

	/** The parameter name for &quot;Generate exponential functions.&quot; */
	public static final String PARAMETER_USE_EXP = "use_exp";

	/** The parameter name for &quot;Generate logarithmic functions.&quot; */
	public static final String PARAMETER_USE_LOG = "use_log";

	/** The parameter name for &quot;Generate absolute values.&quot; */
	public static final String PARAMETER_USE_ABSOLUTE_VALUES = "use_absolute_values";

	/** The parameter name for &quot;Generate minimum values.&quot; */
	public static final String PARAMETER_USE_MIN = "use_min";

	/** The parameter name for &quot;Generate maximum values.&quot; */
	public static final String PARAMETER_USE_MAX = "use_max";

	/** The parameter name for &quot;Generate ceil values.&quot; */
	public static final String PARAMETER_USE_CEIL = "use_ceil";

	/** The parameter name for &quot;Generate floor values.&quot; */
	public static final String PARAMETER_USE_FLOOR = "use_floor";

	/** The parameter name for &quot;Generate rounded values.&quot; */
	public static final String PARAMETER_USE_ROUNDED = "use_rounded";
	public CompleteFeatureGenerationOperator(OperatorDescription description) {
		super(description);
	}

	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);

		// set selection mode to restrictive mode
		FeatureGenerator.setSelectionMode(FeatureGenerator.SELECTION_MODE_RESTRICTIVE);

		List<FeatureGenerator> generators = getGenerators();
		List<FeatureGenerator> generatorList = new LinkedList<FeatureGenerator>();
		Iterator<FeatureGenerator> i = generators.iterator();
		while (i.hasNext()) {
			FeatureGenerator generator = i.next();
			List<Attribute[]> inputAttributes = generator.getInputCandidates(exampleSet, -1, new String[0]);
			Iterator<Attribute[]> a = inputAttributes.iterator();
			while (a.hasNext()) {
				Attribute[] args = a.next();
				FeatureGenerator newGenerator = generator.newInstance();
				newGenerator.setArguments(args);
				generatorList.add(newGenerator);
			}
		}

		// generate all new attributes
		if (!getParameterAsBoolean(PARAMETER_KEEP_ALL)) {
			exampleSet.getAttributes().clearRegular();
		}
		
		List<Attribute> newAttributes = FeatureGenerator.generateAll(exampleSet.getExampleTable(), generatorList);
		for (Attribute newAttribute : newAttributes)
			exampleSet.getAttributes().addRegular(newAttribute);

		return new IOObject[] { exampleSet };
	}

	private List<FeatureGenerator> getGenerators() {
		List<FeatureGenerator> generators = new ArrayList<FeatureGenerator>();
		if (getParameterAsBoolean(PARAMETER_USE_PLUS))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.SUM));
		if (getParameterAsBoolean(PARAMETER_USE_DIFF))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.DIFFERENCE));
		if (getParameterAsBoolean(PARAMETER_USE_MULT))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.PRODUCT));
		if (getParameterAsBoolean(PARAMETER_USE_DIV))
			generators.add(new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.QUOTIENT));
		if (getParameterAsBoolean(PARAMETER_USE_RECIPROCALS))
			generators.add(new ReciprocalValueGenerator());
		if (getParameterAsBoolean(PARAMETER_USE_SQUARE_ROOTS)) {
			generators.add(new SquareRootGenerator());
		}
		if (getParameterAsBoolean(PARAMETER_USE_POWER_FUNCTIONS)) {
			generators.add(new PowerGenerator());
		}

		if (getParameterAsBoolean(PARAMETER_USE_SIN))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.SINUS));
		if (getParameterAsBoolean(PARAMETER_USE_COS))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.COSINUS));
		if (getParameterAsBoolean(PARAMETER_USE_TAN))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.TANGENS));
		if (getParameterAsBoolean(PARAMETER_USE_ATAN))
			generators.add(new TrigonometricFunctionGenerator(TrigonometricFunctionGenerator.ARC_TANGENS));

		if (getParameterAsBoolean(PARAMETER_USE_EXP))
			generators.add(new ExponentialFunctionGenerator(ExponentialFunctionGenerator.EXP));
		if (getParameterAsBoolean(PARAMETER_USE_LOG))
			generators.add(new ExponentialFunctionGenerator(ExponentialFunctionGenerator.LOG));

		if (getParameterAsBoolean(PARAMETER_USE_ABSOLUTE_VALUES))
			generators.add(new AbsoluteValueGenerator());
		if (getParameterAsBoolean(PARAMETER_USE_MIN))
			generators.add(new MinMaxGenerator(MinMaxGenerator.MIN));
		if (getParameterAsBoolean(PARAMETER_USE_MAX))
			generators.add(new MinMaxGenerator(MinMaxGenerator.MAX));

		if (getParameterAsBoolean(PARAMETER_USE_CEIL))
			generators.add(new FloorCeilGenerator(FloorCeilGenerator.CEIL));
		if (getParameterAsBoolean(PARAMETER_USE_FLOOR))
			generators.add(new FloorCeilGenerator(FloorCeilGenerator.FLOOR));
		if (getParameterAsBoolean(PARAMETER_USE_ROUNDED))
			generators.add(new FloorCeilGenerator(FloorCeilGenerator.ROUND));
		return generators;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_KEEP_ALL, "If set to true, all the original attributes are kept, otherwise they are removed from the example set.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_PLUS, "Generate sums.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DIFF, "Generate differences.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MULT, "Generate products.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DIV, "Generate quotients.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_RECIPROCALS, "Generate reciprocal values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_SQUARE_ROOTS, "Generate square root values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_POWER_FUNCTIONS, "Generate the power of one attribute and another.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_SIN, "Generate sinus.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_COS, "Generate cosinus.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_TAN, "Generate tangens.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ATAN, "Generate arc tangens.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_EXP, "Generate exponential functions.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_LOG, "Generate logarithmic functions.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ABSOLUTE_VALUES, "Generate absolute values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MIN, "Generate minimum values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MAX, "Generate maximum values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_CEIL, "Generate ceil values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_FLOOR, "Generate floor values.", false));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_ROUNDED, "Generate rounded values.", false));
		return types;
	}
}
