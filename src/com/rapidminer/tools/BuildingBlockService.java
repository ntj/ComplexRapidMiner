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
import java.io.FileFilter;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.gui.templates.BuildingBlock;


/** This service class can be used to deliver all building blocks, i.e. both predefined
 *  and user defined building blocks.
 *  
 *  @author Ingo Mierswa
 *  @version $Id: BuildingBlockService.java,v 1.1 2007/05/27 21:59:08 ingomierswa Exp $
 */
public class BuildingBlockService {

	/** Returns a sorted list of all building blocks. */
	public static List<BuildingBlock> getBuildingBlocks() {
		List<BuildingBlock> buildingBlocks = getPredefinedBuildingBlocks();
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
			} catch (IOException e) {
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
			} catch (IOException e) {
				LogService.getGlobal().log("Cannot load building block file '" + file + "': " + e.getMessage(), LogService.ERROR);
			}
		}
		return buildingBlocks;
	}
}
