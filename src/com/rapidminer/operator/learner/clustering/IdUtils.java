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
package com.rapidminer.operator.learner.clustering;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.Example;
import com.rapidminer.example.ExampleSet;
import com.rapidminer.tools.Ontology;
import com.rapidminer.tools.RandomGenerator;


/**
 * Some utility methods to connect the clustering plugin to the RapidMiner.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: IdUtils.java,v 1.1 2007/05/27 21:58:38 ingomierswa Exp $
 */
public class IdUtils {

	public static String getIdFromExample(Example e) {
		Attribute idAttribute = e.getAttributes().getId();
		if (idAttribute != null)
			return e.getValueAsString(idAttribute);
		else
			return null;
	}

	public static Example getExampleFromId(ExampleSet es, String id) {
		Attribute idAttribute = es.getAttributes().getId();
		// TODO: kick all stuff out after changing clustering ID handling to doubles...
		Example example = null;
		if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(idAttribute.getValueType(), Ontology.NOMINAL)) {
			double index = idAttribute.getMapping().mapString(id);
			example = es.getExampleFromId(index);
		} else if (Ontology.ATTRIBUTE_VALUE_TYPE.isA(idAttribute.getValueType(), Ontology.NUMERICAL)) {
			double index = Double.parseDouble(id);
			example = es.getExampleFromId(index);
		} else {
			es.getLog().logError("Id must be numerical or nominal");
			example = null;
		}
		if (example == null)
			return null;
		if (example.getDataRow() == null) {
			es.getLog().logError("Example with id " + id + " cannot be found");
			return null;
		}
		return example;
	}

	public static List<String> getRandomIdList(List<String> ids, int k, int randomSeed) {
		List<String> result = new ArrayList<String>(k);
		List<String> idsCopy = new ArrayList<String>(ids);
		Random rng = RandomGenerator.getRandomGenerator(randomSeed);
		for (int i = 0; i < k; i++) {
			int index = rng.nextInt(idsCopy.size());
			result.add(idsCopy.get(index));
			idsCopy.remove(index);
		}
		return result;
	}
}
