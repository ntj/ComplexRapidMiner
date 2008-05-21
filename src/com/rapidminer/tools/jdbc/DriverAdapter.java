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

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.util.Properties;

import com.rapidminer.tools.Tools;

/**
 * This adaptor is needed for dynamical loading of JDBC drivers. It is not possible to use an
 * URLClassLoader and overload class.forName() specifying the ClassLoader for the driver management
 * necessary for creating drivers by Class.forName(). The DriverManager will refuse to use a driver 
 * not loaded by the system ClassLoader in this case. Therefore this adapter was implemented.
 * 
 * @author Ingo Mierswa
 * @version $Id: DriverAdapter.java,v 1.5 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class DriverAdapter implements Driver {

	private Driver driver;
	
	public DriverAdapter(Driver d) {
		this.driver = d;
	}
	
	public boolean acceptsURL(String u) throws SQLException {
		return this.driver.acceptsURL(u);
	}
	
	public Connection connect(String u, Properties p) throws SQLException {
		return this.driver.connect(u, p);
	}
	
	public int getMajorVersion() {
		return this.driver.getMajorVersion();
	}
	
	public int getMinorVersion() {
		return this.driver.getMinorVersion();
	}
	
	public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
		return this.driver.getPropertyInfo(u, p);
	}
	
	public boolean jdbcCompliant() {
		return this.driver.jdbcCompliant();
	}
	
	public String toString() {
		String result = Tools.classNameWOPackage(driver.getClass());
		int index = result.toLowerCase().indexOf("driver");
		if (index >= 0) {
			String newResult = "";
			newResult += result.substring(0, index);
			newResult += result.substring(index + "driver".length());
			result = newResult.trim();
		}
		if (result.trim().length() == 0) {
			result = "Unknown Driver";
		}
		return result;
	}
	
	public String toLongString() {
		return driver.getClass().getName();
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof DriverAdapter))
			return false;
		DriverAdapter a = (DriverAdapter) o;
		if (!this.driver.equals(a.driver))
			return false;
		return true;
	}
	
	public int hashCode() {
		return this.driver.hashCode();
	}
}
