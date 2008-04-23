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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;


/**
 * This service class dynamically registers (additional) JDBC drivers. Please note that drivers
 * cannot be created by Class.forName() but will just be instantiated automatically via
 * DriverManager.getConnection(...).
 *   
 * @author Ingo Mierswa
 * @version $Id: DatabaseService.java,v 1.1 2007/05/27 22:02:53 ingomierswa Exp $
 *
 */
public class DatabaseService {

    private static List<JDBCProperties> jdbcProperties = new ArrayList<JDBCProperties>();
    
	public static void init(boolean searchForJDBDriversInLibDirectory, 
			                boolean searchForJDBCDriversInClasspath) throws IOException {
		registerAllJDBCDrivers(searchForJDBDriversInLibDirectory, searchForJDBCDriversInClasspath);
		URL propertyURL = Tools.getResource("jdbc_properties.xml");
		if (propertyURL != null)
			loadJDBCProperties(propertyURL.openStream(), "jdbc_properties.xml");
	}
	
	private static void registerAllJDBCDrivers(boolean searchForJDBDriversInLibDirectory, boolean searchForJDBCDriversInClasspath) {
		if (searchForJDBDriversInLibDirectory) {
			File jdbcDirectory = ParameterService.getLibraryFile("jdbc");
			if ((jdbcDirectory != null) && (jdbcDirectory.exists())) {
				File[] allFiles = jdbcDirectory.listFiles();
				if (allFiles != null) {
					for (File f : allFiles) {
						if ((f.getName().endsWith(".jar")) || ((f.getName().endsWith(".zip")))) {
							registerDynamicJDBCDrivers(f);
						}
					}
				}
			}
		}

        if (searchForJDBCDriversInClasspath) {
        	String classpath = System.getProperty("java.class.path");
        	String pathComponents[] = classpath.split(File.pathSeparator);
        	for (int i = 0; i < pathComponents.length; i++) {
        		String path = pathComponents[i].trim();
        		if ((path.endsWith(".jar")) || ((path.endsWith(".zip")))) {
        			registerClasspathJDBCDrivers(new File(path).getAbsoluteFile());
        		}
        	}
        }
	}
	
	private static void registerDynamicJDBCDrivers(File file) {
		URLClassLoader ucl = null;
		try {
			URL u = new URL("jar:file:" + file.getAbsolutePath() + "!/");
			ucl = new URLClassLoader(new URL[] { u });
		} catch (MalformedURLException e) {
			LogService.getGlobal().log("DatabaseService: cannot create class loader for file '" + file + "': " + e.getMessage(), LogService.ERROR);	
		}
		
		if (ucl != null) {
			try {
				JarFile jarFile = new JarFile(file);
				List<String> driverNames = new LinkedList<String>();
				Tools.findImplementationsInJar(ucl, jarFile, java.sql.Driver.class, driverNames);
				Iterator<String> i = driverNames.iterator();
				while (i.hasNext()) {
					registerDynamicJDBCDriver(ucl, i.next());	
				}
			} catch (Exception e) {
				LogService.getGlobal().log("DatabaseService: cannot register drivers for file '" + file + "': " + e.getMessage(), LogService.ERROR);
			}
		}
	}
	
