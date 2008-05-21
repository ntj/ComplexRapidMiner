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
package com.rapidminer.operator;

import javax.swing.ImageIcon;

import com.rapidminer.gui.tools.SwingTools;


/**
 * Data container for name, class, short name, path and the (very short)
 * description of an operator. If the corresponding operator is not marked 
 * as deprecated the deprecation info string should be null. If the icon 
 * string is null, the group icon will be used.
 * 
 * @author Ingo Mierswa
 * @version $Id: OperatorDescription.java,v 2.5 2006/03/21 15:35:42 ingomierswa
 *          Exp $
 */
public final class OperatorDescription implements Comparable<OperatorDescription> {
	
	private String name;

	private Class<?> clazz;

	private String shortDescription;
	
	private String longDescription;

	private String group;

	private ImageIcon icon = null;
	
	private String iconPath;

	private String deprecationInfo = null;

	/** Creates a new operator description object. If the corresponding operator is not marked as deprecated the 
	 *  deprecation info string should be null. If the icon string is null, the group icon will be used. */
	public OperatorDescription(ClassLoader classLoader, String name, String className, String shortDescription, String longDescription, String group, String iconName, String deprecationInfo) throws ClassNotFoundException {	
		this.name = name;
		this.clazz = Class.forName(className, true, classLoader);
		this.shortDescription = shortDescription;
		this.longDescription = longDescription;
		this.group = group;
		this.deprecationInfo = deprecationInfo;
		if ((this.deprecationInfo != null) && (this.deprecationInfo.trim().length() == 0))
			this.deprecationInfo = null;
		
		reloadIcon(iconName);
	}

	public void reloadIcon(String iconName) {
        // NOTE: resources must use '/' instead of File.separator!
		if (this.iconPath == null) {
			if ((iconName != null) && (iconName.length() > 0)) {
				this.icon = SwingTools.createIcon("operators/24/" + iconName + ".png");
				this.iconPath = "operators/24/" + iconName + ".png";
			} else {
				// try group from most special to most general group
				String groupIconName = group.toLowerCase();
				this.icon = SwingTools.createIcon("groups/24/" + groupIconName + ".png");
				while ((this.icon == null) && (groupIconName.length() > 0)) {
					if (groupIconName.indexOf(".") >= 0) {
						groupIconName = groupIconName.substring(0, groupIconName.lastIndexOf(".")).toLowerCase();
						this.icon = SwingTools.createIcon("groups/24/" + groupIconName + ".png");
					} else {
						groupIconName = "";
					}
				}
				this.iconPath = "groups/24/" + groupIconName + ".png";
			}
		} else {
			this.icon = SwingTools.createIcon(this.iconPath);
		}
	}
	
	public String getName() {
		return name;
	}

	public Class getOperatorClass() {
		return clazz;
	}

	public String getShortDescription() {
		return this.shortDescription;
	}
	
	public String getLongDescriptionHTML() {
		return this.longDescription;
	}

	public String getGroup() {
		return group;
	}

	public ImageIcon getIcon() {
		return this.icon;
	}

	public String getIconPath() {
		return iconPath;
	}
	
	public String getDeprecationInfo() {
		return deprecationInfo;
	}
	
	public String toString() {
		return "operator '" + name + "' loaded from " + clazz + ", description: " + shortDescription + ", group: " + group + ", icon: " + iconPath;
	}

	public int compareTo(OperatorDescription d) {
		return this.name.compareTo(d.name);
	}
	
	public boolean equals(Object o) {
		if (!(o instanceof OperatorDescription)) {
			return false;
		} else {
			return this.name.equals(((OperatorDescription)o).name);
		}
	}
	
	public int hashCode() {
		return this.name.hashCode();
	}
	
	/** Creates a new operator based on the description. */

	public Operator createOperatorInstance() throws OperatorCreationException {
		Operator operator = null;
		try {
			java.lang.reflect.Constructor constructor = clazz.getConstructor(new Class[] { OperatorDescription.class });
			operator = (Operator) constructor.newInstance(new Object[] { this });
		} catch (InstantiationException e) {
			throw new OperatorCreationException(OperatorCreationException.INSTANTIATION_ERROR, name + "(" + clazz.getName() + ")", e);
		} catch (IllegalAccessException e) {
			throw new OperatorCreationException(OperatorCreationException.ILLEGAL_ACCESS_ERROR, name + "(" + clazz.getName() + ")", e);
		} catch (NoSuchMethodException e) {
			throw new OperatorCreationException(OperatorCreationException.NO_CONSTRUCTOR_ERROR, name + "(" + clazz.getName() + ")", e);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw new OperatorCreationException(OperatorCreationException.CONSTRUCTION_ERROR, name + "(" + clazz.getName() + ")", e);
		}
		return operator;
	}
}
