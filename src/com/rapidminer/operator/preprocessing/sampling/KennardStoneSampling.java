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
package com.rapidminer.operator.preprocessing.sampling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.TreeSet;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Attributes;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.Statistics;
import com.rapidminer.example.set.Partition;
import com.rapidminer.example.set.SplittedExampleSet;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.operator.similarity.attributebased.AbstractValueBasedSimilarity;
import com.rapidminer.operator.similarity.attributebased.EuclideanDistance;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeDouble;
import com.rapidminer.parameter.ParameterTypeInt;

/**
 * This operator performs a Kennard-Stone Sampling. This sampling Algorithm works as follows:
 * First find the two points most separated in the training set.
 * For each candidate point, find the smallest distance to any object already selected.
 * Select that point for the training set which has the largest of these smallest distances
 * As described above, this algorithm always gives the same result, due to the two starting 
 * points which are always the same.
 * This implementation reduces number of iterations by holding a list with candidates of the largest
 * smallest distances. 
 * The parameters controll the number of examples in the sample
 *  
 * @author Sebastian Land
 * @version $Id: KennardStoneSampling.java,v 1.3 2008/05/09 19:23:16 ingomierswa Exp $
 */
public class KennardStoneSampling extends Operator {

	/** The parameter name for &quot;The fraction of examples which should be sampled&quot; */
	public static final String PARAMETER_SAMPLE_RATIO = "sample_ratio";
	public static final String PARAMETER_ABSOLUTE_SAMPLE = "absolute_sample";
	public static final String PARAMETER_SAMPLE_SIZE = "sample_size";
	
	
	private class Candidate implements Comparable<Candidate>{
		private double[] attributeValues;
		private double distance;
		private int exampleIndex;
		
		public Candidate(double[] exampleValues, double distance, int exampleIndex) {
			attributeValues = exampleValues;
			this.distance = distance;
			this.exampleIndex = exampleIndex;
		}
		
		public double getDistance() {
			return distance;
		}

		public double[] getValues() {
			return attributeValues;
		}
		
		public int getExampleIndex() {
			return exampleIndex;
		}
		public int compareTo(Candidate o) {
			return Double.compare(this.distance, o.getDistance());
		}
	}
	
	
	public KennardStoneSampling(OperatorDescription description) {
		super(description);
	}

