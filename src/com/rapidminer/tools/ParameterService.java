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
package com.rapidminer.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import com.rapidminer.RapidMiner;
import com.rapidminer.Version;

/**
 * This class loads the yalrc property files and provides methods to access
 * them. It also provides methods to create files relative to the RapidMiner home
 * directory. As the {@link #getProperty(String)} method throws an exception if
 * the parameter is not set, the <code>System.getProperty(String)</code>
 * methods should be used if this is not desired.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: ParameterService.java,v 2.19 2006/03/21 15:35:53 ingomierswa
 *          Exp $
 */
public class ParameterService {

    /**
	 * Tries to find the rapidminer.home directory if the property is not set and sets
	 * it.
	 */
	public static void ensureRapidMinerHomeSet() {
		String home = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_HOME);
		if (home != null) {
            LogService.getGlobal().log(RapidMiner.PROPERTY_RAPIDMINER_HOME + " is '" + home + "'.", LogService.INIT);
		} else {
            LogService.getGlobal().log("Property " + RapidMiner.PROPERTY_RAPIDMINER_HOME+ " is not set. Guessing.", LogService.INIT);
			String classpath = System.getProperty("java.class.path");
			String pathComponents[] = classpath.split(File.pathSeparator);
			boolean found = false;
			for (int i = 0; i < pathComponents.length; i++) {
				String path = pathComponents[i].trim();
				if (path.endsWith("rapidminer.jar")) {
					File jar = new File(path).getAbsoluteFile();
					String message = "Trying parent directory of '" + jar + "'...";
					File dir = jar.getParentFile();
					if (dir != null) {
						dir = dir.getParentFile();
						if (dir != null) {
							message += "gotcha!";
							System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_HOME, dir.getAbsolutePath());
						} else {
							message += "failed";
						}
					} else {
						message += "failed";
					}
                    LogService.getGlobal().log(message, LogService.INIT);
				}
			}
			
