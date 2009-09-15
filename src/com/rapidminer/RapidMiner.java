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

import java.awt.Frame;
import java.awt.Image;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

import javax.imageio.ImageIO;

import com.rapidminer.gui.renderer.RendererService;
import com.rapidminer.gui.tools.SplashScreen;
import com.rapidminer.parameter.ParameterType;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.parameter.ParameterTypeString;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ObjectVisualizerService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.WekaTools;
import com.rapidminer.tools.XMLException;
import com.rapidminer.tools.XMLSerialization;
import com.rapidminer.tools.cipher.CipherTools;
import com.rapidminer.tools.cipher.KeyGenerationException;
import com.rapidminer.tools.cipher.KeyGeneratorTool;
import com.rapidminer.tools.jdbc.DatabaseService;
import com.rapidminer.tools.plugin.Plugin;

/**
 * Main program. Entry point for command line programm, GUI and wrappers. Please note 
 * that applications which use RapidMiner as a data mining library will have to invoke one of the 
 * init methods provided by this class before applying processes or operators.
 * Several init methods exist and choosing the correct one with optimal parameters
 * might drastically reduce runtime and / or initialization time.
 * 
 * @author Ingo Mierswa
 * @version $Id: RapidMiner.java,v 1.38 2008/08/12 17:23:38 ingomierswa Exp $
 */
public class RapidMiner {

    public static final String SYSTEM_ENCODING_NAME = "SYSTEM";
    
    
    // ---  GENERAL PROPERTIES  ---
    
    /** The name of the property indicating the home directory of RapidMiner. */
    public static final String PROPERTY_RAPIDMINER_HOME = "rapidminer.home";

    /** The name of the property indicating the version of RapidMiner. */
    public static final String PROPERTY_RAPIDMINER_VERSION = "rapidminer.version";
    
    /** The name of the property indicating the path to an additional operator description XML file. */
    public static final String PROPERTY_RAPIDMINER_OPERATORS_ADDITIONAL = "rapidminer.operators.additional";

    /** The name of the property indicating the path to an RC file (settings). */
    public static final String PROPERTY_RAPIDMINER_RC_FILE = "rapidminer.rcfile";
    
    /** The name of the property indicating the path to the Weka Jar file. */
    public static final String PROPERTY_RAPIDMINER_WEKA_JAR = "rapidminer.weka.jar";

    /** The name of the property indicating the path to the global logging file. */
    public static final String PROPERTY_RAPIDMINER_GLOBAL_LOG_FILE = "rapidminer.global.logging.file";

    /** The name of the property indicating the path to the global logging file. */
    public static final String PROPERTY_RAPIDMINER_GLOBAL_LOG_VERBOSITY = "rapidminer.global.logging.verbosity";
    
    
    // ---  INIT PROPERTIES  ---
    
    /** A file path to an operator description XML file. */
    public static final String PROPERTY_RAPIDMINER_INIT_OPERATORS = "rapidminer.init.operators";
    
    /** Boolean parameter indicating if the operators based on Weka should be initialized. */
    public static final String PROPERTY_RAPIDMINER_INIT_WEKA = "rapidminer.init.weka";

    /** A file path to the directory containing the JDBC drivers (usually the lib/jdbc directory of RapidMiner). */
    public static final String PROPERTY_RAPIDMINER_INIT_JDBC_LIB_LOCATION = "rapidminer.init.jdbc.location";
    
    /** Boolean parameter indicating if the drivers located in the lib directory of RapidMiner should be initialized. */
    public static final String PROPERTY_RAPIDMINER_INIT_JDBC_LIB = "rapidminer.init.jdbc.lib";
    
    /** Boolean parameter indicating if the drivers located somewhere in the classpath should be initialized. */
    public static final String PROPERTY_RAPIDMINER_INIT_JDBC_CLASSPATH = "rapidminer.init.jdbc.classpath";
    
    /** Boolean parameter indicating if the plugins should be initialized at all. */
    public static final String PROPERTY_RAPIDMINER_INIT_PLUGINS = "rapidminer.init.plugins";
    
    /** A file path to the directory containing the plugin Jar files. */
    public static final String PROPERTY_RAPIDMINER_INIT_PLUGINS_LOCATION = "rapidminer.init.plugins.location";
    
    
    // ---  OTHER PROPERTIES  ---
    
