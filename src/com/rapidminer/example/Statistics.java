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
package com.rapidminer.example;

import java.io.Serializable;

/** The superclass for all attribute statistics objects. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: Statistics.java,v 1.3 2007/07/10 18:02:02 ingomierswa Exp $
 */
public interface Statistics extends Serializable {
    
    public static final String UNKNOWN  = "unknown";
    public static final String AVERAGE  = "average";
    public static final String VARIANCE = "variance";
    public static final String MINIMUM  = "minimum";
    public static final String MAXIMUM  = "maximum";
    public static final String MODE     = "mode";
    public static final String LEAST    = "least";
    public static final String COUNT    = "count";
    public static final String SUM      = "sum";
    
    public Object clone();
    
    public void startCounting(Attribute attribute);

    public void count(double value);
    
    public boolean handleStatistics(String statisticsName);
    
    public double getStatistics(String statisticsName, String parameter);
    
}
