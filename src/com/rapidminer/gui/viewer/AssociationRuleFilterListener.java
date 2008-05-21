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
package com.rapidminer.gui.viewer;

/**
 * This listener can be used to be notified for association rule filtering changes.
 * 
 * @author Ingo Mierswa
 * @version $Id: AssociationRuleFilterListener.java,v 1.4 2008/05/09 19:23:01 ingomierswa Exp $
 */
public interface AssociationRuleFilterListener {

	public static final String[] CONJUNCTION_NAMES = new String[] {
		"And", 
		"Or"
	};
	
	public static final int CONJUNCTION_AND = 0;
	
	public static final int CONJUNCTION_OR  = 1;
	
	public void setFilter(boolean[] filter);
	
}
