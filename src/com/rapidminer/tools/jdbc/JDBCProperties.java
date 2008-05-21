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

/**
 * This class encapsulates the necessary information to build a JDBC connection string (url)
 * for a specific database system.
 *  
 * @author Ingo Mierswa
 * @version $Id: JDBCProperties.java,v 1.5 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class JDBCProperties {

    private String name;
    private String defaultPort;
    private String urlPrefix;
    private String dbNameSeperator;
    private String integerName;
    private String realName;
    private String varcharName;
    private String identifierQuoteOpen;
    private String identifierQuoteClose;
    
    public JDBCProperties(String name, 
    		String defaultPort, 
    		String urlPrefix, 
    		String dbNameSeperator,
            String varcharName, 
            String integerName, 
            String realName,
            String identifierQuoteOpen,
            String identifierQuoteClose) {
        this.name = name;
        this.defaultPort = defaultPort;
        this.urlPrefix = urlPrefix;
        this.dbNameSeperator = dbNameSeperator;
        this.varcharName = varcharName;
        this.integerName = integerName;
        this.realName = realName;
        this.identifierQuoteOpen = identifierQuoteOpen;
        this.identifierQuoteClose = identifierQuoteClose;
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
    
    public String getIdentifierQuoteOpen() {
    	return this.identifierQuoteOpen;
    }
    
    public String getIdentifierQuoteClose() {
    	return this.identifierQuoteClose;
    }
    
    public static JDBCProperties createDefaultJDBCProperties() {
    	return new JDBCProperties("unknown", "port", "urlprefix://", "/", "VARCHAR", "INTEGER", "REAL", "\"", "\"");
    }
}
