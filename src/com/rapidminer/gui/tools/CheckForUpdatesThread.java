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
package com.rapidminer.gui.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JOptionPane;

import com.rapidminer.Version;
import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;

/**
 * This class tries to connect a RapidMiner server and checks if new versions of RapidMiner
 * are available. Saves the current date as last update check date in the user
 * directory. If a new version is available, an info message is shown. Otherwise
 * a failure dialog might be shown.
 * 
 * @author Ingo Mierswa
 * @version $Id: CheckForUpdatesThread.java,v 1.4 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class CheckForUpdatesThread extends Thread {

	private static final String[] VERSION_URLS = {
        "http://www.rapid-i.com/versions/rapidminer/version.txt",
        "http://www.rapid-i.com/versions/yale/version.txt",
        "http://www-ai.cs.uni-dortmund.de/SOFTWARE/YALE/version.txt"
    };
		
	private boolean showFailureDialog = false;

	private MainFrame mainFrame;

	public CheckForUpdatesThread(MainFrame mainFrame, boolean dialog) {
		this.mainFrame = mainFrame;
		this.showFailureDialog = dialog;
	}

	public void run() {
		List<String> remoteVersions = new LinkedList<String>();
        // retrieve all version tags
		for (String s : VERSION_URLS) {
		    URL url = null;
            try {
                url = new URL(s);
            } catch (MalformedURLException e) {
                LogService.getGlobal().log("Cannot create update target url: " + e.getMessage(), LogService.ERROR);
            }
            if (url != null) {
                BufferedReader in = null;
            	try {
            		in = new BufferedReader(new InputStreamReader(url.openStream()));
            		String remoteVersion = in.readLine();
            		if ((remoteVersion != null) && (remoteVersion.length() > 0) && (Character.isDigit(remoteVersion.charAt(0)))) {
            			remoteVersions.add(remoteVersion);
            		}
            	} catch (IOException e) {
                    LogService.getGlobal().log("Not able to check for updates. Maybe no internet connection.", LogService.WARNING);
            	} finally {
                    try {
                        if (in != null)
                            in.close();
                    } catch (IOException e) {
                        throw new Error(e); // should not occur
                    }
                }
            }
		}
        
        if (remoteVersions.size() > 0) {
            // set current date only if check was successful
            RapidMinerGUI.saveLastUpdateCheckDate();
        }
            
        // check all version tags
        Iterator<String> i = remoteVersions.iterator();
        VersionNumber newestVersion = getVersionNumber(Version.getVersion());
        while (i.hasNext()) {
            String remoteVersionString = i.next();
            if (remoteVersionString != null) {
            	VersionNumber remoteVersion = getVersionNumber(remoteVersionString);
            	if (isNewer(remoteVersion, newestVersion)) {
            		newestVersion = remoteVersion;
            	}
            }
        }
        
        if ((newestVersion != null) && (isNewer(newestVersion, getVersionNumber(Version.getVersion())))) {
            JOptionPane.showMessageDialog(mainFrame, "New version of RapidMiner is available:" + Tools.getLineSeparator() + Tools.getLineSeparator() + "          RapidMiner " + newestVersion + Tools.getLineSeparator() + Tools.getLineSeparator() + "Please download it from:" + Tools.getLineSeparator() + "          http://www.rapidminer.com", "New RapidMiner version", JOptionPane.INFORMATION_MESSAGE);
        } else if (showFailureDialog) {
            JOptionPane.showMessageDialog(mainFrame, "No newer versions of RapidMiner available!", "RapidMiner is up to date", JOptionPane.INFORMATION_MESSAGE);
        }
	}
	
	private VersionNumber getVersionNumber(String versionString) {
		return new VersionNumber(versionString);
	}
	
	private boolean isNewer(VersionNumber remoteVersion, VersionNumber newestVersion) {
		return remoteVersion.compareTo(newestVersion) > 0;
	}
}