			if (!found) {
				String message = "Trying base directory of classes (build) '";
				URL url = ParameterService.class.getClassLoader().getResource(".");
				if (url != null) {
                    try {
                        File dir = new File(new URI(url.toString()));
                        if (dir != null) {
                            dir = dir.getParentFile();
                            message += dir + "'...";
                            if (dir != null) {
                                message += "gotcha!";
                                try {
                                    System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_HOME, dir.getCanonicalPath());
                                } catch (IOException e) {
                                    System.setProperty(RapidMiner.PROPERTY_RAPIDMINER_HOME, dir.getAbsolutePath());
                                }
                            } else {
                                message += "failed";
                            }
                        } else {
                            message += "failed";
                        }
                    } catch (URISyntaxException e) {
                        message += "failed";    
                    }
				} else {
				    message += "failed";
				}
                LogService.getGlobal().log(message, LogService.INIT);
			}
		}
		getProperty(RapidMiner.PROPERTY_RAPIDMINER_HOME); // throws exception if necessary
	}


	/** Invokes {@link #init(InputStream, boolean)} with a null stream meaning that
	 *  the core operators.xml is loaded and with addWekaOperators = true.
	 *  Registers the operators from the stream and reads the rc file. */
	public static void init() {
		init(null, true);
	}
	
	/** Registers the operators from the stream and reads the rc file. If the stream
	 *  is null this method tries to read the core operators.xml. */
	public static void init(InputStream operatorsXMLStream, boolean addWekaOperators) {
		loadRCFile();

		// core operators
		InputStream operatorDescriptionStream = operatorsXMLStream;
		if (operatorDescriptionStream == null) {
			URL operatorURL = Tools.getResource("operators.xml");
			try {
				if (operatorURL != null) {
					operatorDescriptionStream = operatorURL.openStream();
				} else {
                    LogService.getGlobal().log("Cannot find 'operators.xml'.", LogService.ERROR);
				}
			} catch (IOException e) {
                LogService.getGlobal().log("Cannot read 'operators.xml'.", LogService.ERROR);
			}
		}

		if (operatorDescriptionStream != null)
			OperatorService.registerOperators("operators.xml", operatorDescriptionStream, null, addWekaOperators);

		// additional operators
		String additionalOperators = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_OPERATORS_ADDITIONAL);
		if ((additionalOperators != null) && (additionalOperators.length() > 0)) {
			String[] additionalOperatorFileNames = additionalOperators.split(File.pathSeparator);
			for (int i = 0; i < additionalOperatorFileNames.length; i++) {
				File additionalOperatorFile = new File(additionalOperatorFileNames[i]);
				if (additionalOperatorFile.exists()) {
					try {
						OperatorService.registerOperators(additionalOperatorFile.getPath(), new FileInputStream(additionalOperatorFile), null, addWekaOperators);
					} catch (IOException e) {
                        LogService.getGlobal().log("Cannot read '" + additionalOperatorFile + "'.", LogService.ERROR);
					}
				} else {
                    LogService.getGlobal().log("Cannot find operator description file '" + additionalOperatorFileNames[i] + "'", LogService.ERROR);
				}
			}
		}
	}

	/** Returns the configuration file in the user dir .rapidminer and automatically adds 
	 *  the current version number if it is a rc file. */
	public static File getUserConfigFile(String name) {
		String configName = name;
		if (configName.startsWith("rapidminerrc"))
			configName = Version.getVersion().replaceAll("\\.", "_") + "_" + configName;
		return new File(getUserRapidMinerDir(), configName);
	}

	public static File getUserRapidMinerDir() {
		File homeDir = new File(System.getProperty("user.home"));
		File userHomeDir = new File(homeDir, ".rapidminer");
		if (!userHomeDir.exists()) {
            LogService.getGlobal().log("Creating directory '" + userHomeDir + "'", LogService.INIT);
			userHomeDir.mkdir();
		}
		return userHomeDir;
	}

	private static void loadRCFile() {
		File globalRC = getConfigFile("rapidminerrc");
		loadAllRCFiles(globalRC.getPath());
		loadAllRCFiles(getUserConfigFile("rapidminerrc").getAbsolutePath());
		loadAllRCFiles(new File(new File(System.getProperty("user.dir")), "rapidminerrc").getAbsolutePath());
		String localRC = System.getProperty(RapidMiner.PROPERTY_RAPIDMINER_RC_FILE);
		if (localRC != null)
			loadRCFile(localRC);
		else
            LogService.getGlobal().log("Trying rapidminer.rcfile. Property not specified...skipped", LogService.INIT);
	}

	private static void loadAllRCFiles(String rcFileName) {
		loadRCFile(rcFileName);
		loadRCFile(rcFileName + "." + System.getProperty("os.name"));
	}

	private static void loadRCFile(String rcFileName) {
		if (rcFileName == null)
			return;
		File rcFile = new File(rcFileName);
		if (!rcFile.exists()) {
            LogService.getGlobal().log("Trying rcfile '" + rcFile + "'...skipped", LogService.INIT);
			return;
		}

		try {
			InputStream in = new FileInputStream(rcFile);
			System.getProperties().load(in);
			in.close();
            LogService.getGlobal().log("Read rcfile '" + rcFile + "'.", LogService.INIT);
			return;
		} catch (IOException e) {
            LogService.getGlobal().log("Cannot load rcfile: " + rcFile, LogService.ERROR);
			return;
		}
	}

	// -------------------- parameters --------------------

	/**
	 * Returns a system property and throws a runtime exception if the property
	 * is not set.
	 */
	private static String getProperty(String key) {
		String property = System.getProperty(key);
		if (property == null) {
			throw new RuntimeException("Property '" + key + "' not set!");
		}
		return property;
	}

	public static File getRapidMinerHome() {
		return new File(getProperty(RapidMiner.PROPERTY_RAPIDMINER_HOME));
	}

	public static File getConfigFile(String name) {
		File home = getRapidMinerHome();
		return new File(home, "etc" + File.separator + name);
	}

	public static File getLibraryFile(String name) {
		File home = getRapidMinerHome();
		return new File(home, "lib" + File.separator + name);
	}

	public static File getSampleFile(String filename) {
		File home = getRapidMinerHome();
		return new File(home, "sample" + File.separator + filename);
	}
	
	public static File getSourceFile(String filename) {
		File home = getRapidMinerHome();
		return new File(home, "src" + File.separator + filename);
	}

	public static File getPluginDir() {
		return getLibraryFile("plugins");
	}
	
	public static File getSampleDir() {
		File home = getRapidMinerHome();
		return new File(home, "sample");
	}
	
	// -------------------- tools --------------------

	/**
	 * Returns true if value is "true", "yes", "y" or "on". Returns false if
	 * value is "false", "no", "n" or "off". Otherwise returns <tt>deflt</tt>.
	 */
	public static boolean booleanValue(String value, boolean deflt) {
		if (value == null)
			return deflt;

		if (value.equals("true"))
			return true;
		else if (value.equals("yes"))
			return true;
		else if (value.equals("y"))
			return true;
		else if (value.equals("on"))
			return true;
		else if (value.equals("false"))
			return false;
		else if (value.equals("no"))
			return false;
		else if (value.equals("n"))
			return false;
		else if (value.equals("off"))
			return false;

		return deflt;
	}
}
