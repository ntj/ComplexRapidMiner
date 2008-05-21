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
package com.rapidminer.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.rapidminer.NoOpUserError;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.dialog.InitialSettingsDialog;
import com.rapidminer.gui.dialog.ResultHistory;
import com.rapidminer.gui.look.RapidLookAndFeel;
import com.rapidminer.gui.look.fc.BookmarkIO;
import com.rapidminer.gui.tools.CheckForUpdatesThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.VersionNumber;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeCategory;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;


/**
 * The main class if RapidMiner is startet in GUI mode. This class keeps the
 * {@link MainFrame} and some other GUI relevant informations and methods.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: RapidMinerGUI.java,v 1.34 2008/05/09 19:23:23 ingomierswa Exp $
 */
public class RapidMinerGUI extends RapidMiner {

    public static final String PROPERTY_GEOMETRY_X                      = "rapidminer.gui.geometry.x";
    public static final String PROPERTY_GEOMETRY_Y                      = "rapidminer.gui.geometry.y";
    public static final String PROPERTY_GEOMETRY_WIDTH                  = "rapidminer.gui.geometry.width";
    public static final String PROPERTY_GEOMETRY_HEIGHT                 = "rapidminer.gui.geometry.height";
    public static final String PROPERTY_GEOMETRY_DIVIDER_MAIN           = "rapidminer.gui.geometry.divider.main";
    public static final String PROPERTY_GEOMETRY_DIVIDER_EDITOR         = "rapidminer.gui.geometry.divider.editor";;
    public static final String PROPERTY_GEOMETRY_DIVIDER_LOGGING        = "rapidminer.gui.geometry.divider.logging";
    public static final String PROPERTY_GEOMETRY_DIVIDER_GROUPSELECTION = "rapidminer.gui.geometry.divider.groupselection";
    public static final String PROPERTY_EXPERT_MODE                     = "rapidminer.gui.expertmode";
            
	public static final String PROPERTY_RAPIDMINER_GUI_UPDATE_CHECK  = "rapidminer.gui.update.check";
	public static final String PROPERTY_RAPIDMINER_GUI_LOOK_AND_FEEL = "rapidminer.gui.look";
	
	public static final String[] LOOK_AND_FEELS = {
		"modern",
		"classic"
	};
	
	public static final int LOOK_AND_FEEL_MODERN  = 0;
	public static final int LOOK_AND_FEEL_CLASSIC = 1;
	
