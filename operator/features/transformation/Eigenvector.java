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
package com.rapidminer.operator.features.transformation;

/**
 * This class holds information about one eigenvector and eigenvalue.
 *  
 * @author Ingo Mierswa
 * @version $Id: Eigenvector.java,v 1.4 2008/05/09 19:22:52 ingomierswa Exp $
 */
public class Eigenvector implements ComponentVector {
        
    private static final long serialVersionUID = -624000556228376910L;
    
	private double eigenvalue;
    private double[] eigenvector;

    public Eigenvector(double[] eigenvector, double eigenvalue) {
        this.eigenvector = eigenvector;
        this.eigenvalue = eigenvalue;
    }

    public double[] getEigenvector() { 
        return this.eigenvector; 
    }
    
    public double getEigenvalue() { 
        return this.eigenvalue; 
    }

    public double[] getVector() {
        return getEigenvector();
    }

    public int compareTo(ComponentVector o) {
        return -1 * Double.compare(this.eigenvalue, o.getEigenvalue());
    }
    
    public boolean equals(Object o) {
    	if (!(o instanceof Eigenvector)) {
    		return false;
    	} else {
    		Eigenvector ev = (Eigenvector)o;
            for (int i = 0; i < this.eigenvector.length; i++) {
                if (this.eigenvector[i] != ev.eigenvector[i])
                    return false;
            }
    		return this.eigenvalue == ev.eigenvalue;
    	}
    }
    
    public int hashCode() {
    	return Double.valueOf(eigenvalue).hashCode() ^ this.eigenvector.hashCode();
    }
}
