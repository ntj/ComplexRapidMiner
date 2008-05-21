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
package com.rapidminer.tools.plugin;

import java.awt.Frame;
import java.awt.Image;
import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

import com.rapidminer.RapidMiner;
import com.rapidminer.gui.templates.BuildingBlock;
import com.rapidminer.gui.tools.AboutBox;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.OperatorService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.ResourceSource;
import com.rapidminer.tools.Tools;


/**
 * <p>
 * The class for RapidMiner plugins. This class is used to encapsulate the .jar file
 * which must be in the <code>lib/plugins</code> subdirectory of RapidMiner.
 * Provides methods for plugin checks, operator registering, and getting
 * information about the plugin.
 * </p>
 * <p>
 * Plugin dependencies must be defined in the form <br />
 * plugin_name1 (plugin_version1) # ... # plugin_nameM (plugin_versionM) < /br>
 * of the manifest parameter <code>Plugin-Dependencies</code>. You must
 * define both the name and the version of the desired plugins and separate them
 * with &quot;#&quot;.
 * </p>
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: Plugin.java,v 1.6 2008/05/09 19:22:57 ingomierswa Exp $
 */
public class Plugin {

	/**
	 * The jar archive of the plugin which must be placed in the
	 * <code>lib/plugins</code> subdirectory of RapidMiner.
	 */
	private JarFile archive;

	/** The file for this plugin. */
	private File file;
	
	/** The class loader based on the plugin file. */
	private PluginClassLoader classLoader;

	/** The name of the plugin. */
	private String name;

	/** The version of the plugin. */
	private String version;

	/** The vendor of the plugin. */
	private String vendor;

	/** The url for this plugin (in WWW). */
	private String url;

	/** The RapidMiner version which is needed for this plugin. */
	private String necessaryRapidMinerVersion = "0";

	/** The plugins and their versions which are needed for this plugin. */
	private List<Dependency> pluginDependencies = new LinkedList<Dependency>();

	/** The collection of all plugins. */
	private static List<Plugin> allPlugins = new LinkedList<Plugin>();

	/** Creates a new pluging based on the plugin .jar file. */
	public Plugin(File file) throws IOException {
		this.file = file;
		this.archive = new JarFile(this.file);
		URL url = new URL("file", null, this.file.getAbsolutePath());
		this.classLoader = new PluginClassLoader(new URL[] { url });
		Tools.addResourceSource(new ResourceSource(this.classLoader));
		getMetaData();
	}

	/** Returns the name of the plugin. */
	public String getName() {
		return name;
	}

	/** Returns the version of this plugin. */
	public String getVersion() {
		return version;
	}

	/** Returns the necessary RapidMiner version. */
	public String getNecessaryRapidMinerVersion() {
		return necessaryRapidMinerVersion;
	}

	/** Returns the plugin dependencies of this plugin. */
	public List getPluginDependencies() {
		return pluginDependencies;
	}

	/** Returns the class loader of this plugin. */
	public ClassLoader getClassLoader() {
		return this.classLoader;
	}

	/** Checks the RapidMiner version and plugin dependencies. */
	private boolean checkDependencies(List plugins) {
		if (RapidMiner.getVersion().compareTo(necessaryRapidMinerVersion) < 0)
			return false;
		// other plugins
		Iterator i = pluginDependencies.iterator();
		while (i.hasNext()) {
			Dependency dependency = (Dependency) i.next();
			if (!dependency.isFulfilled(plugins))
				return false;
		}
		// all ok
		return true;
	}

