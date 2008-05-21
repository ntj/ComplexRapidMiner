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
package com.rapidminer.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import com.rapidminer.RapidMiner;
import com.rapidminer.Version;
import com.rapidminer.gui.look.fc.Bookmark;
import com.rapidminer.gui.look.fc.BookmarkIO;
import com.rapidminer.gui.tools.VersionNumber;

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
                        if (dir.exists()) {
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
		init(operatorsXMLStream, null, addWekaOperators);
	}
	
	/** Registers the operators from the stream and reads the rc file. If the stream
	 *  is null this method tries to read the core operators.xml. */
	public static void init(InputStream operatorsXMLStream, InputStream additionalXMLStream, boolean addWekaOperators) {
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

		// additional operators from init method
		if (additionalXMLStream != null) {
			OperatorService.registerOperators("Additional Operators from Init", additionalXMLStream, null, addWekaOperators);			
		}
		
		// additional operators from starting parameter
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

	/** Returns the user workspace (if one was set). Might return null if no workspace
	 *  definition can be found. This method also creates the workspace directory if
	 *  the file does not exist. */
	public static File getUserWorkspace() {
		File workspaceLocationFile = getUserConfigFile("workspace");
		if (!workspaceLocationFile.exists()) {
			return null;
		} else {
			BufferedReader in = null;
			String workspacePath = null;
			try {
				in = new BufferedReader(new FileReader(workspaceLocationFile));
				 workspacePath = in.readLine();
			} catch (IOException e) {
				// does nothing
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// cannot happen
					}
				}
			}
			
			if (workspacePath != null) {
				File workspace = new File(workspacePath);
				if (!workspace.exists()) {
					boolean creationResult = workspace.mkdir();
					if (!creationResult) {
						LogService.getGlobal().logWarning("Cannot create workspace directory: " + workspace);
						return null;
					}
				}
				return workspace;
			} else {
				return null;
			}
		}
	}
	
	/** Sets the current user workspace to this file. If the directory does not exist
	 *  it will be created. The absolute path is also written into the corresponding
	 *  user config file. Additionally, the favorites for the file chooser 
	 *  will be extended by the workspace and sample links if appropriate. */
	@SuppressWarnings("unchecked")
	public static final void setUserWorkspace(File workspace) {
		if (!workspace.exists()) {
			boolean result = workspace.mkdir();
			if (!result)
				LogService.getGlobal().logWarning("Unable to create workspace directory: " + workspace);
		}
		
		// copy sample files
		File newSampleDir = new File(workspace, "sample");
		if (newSampleDir.exists()) {
			Tools.delete(newSampleDir);
			boolean creationResult = newSampleDir.mkdir();
			if (!creationResult) {
				LogService.getGlobal().logWarning("Cannot create user sample directory: " + newSampleDir);
			}
		}
		
		File globalSampleDir = ParameterService.getSampleDir();
		try {
			Tools.copy(globalSampleDir, newSampleDir);
		} catch (IOException e1) {
			LogService.getGlobal().logWarning("Cannot copy sample directory to workspace: " + e1.getMessage());
		}
		
		// add basic bookmarks for new workspace
		List<Bookmark> bookmarks = null;
		File bookmarksFile = new File(ParameterService.getUserRapidMinerDir(), ".bookmarks");
		if (bookmarksFile.exists()) {
			bookmarks = BookmarkIO.readBookmarks(bookmarksFile);
			
			boolean changedWorkspace  = false;
			boolean changedSamples    = false;
			boolean changedSampleData = false;
			if (bookmarks != null) {
				for (Bookmark bookmark : bookmarks) {
					// change existing entries
					if (bookmark.getName().equals("Workspace")) {
						bookmark.setPath(workspace.getAbsolutePath());
						changedWorkspace = true;
					} else if (bookmark.getName().equals("Samples")) {
						if (newSampleDir.exists()) {
							bookmark.setPath(newSampleDir.getAbsolutePath());
						}
						changedSamples = true;
					} else if (bookmark.getName().equals("Sample Data")) {
						File newSampleDataDir = new File(newSampleDir, "data");
						if (newSampleDataDir.exists()) {
							bookmark.setPath(newSampleDataDir.getAbsolutePath());
						}
						changedSampleData = true;
					}
				}
				
				if (!changedWorkspace) {
					Bookmark workspaceBookmark = new Bookmark("Workspace", workspace.getAbsolutePath());
					bookmarks.add(workspaceBookmark);
				}
				
				if (!changedSamples) {
					if (newSampleDir.exists()) {
						Bookmark sampleBookmark = new Bookmark("Samples", newSampleDir.getAbsolutePath());
						bookmarks.add(sampleBookmark);
					}
				}
				
				if (!changedSampleData) {
					File newSampleDataDir = new File(newSampleDir, "data");
					if (newSampleDataDir.exists()) {
						Bookmark sampleDataBookmark = new Bookmark("Sample Data", newSampleDataDir.getAbsolutePath());
						bookmarks.add(sampleDataBookmark);
					}
				}
			}
		}
		
		if (bookmarks == null) {
			// favorites file not existing --> create new one containing workspace links
			bookmarks = new LinkedList<Bookmark>();

			Bookmark workspaceBookmark = new Bookmark("Workspace", workspace.getAbsolutePath());
			bookmarks.add(workspaceBookmark);

			if (newSampleDir.exists()) {
				Bookmark sampleBookmark = new Bookmark("Samples", newSampleDir.getAbsolutePath());
				bookmarks.add(sampleBookmark);
			}

			File newSampleDataDir = new File(newSampleDir, "data");
			if (newSampleDataDir.exists()) {
				Bookmark sampleDataBookmark = new Bookmark("Sample Data", newSampleDataDir.getAbsolutePath());
				bookmarks.add(sampleDataBookmark);
			}
		}
		
		// write bookmarks file
		BookmarkIO.writeBookmarks(bookmarks, bookmarksFile);
		
		// write workspace location into user config file
		File workspaceLocationFile = getUserConfigFile("workspace");
		PrintWriter workspaceOut = null;
		try {
			workspaceOut = new PrintWriter(new FileWriter(workspaceLocationFile));
			workspaceOut.println(workspace.getAbsolutePath());
		} catch (IOException e) {
			LogService.getGlobal().logWarning("Cannot write workspace location: " + e.getMessage());
		} finally {
			if (workspaceOut != null)
				workspaceOut.close();
		}
	}
	
	public static void copyMainUserConfigFile(VersionNumber oldVersion, VersionNumber newVersion) {
		Properties oldProperties = readPropertyFile(getVersionedUserConfigFile(oldVersion, "rapidminerrc" + "." + System.getProperty("os.name")));
		writeProperties(oldProperties, getMainUserConfigFile());
	}
	
	private static Properties readPropertyFile(File file) {
		Properties properties = new Properties();
		if (file.exists()) {
			InputStream in = null;
			try {
				in = new FileInputStream(file);
				properties.load(in);
			} catch (IOException e) {
				LogService.getGlobal().logWarning("Cannot read main user properties: " + e.getMessage());
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						LogService.getGlobal().logWarning("Cannot close connection to user properties: " + e.getMessage());
					}
				}
			}
		}
		return properties;
	}
	
	private static void writeProperties(Properties properties, File file) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(getMainUserConfigFile()));
			for (Object keyName : properties.keySet()) {
				String typeKey = (String)keyName;
				String typeValue = properties.getProperty(typeKey);
				if (typeValue != null) {
					System.setProperty(typeKey, typeValue);
					out.println(typeKey + " = " + typeValue);
				}
			}
		} catch (IOException e) {
			LogService.getGlobal().logWarning("Cannot write user properties: " + e.getMessage());
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}
	
	public static void writePropertyIntoMainUserConfigFile(String key, String value) {
		// read old configuration
		Properties userProperties = readPropertyFile(getMainUserConfigFile());
		
		// set new property
		userProperties.setProperty(key, value);
		System.setProperty(key, value);
		
		// write complete configuration back into the file
		writeProperties(userProperties, getMainUserConfigFile());
	}
	
	/** Returns the main user configuration file containing the version number and the OS. */
	public static File getMainUserConfigFile() {
		return ParameterService.getUserConfigFile("rapidminerrc" + "." + System.getProperty("os.name"));	
	}
	
	/** Returns the configuration file in the user dir .rapidminer and automatically adds 
	 *  the current version number if it is a rc file. */
	public static File getUserConfigFile(String name) {
		return getVersionedUserConfigFile(new VersionNumber(Version.getVersion()), name);
	}
	
	public static File getVersionedUserConfigFile(VersionNumber versionNumber, String name) {
		String configName = name;
		if (configName.startsWith("rapidminerrc")) {
			if (versionNumber != null)
				configName = versionNumber.toString().replaceAll("\\.", "_") + "_" + configName;
		}
		return new File(getUserRapidMinerDir(), configName);		
	}

	public static File getUserRapidMinerDir() {
		File homeDir = new File(System.getProperty("user.home"));
		File userHomeDir = new File(homeDir, ".rapidminer");
		if (!userHomeDir.exists()) {
            LogService.getGlobal().log("Creating directory '" + userHomeDir + "'", LogService.INIT);
            boolean result = userHomeDir.mkdir();
            if (!result)
            	LogService.getGlobal().logWarning("Unable to create user home rapidminer directory " + userHomeDir);
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

		InputStream in = null;
		try {
			in = new FileInputStream(rcFile);
			System.getProperties().load(in);
            LogService.getGlobal().log("Read rcfile '" + rcFile + "'.", LogService.INIT);
		} catch (IOException e) {
            LogService.getGlobal().log("Cannot load rcfile: " + rcFile, LogService.ERROR);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LogService.getGlobal().logError("Cannot close stream to rcfile: " + e.getMessage());
				}
			}
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
	
	public static File getUserSampleFile(String filename) {
		File workspace = getUserWorkspace();
		return new File(workspace, "sample" + File.separator + filename);
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
