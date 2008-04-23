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
package com.rapidminer.gui;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import javax.swing.LookAndFeel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import com.rapidminer.NoOpUserError;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.dialog.ResultHistory;
import com.rapidminer.gui.tools.CheckForUpdatesThread;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.similarity.attributebased.Matrix;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;


/**
 * The main class if RapidMiner is startet in GUI mode. This class keeps the
 * {@link MainFrame} and some other GUI relevant informations and methods.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: RapidMinerGUI.java,v 1.11 2007/07/15 22:06:25 ingomierswa Exp $
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
            
	public static final String PROPERTY_RAPIDMINER_GUI_UPDATE_CHECK = "rapidminer.gui.update.check";

	static {
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_UPDATE_CHECK, "Check for new RapidMiner versions at start up time?", true)); 
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
	private static class ShutdownHook extends Thread {
		public void run() {
			LogService.getGlobal().log("Running shutdown sequence.", LogService.INIT);
			RapidMinerGUI.saveRecentFileList();
			RapidMinerGUI.saveGUIProperties();
		}
	}

	private static void setupGUI() throws NoOpUserError {  
		// check for favourites file and add basic favourites if necessary
		File favouritesFile = new File(ParameterService.getUserRapidMinerDir(), ".TFileChooserFavourites");
		if (!favouritesFile.exists()) {
			Vector<Hashtable<String, String>> favouritesVector = new Vector<Hashtable<String, String>>();
			Hashtable<String,String> sampleFavourite = new Hashtable<String,String>();
			sampleFavourite.put("time", Long.valueOf((new Date()).getTime()).toString());
			sampleFavourite.put("name", "samples");
			sampleFavourite.put("path", ParameterService.getSampleDir().getAbsolutePath());
			favouritesVector.add(sampleFavourite);
			
			Hashtable<String,String> dataFavourite = new Hashtable<String,String>();
			dataFavourite.put("time", Long.valueOf((new Date()).getTime()).toString());
			dataFavourite.put("name", "sample_data");
			dataFavourite.put("path", ParameterService.getSampleFile("data").getAbsolutePath());
			favouritesVector.add(dataFavourite);
			
			try {
				ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(favouritesFile));
				out.writeObject(favouritesVector);
				out.close();
			} catch (FileNotFoundException e) {
				LogService.getGlobal().logWarning("Cannot write default favourites for file choose: " + e.getMessage());
			} catch (IOException e) {
				LogService.getGlobal().logWarning("Cannot write default favourites for file choose: " + e.getMessage());
			}
		}

    	// set favourites dir
    	System.setProperty("trendy.favourites.dir", ParameterService.getUserRapidMinerDir().getAbsolutePath());
		
		// setup pluggable look and feel
        try {
            Class<?> configurationClazz = Class.forName("com.Trendy.swing.plaf.TrendyConfiguration");
            // enable antialiasing
            Method antiAliasingMethod = configurationClazz.getMethod("setGlobal_ANTIALIASED_MINIMUM_FONT", new Class[] { Integer.class });
            antiAliasingMethod.invoke(null, new Object[] { Integer.valueOf(0) } );  

            // 1: round (default); -1: squared (nicer)
            Field roundField = configurationClazz.getField("Component_RoundRect_Status");
            String roundString = (String)roundField.get(null); 
            UIManager.put("Button." + roundString, Integer.valueOf(-1)); // round buttons
            UIManager.put("ToggleButton." + roundString, Integer.valueOf(-1)); // round toggle buttons
            UIManager.put("ComboBox." + roundString, Integer.valueOf(-1)); // round combo boxes
            
            // init PLAF
            Class<?> lookAndFeelClass = Class.forName("com.Trendy.swing.plaf.TrendyLookAndFeel");
            LookAndFeel laf = (LookAndFeel)lookAndFeelClass.newInstance();
            
            // use enhanced color scheme (nice menus etc.)
            Class themeClass = Class.forName("com.Trendy.swing.plaf.Themes.TrendyEnhancedDefaultTheme");
            Class generalThemeClass = Class.forName("com.Trendy.swing.plaf.Themes.TrendyTheme"); 
            Method themeMethod = lookAndFeelClass.getMethod("setCurrentTheme", new Class[] { generalThemeClass });
            themeMethod.invoke(laf, themeClass.newInstance()); 
            UIManager.setLookAndFeel(laf);
            
        } catch (Throwable e) {
        	LogService.getGlobal().log("Cannot setup RapidMiner look and feel, using system default.", LogService.INIT);
        }

        // setup tool tip text manager
        ToolTipManager manager = ToolTipManager.sharedInstance();
        manager.setDismissDelay(25000); // original: 4000
        manager.setInitialDelay(1500);   // original: 750
        manager.setReshowDelay(50);    // original: 500
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
	
		RapidMiner.splashMessage("Setting up Look and Feel");
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

	public static void setMainFrame(MainFrame mf) {
		mainFrame = mf;
	}
	
	public static MainFrame getMainFrame() {
		return mainFrame;
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
		try {
			File file = ParameterService.getUserConfigFile("history");
			if (!file.exists())
				return;
			BufferedReader in = new BufferedReader(new FileReader(file));
			recentFiles.clear();
			String line = null;
			while ((line = in.readLine()) != null) {
				recentFiles.add(new File(line));
			}
			in.close();
		} catch (IOException e) {
			// cannot happen
			SwingTools.showSimpleErrorMessage("Cannot read history file", e);
		}
	}

	private static void saveRecentFileList() {
		try {
			File file = ParameterService.getUserConfigFile("history");
			PrintWriter out = new PrintWriter(new FileWriter(file));
			Iterator i = recentFiles.iterator();
			while (i.hasNext()) {
				out.println(((File) i.next()).getAbsolutePath());
			}
			out.close();
		} catch (IOException e) {
			SwingTools.showSimpleErrorMessage("Cannot write history file", e);
		}
	}

	public static void saveLastUpdateCheckDate() {
		File file = ParameterService.getUserConfigFile("updatecheck.date");
		try {
			PrintWriter out = new PrintWriter(new FileWriter(file));
			Calendar currentDate = Calendar.getInstance();
			out.println(currentDate.get(Calendar.YEAR));
			out.println(currentDate.get(Calendar.MONTH));
			out.println(currentDate.get(Calendar.DAY_OF_MONTH));
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static Calendar loadLastUpdateCheckDate() {
		File file = ParameterService.getUserConfigFile("updatecheck.date");
		if (!file.exists())
			return null;
		try {
			BufferedReader in = new BufferedReader(new FileReader(file));
			String yearLine = in.readLine();
			String monthLine = in.readLine();
			String dayLine = in.readLine();
			in.close();
			int year = 2001;
			if (yearLine != null)
				year = Integer.parseInt(yearLine.trim());
			int month = 1;
			if (monthLine != null)
				month = Integer.parseInt(monthLine.trim());
			int day = 1;
			if (dayLine != null)
				day = Integer.parseInt(dayLine.trim());
			Calendar lastCheck = Calendar.getInstance();
			lastCheck.set(Calendar.YEAR, year);
			lastCheck.set(Calendar.MONTH, month);
			lastCheck.set(Calendar.DAY_OF_MONTH, day);
			return lastCheck;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
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
			try {
				OutputStream out = new FileOutputStream(file);
				properties.store(out, "RapidMiner GUI properties");
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void loadGUIProperties(MainFrame mainFrame) {
		Properties properties = new Properties();
		File file = ParameterService.getUserConfigFile("gui.properties");
		if (file.exists()) {
			try {
				InputStream in = new FileInputStream(file);
				properties.load(in);
				in.close();
			} catch (IOException e) {
				setDefaultGUIProperties();
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
