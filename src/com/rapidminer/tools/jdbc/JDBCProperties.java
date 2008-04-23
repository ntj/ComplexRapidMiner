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
package com.rapidminer.tools.jdbc;

/**
 * This class encapsulates the necessary information to build a JDBC connection string (url)
 * for a specific database system.
 *  
 * @author Ingo Mierswa
 * @version $Id: JDBCProperties.java,v 1.1 2007/05/27 22:02:53 ingomierswa Exp $
 */
public class JDBCProperties {

    private String name;
    private String defaultPort;
    private String urlPrefix;
    private String dbNameSeperator;
    private String integerName;
    private String realName;
    private String varcharName;
    
    public JDBCProperties(String name, String defaultPort, String urlPrefix, String dbNameSeperator,
            String varcharName, String integerName, String realName) {
        this.name = name;
        this.defaultPort = defaultPort;
        this.urlPrefix = urlPrefix;
        this.dbNameSeperator = dbNameSeperator;
        this.varcharName = varcharName;
        this.integerName = integerName;
        this.realName = realName;
    }

    public String getDbNameSeperator() {
        return dbNameSeperator;
    }

    public String getDefaultPort() {
        return defaultPort;
    }

    public String getName() {
        return name;
    }

    public String getUrlPrefix() {
        return urlPrefix;
    }
    
    public String getIntegerName() {
        return integerName;
    }
    
    public String getRealName() {
        return realName;
    }
    
    public String getVarcharName() {
        return varcharName;
    }
}