    /** The property name for &quot;The number of fraction digits of formatted numbers.&quot; */
    public static final String PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS = "rapidminer.general.fractiondigits.numbers";

    /** The property name for &quot;The number of fraction digits of formatted percent values.&quot; */
    public static final String PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_PERCENT = "rapidminer.general.fractiondigits.percent";
    
	/** The property name for &quot;Path to external Java editor. %f is replaced by filename and %l by the linenumber.&quot; */
	public static final String PROPERTY_RAPIDMINER_TOOLS_EDITOR = "rapidminer.tools.editor";

	/** The property name for &quot;Path to sendmail. Used for email notifications.&quot; */
	public static final String PROPERTY_RAPIDMINER_TOOLS_SENDMAIL_COMMAND = "rapidminer.tools.sendmail.command";

	/** The property name for &quot;Use unix special characters for logfile highlighting (requires new RapidMiner instance).&quot; */
	public static final String PROPERTY_RAPIDMINER_GENERAL_LOGFILE_FORMAT = "rapidminer.general.logfile.format";

	/** The property name for &quot;Indicates if RapidMiner should be used in debug mode (print exception stacks and shows more technical error messages)&quot; */
	public static final String PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE = "rapidminer.general.debugmode";
    
    /** The name of the property indicating the default encoding for files. */
    public static final String PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING = "rapidminer.general.encoding";

    public static boolean isInitialized = false;
    
	/**
	 * A set of some non-gui and operator related system properties (starting with "rapidminer."). Properties
	 * can be registered using {@link RapidMiner#registerRapidMinerProperty(ParameterType)}.
	 */
	private static final java.util.Set<ParameterType> PROPERTY_TYPES = new java.util.TreeSet<ParameterType>();

