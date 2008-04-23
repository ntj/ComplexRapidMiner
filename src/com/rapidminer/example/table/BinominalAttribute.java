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
package com.rapidminer.example.table;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Iterator;

import com.rapidminer.tools.Ontology;


/**
 * This class holds all information on a single binary attribute. In addition to
 * the generic attribute fields this class keeps information about the both
 * values and the value to index mappings. If one of the methods designed for
 * numerical attributes was invoked a RuntimeException will be thrown.
 * 
 * @author Ingo Mierswa
 * @version $Id: BinominalAttribute.java,v 1.1 2007/05/27 22:01:17 ingomierswa Exp $
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
		super(a.getName(), Ontology.BINOMINAL);
		this.nominalMapping = (NominalMapping)a.nominalMapping.clone();
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
	
	/**
	 * Overrides the super method and add information about the nominal values.
	 * Writes the (non transient) attribute data to an output stream.
	 */
	public void writeAttributeData(DataOutput out) throws IOException {
		super.writeAttributeData(out);
		out.writeInt(this.nominalMapping.size());
		Iterator i = this.nominalMapping.getValues().iterator();
		while (i.hasNext()) {
			out.writeUTF((String) i.next());
		}
	}

	/**
	 * Overrides the super method and reads information about the nominal
	 * values.
	 */
	public void readAttributeData(DataInput in) throws IOException {
		super.readAttributeData(in);
		int num = in.readInt();
		for (int i = 0; i < num; i++)
			this.nominalMapping.mapString(in.readUTF());
	}
}
