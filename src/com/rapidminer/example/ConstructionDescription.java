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
package com.rapidminer.example;

import java.io.Serializable;

/**
 * This class encapsulates all information about the construction
 * of the corresponding attribute.
 * 
 * @author Ingo Mierswa
 * @version $Id: ConstructionDescription.java,v 1.6 2008/05/09 19:22:43 ingomierswa Exp $
 */
public class ConstructionDescription implements Serializable {

	private static final long serialVersionUID = 3037807970685835836L;

	/** Name of the function if this attribute was generated. */
	private String generatingFunctionName = null;

	/**
	 * If this attribute was generated, this array of attributes holds the input
	 * arguments of the generation.
	 */
	private ConstructionDescription[] generatingFunctionArguments = null;
	
	/** The attribute for which this description holds. */
	private Attribute attribute;
	
	
	/** Creates a basic construction description. */
	public ConstructionDescription(Attribute attribute) {
		this.attribute = attribute;
	}
	
	private ConstructionDescription(ConstructionDescription other) {
		this.attribute = other.attribute;
		this.generatingFunctionName = other.generatingFunctionName;
		if (other.generatingFunctionArguments != null) {
			this.generatingFunctionArguments = new ConstructionDescription[other.generatingFunctionArguments.length];
			for (int i = 0; i < other.generatingFunctionArguments.length; i++) {
				this.generatingFunctionArguments[i] = (ConstructionDescription)other.generatingFunctionArguments[i].clone();
			}
		}			
	}
	
	public Object clone() {
		return new ConstructionDescription(this);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof ConstructionDescription)) {
			return false;
		}

		ConstructionDescription other = (ConstructionDescription)o;
		if (generatingFunctionName == null) {
			if (other.generatingFunctionName != null)
				return false;
		} else {
			if (!generatingFunctionName.equals(other.generatingFunctionName))
				return false;
		}
		if ((generatingFunctionArguments == null) && (other.generatingFunctionArguments != null))
			return false;
		if ((generatingFunctionArguments != null) && (other.generatingFunctionArguments == null))
			return false;
		if ((generatingFunctionArguments != null) && (other.generatingFunctionArguments != null)) {
			if (generatingFunctionArguments.length != other.generatingFunctionArguments.length)
				return false;
			for (int i = 0; i < generatingFunctionArguments.length; i++) {
				if (!generatingFunctionArguments[i].equals(other.generatingFunctionArguments[i]))
					return false;
			}
		}
		return true;
	}
	
	public int hashCode() {
		int hashCode = super.hashCode();
		if (generatingFunctionName != null)
			hashCode ^= generatingFunctionName.hashCode();
		if (generatingFunctionArguments != null)
			hashCode ^= generatingFunctionArguments.hashCode();
		return hashCode;
	}
	
	/** Sets the name of the function that generated this attribute. */
	public void setFunction(String functionName) {
		this.generatingFunctionName = functionName;
	}

	/** Returns the name of the function that generated this attribute. */
	public String getFunction() {
		return generatingFunctionName;
	}

	public Attribute getAttribute() {
		return attribute;
	}
	
	/** Sets the arguments that were used to generate this attribute. */
	public void setArguments(ConstructionDescription[] arguments) {
		this.generatingFunctionArguments = arguments;
	}

	/** Returns the arguments that were used to generate this attribute. */
	public ConstructionDescription[] getArguments() {
		return generatingFunctionArguments;
	}

	/**
	 * Returns a string that describes how this attribute was generated from
	 * other attributes.
	 */
	public String getDescription() {
		return getDescription(true);
	}

	/**
	 * Returns a string that describes how this attribute was generated from
	 * other attributes.
	 * 
	 * @param useInfix
	 *            Whether or not to use infix notation for binary generators
	 */
	public String getDescription(boolean useInfix) {
		boolean infixPossible = (generatingFunctionArguments != null) && (generatingFunctionArguments.length == 2);
		if (generatingFunctionArguments == null) {
			if (generatingFunctionName != null)
				return this.generatingFunctionName;
			else
				return this.attribute.getName();
		} else {
			if (generatingFunctionName == null) {
				return this.attribute.getName();
			} else {
				if ((infixPossible) && (useInfix)) {
					return "(" + generatingFunctionArguments[0].getDescription(useInfix) + generatingFunctionName + generatingFunctionArguments[1].getDescription(useInfix) + ")";
				} else {
					StringBuffer cd = new StringBuffer(generatingFunctionName + "(");
					for (int i = 0; i < generatingFunctionArguments.length; i++)
						cd.append((i == 0 ? "" : ", ") + generatingFunctionArguments[i].getDescription(useInfix));
					cd.append(")");
					return cd.toString();
				}
			}
		}
	}

	/** Returns the depth of the syntax tree of the construction description. */
	public int getDepth() {
		if (!isGenerated())
			return 0;
		else {
			int max = -1;
			for (int i = 0; i < generatingFunctionArguments.length; i++) {
				max = Math.max(max, generatingFunctionArguments[i].getDepth());
			}
			return max + 1;
		}
	}

	/** Returns true iff this attribute was generated. */
	public boolean isGenerated() {
		return generatingFunctionName != null;
	}
	
	/** Clears the construction description. */
	public void clear() {
		this.generatingFunctionName = null;
		this.generatingFunctionArguments = null;
	}
	
	public String toString() {
		return getDescription(true);
	}
}
