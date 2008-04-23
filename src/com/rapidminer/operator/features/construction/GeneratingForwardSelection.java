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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.set.AttributeWeightedExampleSet;
import com.rapidminer.generator.BasicArithmeticOperationGenerator;
import com.rapidminer.generator.FeatureGenerator;
import com.rapidminer.generator.GenerationException;
import com.rapidminer.generator.MinMaxGenerator;
import com.rapidminer.generator.ReciprocalValueGenerator;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.UserError;
import com.rapidminer.operator.Value;
import com.rapidminer.operator.features.Individual;
import com.rapidminer.operator.features.KeepBest;
import com.rapidminer.operator.features.Population;
import com.rapidminer.operator.features.PopulationOperator;
import com.rapidminer.operator.features.RedundanceRemoval;
import com.rapidminer.operator.features.selection.FeatureSelectionOperator;
import com.rapidminer.operator.features.selection.SwitchingForwardSelection;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.Tools;


/**
 * This operator is a kind of nested forward selection and thus is (in contrast
 * to a genetic algorithm) a directed search.
 * <ol>
 * <li>use forward selection in order to determine the best attributes</li>
 * <li>Create a new attribute by multiplying any of the original attributes
 * with any of the attributes selected by the forward selection in the last turn</li>
 * <li>loop as long as performance increases</li>
 * </ol>
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: GeneratingForwardSelection.java,v 1.1 2006/04/14 11:42:27
 *          ingomierswa Exp $
 */
public class GeneratingForwardSelection extends FeatureSelectionOperator {


	/** The parameter name for &quot;Generate reciprocal values.&quot; */
	public static final String PARAMETER_RECIPROCAL_VALUE = "reciprocal_value";

	/** The parameter name for &quot;Generate sums.&quot; */
	public static final String PARAMETER_USE_PLUS = "use_plus";

	/** The parameter name for &quot;Generate differences.&quot; */
	public static final String PARAMETER_USE_DIFF = "use_diff";

	/** The parameter name for &quot;Generate products.&quot; */
	public static final String PARAMETER_USE_MULT = "use_mult";

	/** The parameter name for &quot;Generate quotients.&quot; */
	public static final String PARAMETER_USE_DIV = "use_div";

	/** The parameter name for &quot;Generate maximum.&quot; */
	public static final String PARAMETER_USE_MAX = "use_max";

	/** The parameter name for &quot;Use restrictive generator selection (faster).&quot; */
	public static final String PARAMETER_RESTRICTIVE_SELECTION = "restrictive_selection";
	/** List of AttributeReferences. */
	private Attribute[] originalAttributes;

	private Individual bestIndividual;

	private List<FeatureGenerator> useGenerators;

	private int newAttributeStart;

	private int turn;

	public GeneratingForwardSelection(OperatorDescription description) {
		super(description);
		addValue(new Value("turn", "The number of the current turn.") {

			public double getValue() {
				return turn;
			}
		});
	}