	/** Collects all meta data of the plugin from the manifest file. */
	private void getMetaData() {
		try {
			java.util.jar.Attributes atts = archive.getManifest().getMainAttributes();
			name = atts.getValue("Implementation-Title");
			if (name == null) {
				name = archive.getName();
			}
			version = atts.getValue("Implementation-Version");
			if (version == null)
				version = "";

			url = atts.getValue("Implementation-URL");
			vendor = atts.getValue("Implementation-Vendor");

			necessaryRapidMinerVersion = atts.getValue("RapidMiner-Version");
			if (necessaryRapidMinerVersion == null)
				necessaryRapidMinerVersion = "0";

			String dependencies = atts.getValue("Plugin-Dependencies");
			if (dependencies == null)
				dependencies = "";
			addDependencies(dependencies);
			RapidMiner.splashMessage("Loading " + name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/** Register plugin dependencies. */
	private void addDependencies(String dependencies) {
		String[] singleDependencies = dependencies.trim().split("#");
		for (int i = 0; i < singleDependencies.length; i++) {
			if (singleDependencies[i].trim().length() > 0) {
				String dependencyName = singleDependencies[i].trim();
				String dependencyVersion = "0";
				if (singleDependencies[i].trim().indexOf("[") >= 0) {
					dependencyName = singleDependencies[i].trim().substring(0, singleDependencies[i].trim().indexOf("[")).trim();
					dependencyVersion = singleDependencies[i].trim().substring(singleDependencies[i].trim().indexOf("[") + 1, singleDependencies[i].trim().indexOf("]")).trim();
				}
				pluginDependencies.add(new Dependency(dependencyName, dependencyVersion));
			}
		}
	}

	/** Register the operators of this plugin in RapidMiner. */
	public void register() {
		URL operatorsURL = this.classLoader.getResource("META-INF/operators.xml");
		if (operatorsURL == null) {
			operatorsURL = this.classLoader.getResource("operators.xml");
			if (operatorsURL != null) {
				LogService.getGlobal().log(name + ": putting operators.xml in root directory of jar is deprecated. Use META-INF directory instead!", LogService.WARNING);
			}
		}
		if (operatorsURL == null) {
			LogService.getGlobal().log("Plugin '" + archive.getName() + "' does not contain operators.xml!", LogService.ERROR);
		} else {
			// add URLs of plugins this plugin depends on
			Iterator i = pluginDependencies.iterator();
			while (i.hasNext()) {
				String pluginName = ((Dependency) i.next()).getPluginName();
				Plugin other = getPlugin(pluginName);
				mergeClassLoader(other);
			}
			// register operators
			InputStream in = null;
			try {
				in = operatorsURL.openStream();
			} catch (IOException e) {
				LogService.getGlobal().log("Cannot read operators.xml from '" + archive.getName() + "'!", LogService.ERROR);
			}
			LogService.getGlobal().log("Loading " + name, LogService.INIT);
			OperatorService.registerOperators(archive.getName(), in, this.classLoader, true);
		}
	}
	
	/** Returns a list of building blocks. If this plugin does not define any
	 *  building blocks, an empty list will be returned. */
	public List<BuildingBlock> getBuildingBlocks() {
		List<BuildingBlock> result = new LinkedList<BuildingBlock>();

		URL url = null;
		try {
			url = new URL("file", null, this.file.getAbsolutePath());
		} catch (MalformedURLException e1) {
			LogService.getGlobal().log("Cannot load plugin building blocks. Skipping...", LogService.ERROR);
		}
		if (url != null) {
			ClassLoader independentLoader = new PluginClassLoader(new URL[] { url });
			URL bbDefinition = independentLoader.getResource(Tools.RESOURCE_PREFIX + "buildingblocks.txt");
			if (bbDefinition != null) {
				BufferedReader in = null;
				try {
					in = new BufferedReader(new InputStreamReader(bbDefinition.openStream()));

					String line = null;
					while ((line = in.readLine()) != null) {
						URL bbURL = this.classLoader.getResource(Tools.RESOURCE_PREFIX + line);
						BufferedReader bbIn = null;
						try {
							bbIn = new BufferedReader(new InputStreamReader(bbURL.openStream()));
							result.add(new BuildingBlock(bbIn));
						} catch (IOException e) {
							LogService.getGlobal().log("Cannot load plugin building blocks. Skipping...", LogService.ERROR);		
						} finally {
							if (bbIn != null) {
								bbIn.close();
							}
						}
					}
				} catch (IOException e) {
					LogService.getGlobal().log("Cannot load plugin building blocks.", LogService.WARNING);					
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							LogService.getGlobal().log("Cannot close stream to plugin building blocks.", LogService.ERROR);
						}
					}
				}
			}
		}
		return result;
	}
	
	/** Creates the about box for this plugin. */
	public AboutBox createAboutBox(Frame owner, Image productLogo) {
		String about = "";
		try {
			URL url = this.classLoader.getResource("META-INF/ABOUT.NFO");
			if (url != null)
				about = Tools.readTextFile(new InputStreamReader(url.openStream()));
		} catch (Throwable e) {}
		return new AboutBox(owner, name, version, "Vendor: " + ((vendor != null) ? vendor : "unknown"), "URL: " + ((url != null) ? url : "unknown"), about, productLogo);
	}

	/** Returns a list of Plugins found in the plugins directory. */
	public static void findPlugins(File pluginDir) {
		if (!(pluginDir.exists() && pluginDir.isDirectory()))
			return;
		File[] files = pluginDir.listFiles(new FilenameFilter() {

			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");
			}
		});
		allPlugins = new LinkedList<Plugin>();
		for (int i = 0; i < files.length; i++) {
			try {
				allPlugins.add(new Plugin(files[i]));
			} catch (Throwable e) {
				LogService.getGlobal().log("Cannot load plugin '" + files[i] + "': " + e.getMessage(), LogService.ERROR);
			}
		}
	}