	private static void registerDynamicJDBCDriver(URLClassLoader ucl, String driverName) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        if (!driverName.equals(DriverAdapter.class.getName())) {
            Driver d = (Driver)Class.forName(driverName, true, ucl).newInstance();
            DriverManager.registerDriver(new DriverAdapter(d));	
        }
	}
    
    private static void registerClasspathJDBCDrivers(File file) {
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(file);
        } catch (IOException e) {
            LogService.getGlobal().log("DatabaseService: cannot register drivers from file '" + file + "': " + e.getMessage(), LogService.ERROR);
            return;
        }
        List<String> driverNames = new LinkedList<String>();
        Tools.findImplementationsInJar(jarFile, java.sql.Driver.class, driverNames);
        Iterator<String> i = driverNames.iterator();
        while (i.hasNext()) {
            String driverName = i.next();
            try {
                Class.forName(driverName);
            } catch (Exception e) {
                LogService.getGlobal().log("DatabaseService: cannot register driver '"+driverName+"' from file '" + file + "': " + e.getMessage(), LogService.ERROR);
            }
        }

    }

	private static void loadJDBCProperties(InputStream in, String name) {
        jdbcProperties.clear();
        LogService.getGlobal().log("Loading JDBC driver information from '" + name + "'.", LogService.INIT);
        Document document = null;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
        } catch (Exception e) {
            LogService.getGlobal().log("Cannot read JDBC driver description file '" + name + "': no valid XML: " + e.getMessage(), LogService.ERROR);
        }
        if (document != null) {
            if (!document.getDocumentElement().getTagName().toLowerCase().equals("drivers")) {
                LogService.getGlobal().log("JDBC driver description file '" + name + "': outermost tag must be <drivers>!", LogService.ERROR);
                return;
            }

            NodeList driverTags = document.getDocumentElement().getElementsByTagName("driver");
            for (int i = 0; i < driverTags.getLength(); i++) {
                Element currentElement = (Element) driverTags.item(i);
                try {
                    addDriverInformation(currentElement);
                } catch (Exception e) {
                    Attr currentNameAttr = currentElement.getAttributeNode("name");
                    if (currentNameAttr != null)
                        LogService.getGlobal().log("JDBC driver description: cannot register '" + currentNameAttr.getValue() + "': " + e, LogService.ERROR);
                    else
                        LogService.getGlobal().log("JDBC driver registration: cannot register '" + currentElement + "': " + e, LogService.ERROR);
                }
            }
        }	    
    }

    private static void addDriverInformation(Element driverElement) throws Exception {
        Attr nameAttr        = driverElement.getAttributeNode("name");
        Attr portAttr        = driverElement.getAttributeNode("defaultport");
        Attr urlAttr         = driverElement.getAttributeNode("urlprefix");
        Attr dbNameAttr      = driverElement.getAttributeNode("dbnameseperator");
        Attr varcharNameAttr = driverElement.getAttributeNode("type_varchar");
        Attr integerNameAttr = driverElement.getAttributeNode("type_integer");
        Attr realNameAttr    = driverElement.getAttributeNode("type_real");
        
        if (nameAttr == null)
            throw new Exception("Missing name for <driver> tag");
        if (portAttr == null)
            throw new Exception("Missing defaultport for <driver> tag");
        if (urlAttr == null)
            throw new Exception("Missing urlprefix for <driver> tag");
        if (dbNameAttr == null)
            throw new Exception("Missing dbnameseperator for <driver> tag");
        
        String varcharString = "VARCHAR";
        if (varcharNameAttr != null) {
            varcharString = varcharNameAttr.getValue();
        } else {
            LogService.getGlobal().logWarning("No definition of 'type_varchar' found for driver " + nameAttr.getValue() + ", using default (VARCHAR)...");
        }
        
        String integerString = "INTEGER";
        if (integerNameAttr != null) {
            integerString = integerNameAttr.getValue();
        } else {
            LogService.getGlobal().logWarning("No definition of 'type_integer' found for driver " + nameAttr.getValue() + ", using default (INTEGER)...");
        }
        
        String realString    = "REAL";
        if (realNameAttr != null) {
            realString = realNameAttr.getValue();
        } else {
            LogService.getGlobal().logWarning("No definition of 'type_real' found for driver " + nameAttr.getValue() + ", using default (REAL)...");
        }
        
        JDBCProperties properties = 
            new JDBCProperties(nameAttr.getValue(), 
                               portAttr.getValue(), 
                               urlAttr.getValue(), 
                               dbNameAttr.getValue(),
                               varcharString,
                               integerString,
                               realString);
        jdbcProperties.add(properties);
    }
    
	public static Enumeration<Driver> getAllDrivers() {
		return DriverManager.getDrivers();
	}
	
	public static String[] getAllDriverNames() {
		Enumeration<Driver> drivers = getAllDrivers();
		List<String> driverNameList = new LinkedList<String>();
		while (drivers.hasMoreElements()) {
		    Driver driver = drivers.nextElement();
            if (driver instanceof DriverAdapter) {
                driverNameList.add(driver.toString());                
            } else {
                driverNameList.add(driver.getClass().getName());
            }
        }

        Collections.sort(driverNameList);
		String[] driverArray = new String[driverNameList.size()];
		driverNameList.toArray(driverArray);
		return driverArray;
	}
    
    public static List<JDBCProperties> getJDBCProperties() {
        return jdbcProperties;
    }
    
    public static String[] getDBSystemNames() {
        String[] names = new String[jdbcProperties.size()];
        int counter = 0;
        Iterator<JDBCProperties> i = jdbcProperties.iterator();
        while (i.hasNext()) {
            names[counter++] = i.next().getName();
        }
        return names;
    }
}
