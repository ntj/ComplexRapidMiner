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
package com.rapidminer.example.test;

import com.rapidminer.example.Attribute;
import com.rapidminer.example.table.DataRow;
import com.rapidminer.example.table.DataRowFactory;
import com.rapidminer.test.TestCase;


/**
 * Tests creation and parsing of sparse and dense data rows, including missing
 * values.
 * 
 * @author Simon Fischer
 * @version $Id: DataRowTest.java,v 1.1 2007/05/27 22:03:41 ingomierswa Exp $
 */
public class DataRowTest extends TestCase {

	private Attribute[] attributes;

	private double[] expected;

	public void setUp() throws Exception {
		super.setUp();
		attributes = ExampleTestTools.createFourAttributes();
		expected = new double[] { 1, Double.NaN, 5.0, 2.3 };
	}

	public void tearDown() throws Exception {
		attributes = null;
		expected = null;
		super.tearDown();
	}

	private void assertDataRow(String message, DataRow dataRow, double[] expected) {
		for (int i = 0; i < expected.length; i++) {
			assertEqualsNaN(message + " " + attributes[i].getName(), expected[i], dataRow.get(attributes[i]));
		}
	}

	private void objectTest(String message, DataRowFactory factory) {
		DataRow dataRow = factory.create(new Object[] { "cat", null, Integer.valueOf(5), new Double(2.3) }, attributes);
		assertDataRow(message + " object", dataRow, expected);
	}

	private void stringTest(String message, DataRowFactory factory) {
		DataRow dataRow = factory.create(new String[] { "cat", "?", "5", "2.3" }, attributes);
		assertDataRow(message + " string", dataRow, expected);
	}

	public void testDoubleArrayStrings() {
		objectTest("double_array", new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'));
	}

	public void testDoubleArrayObjects() {
		stringTest("double_array", new DataRowFactory(DataRowFactory.TYPE_DOUBLE_ARRAY, '.'));
	}

	public void testSparseMapStrings() {
		stringTest("sparse_map", new DataRowFactory(DataRowFactory.TYPE_SPARSE_MAP, '.'));
	}

	public void testSparseMapObjects() {
		objectTest("sparse_map", new DataRowFactory(DataRowFactory.TYPE_SPARSE_MAP, '.'));
	}

}
