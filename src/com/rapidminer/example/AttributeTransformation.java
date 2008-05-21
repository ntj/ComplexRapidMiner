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


/** This interface is used to define on-the-fly transformations in data views. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: AttributeTransformation.java,v 1.3 2008/05/09 19:22:43 ingomierswa Exp $
 */
public interface AttributeTransformation {

	public Object clone();
	
	public double transform(Attribute attribute, double value);
	
	public double inverseTransform(Attribute attribute, double value);
	
	public boolean isReversable();
	
}
