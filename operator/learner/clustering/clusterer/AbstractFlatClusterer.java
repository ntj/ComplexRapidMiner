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
package com.rapidminer.operator.learner.clustering.clusterer;

import com.rapidminer.operator.OperatorDescription;

/**
 * Indicates that the resulting clusters will be flat.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractFlatClusterer.java,v 1.5 2008/09/12 10:31:37 tobiasmalbrecht Exp $
 */
public abstract class AbstractFlatClusterer extends AbstractClustering {

    public AbstractFlatClusterer(OperatorDescription description) {
        super(description);
    }

    public boolean isFlatClusterer() {
        return true;
    } 
}
