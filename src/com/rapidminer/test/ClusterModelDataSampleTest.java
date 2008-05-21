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
package com.rapidminer.test;

import java.util.Iterator;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.MissingIOObjectException;
import com.rapidminer.operator.learner.clustering.FlatCrispClusterModel;

/**
* Tests for the data of several ExampleSets
*
* @author Marcin Skirzynski
* @version $Id: ClusterModelDataSampleTest.java,v 1.4 2008/05/09 19:22:48 ingomierswa Exp $
*/
public class ClusterModelDataSampleTest extends OperatorDataSampleTest {
	
	private String[] values5;
	private String[] values6;

	public ClusterModelDataSampleTest(String file, String attributeName, String[] values6, String[] values5) {
		super(file);
		this.values6 = values6;
		this.values5 = values5;
	}
	
	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		
		FlatCrispClusterModel cluster = output.get(FlatCrispClusterModel.class);
		Iterator<String> it = cluster.getClusterAt(0).getObjects();
		if(System.getProperty("java.version").charAt(2) == '5') {
			for(int i = 0;i<values5.length;i++){
				assertEquals(it.next(),values5[i]);
			}
		}
		if(System.getProperty("java.version").charAt(2) == '6') {
			for(int i = 0;i<values6.length;i++){
				assertEquals(it.next(),values6[i]);
			}
		}
		
		 
	}

}
