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
package com.rapidminer.operator;

import java.awt.Image;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import com.rapidminer.tools.Tools;


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

	private String description;

	private String group;

	private Image icon = null;

	private String iconPath;

	private String deprecationInfo = null;

	/** Creates a new operator description object. If the corresponding operator is not marked as deprecated the 
	 *  deprecation info string should be null. If the icon string is null, the group icon will be used. */
	public OperatorDescription(ClassLoader classLoader, String name, String className, String description, String group, String icon, String deprecationInfo) throws ClassNotFoundException {	
		this.name = name;
		this.clazz = Class.forName(className, true, classLoader);
		this.description = description;
		this.group = group;
		this.deprecationInfo = deprecationInfo;
		if ((this.deprecationInfo != null) && (this.deprecationInfo.trim().length() == 0))
			this.deprecationInfo = null;
		
		URL url = null;
        
        // NOTE: resources must use '/' instead of File.separator!
		if ((icon != null) && (icon.length() > 0)) {
			url = Tools.getResource("icons/operators/24/" + icon + ".png");
			this.iconPath = "/operators/24/" + icon + ".png";
		} else {
			// try group from most special to most general group
			String groupIconName = group.toLowerCase();
			url = Tools.getResource("icons/groups/24/" + groupIconName + ".png");
			while ((url == null) && (groupIconName.length() > 0)) {
				if (groupIconName.indexOf(".") >= 0) {
					groupIconName = groupIconName.substring(0, groupIconName.lastIndexOf(".")).toLowerCase();
					url = Tools.getResource("icons/groups/24/" + groupIconName + ".png");
				} else {
					groupIconName = "";
				}
			}
			this.iconPath = "/groups/24/" + groupIconName + ".png";
		}
		if (url != null) {
			try {
				this.icon = ImageIO.read(url);
			} catch (IOException e) {
				this.iconPath = null;
			}
		} else {
			this.iconPath = null;
		}
	}

	public String getName() {
		return name;
	}

	public Class getOperatorClass() {
		return clazz;
	}

	public String getDescription() {
		return description;
	}

	public String getGroup() {
		return group;
	}

	public Image getIcon() {
		return icon;
	}

	public String getIconPath() {
		return iconPath;
	}
	
	public String getDeprecationInfo() {
		return deprecationInfo;
	}
	
	public String toString() {
		return "operator '" + name + "' loaded from " + clazz + ", description: " + description + ", group: " + group + ", icon: " + iconPath;
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
