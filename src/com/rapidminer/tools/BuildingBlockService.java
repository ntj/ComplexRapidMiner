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

import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.gui.templates.BuildingBlock;
import com.rapidminer.tools.plugin.Plugin;


/** This service class can be used to deliver all building blocks, i.e. both predefined
 *  and user defined building blocks.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: BuildingBlockService.java,v 1.5 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class BuildingBlockService {

	/** Returns a sorted list of all building blocks. */
	public static List<BuildingBlock> getBuildingBlocks() {
		List<BuildingBlock> buildingBlocks = getPredefinedBuildingBlocks();
		buildingBlocks.addAll(getPluginBuildingBlocks());
		buildingBlocks.addAll(getUserBuildingBlocks());
		Collections.sort(buildingBlocks);
		return buildingBlocks;
	}
	
	/** Returns all user defined building blocks. The result is not sorted. */
	public static List<BuildingBlock> getUserBuildingBlocks() {
		File[] userDefinedBuildingBlockFiles = ParameterService.getUserRapidMinerDir().listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".buildingblock");
			}
		});

		List<BuildingBlock> buildingBlocks = new LinkedList<BuildingBlock>();
		for (File file : userDefinedBuildingBlockFiles) {
			try {
				buildingBlocks.add(new BuildingBlock(file));
			} catch (InstantiationException e) {
				LogService.getGlobal().log("Cannot load building block file '" + file + "': " + e.getMessage(), LogService.ERROR);
			}
		}
		return buildingBlocks;
	}

	/** Returns all predefined building blocks. The result is not sorted. */
	public static List<BuildingBlock> getPredefinedBuildingBlocks() {
		File[] preDefinedBuildingBlockFiles = ParameterService.getConfigFile("buildingblocks").listFiles(new FileFilter() {

			public boolean accept(File file) {
				return file.getName().endsWith(".buildingblock");
			}
		});

		List<BuildingBlock> buildingBlocks = new LinkedList<BuildingBlock>();
		for (File file : preDefinedBuildingBlockFiles) {
			try {
				buildingBlocks.add(new BuildingBlock(file));
			} catch (InstantiationException e) {
				LogService.getGlobal().log("Cannot load building block file '" + file + "': " + e.getMessage(), LogService.ERROR);
			}
		}
		return buildingBlocks;
	}
	
	/** Returns all building blocks defined by plugins. */
	public static List<BuildingBlock> getPluginBuildingBlocks() {
		List<BuildingBlock> buildingBlocks = new LinkedList<BuildingBlock>();
		Iterator<Plugin> p = Plugin.getAllPlugins().iterator();
		while (p.hasNext()) {
			buildingBlocks.addAll(p.next().getBuildingBlocks());
		}
		return buildingBlocks;
	}
}