	static {
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_UPDATE_CHECK, "Check for new RapidMiner versions at start up time?", true)); 
		RapidMiner.registerRapidMinerProperty(new ParameterTypeCategory(PROPERTY_RAPIDMINER_GUI_LOOK_AND_FEEL, "Indicates which look and feel should be used (you have to restart RapidMiner in order to see changes).", LOOK_AND_FEELS, LOOK_AND_FEEL_MODERN));
	}

	private static final int NUMBER_OF_RECENT_FILES = 8;

	private static MainFrame mainFrame;

	private static LinkedList<File> recentFiles = new LinkedList<File>();

	private static ResultHistory resultHistory = new ResultHistory();
	
	private static CheckForUpdatesThread updateCheckThread = null;
	
	/**
	 * This thread listens for System shutdown and cleans up after shutdown.
	 * This included saving the recent file list and other GUI properties.
	 */
	protected static class ShutdownHook extends Thread {
		public void run() {
			LogService.getGlobal().log("Running shutdown sequence.", LogService.INIT);
			RapidMinerGUI.saveRecentFileList();
			RapidMinerGUI.saveGUIProperties();
		}
	}

	public void run(File file) throws Exception {		
		// check if resources were copied
		URL logoURL = Tools.getResource("rapidminer_logo.png");
		if (logoURL == null) {
			System.err.println("ERROR: cannot find resources. Probably the ant target 'copy-resources' must be performed!");
			RapidMiner.quit(1);
		}
			
		RapidMiner.showSplash();
		
		RapidMiner.splashMessage("Basic Initialization");
		RapidMiner.init();
		
	    // set locale fix to US
	    RapidMiner.splashMessage("Using US Local");
	    Locale.setDefault(Locale.US);
	    JOptionPane.setDefaultLocale(Locale.US);
			
		// check if this version is started for the first time
		RapidMiner.splashMessage("Workspace Initialization");
		performInitialSettings();
		
		RapidMiner.splashMessage("Setting up Look and Feel");
		setupToolTipManager();
		setupGUI();
		
		RapidMiner.splashMessage("Loading History");
		loadRecentFileList();
		
		RapidMiner.splashMessage("Creating Frame");
		setMainFrame(new MainFrame());

		RapidMiner.splashMessage("Initialize Global Logging");
		LogService.getGlobal().initGUI();
		
		RapidMiner.splashMessage("Loading GUI Properties");
		loadGUIProperties(mainFrame);
		
		RapidMiner.splashMessage("Showing Frame");
		mainFrame.setVisible(true);

		RapidMiner.splashMessage("Initialize Process Logging");
        mainFrame.getProcess().getLog().initGUI();
        
		RapidMiner.splashMessage("Ready.");
		
		RapidMiner.hideSplash();

		// file from command line or Welcome Dialog
		if (file != null) {
			mainFrame.open(file);
			mainFrame.changeMode(MainFrame.EDIT_MODE);
		} else {
			mainFrame.changeMode(MainFrame.WELCOME_MODE);
		}

		// check for updates
		String updateProperty = System.getProperty(PROPERTY_RAPIDMINER_GUI_UPDATE_CHECK);
		if (Tools.booleanValue(updateProperty, true)) {
			boolean check = true;
			Calendar lastCheck = loadLastUpdateCheckDate();
			if (lastCheck != null) {
				Calendar currentDate = Calendar.getInstance();
				currentDate.add(Calendar.DAY_OF_YEAR, -7);
				if (!lastCheck.before(currentDate))
					check = false;
			}
			if (check) {
				checkForUpdates(false);
			}
		}
	}

	private void setupToolTipManager() {
        // setup tool tip text manager
        ToolTipManager manager = ToolTipManager.sharedInstance();
        manager.setDismissDelay(25000); // original: 4000
        manager.setInitialDelay(1500);   // original: 750
        manager.setReshowDelay(50);    // original: 500
	}
	
	/** This default implementation only setup the tool tip durations. Subclasses might
	 *  override this method. */
	protected void setupGUI() throws NoOpUserError {
		String lookAndFeelString = System.getProperty(PROPERTY_RAPIDMINER_GUI_LOOK_AND_FEEL);
		int lookAndFeel = LOOK_AND_FEEL_MODERN;
		if (lookAndFeelString != null) {
			try {
				lookAndFeel = Integer.parseInt(lookAndFeelString);
			} catch (NumberFormatException e) {
				LogService.getGlobal().log("Cannot setup look and feel ('" + lookAndFeelString + "'), using default.", LogService.INIT);			
			}
		}
		if (lookAndFeel == LOOK_AND_FEEL_CLASSIC) {
			try {
				Class<?> clazz = Class.forName("com.jgoodies.looks.plastic.PlasticLookAndFeel");
				Method method = clazz.getMethod("setTabStyle", String.class);
				method.invoke( null, new Object[]{ "Metal" } );

				Class themeClazz = Class.forName("com.jgoodies.looks.plastic.PlasticTheme");

				Class skyBluerClazz = Class.forName("com.jgoodies.looks.plastic.theme.SkyBluer");
				Object theme = skyBluerClazz.newInstance();
				method = clazz.getMethod("setPlasticTheme", themeClazz );
				method.invoke( null, new Object[]{ theme } );

				Class lafClazz = Class.forName("com.jgoodies.looks.plastic.Plastic3DLookAndFeel");
				Object lafInstance = lafClazz.newInstance();

				UIManager.setLookAndFeel((LookAndFeel) lafInstance);
				SwingTools.setIconType(LOOK_AND_FEELS[LOOK_AND_FEEL_CLASSIC]);
				OperatorService.reloadIcons();
			} catch (Throwable e) {
				LogService.getGlobal().log("Cannot setup classic look and feel, using default.", LogService.INIT);
			} 
		} else if (lookAndFeel == LOOK_AND_FEEL_MODERN) {
			try {
				System.setProperty(BookmarkIO.PROPERTY_BOOKMARKS_DIR, ParameterService.getUserRapidMinerDir().getAbsolutePath());
				System.setProperty(BookmarkIO.PROPERTY_BOOKMARKS_FILE, ".bookmarks");
				UIManager.setLookAndFeel(new RapidLookAndFeel());
				SwingTools.setIconType(LOOK_AND_FEELS[LOOK_AND_FEEL_MODERN]);
				OperatorService.reloadIcons();
			} catch (Throwable e) {
				e.printStackTrace();
				LogService.getGlobal().log("Cannot setup modern look and feel, using default.", LogService.INIT);
			} 
		} else {
			LogService.getGlobal().log("Cannot setup look and feel ('" + lookAndFeel + "'), using default.", LogService.INIT);
		}
	}
	
	public static void setMainFrame(MainFrame mf) {
		mainFrame = mf;
	}
	
	public static MainFrame getMainFrame() {
		return mainFrame;
	}

	private void performInitialSettings() {
		boolean firstStart = false;
		VersionNumber lastVersionNumber = null;
		VersionNumber currentVersionNumber = new VersionNumber(getVersion());
		
		File lastVersionFile = new File(ParameterService.getUserRapidMinerDir(), "lastversion");
		if (!lastVersionFile.exists()) {
			firstStart = true;
		} else {
			String versionString = null;
			BufferedReader in = null;
			try {
				in = new BufferedReader(new FileReader(lastVersionFile));
				versionString = in.readLine();
			} catch (IOException e) {
				LogService.getGlobal().logWarning("Cannot read global version file of last used version.");
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						LogService.getGlobal().logError("Cannnot close stream to file " + lastVersionFile);
					}
				}
			}
			
			if (versionString != null) {
				lastVersionNumber = new VersionNumber(versionString);			
				if (currentVersionNumber.compareTo(lastVersionNumber) > 0) {
					firstStart = true;
				}
			} else {
				firstStart = true;
			}
		}
		
		// init this version (workspace etc.)
		if (firstStart) {
			performFirstInitialization(lastVersionNumber, currentVersionNumber);
		}
		
		// write version file
		writeLastVersion(lastVersionFile);	
	}
	
	private void performFirstInitialization(VersionNumber lastVersion, VersionNumber currentVersion) {
		if (currentVersion != null)
			LogService.getGlobal().logNote("Performing upgrade" + (lastVersion != null ? " from version " + lastVersion : "") + " to version " + currentVersion);
		
		// copy old settings to new version file
		ParameterService.copyMainUserConfigFile(lastVersion, currentVersion);
		
		// create workspace selection dialog
		File oldWorkspace = ParameterService.getUserWorkspace();
		String lookAndFeelString = System.getProperty(PROPERTY_RAPIDMINER_GUI_LOOK_AND_FEEL);
		int lookAndFeel = LOOK_AND_FEEL_MODERN;
		if (lookAndFeelString != null) {
			try {
				lookAndFeel = Integer.parseInt(lookAndFeelString);
			} catch (NumberFormatException e) {
				LogService.getGlobal().log("Cannot setup look and feel ('" + lookAndFeelString + "'), using default.", LogService.INIT);			
			}
		}
		
		InitialSettingsDialog dialog = new InitialSettingsDialog(getSplashScreenFrame(), oldWorkspace, "rm_workspace", null, lookAndFeel, true);
		dialog.setVisible(true);
		String newPath = dialog.getWorkspacePath();
		File newWorkspace = new File(newPath);
		ParameterService.setUserWorkspace(newWorkspace);
		
		int selectedLookAndFeel = dialog.getSelectedLookAndFeel();
		ParameterService.writePropertyIntoMainUserConfigFile(PROPERTY_RAPIDMINER_GUI_LOOK_AND_FEEL, selectedLookAndFeel + "");
	}
	
	private void writeLastVersion(File versionFile) {
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(versionFile));
			out.println(getVersion());
		} catch (IOException e) {
			LogService.getGlobal().logWarning("Cannot write current version into property file.");
		} finally {
			if (out != null)
				out.close();
		}
	}
	
	public static void useProcessFile(Process process) {
		File file = process.getProcessFile();
		file = new File(file.getAbsolutePath());
		if (recentFiles.contains(file)) {
			recentFiles.remove(file);
		}
		recentFiles.addFirst(file);
		while (recentFiles.size() > NUMBER_OF_RECENT_FILES)
			recentFiles.removeLast();
	}

	public static ResultHistory getResultHistory() {
		return resultHistory;
	}
	
	public static List<File> getRecentFiles() {
		return recentFiles;
	}

	private static void loadRecentFileList() {
		File file = ParameterService.getUserConfigFile("history");
		if (!file.exists())
			return;
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			recentFiles.clear();
			String line = null;
			while ((line = in.readLine()) != null) {
				recentFiles.add(new File(line));
			}
		} catch (IOException e) {
			// cannot happen
			SwingTools.showSimpleErrorMessage("Cannot read history file", e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					SwingTools.showSimpleErrorMessage("Cannot close connection to history file.", e);
				}
			}
		}
	}

	private static void saveRecentFileList() {
		File file = ParameterService.getUserConfigFile("history");
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file));
			Iterator i = recentFiles.iterator();
			while (i.hasNext()) {
				out.println(((File) i.next()).getAbsolutePath());
			}
		} catch (IOException e) {
			SwingTools.showSimpleErrorMessage("Cannot write history file", e);
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	public static void saveLastUpdateCheckDate() {
		File file = ParameterService.getUserConfigFile("updatecheck.date");
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileWriter(file));
			Calendar currentDate = Calendar.getInstance();
			out.println(currentDate.get(Calendar.YEAR));
			out.println(currentDate.get(Calendar.MONTH));
			out.println(currentDate.get(Calendar.DAY_OF_MONTH));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private static Calendar loadLastUpdateCheckDate() {
		File file = ParameterService.getUserConfigFile("updatecheck.date");
		if (!file.exists())
			return null;

		Calendar lastCheck = null;
        BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String yearLine = in.readLine();
			String monthLine = in.readLine();
			String dayLine = in.readLine();

            int year = 2001;
			if (yearLine != null)
				year = Integer.parseInt(yearLine.trim());
			int month = 1;
			if (monthLine != null)
				month = Integer.parseInt(monthLine.trim());
			int day = 1;
			if (dayLine != null)
				day = Integer.parseInt(dayLine.trim());
			lastCheck = Calendar.getInstance();
			lastCheck.set(Calendar.YEAR, year);
			lastCheck.set(Calendar.MONTH, month);
			lastCheck.set(Calendar.DAY_OF_MONTH, day);
		} catch (IOException e) {
			LogService.getGlobal().logWarning("Cannot read last date of update check.");
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					// cannot happen
				}
			}
		}
		
		return lastCheck;
	}

	public static void checkForUpdates(boolean showFailureDialog) {
		updateCheckThread = new CheckForUpdatesThread(getMainFrame(), showFailureDialog);
		updateCheckThread.start();
	}
	
	private static void saveGUIProperties() {
		Properties properties = new Properties();
		MainFrame mainFrame = getMainFrame();
		if (mainFrame != null) {
			properties.setProperty(PROPERTY_GEOMETRY_X, "" + (int) mainFrame.getLocation().getX());
			properties.setProperty(PROPERTY_GEOMETRY_Y, "" + (int) mainFrame.getLocation().getY());
			properties.setProperty(PROPERTY_GEOMETRY_WIDTH, "" + mainFrame.getWidth());
			properties.setProperty(PROPERTY_GEOMETRY_HEIGHT, "" + mainFrame.getHeight());
			properties.setProperty(PROPERTY_GEOMETRY_DIVIDER_MAIN, "" + mainFrame.getMainDividerLocation());
			properties.setProperty(PROPERTY_GEOMETRY_DIVIDER_EDITOR, "" + mainFrame.getEditorDividerLocation());
			properties.setProperty(PROPERTY_GEOMETRY_DIVIDER_LOGGING, "" + mainFrame.getLoggingDividerLocation());
			properties.setProperty(PROPERTY_GEOMETRY_DIVIDER_GROUPSELECTION, "" + mainFrame.getGroupSelectionDividerLocation());
			properties.setProperty(PROPERTY_EXPERT_MODE, "" + mainFrame.getPropertyTable().isExpertMode());
			File file = ParameterService.getUserConfigFile("gui.properties");
            OutputStream out = null;
			try {
				out = new FileOutputStream(file);
				properties.store(out, "RapidMiner GUI properties");
			} catch (IOException e) {
				LogService.getGlobal().logWarning("Cannot write GUI properties: " + e.getMessage());
			} finally {
                try {
                    if (out != null)
                        out.close();
                } catch (IOException e) {
                    throw new Error(e); // should not occur
                }
            }
		}
	}

	private static void loadGUIProperties(MainFrame mainFrame) {
		Properties properties = new Properties();
		File file = ParameterService.getUserConfigFile("gui.properties");
		if (file.exists()) {
            InputStream in = null;
			try {
				in = new FileInputStream(file);
				properties.load(in);
			} catch (IOException e) {
				setDefaultGUIProperties();
			} finally {
                try {
                    if (in != null)
                        in.close();
                } catch (IOException e) {
                    throw new Error(e); // should not occur
                }
            }
			try {
				mainFrame.setLocation(Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_X)), Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_Y)));
				mainFrame.setSize(new Dimension(Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_WIDTH)), Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_HEIGHT))));
				mainFrame.setDividerLocations(
						Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_DIVIDER_MAIN)),
						Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_DIVIDER_EDITOR)),
						Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_DIVIDER_LOGGING)),
						Integer.parseInt(properties.getProperty(PROPERTY_GEOMETRY_DIVIDER_GROUPSELECTION)));
				mainFrame.getPropertyTable().setExpertMode(Boolean.valueOf(properties.getProperty(PROPERTY_EXPERT_MODE)).booleanValue());
				mainFrame.updateToggleExpertModeIcon();
			} catch (NumberFormatException e) {
			    setDefaultGUIProperties();
			}
		} else {
		    setDefaultGUIProperties();
        }
	}
    
    /** This method sets some default GUI properties. This method can be invoked if the properties
     *  file was not found or produced any error messages (which might happen after version changes).
     */
    private static void setDefaultGUIProperties() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        mainFrame.setLocation((int)(0.05d * screenSize.getWidth()), (int)(0.05d * screenSize.getHeight()));
        mainFrame.setSize((int)(0.9d * screenSize.getWidth()), (int)(0.9d * screenSize.getHeight()));
        mainFrame.setDividerLocations((int)(0.6d * screenSize.getHeight()), (int)(0.2d * screenSize.getWidth()), (int)(0.75d * screenSize.getWidth()), (int)(0.4d * screenSize.getWidth())); 
        mainFrame.getPropertyTable().setExpertMode(false);
        mainFrame.updateToggleExpertModeIcon();
    }
    
	public static void main(String[] args) throws Exception {		
		Runtime.getRuntime().addShutdownHook(new ShutdownHook());
		File file = null;
		if (args.length > 0) {
			if (args.length != 1) {
				System.out.println("java " + RapidMinerGUI.class.getName() + " [processfile]");
				return;
			}
			file = new File(args[0]);
			if (!file.exists()) {
				System.err.println("File '" + args[0] + "' not found.");
				return;
			}
			if (!file.canRead()) {
				System.err.println("Cannot read file '" + args[0] + "'.");
				return;
			}
		}
		RapidMiner.setInputHandler(new GUIInputHandler());
		new RapidMinerGUI().run(file);
	}
}
