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
package com.rapidminer.operator.preprocessing.outlier;

import java.util.Iterator;
import java.util.List;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.example.table.AttributeFactory;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.OperatorException;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.Ontology;


/**
 * <p>This operator performs a LOF outlier search. LOF outliers or outliers with a local 
 * outlier factor per object are density based outliers according to Breuning, 
 * Kriegel, et al.</p>
 * 
 * <p>The approach to find those outliers is based on measuring the density of objects 
 * and its relation to each other (referred to as local reachability density). 
 * Based on the average ratio of the local reachability density of an object and its
 * k-nearest neighbours (e.g. the objects in its k-distance neighbourhood), a local 
 * outlier factor (LOF) is computed. The approach takes a parameter MinPts 
 * (actually specifying the "k") and it uses the maximum LOFs for objects in a MinPts range
 * (lower bound and upper bound to MinPts).</p>
 * 
 * <p>Currently, the operator supports cosine, sine or squared distances in addition
 * to the usual euclidian distance which can be specified by the corresponding parameter.
 * In the first step, the objects are grouped into containers. For each object, using a 
 * radius screening of all other objects, all the available distances between that object 
 * and another object (or group of objects) on the (same) radius given by the
 * distance are associated with a container. That container than has the distance 
 * information as well as the list of objects within that distance (usually only a few) 
 * and the information, how many objects are in the container.</p>
 * 
 * <p>In the second step, three things are done: (1) The containers for each object are counted 
 * in acending order according to the cardinality of the object list within the container 
 * (= that distance) to find the k-distances for each object and the
 * objects in that k-distance (all objects in all the subsequent containers with a smaller 
 * distance). (2) Using this information, the local reachability densities are computed by 
 * using the maximum of the actual distance and the k-distance for each
 * object pair (object and objects in k-distance) and averaging it by the cardinality of 
 * the k-neighbourhood and than taking the reciprocal value. (3) The LOF is computed for 
 * each MinPts value in the range (actually for all up to upper bound) by
 * averaging the ratio between the MinPts-local reachability-density of all objects in 
 * the k-neighbourhood and the object itself. The maximum LOF in the MinPts range is 
 * passed as final LOF to each object.</p>
 * 
 * <p>Afterwards LOFs are added as values for a special real-valued outlier attribute 
 * in the example set which the operator will return.</p> 
 * 
 * @author Stephan Deutsch, Ingo Mierswa
 * @version $Id: LOFOutlierOperator.java,v 1.4 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class LOFOutlierOperator extends Operator {


	/** The parameter name for &quot;The lower bound for MinPts for the Outlier test &quot; */
	public static final String PARAMETER_MINIMAL_POINTS_LOWER_BOUND = "minimal_points_lower_bound";

	/** The parameter name for &quot;The upper bound for MinPts for the Outlier test &quot; */
	public static final String PARAMETER_MINIMAL_POINTS_UPPER_BOUND = "minimal_points_upper_bound";

	/** The parameter name for &quot;choose which distance function will be used for calculating &quot; */
	public static final String PARAMETER_DISTANCE_FUNCTION = "distance_function";
	private static final String[] distanceFunctionList = {
		"euclidian distance", 
		"squared distance", 
		"cosine distance", 
		"inverted cosine distance", 
		"angle" 
	};


	public LOFOutlierOperator(OperatorDescription description) {
		super(description);
	}


	/**
	 * This method implements the main functionality of the Operator but can be considered 
	 * as a sort of wrapper to pass the RapidMiner operator chain data deeper into the 
	 * SearchSpace class, so do not expect a lot of things happening here.
	 */
	public IOObject[] apply() throws OperatorException {
		// declaration and initializing the necessary fields from input
		int minPtsLowerBound = 0;
		int minPtsUpperBound = 0;
		int minPtsLB = this.getParameterAsInt(PARAMETER_MINIMAL_POINTS_LOWER_BOUND);
		int minPtsUB = this.getParameterAsInt(PARAMETER_MINIMAL_POINTS_UPPER_BOUND);
		int kindOfDistance = this.getParameterAsInt(PARAMETER_DISTANCE_FUNCTION);

		// check for the sanity of entered parameters:
		if (minPtsLB <= minPtsUB) { // if lower bound is smaller or equal upper bound, pass them on
			minPtsLowerBound = minPtsLB;
			minPtsUpperBound = minPtsUB;
		} else { // else change both to have a sensible set of parameters ;-)
			minPtsLowerBound = minPtsUB;
			minPtsUpperBound = minPtsLB;
		}

		// create a new SearchSpace for the LOF-Outlier search
		ExampleSet eSet = getInput(ExampleSet.class);
		Iterator<Example> reader = eSet.iterator();
		int searchSpaceDimension = eSet.getAttributes().size();
		SearchSpace sr = new SearchSpace(searchSpaceDimension, minPtsLowerBound, minPtsUpperBound + 1);

		// now read through the Examples of the ExampleSet
		int counter = 0;
		while (reader.hasNext()) {
			Example example = reader.next(); // read the next example & create a search object
			SearchObject so = new SearchObject(searchSpaceDimension, "object" + counter, minPtsLowerBound, minPtsUpperBound);
			// for now, give so an id like label and add the MinPts ranges, so that arrays are initialized
			counter++;
			int i = 0;
			for (Attribute attribute : eSet.getAttributes()) {
				so.setVektor(i++, example.getValue(attribute));
			}
			sr.addObject(so); // finally add the search object to the search room
		}
		log("Searching d=" + sr.getDimensions() + " dimensions with" + " MinPts Interval [" + minPtsLowerBound + " ; " + minPtsUpperBound + "]");

		// set all Outlier Factors to ZERO to be sure
		sr.resetOutlierStatus();

        // find all Containers for the LOF first
		sr.findAllKdContainers(kindOfDistance); 
		
        // perform the LOF-Outlier search 
		sr.computeLOF(minPtsLowerBound, minPtsUpperBound); 

		Attribute outlierAttribute = AttributeFactory.createAttribute("Outlier", Ontology.REAL);
		eSet.getExampleTable().addAttribute(outlierAttribute);
		eSet.getAttributes().setOutlier(outlierAttribute);

		counter = 0; // reset counter to zero
		Iterator<Example> reader2 = eSet.iterator();
		while (reader2.hasNext()) {
			Example example = reader2.next(); // read the next example
			SearchObject sobj = sr.getSearchObjects().elementAt(counter);
			example.setValue(outlierAttribute, sobj.getOutlierFactor());
			counter++;
		}

		return new IOObject[] { eSet };
	}

	/**
	 * This method override specifies the LOFOutlierOperator to take an ExampleSet as input.
	 */
	public Class[] getInputClasses() {
		return new Class[] { ExampleSet.class };
	}

	/**
	 * This method override specifies the LOFOutlierOperator to probide an ExampleSet 
	 * as output. (please note, that the output ExampleSets will be a modified version 
	 * of the input ExampleSet, e.g. a predicted label will be added representing the
	 * outlier factor (double value).
	 */
	public Class[] getOutputClasses() {
		return new Class[] { ExampleSet.class };
	}

	public List<ParameterType> getParameterTypes() {
		List<ParameterType> types = super.getParameterTypes();
		types.add(new ParameterTypeInt(PARAMETER_MINIMAL_POINTS_LOWER_BOUND, "The lower bound for MinPts for the Outlier test " + "(default value is 10)", 0, Integer.MAX_VALUE, 10));
		types.add(new ParameterTypeInt(PARAMETER_MINIMAL_POINTS_UPPER_BOUND, "The upper bound for MinPts for the Outlier test " + "(default value is 20)", 0, Integer.MAX_VALUE, 20));
		types.add(new ParameterTypeCategory(PARAMETER_DISTANCE_FUNCTION, "choose which distance function will be used for calculating " + "the distance between two objects", distanceFunctionList, 0));
		return types;
	}
}
