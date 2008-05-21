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
package com.rapidminer.tools.math.matrix;

import java.awt.Component;

import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;
import com.rapidminer.tools.IterationArrayList;
import com.rapidminer.tools.Tools;


/**
 * Abstract class that implements basic matrix operations as visualization.
 * 
 * @author Michael Wurst, Ingo Mierswa
 * @version $Id: AbstractMatrix.java,v 1.6 2008/05/09 19:23:23 ingomierswa Exp $
 * 
 */
public abstract class AbstractMatrix<Ex, Ey> extends ResultObjectAdapter implements Matrix<Ex, Ey> {

    public String getName() {
        return "Matrix";
    }

    public Component getVisualizationComponent(IOContainer container) {
        return new MatrixVisualizer<Ex, Ey>(this);
    }

    public String toResultString() {
        return "A matrix with the following labels "+Tools.getLineSeparator()+" x:" + new IterationArrayList<Ex>(getXLabels()) + Tools.getLineSeparator() + " y:" + new IterationArrayList<Ey>(getYLabels());
    }

    public String getExtension() { return "mat"; }
    
    public String getFileDescription() { return "matrix"; }
    
}