	static {
		System.setProperty(PROPERTY_RAPIDMINER_VERSION, RapidMiner.getLongVersion());
        registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_NUMBERS, "The number of fraction digits of formatted numbers.", 0, Integer.MAX_VALUE, 3));
        registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GENERAL_FRACTIONDIGITS_PERCENT, "The number of fraction digits of formatted percent values.", 0, Integer.MAX_VALUE, 2));
		registerRapidMinerProperty(new ParameterTypeString(PROPERTY_RAPIDMINER_TOOLS_EDITOR, "Path to external Java editor. %f is replaced by filename and %l by the linenumber.", true));
		registerRapidMinerProperty(new ParameterTypeString(PROPERTY_RAPIDMINER_TOOLS_SENDMAIL_COMMAND, "Path to sendmail. Used for email notifications.", true));
		registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GENERAL_LOGFILE_FORMAT, "Use unix special characters for logfile highlighting (requires new RapidMiner instance).", false));
		registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GENERAL_DEBUGMODE, "Indicates if RapidMiner should be used in debug mode (print exception stacks and shows more technical error messages)", false));
		registerRapidMinerProperty(new ParameterTypeString(PROPERTY_RAPIDMINER_GENERAL_DEFAULT_ENCODING, "The default encoding used for file operations (default: 'SYSTEM' uses the underlying system encoding, 'UTF-8' or 'ISO-8859-1' are other common options)", SYSTEM_ENCODING_NAME));
	}
	
	private static InputHandler inputHandler = new ConsoleInputHandler();
	
	private static SplashScreen splashScreen;
	
	public static String getShortVersion() {
		return Version.getShortVersion();
	}

	public static String getLongVersion() {
		return Version.getLongVersion();
	}
	
	/**
	 * @deprecated Use {@link #readProcessFile(File)} instead
	 */
	@Deprecated
	public static Process readExperimentFile(File experimentfile) throws XMLException, IOException, InstantiationException, IllegalAccessException {
		return readProcessFile(experimentfile);
	}

	public static Process readProcessFile(File processFile) throws XMLException, IOException, InstantiationException, IllegalAccessException {
		try {
			LogService.getGlobal().log("Reading process file '" + processFile + "'.", LogService.STATUS);
			if (!processFile.exists() || !processFile.canRead()) {
				LogService.getGlobal().log("Cannot read config file '" + processFile + "'!", LogService.FATAL);
			}
			return new Process(processFile);
		} catch (XMLException e) {
			throw new XMLException(processFile.getName() + ":" + e.getMessage());
		}
	}

	/**
	 * Initializes RapidMiner.
	 * 
	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
	 * @param pluginDir the directory where plugins are located, use core plugin directory if null
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded 
	 */
	public static void init(InputStream operatorsXMLStream, 
			                File pluginDir, 
			                boolean addWekaOperators, 
			                boolean searchJDBCInLibDir, 
			                boolean searchJDBCInClasspath, 
			                boolean addPlugins) {
		init(operatorsXMLStream, null, pluginDir, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
	}

	/**
	 * Initializes RapidMiner.
	 * 
	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
	 * @param pluginDir the directory where plugins are located, use core plugin directory if null
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded 
	 */
	public static void init(InputStream operatorsXMLStream, 
			                File pluginDir,
			                File jdbcDir,
			                boolean addWekaOperators, 
			                boolean searchJDBCInLibDir, 
			                boolean searchJDBCInClasspath, 
			                boolean addPlugins) {
		init(operatorsXMLStream, null, pluginDir, jdbcDir, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
	}
	
	/**
	 * Initializes RapidMiner.
	 * 
	 * @param operatorsXMLStream the stream to the core operators.xml (operator description), use core operators.xml if null
	 * @param additionalXMLStream the stream to possibly additional operators.xml (operator description), use no additional if null
	 * @param pluginDir the directory where plugins are located, use core plugin directory if null
	 * @param jdbcDir the directory where the JDBC drivers are located, use core lib/jdbc directory if null
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded 
	 */
	public static void init(InputStream operatorsXMLStream,
							InputStream additionalXMLStream,
			                File pluginDir,
			                File jdbcDir,
			                boolean addWekaOperators, 
			                boolean searchJDBCInLibDir, 
			                boolean searchJDBCInClasspath, 
			                boolean addPlugins) {		
		isInitialized = true;
	    // set locale fix to US
	    RapidMiner.splashMessage("Using US Local");
	    Locale.setDefault(Locale.US);
	    
		// ensure rapidminer.home is set
	    RapidMiner.splashMessage("Ensure RapidMiner Home is set");
		ParameterService.ensureRapidMinerHomeSet();
		
	    if (addPlugins) {
	    	RapidMiner.splashMessage("Register Plugins");
	    	Plugin.registerAllPlugins(pluginDir, true);
	    }
	    Plugin.initPluginSplashTexts();
	    RapidMiner.showSplashInfos();
	    
	    
		RapidMiner.splashMessage("Init Setup");
		
		// search for Weka
		File wekaJar = null;
		try {
			wekaJar = WekaTools.getWekaJarAsFile();
		} catch (Throwable e) {
			// do nothing
		}
		
		String wekaMessage = addWekaOperators + "";
		if ((wekaJar == null) || (!wekaJar.exists())) {
			wekaMessage = "weka not found";
			addWekaOperators = false;
		}
		
		LogService.getGlobal().log("----------------------------------------------------", LogService.INIT);
		LogService.getGlobal().log("Initialization Settings", LogService.INIT);
		LogService.getGlobal().log("----------------------------------------------------", LogService.INIT);
		LogService.getGlobal().log("Default system encoding for IO: " + Tools.getDefaultEncoding(), LogService.INIT);
		LogService.getGlobal().log("Load " + (operatorsXMLStream == null ? "core" : "specific") + " operators...", LogService.INIT);
	    LogService.getGlobal().log("Load Weka operators: " + wekaMessage, LogService.INIT);
		LogService.getGlobal().log("Load JDBC drivers from lib directory: " + searchJDBCInLibDir, LogService.INIT);
		LogService.getGlobal().log("Load JDBC drivers from classpath: " + searchJDBCInClasspath, LogService.INIT);
	    LogService.getGlobal().log("Load plugins: " + addPlugins, LogService.INIT);
	    LogService.getGlobal().log("Load plugins from '" + (pluginDir == null ? ParameterService.getPluginDir() : pluginDir) + "'", LogService.INIT);
	    LogService.getGlobal().log("----------------------------------------------------", LogService.INIT);
	    
	    RapidMiner.splashMessage("Initializing Operators");
		ParameterService.init(operatorsXMLStream, additionalXMLStream, addWekaOperators);
		
	    RapidMiner.splashMessage("Loading JDBC Drivers");
	    DatabaseService.init(jdbcDir, searchJDBCInLibDir, searchJDBCInClasspath);
	    
		RapidMiner.splashMessage("Initialize XML serialization");
		XMLSerialization.init(Plugin.getMajorClassLoader());
		
		RapidMiner.splashMessage("Define XML Serialization Alias Pairs");
		OperatorService.defineXMLAliasPairs();
		
		// generate encryption key if necessary
		if (!CipherTools.isKeyAvailable()) {
			RapidMiner.splashMessage("Generate Encryption Key");
			try {
				KeyGeneratorTool.createAndStoreKey();
			} catch (KeyGenerationException e) {
				LogService.getGlobal().logError("Cannot generate encryption key: " + e.getMessage());
			}
		}
		
		// initialize renderers
		RapidMiner.splashMessage("Initialize renderers");
		RendererService.init();
	}

	private static void showSplashInfos() {
		if (getSplashScreen() != null)
			getSplashScreen().setInfosVisible(true);
	}

	/**
	 * Initializes RapidMiner.
	 * 
	 * @param operatorsXMLStream the stream to the operators.xml (operator description), use core operators.xml if null
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded 
	 */
	public static void init(InputStream operatorsXMLStream, 
			                boolean addWekaOperators, 
			                boolean searchJDBCInLibDir, 
			                boolean searchJDBCInClasspath, 
			                boolean addPlugins) {
		init(operatorsXMLStream, null, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
	}

	/**
	 * Initializes RapidMiner with its core operators.
	 * 
	 * @param addWekaOperators inidcates if the operator wrappers for Weka should be loaded
	 * @param searchJDBCInLibDir indicates if JDBC drivers from the directory RAPID_MINER_HOME/lib/jdbc should be loaded
	 * @param searchJDBCInClasspath indicates if JDBC drivers from the classpath libraries should be loaded
	 * @param addPlugins indicates if the plugins should be loaded
	 */
	public static void init(boolean addWekaOperators, boolean searchJDBCInLibDir, boolean searchJDBCInClasspath, boolean addPlugins) {
		init(null, addWekaOperators, searchJDBCInLibDir, searchJDBCInClasspath, addPlugins);
	}
    
	/**
	 * Initializes RapidMiner. Will use the core operators.xml operator description, all 
	 * available Weka operators, and all JDBC drivers found in the directory 
	 * RAPID_MINER_HOME/lib/jdbc. Will not search for JDBC drivers in other classpath
	 * libraries. Will use all plugins in the plugins directory. 
	 * Use the method {@link #init(InputStream, File, boolean, boolean, boolean, boolean)}
	 * for more sophisticated initialization possibilities. Alternatively, you could
	 * also set the following system properties, e.g. during startup via 
	 * &quot;-Drapidminer.init.weka=false&quot; or with {@link System#setProperty(String, String)}:
	 * <ul>
	 * <li>rapidminer.init.operators (file path)</li>
	 * <li>rapidminer.init.plugins (true or false)</li>
	 * <li>rapidminer.init.plugins.location (directory path)</li>
	 * <li>rapidminer.init.weka (true or false)</li>
	 * <li>rapidminer.init.jdbc.lib (true or false)</li>
	 * <li>rapidminer.init.jdbc.lib.location (directory path)</li>
	 * <li>rapidminer.init.jdbc.classpath (true or false)</li>
	 * </ul>
	 */
	public static void init() {		
		File pluginDir = null;
		String pluginDirString = System.getProperty(PROPERTY_RAPIDMINER_INIT_PLUGINS_LOCATION);
		if (pluginDirString != null)
			pluginDir = new File(pluginDirString);
		
	    String loadWekaString = System.getProperty(PROPERTY_RAPIDMINER_INIT_WEKA);
	    boolean loadWeka = Tools.booleanValue(loadWekaString, true);
	    
	    String loadJDBCDirString = System.getProperty(PROPERTY_RAPIDMINER_INIT_JDBC_LIB);
	    boolean loadJDBCDir = Tools.booleanValue(loadJDBCDirString, true);
	    
		File jdbcDir = null;
		String jdbcDirString = System.getProperty(PROPERTY_RAPIDMINER_INIT_JDBC_LIB_LOCATION);
		if (jdbcDirString != null)
			jdbcDir = new File(jdbcDirString);
	    
	    String loadJDBCClasspathString = System.getProperty(PROPERTY_RAPIDMINER_INIT_JDBC_CLASSPATH);
	    boolean loadJDBCClasspath = Tools.booleanValue(loadJDBCClasspathString, false);
	
	    String loadPluginsString = System.getProperty(PROPERTY_RAPIDMINER_INIT_PLUGINS);
	    boolean loadPlugins = Tools.booleanValue(loadPluginsString, true);
	    
		InputStream operatorStream = null;
		try {
			String operatorsXML = System.getProperty(PROPERTY_RAPIDMINER_INIT_OPERATORS);
			if (operatorsXML != null) {
				operatorStream = new FileInputStream(operatorsXML);
			}
			init(operatorStream, pluginDir, jdbcDir, loadWeka, loadJDBCDir, loadJDBCClasspath, loadPlugins);
		} catch (IOException e) {
			// do nothing
		} finally {
			if (operatorStream != null) {
				try {
					operatorStream.close();
				} catch (IOException e) {
					
				}
			}
		}
	}

	/** Cleans up the object visualizers available for this process and clears the 
	 *  current temp directory. This method should be performed in cases where RapidMiner
	 *  is embedded into other applications and only single operators (in contrast to
	 *  a complete process) are performed within several runs, e.g. in a loop.
	 *  
	 *  TODO: bind object visualizers and temp file service to a 
	 *  process instead of managing these things in a static way.
	 */
	public static void cleanUp() {
	    ObjectVisualizerService.clearVisualizers();
	}

	public static SplashScreen showSplash() {
		URL url = Tools.getResource("rapidminer_logo.png");
		Image logo = null;
		try {
			if (url != null) {
				logo = ImageIO.read(url);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return showSplash(logo);
	}
	
	public static SplashScreen showSplash(Image productLogo) {
		RapidMiner.splashScreen = new SplashScreen(getShortVersion(), productLogo);
		RapidMiner.splashScreen.showSplashScreen();
		return RapidMiner.splashScreen;
	}

	public static void hideSplash() {
		RapidMiner.splashScreen.dispose();
	}

	public static void splashMessage(String message) {
		if (RapidMiner.splashScreen != null) {
			RapidMiner.splashScreen.setMessage(message);
		}
	}

	public static SplashScreen getSplashScreen() {
		return RapidMiner.splashScreen;
	}
	
	public static Frame getSplashScreenFrame() {
		if (RapidMiner.splashScreen != null)
			return RapidMiner.splashScreen.getSplashScreenFrame();
		else
			return null;
	}
	
	public static void setInputHandler(InputHandler inputHandler) {
		RapidMiner.inputHandler = inputHandler;
	}

	public static InputHandler getInputHandler() {
		return inputHandler;
	}

	/** Returns a set of {@link ParameterType}s for the RapidMiner system properties. 
	 * @deprecated Use {@link #getRapidMinerProperties()} instead*/
	@Deprecated
	public static java.util.Set<ParameterType> getYaleProperties() {
		return getRapidMinerProperties();
	}

	/** Returns a set of {@link ParameterType}s for the RapidMiner system properties. */
	public static java.util.Set<ParameterType> getRapidMinerProperties() {
		return PROPERTY_TYPES;
	}

	/**
	 * @deprecated Use {@link #registerRapidMinerProperty(ParameterType)} instead
	 */
	@Deprecated
	public static void registerYaleProperty(ParameterType type) {
		registerRapidMinerProperty(type);
	}

	public static void registerRapidMinerProperty(ParameterType type) {
		PROPERTY_TYPES.add(type);
	}

	public static void quit(int errorcode) {
		try {
			Runtime.getRuntime().runFinalization();
		} catch (Exception e) {
			System.err.println("ERROR during SHUTDOWN: " + e.getMessage());
		}
		System.exit(errorcode);	
	}

	public static boolean isInitialized() {
		return isInitialized;
	}
}