	/**
	 * Adds the URLs of the given Plugin to class loader of this one. This
	 * method should only be used to resolve plugin dependencies.
	 */
	protected void mergeClassLoader(Plugin other) {
		mergeClassLoaders(other.classLoader, this.classLoader);
	}
	
	/** Merges the URL from the first classloader into the second one. */
	private static void mergeClassLoaders(PluginClassLoader first, PluginClassLoader second) {
		URL[] otherURLs = first.getURLs();
		//this.classLoader = new PluginClassLoader(otherURLs);
		for (int i = 0; i < otherURLs.length; i++)
			second.addDependingURL(otherURLs[i]);
	}

	public String toString() {
		return name + " " + version + " (" + archive.getName() + ") depending on " + pluginDependencies;
	}

	/** Returns a list of Plugins found in the given plugins directory. If the given directory is null, 
	 *  then RapidMiner tries to find plugins in the directory rapidminer.home/lib/plugins. */
	public static void registerAllPlugins(File pluginDirectory) {
		File pluginDir = pluginDirectory;
		if (pluginDir == null)
			pluginDir = ParameterService.getPluginDir();
		findPlugins(pluginDir);
		Iterator i = allPlugins.iterator();
		while (i.hasNext()) {
			Plugin plugin = (Plugin) i.next();
			if (!plugin.checkDependencies(allPlugins)) {
				LogService.getGlobal().log("Cannot register operators from '" + plugin.getName() + "': Dependencies not fulfilled! This plugin needs a RapidMiner version " + plugin.getNecessaryRapidMinerVersion() + " and the following plugins:" + Tools.getLineSeparator() + plugin.getPluginDependencies(), LogService.ERROR);
				i.remove();
			}
		}

		if (allPlugins.size() > 0) {
			LogService.getGlobal().log("Found " + allPlugins.size() + " plugins in " + ParameterService.getPluginDir(), LogService.INIT);
			i = allPlugins.iterator();
			while (i.hasNext()) {
				((Plugin) i.next()).register();
			}			
		}
	}

	/** Returns a class loader which is able to load all classes (core _and_ all plugins). */
	public static ClassLoader getMajorClassLoader() {
		PluginClassLoader majorPluginClassLoader = null;
		Iterator<Plugin> i = allPlugins.iterator();
		while (i.hasNext()) {
			Plugin plugin = i.next();
			if (majorPluginClassLoader == null) {
				majorPluginClassLoader = plugin.classLoader;
			} else {
				mergeClassLoaders(plugin.classLoader, majorPluginClassLoader);
			}
		}
		if (majorPluginClassLoader == null) {
			return ClassLoader.getSystemClassLoader();
		} else {
			return majorPluginClassLoader;
		}
	}
	
	/** Returns the collection of all plugins. */
	public static List<Plugin> getAllPlugins() {
		return allPlugins;
	}

	/** Returns the desired plugin. */
	public static Plugin getPlugin(String name) {
		Iterator<Plugin> i = allPlugins.iterator();
		while (i.hasNext()) {
			Plugin plugin = i.next();
			if (plugin.getName().equals(name))
				return plugin;
		}
		return null;
	}
}
