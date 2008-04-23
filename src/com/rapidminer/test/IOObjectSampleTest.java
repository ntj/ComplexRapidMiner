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
package com.rapidminer.test;

import java.util.Collection;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.MissingIOObjectException;

/**
 * Performs the sample process and checks if the given IOObjects are part of
 * the output.
 * 
 * @author Ingo Mierswa
 * @version $Id: IOObjectSampleTest.java,v 2.7 2006/03/21 15:35:53 ingomierswa
 *          Exp $
 */
public class IOObjectSampleTest extends SampleTest {

	private Collection<Class<IOObject>> ioObjects;

	public IOObjectSampleTest(String file, Collection<Class<IOObject>> ioObjects) {
		super(file);
		this.ioObjects = ioObjects;
	}

	public void checkOutput(IOContainer output) throws MissingIOObjectException {
		for (Class<IOObject> o : ioObjects)
			output.get(o);
	}
}
