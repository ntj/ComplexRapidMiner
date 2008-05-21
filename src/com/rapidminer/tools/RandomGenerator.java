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
package com.rapidminer.tools;

import java.util.Random;

import com.rapidminer.Process;
import com.rapidminer.operator.ProcessRootOperator;
import com.rapidminer.parameter.UndefinedParameterError;


/**
 * The global random number generator. This should be used for all random
 * purposes of RapidMiner to ensure that two runs of the same process setup provide the
 * same results.
 * 
 * @author Ralf Klinkenberg, Ingo Mierswa
 * @version $Id: RandomGenerator.java,v 1.8 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class RandomGenerator extends Random {

	private static final long serialVersionUID = 7562534107359981433L;

	/** Use this alphabet for random String creation. */
	private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

	/**
	 * Global random number generator using the random number generator seed
	 * specified for the root operator (ProcessRootOperator).
	 */
	private static RandomGenerator globalRandomGenerator = new RandomGenerator(2001);

	/** Initializes the random number generator without a seed. */
	private RandomGenerator() {
		super();
	}

	/** Initializes the random number generator with the given <code>seed</code> */
	public RandomGenerator(long seed) {
		super(seed);
	}

	// ================================================================================

    /** Returns the global random number generator. */
    public static RandomGenerator getGlobalRandomGenerator() {
        return getRandomGenerator(null, -1);
    }
    
	/** Returns the global random number generator if the seed is negative and a new RandomGenerator
     *  with the given seed if the seed is positive or zero. This way is is possible to allow for local
     *  random seeds. Operators like learners or validation operators should definitely make use of such
     *  a local random generator. */
	public static RandomGenerator getRandomGenerator(int seed) {
        return getRandomGenerator(null, seed);
    }
    
	/** Returns the global random number generator if the seed is negative and a new RandomGenerator
	 *  with the given seed if the seed is positive or zero. This way is is possible to allow for local
	 *  random seeds. Operators like learners or validation operators should definitely make use of such
	 *  a local random generator. */
	public static RandomGenerator getRandomGenerator(Process process, int seed) {
        if (seed < 0) {
        	if (globalRandomGenerator == null) { // might happen
        		init(process);
            }
            return globalRandomGenerator;
        } else {
            return new RandomGenerator(seed);
        }
	}

	/**
	 * Instantiates the global random number generator and initializes it with
	 * the random number generator seed specified in the <code>global</code>
	 * section of the configuration file. Should be invoked before the
	 * process starts.
	 */
	public static void init(Process process) {
		long seed = 2001;
        if (process != null) {
            try {
                seed = process.getRootOperator().getParameterAsInt(ProcessRootOperator.PARAMETER_RANDOM_SEED);
            } catch (UndefinedParameterError e) {
                // tries to read the general random seed
                // if no seed was specified (cannot happen) use seed 2001
                seed = 2001;
            }
        }
		if (seed == -1) // could be from process parameter
			globalRandomGenerator = new RandomGenerator();
		else
			globalRandomGenerator = new RandomGenerator(seed);
	}

	// ================================================================================

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>double</code>
	 * value between <code>lowerBound</code> and <code>upperBound</code>
	 * from this random number generator's sequence (exclusive of the interval
	 * endpoint values).
	 */
	public double nextDoubleInRange(double lowerBound, double upperBound) {
		if (upperBound <= lowerBound) {
			throw new IllegalArgumentException("RandomGenerator.nextDoubleInRange : the upper bound of the " + "random number range should be greater than the lower bound.");
		}
		return ((nextDouble() * (upperBound - lowerBound)) + lowerBound);
	}

	/**
	 * returns the next pseudorandom, uniformly distributed <code>long</code>
	 * value between <code>lowerBound</code> and <code>upperBound</code>
	 * from this random number generator's sequence (exclusive of the interval
	 * endpoint values).
	 */
	public long nextLongInRange(long lowerBound, long upperBound) {
		if (upperBound <= lowerBound) {
			throw new IllegalArgumentException("RandomGenerator.nextLongInRange : the upper bound of the " + "random number range should be greater than the lower bound.");
		}
		return ((long) (nextDouble() * (upperBound - lowerBound + 1)) + lowerBound);
	}

	/**
	 * Returns the next pseudorandom, uniformly distributed <code>int</code>
	 * value between <code>lowerBound</code> and <code>upperBound</code>
	 * from this random number generator's sequence (lower bound inclusive, upper 
	 * bout exclusive).
	 */
	public int nextIntInRange(int lowerBound, int upperBound) {
		if (upperBound <= lowerBound) {
			throw new IllegalArgumentException("RandomGenerator.nextIntInRange : the upper bound of the " + "random number range should be greater than the lower bound.");
		}
		return ((int) (nextDouble() * (upperBound - lowerBound + 1)) + lowerBound);
	}

	/** Returns a random String of the given length. */
	public String nextString(int length) {
		char[] chars = new char[length];
		for (int i = 0; i < chars.length; i++)
			chars[i] = ALPHABET.charAt(nextInt(ALPHABET.length()));
		return new String(chars);
	}

	/**
	 * Returns a randomly selected integer between 0 and the length of the given
	 * array. Uses the given probabilities to determine the index, all values in
	 * this array must sum up to 1.
	 */
	public int randomIndex(double[] probs) {
		double r = nextDouble();
		double sum = 0.0d;
		for (int i = 0; i < probs.length; i++) {
			sum += probs[i];
			if (r < sum)
				return i;
		}
		return probs.length - 1;
	}
}
