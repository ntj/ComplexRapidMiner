package com.rapidminer.tools.math.similarity.nominal;

/**
 * Implements the Kulczynski similarity for nominal attributes.
 * 
 * @author Sebastian Land, Michael Wurst
 * @version $Id: KulczynskiNominalSimilarity.java,v 1.1 2008/08/05 09:40:31 stiefelolm Exp $
 */
public class KulczynskiNominalSimilarity extends AbstractNominalSimilarity {

	protected double calculateSimilarity(double equalNonFalseValues, double unequalValues, double falseValues) {
		return equalNonFalseValues / unequalValues;
	}

}
