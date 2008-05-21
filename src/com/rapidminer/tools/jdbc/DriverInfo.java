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
package com.rapidminer.tools.jdbc;

import java.sql.Driver;

import com.rapidminer.tools.Tools;

/** Some basic information about a JDBC driver. 
 * 
 *  @author Ingo Mierswa
 *  @version $Id: DriverInfo.java,v 1.4 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class DriverInfo implements Comparable<DriverInfo> {
	
	private Driver driver;
	
	private String shortName;
	
	private String longName;
	
	public DriverInfo(Driver driver) {
		this.driver = driver;
		
        if (driver instanceof DriverAdapter) {
            this.shortName = driver.toString();                
        } else {
        	this.shortName = Tools.classNameWOPackage(driver.getClass());
        }
        
        if (driver instanceof DriverAdapter) {
            this.longName = ((DriverAdapter)driver).toLongString();             
        } else {
        	this.longName = driver.getClass().getName();
        }
	}
	
	public DriverInfo(String shortName) {
		this(shortName, null);
	}
	
	public DriverInfo(String shortName, String longName) {
		this.driver = null;
		this.shortName = shortName;
		this.longName = longName;
	}
	
	public Driver getDriver() {
		return this.driver;
	}
	
	public void setShortName(String shortName) {
		this.shortName = shortName;
	}
	
	public String getShortName() {
		return this.shortName;
	}
	
	public String getClassName() {
		return this.longName;
	}
	
	public String toString() {
		return getShortName() + " (" + getClassName() + ")";
	}

	public int compareTo(DriverInfo o) {
		return this.getShortName().compareTo(o.getShortName());
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DriverInfo))
			return false;
		DriverInfo a = (DriverInfo) o;
		if (!this.getShortName().equals(a.getShortName()))
			return false;
		return true;
	}
	
    public int hashCode() {
    	return this.getShortName().hashCode();
    }
}
