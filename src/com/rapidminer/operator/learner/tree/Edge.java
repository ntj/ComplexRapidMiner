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
package com.rapidminer.operator.learner.tree;

import java.io.Serializable;

/**
 * The class edge holds the information about a split condition to a tree (child).
 * @author Sebastian Land, Ingo Mierswa
 * @version $Id: Edge.java,v 1.2 2007/06/24 14:30:54 ingomierswa Exp $
 */
public class Edge implements Serializable {
    
    private static final long serialVersionUID = -6470281011799533198L;
    
	private SplitCondition condition;
	
    private Tree child;
    
    public Edge(Tree child, SplitCondition condition) {
        this.condition = condition;
        this.child = child;
    }
    
    public SplitCondition getCondition() { 
        return this.condition; 
    }
    
    public Tree getChild() { 
        return this.child; 
    }
}
