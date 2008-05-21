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
package com.rapidminer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.rapidminer.gui.tools.SplashScreen;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.tools.XMLException;

/**
 * Former main program (before renaming YALE into RapidMiner). Entry point for the command 
 * line programm, the GUI and wrappers. Please note that applications which use RapidMiner 
 * as a data mining library will have to invoke one of the 
 * init methods provided by this class before applying processes or operators.
 * Several init methods exist and choosing the correct one with optimal parameters
 * might drastically reduce runtime and / or initialization time.
 * 
 * @author Ingo Mierswa
 * @version $Id: Yale.java,v 1.4 2008/05/09 19:23:20 ingomierswa Exp $
 */
public class Yale {

	/**
	 * @deprecated Use {@link RapidMiner#getVersion()} instead
	 */
	@Deprecated
	public static String getVersion() {
		return RapidMiner.getVersion();
	}

	/**
	 * @deprecated Use {@link RapidMiner#readProcessFile(File)} instead
	 */
	@Deprecated
	public static Process readExperimentFile(File experimentfile) throws XMLException, IOException, InstantiationException, IllegalAccessException {
		return RapidMiner.readProcessFile(experimentfile);
	}
	
	/**
	 * Initializes YALE. Will use the core operators.xml operator description, all 
	 * available Weka operators, and all JDBC drivers found in the directory 
	 * YALE_HOME/lib/jdbc. Will not search for JDBC drivers in other classpath
	 * libraries. Will use all plugins in the plugins directory. 
	 * Use the method {@link RapidMiner#init(InputStream, boolean, boolean, boolean, boolean)}
	 * for more sophisticated initialization possibilities. Alternatively, you could
	 * also set the following system properties, e.g. during startup via 
	 * &quot;-Dyale.init.weka=false&quot;:
	 * <ul>
	 * <li>yale.init.operators</li>
	 * <li>yale.init.plugins.location</li>
	 * <li>yale.init.weka</li>
	 * <li>yale.init.jdbc.lib</li>
	 * <li>yale.init.jdbc.classpath</li>
	 * <li>yale.init.plugins</li>
	 * </ul>
	 * 
	 * @throws IOException if something goes wrong during initialization
	 * @deprecated Use {@link RapidMiner#init()} instead
	 */
	@Deprecated
	public static void init() throws IOException {
		RapidMiner.init();
	}

	/**
	 * Initializes YALE with its core operators.
	 * 
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory YALE_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded
	 * @throws IOException if something goes wrong during initialization
	 * @deprecated Use {@link RapidMiner#init(boolean,boolean,boolean,boolean)} instead
	 */
	@Deprecated
	public static void init(boolean addWekaOperators, boolean searchJDBCInLibDir, boolean searchJDBCInClasspath, boolean addPlugins) throws IOException {
		RapidMiner.init(addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
	}
	
	/**
	 * Initializes YALE.
	 * 
	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory YALE_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded 
	 * @throws IOException if something goes wrong during initialization
	 * @deprecated Use {@link RapidMiner#init(InputStream,boolean,boolean,boolean,boolean)} instead
	 */
	@Deprecated
	public static void init(InputStream operatorsXMLStream, 
			                boolean addWekaOperators, 
			                boolean searchJDBCInLibDir, 
			                boolean searchJDBCInClasspath, 
			                boolean addPlugins) throws IOException {
								RapidMiner.init(operatorsXMLStream, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
							}
	
	/**
	 * Initializes YALE.
	 * 
	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
	 * @param pluginDir the directory where plugins are located, use core plugin directory if null
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory YALE_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded 
	 * @throws IOException if something goes wrong during initialization
	 * @deprecated Use {@link RapidMiner#init(InputStream,File,boolean,boolean,boolean,boolean)} instead
	 */
	@Deprecated
	public static void init(InputStream operatorsXMLStream, 
			                File pluginDir, 
			                boolean addWekaOperators, 
			                boolean searchJDBCInLibDir, 
			                boolean searchJDBCInClasspath, 
			                boolean addPlugins) throws IOException {
								RapidMiner.init(operatorsXMLStream, pluginDir, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
							}

    /** Cleans up the object visualizers available for this experiment and clears the 
	 *  current temp directory. This method should be performed in cases where YALE
	 *  is embedded into other applications and only single operators (in contrast to
	 *  a complete experiment) are performed within several runs, e.g. in a loop.
	 *  
	 *  TODO: bind object visualizers, log service, and temp file service to an 
	 *  experiment instead of managing these things in a static way.
	 * @deprecated Use {@link RapidMiner#cleanUp()} instead
	 */
	@Deprecated
	public static void cleanUp() {
		RapidMiner.cleanUp();
	}
    
	/**
	 * @deprecated Use {@link RapidMiner#showSplash()} instead
	 */
	@Deprecated
	public static SplashScreen showSplash() {
		return RapidMiner.showSplash();
	}

	/**
	 * @deprecated Use {@link RapidMiner#hideSplash()} instead
	 */
	@Deprecated
	public static void hideSplash() {
		RapidMiner.hideSplash();
	}

	/**
	 * @deprecated Use {@link RapidMiner#splashMessage(String)} instead
	 */
	@Deprecated
	public static void splashMessage(String message) {
		RapidMiner.splashMessage(message);
	}

	/**
	 * @deprecated Use {@link RapidMiner#setInputHandler(InputHandler)} instead
	 */
	@Deprecated
	public static void setInputHandler(InputHandler inputHandler) {
		RapidMiner.setInputHandler(inputHandler);
	}

	/**
	 * @deprecated Use {@link RapidMiner#getInputHandler()} instead
	 */
	@Deprecated
	public static InputHandler getInputHandler() {
		return RapidMiner.getInputHandler();
	}

	/** Returns a set of {@link ParameterType}s for the Yale system properties. 
	 * @deprecated Use {@link RapidMiner#getRapidMinerProperties()} instead*/
	@Deprecated
	public static java.util.Set<ParameterType> getYaleProperties() {
		return RapidMiner.getRapidMinerProperties();
	}

	/**
	 * @deprecated Use {@link RapidMiner#registerRapidMinerProperty(ParameterType)} instead
	 */
	@Deprecated
	public static void registerYaleProperty(ParameterType type) {
		RapidMiner.registerRapidMinerProperty(type);
	}

	/**
	 * @deprecated Use {@link RapidMiner#quit(int)} instead
	 */
	@Deprecated
	public static void quit(int errorcode) {
		RapidMiner.quit(errorcode);
	}
}