	public IOObject[] apply() throws OperatorException {
		ExampleSet exampleSet = getInput(ExampleSet.class);
		// creating kernel and settings from Parameters
		int k = Math.min(100, exampleSet.getAttributes().size() * 2);
		int desiredNumber = (int) ((double)exampleSet.size() * getParameterAsDouble(PARAMETER_SAMPLE_RATIO));
		if (getParameterAsBoolean(PARAMETER_ABSOLUTE_SAMPLE))
			desiredNumber = getParameterAsInt(PARAMETER_SAMPLE_SIZE);

		AbstractValueBasedSimilarity distanceMeasure = new EuclideanDistance();
		
		// finding farthest and nearest example to mean Vector
		double[] meanVector = getMeanVector(exampleSet);
		Candidate min = new Candidate(meanVector, Double.POSITIVE_INFINITY, 0);
		Candidate max = new Candidate(meanVector, Double.NEGATIVE_INFINITY, 0);
		int i = 0;
		for (Example example: exampleSet) {
			double[] exampleValues = getExampleValues(example);
			Candidate current = new Candidate(exampleValues, Math.abs(distanceMeasure.similarity(meanVector, exampleValues)), i);
			if (current.compareTo(min) < 0) {
				min = current;
			}
			if (current.compareTo(max) > 0) {
				max = current;
			}
			i++;
		}
		ArrayList<Candidate> recentlySelected = new ArrayList<Candidate>(10);
		int[] partition = new int[exampleSet.size()];
		int numberOfSelectedExamples = 2;
		recentlySelected.add(min);
		recentlySelected.add(max);
		partition[min.getExampleIndex()] = 1;
		partition[max.getExampleIndex()] = 1;
		double[] minimalDistances = new double[exampleSet.size()];
		Arrays.fill(minimalDistances, Double.POSITIVE_INFINITY);
		
		// running now through examples, checking for smallest distance to one of the candidates
		while (numberOfSelectedExamples < desiredNumber) {
			TreeSet<Candidate> candidates = new TreeSet<Candidate>();
			
			i = 0;
			// check distance only for candidates recently selected.
			for (Example example: exampleSet) {
				// if example not has been selected allready
				if (partition[i] == 0) {
					double[] exampleValues = getExampleValues(example);
					for (Candidate candidate: recentlySelected) {
						minimalDistances[i] = Math.min(minimalDistances[i], Math.abs(distanceMeasure.similarity(exampleValues, candidate.getValues())));
					}
					Candidate newCandidate = new Candidate(exampleValues, minimalDistances[i], i);
					candidates.add(newCandidate);
					if (candidates.size() > k) {
						Iterator<Candidate> iterator = candidates.iterator();
						iterator.next();
						iterator.remove();
					}
				}
				i++;
			}
			// clearing recently selected since now new ones will be selected
			recentlySelected.clear();

			// now running in descending order through candidates and adding to selected
			// IM: descendingIterator() is not available in Java versions less than 6 !!!
			// IM: Bad workaround for now by adding all candidates into a list and using a listIterator() and hasPrevious...
			/*
			Iterator<Candidate> descendingIterator = candidates.descendingIterator();
			while (descendingIterator.hasNext() && numberOfSelectedExamples < desiredNumber) {
				Candidate candidate = descendingIterator.next();
			 */
			
			List<Candidate> reverseCandidateList = new LinkedList<Candidate>();
			Iterator<Candidate> it = candidates.iterator();
			while (it.hasNext()) {
				reverseCandidateList.add(it.next());
			}
				
			ListIterator<Candidate> lit = reverseCandidateList.listIterator(reverseCandidateList.size() - 1);
			while (lit.hasPrevious()) {
				Candidate candidate = lit.previous();
				// IM: end of workaround
				
				boolean existSmallerDistance = false;
				Iterator<Candidate> addedIterator = recentlySelected.iterator();
				// test if a distance to recently selected is smaller than previously calculated minimal distance
				// if one exists: This is not selected
				while (addedIterator.hasNext()) {
					double distance = Math.abs(distanceMeasure.similarity(addedIterator.next().getValues(), candidate.getValues()));
					existSmallerDistance = existSmallerDistance || distance < candidate.getDistance();
				}
				if (!existSmallerDistance) {
					recentlySelected.add(candidate);
					partition[candidate.getExampleIndex()] = 1;
					numberOfSelectedExamples++;
				} else
					break;
				
			}
		}
		
		// building new exampleSet containing only Examples with indices in selectedExamples

		SplittedExampleSet sample = new SplittedExampleSet(exampleSet, new Partition(partition, 2));
		sample.selectSingleSubset(1);
		return new IOObject[] {sample};
	}

	private double[] getMeanVector(ExampleSet exampleSet) {
		exampleSet.recalculateAllAttributeStatistics();
		Attributes attributes = exampleSet.getAttributes();
		double[] meanVector = new double[attributes.size()];
		int i = 0;
		for (Attribute attribute: attributes) {
			if (attribute.isNominal())
				meanVector[i] = exampleSet.getStatistics(attribute, Statistics.MODE);
			else
				meanVector[i] = exampleSet.getStatistics(attribute, Statistics.AVERAGE);
			i++;
		}
		return meanVector;
	}

	
	private double[] getExampleValues(Example example) {
		Attributes attributes = example.getAttributes();
		double[] attributeValues = new double[attributes.size()];
		
		int i = 0;
		for (Attribute attribute: attributes) {
			attributeValues[i] = example.getValue(attribute);
			i++;
		}
		return attributeValues;
	}

	public Class[] getInputClasses() {
		return new Class[] {ExampleSet.class};	
	}
	
	public Class[] getOutputClasses() {
		return new Class[] {ExampleSet.class};
	}
	
	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		ParameterType type = new ParameterTypeDouble(PARAMETER_SAMPLE_RATIO, "The fraction of examples which should be sampled", 0.0d, 1.0d, 0.1d);
		type.setExpert(false);
		types.add(type);
		types.add(new ParameterTypeBoolean(PARAMETER_ABSOLUTE_SAMPLE, "If checked, the absolute number of examples will be used. Otherwise the ratio.", false));
		types.add(new ParameterTypeInt(PARAMETER_SAMPLE_SIZE, "The number of examples which should be sampled", 1, Integer.MAX_VALUE, 1000));		
		return types;
	}
}
