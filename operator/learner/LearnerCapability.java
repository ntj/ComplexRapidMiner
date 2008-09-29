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
package com.rapidminer.operator.learner;

import java.util.ArrayList;
import java.util.List;

/**
 * The possible capabilities for all learners.
 * 
 * @rapidminer.todo replace by Enumeration after change to Java 1.5
 * @author Julien Nioche, Ingo Mierswa
 * @version $Id: LearnerCapability.java,v 2.5 2006/03/27 13:22:01 ingomierswa
 *          Exp $
 */
public class LearnerCapability {

	public static final LearnerCapability POLYNOMINAL_ATTRIBUTES;

	public static final LearnerCapability BINOMINAL_ATTRIBUTES;

	public static final LearnerCapability NUMERICAL_ATTRIBUTES;

	public static final LearnerCapability POLYNOMINAL_CLASS;

	public static final LearnerCapability BINOMINAL_CLASS;

	public static final LearnerCapability NUMERICAL_CLASS;

	public static final LearnerCapability UPDATABLE;
    
    public static final LearnerCapability WEIGHTED_EXAMPLES;

	// TODO: remove after change to Enumerations
	private static List<LearnerCapability> ALL_CAPABILITIES = new ArrayList<LearnerCapability>();

	static {
		POLYNOMINAL_ATTRIBUTES = new LearnerCapability("polynominal attributes");
		ALL_CAPABILITIES.add(POLYNOMINAL_ATTRIBUTES);
		BINOMINAL_ATTRIBUTES = new LearnerCapability("binominal attributes");
		ALL_CAPABILITIES.add(BINOMINAL_ATTRIBUTES);
		NUMERICAL_ATTRIBUTES = new LearnerCapability("numerical attributes");
		ALL_CAPABILITIES.add(NUMERICAL_ATTRIBUTES);
		POLYNOMINAL_CLASS = new LearnerCapability("polynominal label");
		ALL_CAPABILITIES.add(POLYNOMINAL_CLASS);
		BINOMINAL_CLASS = new LearnerCapability("binominal label");
		ALL_CAPABILITIES.add(BINOMINAL_CLASS);
		NUMERICAL_CLASS = new LearnerCapability("numerical label");
		ALL_CAPABILITIES.add(NUMERICAL_CLASS);
		UPDATABLE = new LearnerCapability("updatable");
		ALL_CAPABILITIES.add(UPDATABLE);
        WEIGHTED_EXAMPLES = new LearnerCapability("weighted examples");
        ALL_CAPABILITIES.add(WEIGHTED_EXAMPLES);
	}

	private String description;

	public LearnerCapability(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public static LearnerCapability getCapability(int index) {
		return ALL_CAPABILITIES.get(index);
	}
	
	public static List<LearnerCapability> getAllCapabilities() {
		return ALL_CAPABILITIES;
	}
}
