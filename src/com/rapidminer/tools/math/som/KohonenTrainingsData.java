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
package com.rapidminer.tools.math.som;

import java.util.Random;

/** This interface describes the training data for a Kohonen net. 
 * 
 *  @author Sebastian Land
 *  @version $Id: KohonenTrainingsData.java,v 1.3 2008/05/09 19:23:19 ingomierswa Exp $
 */
public interface KohonenTrainingsData {

	public double[] getNext();

	public int countData();

	public void reset();

	public void setRandomGenerator(Random generator);

	public double[] get(int index);
}
