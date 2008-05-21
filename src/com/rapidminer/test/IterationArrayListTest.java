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

import java.util.ArrayList;

import com.rapidminer.tools.IterationArrayList;

import junit.framework.TestCase;

/**
 * A test for the  {@link IterationArrayList}.
 * 
 * @author Michael Wurst
 * @version $Id: IterationArrayListTest.java,v 1.3 2008/05/09 19:22:48 ingomierswa Exp $
 */
public class IterationArrayListTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testAccess() {

		ArrayList<String> l = new ArrayList<String>();
		l.add("a");
		l.add("b");
		l.add("c");

		ArrayList<String> l2 = new IterationArrayList<String>(l.iterator());

		for (int i = 0; i < l.size(); i++)
			assertEquals(l2.get(i), l.get(i));

	}

}
