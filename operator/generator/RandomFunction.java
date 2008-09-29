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
package com.rapidminer.operator.generator;

import com.rapidminer.tools.RandomGenerator;

/** The label is randomly generated. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: RandomFunction.java,v 1.3 2008/05/09 19:22:51 ingomierswa Exp $
 */
public class RandomFunction extends RegressionFunction {

    private RandomGenerator random;
    
    public void init(RandomGenerator random) {
        this.random = random;
    }
    
	public double calculate(double[] args) {
		return random.nextDouble();
	}
}
