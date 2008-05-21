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
package com.rapidminer.example.table;

import com.rapidminer.tools.Ontology;


/**
 * This class holds all information on a single binary attribute. In addition to
 * the generic attribute fields this class keeps information about the both
 * values and the value to index mappings. If one of the methods designed for
 * numerical attributes was invoked a RuntimeException will be thrown.
 * 
 * @author Ingo Mierswa
 * @version $Id: BinominalAttribute.java,v 1.5 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class BinominalAttribute extends NominalAttribute {

	private static final long serialVersionUID = 2932687830235332221L;
	
	private NominalMapping nominalMapping = new BinominalMapping();
	
	/**
	 * Creates a simple binary attribute which is not part of a series and does
	 * not provide a unit string.
	 */
	/* pp */ BinominalAttribute(String name) {
		super(name, Ontology.BINOMINAL);
	}

	/**
	 * Clone constructor.
	 */
	private BinominalAttribute(BinominalAttribute a) {
		super(a);
		//this.nominalMapping = (NominalMapping)a.nominalMapping.clone();
		this.nominalMapping = a.nominalMapping;
	}
	
	/** Clones this attribute. */
	public Object clone() {
		return new BinominalAttribute(this);
	}

	public NominalMapping getMapping() {
		return this.nominalMapping;
	}
	
	public void setMapping(NominalMapping newMapping) {
		this.nominalMapping = newMapping;
	}
}
