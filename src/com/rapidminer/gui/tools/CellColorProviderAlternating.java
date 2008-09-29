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
package com.rapidminer.gui.tools;

import java.awt.Color;

/**
 * This provider delivers an alternating color scheme.
 * 
 * @author Ingo Mierswa
 * @version $Id: CellColorProviderAlternating.java,v 1.1 2008/08/25 08:10:40 ingomierswa Exp $
 */
public class CellColorProviderAlternating implements CellColorProvider {

	public Color getCellColor(int row, int column) {
        if (row % 2 == 0)
            return Color.WHITE;
        else
            return SwingTools.LIGHTEST_BLUE;
	}
	
}

