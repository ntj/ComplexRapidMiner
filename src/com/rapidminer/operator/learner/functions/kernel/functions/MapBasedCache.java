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
package com.rapidminer.operator.learner.functions.kernel.functions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/** Stores all distances in a map. Uses only a fixed maximum amount of
 *  entries for this map (default: 10,000,000, enough for about 3000 examples).
 * 
 *  @author Ingo Mierswa
 *  @version $Id: MapBasedCache.java,v 1.2 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class MapBasedCache implements KernelCache {

	private int maxSize = 10000000;
	
	private int exampleSetSize;
	
	private int accessCounter = 0;
	
	private Map<Integer,Integer> accessMap;
	
	private Map<Integer,Double> entries;
	
	public MapBasedCache(int exampleSetSize) {
		this(10000000, exampleSetSize);
	}
	
	public MapBasedCache(int maxSize, int exampleSetSize) {
		this.maxSize = maxSize;
		this.exampleSetSize = exampleSetSize;
		this.accessMap = new HashMap<Integer,Integer>(maxSize);
		this.entries = new HashMap<Integer,Double>(maxSize);
	}
	
	public double get(int i, int j) {
		accessCounter++;
		Double result = entries.get(i * exampleSetSize + j);
		if (result == null) {
			return Double.NaN;
		} else {
			accessMap.put(i * exampleSetSize + j, accessCounter);
			return result;
		}
	}

	public void store(int i, int j, double value) {
		if (accessMap.size() > this.maxSize) {
			System.out.println("Clean cache!");
			int oldestKey = -1; 
			int oldestAcess = Integer.MAX_VALUE;
			Iterator<Map.Entry<Integer,Integer>> it = accessMap.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<Integer,Integer> entry = it.next();
				int access = entry.getValue();
				if (access < oldestAcess) {
					oldestKey = entry.getKey();
					oldestAcess = access;
				}
			}
			
			if (oldestKey != -1) {
				accessMap.remove(oldestKey);
				entries.remove(oldestKey);
			}
		}
		
		accessMap.put(i * exampleSetSize + j, accessCounter);
		entries.put(i * exampleSetSize + j, value);
	}
}