	public IOObject[] apply() throws OperatorException {
		newAttributeStart = 0;
		turn = 0;
		bestIndividual = null;
		originalAttributes = null;

		useGenerators = new LinkedList<FeatureGenerator>();

		if (getParameterAsBoolean(PARAMETER_RECIPROCAL_VALUE)) {
			FeatureGenerator g = new ReciprocalValueGenerator();
			useGenerators.add(g);
		}
		if (getParameterAsBoolean(PARAMETER_USE_PLUS)) {
			FeatureGenerator g = new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.SUM);
			useGenerators.add(g);
		}
		if (getParameterAsBoolean(PARAMETER_USE_DIFF)) {
			FeatureGenerator g = new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.DIFFERENCE);
			useGenerators.add(g);
		}
		if (getParameterAsBoolean(PARAMETER_USE_MULT)) {
			FeatureGenerator g = new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.PRODUCT);
			useGenerators.add(g);
		}
		if (getParameterAsBoolean(PARAMETER_USE_DIV)) {
			FeatureGenerator g = new BasicArithmeticOperationGenerator(BasicArithmeticOperationGenerator.QUOTIENT);
			useGenerators.add(g);
		}
		if (getParameterAsBoolean(PARAMETER_USE_MAX)) {
			FeatureGenerator g = new MinMaxGenerator(MinMaxGenerator.MAX);
			useGenerators.add(g);
		}

		if (useGenerators.size() == 0) {
			logWarning("No FeatureGenerators specified for " + getName() + ".");
		}

		if (getParameterAsBoolean(PARAMETER_RESTRICTIVE_SELECTION))
			FeatureGenerator.setSelectionMode(FeatureGenerator.SELECTION_MODE_RESTRICTIVE);
		else
			FeatureGenerator.setSelectionMode(FeatureGenerator.SELECTION_MODE_ALL);

		return super.apply();
	}

	/**
	 * May <tt>es</tt> have <i>n</i> features. The initial population
	 * contains
	 * <li><i>n</i> elements with exactly 1 feature switched on.
	 */
	public Population createInitialPopulation(ExampleSet es) {
		// remember the original attributes
		originalAttributes = es.getAttributes().createRegularAttributeArray();

		Population initP = new Population();
		AttributeWeightedExampleSet nes = new AttributeWeightedExampleSet((ExampleSet) es.clone());
		for (Attribute attribute : es.getAttributes())
			nes.setAttributeUsed(attribute, false);
		for (Attribute attribute : es.getAttributes()) {
			AttributeWeightedExampleSet forwardES = (AttributeWeightedExampleSet) nes.clone();
			forwardES.setAttributeUsed(attribute, true);
			initP.add(new Individual(forwardES));
		}
		return initP;
	}

	/**
	 * The operators performs two steps:
	 * <ol>
	 * <li>forward selection/backward elimination
	 * <li>kick out all but the <tt>keep_best</tt> individuals
	 * <li>remove redundant individuals
	 * </ol>
	 */
	public List<PopulationOperator> getPreEvaluationPopulationOperators(ExampleSet input) throws OperatorException {
		List<PopulationOperator> preOp = new LinkedList<PopulationOperator>();
		preOp.add(new KeepBest(getParameterAsInt(PARAMETER_KEEP_BEST))); 
		preOp.add(new SwitchingForwardSelection());
		preOp.add(new RedundanceRemoval());
		return preOp;
	}

	public boolean solutionGoodEnough(Population pop) throws OperatorException {
		if (super.solutionGoodEnough(pop)) {
			if (pop.getNumberOfIndividuals() <= 0) {
				return true;
			}

			// The forward selection is finished
			Individual fsBest = pop.getBestIndividualEver();
			AttributeWeightedExampleSet fsBestExampleSet = (AttributeWeightedExampleSet) fsBest.getExampleSet().clone();

			// Check whether the performance was improved by this turn
			if ((bestIndividual == null) || (bestIndividual.getPerformance() == null) || ((fsBest.getPerformance().compareTo(bestIndividual.getPerformance()) > 0))) {
				turn++;
				bestIndividual = new Individual((AttributeWeightedExampleSet) fsBestExampleSet.clone());

				fsBestExampleSet = new AttributeWeightedExampleSet(fsBestExampleSet.createCleanClone());
				Attribute[] fsBestAttributes = fsBestExampleSet.getAttributes().createRegularAttributeArray();
				log(Tools.ordinalNumber(turn) + " turn's FS result: " + fsBest);
				// and generate all new attributes using the generators
				List<FeatureGenerator> generators = new LinkedList<FeatureGenerator>();
				Iterator<FeatureGenerator> i = useGenerators.listIterator();
				// for all generator types
				while (i.hasNext()) {
					FeatureGenerator fg = i.next();
					// for all new arguments
					if (fg.getInputAttributes().length == 2) {
						for (int a = newAttributeStart; a < fsBestExampleSet.getAttributes().size(); a++) {
							// for all original attributes
							for (int o = 0; o < originalAttributes.length; o++) {
								FeatureGenerator g = fg.newInstance();
								g.setArguments(new Attribute[] { originalAttributes[o], fsBestAttributes[a] });
								generators.add(g);
							}
						}
					} else if (fg.getInputAttributes().length == 1) {
						for (int a = 0; a < fsBestAttributes.length; a++) {
							FeatureGenerator g = fg.newInstance();
							g.setArguments(new Attribute[] { fsBestAttributes[a] });
							generators.add(g);
						}
					} else {
						logWarning("Functions with arity " + fg.getInputAttributes().length + " not supported: " + fg);
					}
				}
				log("Generating " + generators.size() + " new attributes.");
				newAttributeStart = fsBestExampleSet.getAttributes().size();

				// generate the new attributes
				try {
					List<Attribute> attributes = FeatureGenerator.generateAll(fsBestExampleSet.getExampleTable(), generators);
					Iterator<Attribute> j = attributes.iterator();
					while (j.hasNext()) {
						Attribute attr = j.next();
						try {
							fsBestExampleSet.getAttributes().addRegular(attr);
							fsBestExampleSet.setAttributeUsed(attr, false);
						} catch (Exception e) {
							logWarning(e.getMessage());
						}
					}
				} catch (GenerationException e) {
					throw new UserError(this, e, 108, e.getMessage());
				}
				// clear the population, add the generated set
				pop.clear();
				pop.add(new Individual(fsBestExampleSet));
				return false;
			} else {
				// otherwise quit
				return true;
			}
		}
		// go on with the forward selection
		return false;
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeBoolean(PARAMETER_RECIPROCAL_VALUE, "Generate reciprocal values.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_PLUS, "Generate sums.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DIFF, "Generate differences.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MULT, "Generate products.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_DIV, "Generate quotients.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_USE_MAX, "Generate maximum.", true));
		types.add(new ParameterTypeBoolean(PARAMETER_RESTRICTIVE_SELECTION, "Use restrictive generator selection (faster).", true));
		return types;
	}

}
