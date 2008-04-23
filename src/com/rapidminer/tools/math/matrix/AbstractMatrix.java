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
 * @version $Id: AbstractMatrix.java,v 1.3 2007/05/28 21:23:33 ingomierswa Exp $
 * 
 */
public abstract class AbstractMatrix<Ex, Ey> extends ResultObjectAdapter implements ExtendedMatrix<Ex, Ey> {

    public String getName() {
        return "Matrix";
    }

    public Component getVisualizationComponent(IOContainer container) {
        return new MatrixVisualizer<Ex, Ey>(new IterationArrayList<Ex>(this.getXLabels()), new IterationArrayList<Ey>(this.getYLabels()), this);
    }

    public String toResultString() {
        return "A matrix with the following labels "+Tools.getLineSeparator()+" x:" + new IterationArrayList<Ex>(getXLabels()) + Tools.getLineSeparator() + " y:" + new IterationArrayList<Ey>(getYLabels());
    }

    public String getExtension() { return "mat"; }
    
    public String getFileDescription() { return "matrix"; }
    
}
