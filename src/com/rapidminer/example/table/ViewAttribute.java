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

import com.rapidminer.example.Attribute;
import com.rapidminer.example.NominalStatistics;
import com.rapidminer.example.NumericalStatistics;
import com.rapidminer.example.UnknownStatistics;
import com.rapidminer.operator.ViewModel;

/**
 * A view attribute is based on a ViewModel (Preprocessing Model) and
 * applies the model on the fly.
 *
 * @author Sebastian Land
 * @version $Id: ViewAttribute.java,v 1.8 2008/05/09 19:22:44 ingomierswa Exp $
 */
public class ViewAttribute extends AbstractAttribute {
	
	private static final long serialVersionUID = -4075558616549596028L;

	private NominalMapping mapping;

	private boolean isNominal;

	private ViewModel model;

	private Attribute parent;

	/* pp */ ViewAttribute(ViewAttribute other) {
		super(other);
		if (other.mapping != null)
			this.mapping = (NominalMapping) other.mapping.clone();
		this.isNominal = other.isNominal;
		this.model = other.model;
		if (other.parent != null)
			this.parent = (Attribute)other.parent.clone();
	}

	public ViewAttribute(ViewModel model, Attribute parent, String name, int valueType, NominalMapping mapping) {
		super(name, valueType);
		this.model = model;
		this.mapping = mapping;
		this.isNominal = mapping != null;
		this.parent = parent;
		if (isNominal) {
			registerStatistics(new NominalStatistics());
			registerStatistics(new UnknownStatistics());
		} else {
			registerStatistics(new NumericalStatistics());
			registerStatistics(new UnknownStatistics());
		}
	}

	public double getValue(DataRow row) {
		return model.getValue(this, row.get(parent));
	}

	public Object clone() {
		return new ViewAttribute(this);
	}

	public String getAsString(double value, int numberOfDigits, boolean quoteWhitespace) {
		if (isNominal) {
			if (Double.isNaN(value)) {
				return "?";
			} else {
				try {
					String result = mapping.mapIndex((int) value);
					if (quoteWhitespace) {
						if (result.indexOf(" ") >= 0) {
							result = "\"" + result + "\"";
						}
					}
					return result;
				} catch (Throwable e) {
					return "?";
				}
			}
		} else {
			if (Double.isNaN(value)) {
				return "?";
			} else {
				switch (numberOfDigits) {
				case NumericalAttribute.UNLIMITED_NUMBER_OF_DIGITS:
					return Double.toString(value);
				case NumericalAttribute.DEFAULT_NUMBER_OF_DIGITS:
					return com.rapidminer.tools.Tools.formatIntegerIfPossible(value, -1);
				default:
					return com.rapidminer.tools.Tools.formatIntegerIfPossible(value, numberOfDigits);
				}
			}
		}

	}

	public NominalMapping getMapping() {
		return mapping;
	}

	public boolean isNominal() {
		return isNominal;
	}

	public void setMapping(NominalMapping nominalMapping) {
		mapping = nominalMapping;
	}

	public int getTableIndex() {
		//return Attribute.VIEW_ATTRIBUTE_INDEX;
		// TODO: is this correct? without parent index, this might lead to problems,
		// e.g. for discretizations of the label inside of an AttributeSubsetPreprocessing
		// (index of label is then -1)
		return parent.getTableIndex();
	}
}
