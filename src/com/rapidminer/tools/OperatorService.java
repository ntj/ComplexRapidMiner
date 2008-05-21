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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.rapidminer.operator.IOObject;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorCreationException;
import com.rapidminer.operator.OperatorDescription;
import com.rapidminer.operator.performance.AbstractPerformanceEvaluator;
import com.rapidminer.operator.performance.PerformanceCriterion;


/**
 * <p>This class reads the description of the RapidMiner operators. These descriptions
 * are entries in a XML File like:</p> <br>
 * <code>
 *    &lt;operators&gt;<br>
 *    &nbsp;&nbsp;&lt;operator<br>
 *    &nbsp;&nbsp;&nbsp;&nbsp;name="OperatorName" <br>
 *    &nbsp;&nbsp;&nbsp;&nbsp;class="java.path.OperatorClass" <br>
 *    &nbsp;&nbsp;&nbsp;&nbsp;description="OperatorDescription" <br>
 *    &nbsp;&nbsp;&nbsp;&nbsp;deprecation="OperatorDeprecationInfo" <br>
 *    &nbsp;&nbsp;&nbsp;&nbsp;group="OperatorGroup" <br>
 *    &nbsp;&nbsp;&nbsp;&nbsp;icon="OperatorIcon" <br> 
 *    /&gt;<br>
 *  </code><br>
 * 
 * <p>The values (and the whole tag) for deprecation and icon might be omitted. If no
 * deprecation info was specified, the operator is simply not deprecated. If no icon
 * is specified, RapidMiner just uses the icon of the parent group.</p>
 * 
 * <p>NOTE: This class should be used to create operators and is therefore an
 * operator factory.</p>
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: OperatorService.java,v 1.10 2008/05/09 19:22:55 ingomierswa Exp $
 */
public class OperatorService {

	/**
	 * Maps operator names of form classname|subclassname to operator
	 * descriptions.
	 */
	private static Map<String, OperatorDescription> names2descriptions = new HashMap<String, OperatorDescription>();

	/** Map for group name &lt;-&gt; group (list). */
	private static GroupTree groupTree = new GroupTree("");

	/** The Map for all IO objects (maps short names on classes). */
	private static Map<String, Class<IOObject>> ioObjects = new TreeMap<String, Class<IOObject>>();

	
	/** Registers all operators from a given XML input stream. */
	public static void registerOperators(String name, InputStream operatorsXML, ClassLoader classLoader, boolean addWekaOperators) {
		// create long descriptions map
		Map<String, String> descriptionMap = loadDescriptionMap();
		
		// register operators
		if (classLoader == null)
			classLoader = OperatorService.class.getClassLoader();
		LogService.getGlobal().log("Loading operators from '" + name + "'.", LogService.INIT);
		Document document = null;
		try {
			document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(operatorsXML);
		} catch (Exception e) {
			LogService.getGlobal().log("Cannot read operator description file '" + name + "': no valid XML: " + e.getMessage(), LogService.ERROR);
		}
		if (document != null) {
			if (!document.getDocumentElement().getTagName().toLowerCase().equals("operators")) {
                LogService.getGlobal().log("Operator description file '" + name + "': outermost tag must be <operators>!", LogService.ERROR);
				return;
			}

			// operator factories
			NodeList factoryTags = document.getDocumentElement().getElementsByTagName("factory");
			for (int i = 0; i < factoryTags.getLength(); i++) {
				Element factoryTag = (Element) factoryTags.item(i);
				Attr classAttr = factoryTag.getAttributeNode("class");
				if (classAttr == null) {
                    LogService.getGlobal().log("Operator description file '" + name + "': factory tag must provide class attribute!", LogService.ERROR);
				} else {
					Class factoryClass = null;
					try {
						factoryClass = classLoader.loadClass(classAttr.getValue());
					} catch (ClassNotFoundException e) {
                        LogService.getGlobal().log("Operator factory class '" + classAttr.getValue() + "' not found!", LogService.ERROR);
					}
					if (factoryClass != null) {
						if (GenericOperatorFactory.class.isAssignableFrom(factoryClass)) {
							GenericOperatorFactory factory = null;
							try {
								factory = (GenericOperatorFactory) factoryClass.newInstance();
							} catch (Exception e) {
                                LogService.getGlobal().log("Cannot instantiate operator factory class '" + factoryClass.getName() + "'!", LogService.ERROR);
							}
							if (factory != null) {
								if (addWekaOperators || (!(factory instanceof WekaOperatorFactory))) {
									factory.registerOperators(classLoader);
								}
							}
						} else {
                            LogService.getGlobal().log("Operator description file '" + name + "': only subclasses of GenericOperatorFactory may be defined as class, was '" + classAttr.getValue() + "'!", LogService.ERROR);
						}
					}
				}
			}

			// operators
			NodeList operatorTags = document.getDocumentElement().getElementsByTagName("operator");
			for (int i = 0; i < operatorTags.getLength(); i++) {
				Element currentElement = (Element) operatorTags.item(i);
				try {
					registerOperator(currentElement, classLoader, descriptionMap);
				} catch (Throwable e) {
					//e.printStackTrace();
					Attr currentNameAttr = currentElement.getAttributeNode("name");
					if (currentNameAttr != null)
                        LogService.getGlobal().log("Cannot register '" + currentNameAttr.getValue() + "': " + e, LogService.ERROR);
					else
                        LogService.getGlobal().log("Cannot register '" + currentElement + "': " + e, LogService.ERROR);
				}
			}
		}
	}

	/**
	 * Registers an operator description from an XML tag (operator description
	 * file, mostly operators.xml).
	 */
	private static void registerOperator(Element operatorTag, ClassLoader classLoader, Map<String, String> descriptionMap) throws Exception {
		Attr nameAttr = operatorTag.getAttributeNode("name");
		Attr classAttr = operatorTag.getAttributeNode("class");
		if (nameAttr == null)
			throw new Exception("Missing name for <operator> tag");
		if (classAttr == null)
			throw new Exception("Missing class for <operator> tag");

		String name = nameAttr.getValue();
		String shortDescription = operatorTag.getAttribute("description");
		String longDescription = descriptionMap.get(name);
		if (longDescription == null)
			longDescription = shortDescription;
		registerOperator(classLoader, nameAttr.getValue(), classAttr.getValue(), shortDescription, longDescription, operatorTag.getAttribute("group"), operatorTag.getAttribute("icon"), operatorTag.getAttribute("deprecation"));
	}

	/** Registers an operator description from the given meta data. */
	private static void registerOperator(ClassLoader classLoader, String name, String clazz, String shortDescription, String longDescription, String group, String icon, String deprecationInfo) throws Exception {
		registerOperator(new OperatorDescription(classLoader, name, clazz, shortDescription, longDescription, group, icon, deprecationInfo));
	}

	/**
	 * Registers the given operator description. Please note that two different
	 * descriptions must not have the same name. Otherwise the
	 * second description overwrite the first in the description map.
	 */
	public static void registerOperator(OperatorDescription description) throws Exception {
		// check if this operator was not registered earlier
		OperatorDescription oldDescription = names2descriptions.get(description.getName());
		if (oldDescription != null) {
            LogService.getGlobal().log("An operator '" + description.getName() + "' was already registered. Overwriting...", LogService.WARNING);
		}

		// register
		names2descriptions.put(description.getName(), description);
		Operator currentOperator = description.createOperatorInstance();
		checkIOObjects(currentOperator.getInputClasses());
		checkIOObjects(currentOperator.getOutputClasses());

		// add to group
		String groupString = description.getGroup();
		String[] groupNames = groupString.split("\\.");
		GroupTree currentGroup = groupTree;
		for (int j = 0; j < groupNames.length; j++) {
			String currentGroupName = groupNames[j].trim();
			if (currentGroupName.length() > 0) {
				GroupTree subGroup = currentGroup.getSubGroup(currentGroupName);
				if (subGroup == null) {
					subGroup = new GroupTree(currentGroupName);
					currentGroup.addSubGroup(subGroup);
				}
				currentGroup = subGroup;
			}
		}
		currentGroup.addOperatorDescription(description);
	}

	private static Map<String, String> loadDescriptionMap() {
		URL descriptionUrl = Tools.getResource("long_documentation.txt");
		Map<String, String> descriptionMap = new HashMap<String, String>();
		if (descriptionUrl != null) {
			BufferedReader in = null;
			boolean beginNew = true;
			try {
				in = new BufferedReader(new InputStreamReader(descriptionUrl.openStream()));
				String line = null;
				String currentName = null;
				StringBuffer currentDescription = null;
				while ((line = in.readLine()) != null) {
					if (line.trim().length() == 0)
						continue;
					if (beginNew) {
						currentName = line;
						currentDescription = new StringBuffer();
						beginNew = false;
					} else {
						if (line.startsWith("#####")) {
							if (currentName != null) {
								descriptionMap.put(currentName, currentDescription.toString());
								currentName = null;
								currentDescription = null;
								beginNew = true;
							} else {
								currentName = null;
								currentDescription = null;
								beginNew = true;
							}
						} else {
							String transformed = Tools.removeAllLineSeparators(line);
							currentDescription.append(transformed);
						}
					}
				}
			} catch (IOException e) {
				LogService.getGlobal().logError("Cannot read long descriptions from resources.");
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// do nothing
					}
				}
			}
		}
		return descriptionMap;
	}
	
	/** This method is only necessary since the operators deliver Class arrays (which
	 *  cannot be instantiated with Generics) and can be removed after this was changed to 
	 *  collections.
	 *  TODO: remove this method after getInputClasses() and getOutputClasses() deliver
	 *  collections and call checkIOObjects(Collection) directly. */
	@SuppressWarnings("unchecked")
	private static void checkIOObjects(Class[] objects) {
		List<Class<IOObject>> result = new LinkedList<Class<IOObject>>();
		if (objects != null) {
			for (int i = 0; i < objects.length; i++) {
				//Class<IOObject> newClass = (Class<IOObject>)objects[i];
				result.add(objects[i]);
			}
		}
		checkIOObjects(result);
	}
	
	/** Checks if the given classes are already registered and adds them if not. */
	private static void checkIOObjects(Collection<Class<IOObject>> objects) {
		Iterator<Class<IOObject>> i = objects.iterator();
		while (i.hasNext()) {
			Class<IOObject> currentClass = i.next();
			String current = currentClass.getName();
			ioObjects.put(current.substring(current.lastIndexOf(".") + 1), currentClass);
		}
	}

	/** Returns a sorted set of all short IO object names. */
	public static Set<String> getIOObjectsNames() {
		return ioObjects.keySet();
	}

	/** Defines the alias pairs for the {@link XMLSerialization} for all IOObject pairs. */
	public static void defineXMLAliasPairs() {
		// pairs for IOObjects
		Iterator<Map.Entry<String, Class<IOObject>>> i = ioObjects.entrySet().iterator();
		while (i.hasNext()) {
			Map.Entry<String, Class<IOObject>> entry = i.next();
			String objectName = entry.getKey();
			Class objectClass = entry.getValue();
			XMLSerialization.getXMLSerialization().addAlias(objectName, objectClass);
		}
		
		// pairs for performance criteria
		Iterator<String> o = getOperatorNames().iterator();
		while (o.hasNext()) {
			String name = o.next();
			Operator operator = null;
			try {
				operator = createOperator(name);
			} catch (OperatorCreationException e) {
				// does nothing
			}
			if (operator != null) {
				if (operator instanceof AbstractPerformanceEvaluator) {
					AbstractPerformanceEvaluator evaluator = (AbstractPerformanceEvaluator)operator;
					List<PerformanceCriterion> criteria = evaluator.getCriteria();
					for (PerformanceCriterion criterion : criteria) {
						XMLSerialization.getXMLSerialization().addAlias(criterion.getName(), criterion.getClass());
					}
				}
			}
		}
	}
	
	
	/**
	 * Returns a collection of all operator descriptions of operators which
	 * return the desired IO object as output.
	 */
	public static Set<OperatorDescription> getOperatorsDelivering(Class ioObject) {
		Set<OperatorDescription> result = new HashSet<OperatorDescription>();
		Iterator<String> i = names2descriptions.keySet().iterator();
		while (i.hasNext()) {
			String name = i.next();
			OperatorDescription description = getOperatorDescription(name);
			try {
				Operator currentOperator = description.createOperatorInstance();
				if (containsClass(currentOperator.getOutputClasses(), ioObject))
					result.add(description);
			} catch (Exception e) {}
		}
		return result;
	}

	/**
	 * Returns a collection of all operator descriptions which requires the
	 * given IO object as input.
	 */
	public static Set<OperatorDescription> getOperatorsRequiring(Class ioObject) {
		Set<OperatorDescription> result = new HashSet<OperatorDescription>();
		Iterator<String> i = names2descriptions.keySet().iterator();
		while (i.hasNext()) {
			String name = i.next();
			OperatorDescription description = getOperatorDescription(name);
			try {
				Operator currentOperator = description.createOperatorInstance();
				if (containsClass(currentOperator.getInputClasses(), ioObject))
					result.add(description);
			} catch (Exception e) {}
		}
		return result;
	}

	/**
	 * Returns true if the given class array contains the given class itself or
	 * a subclass.
	 */
	private static boolean containsClass(Class<?>[] types, Class<?> type) {
		if (types != null) {
			for (int i = 0; i < types.length; i++) {
				if (type.isAssignableFrom(types[i]))
					return true;
			}
		}
		return false;
	}

	/** Returns the class for the short name of an IO object. */
	public static Class<IOObject> getIOObjectClass(String name) {
		return ioObjects.get(name);
	}

	/**
	 * Returns a collection of all operator names. A name has the structure
	 * classname|subclassname.
	 */
	public static Set<String> getOperatorNames() {
		return names2descriptions.keySet();
	}

	/** Returns the group hierarchy of all operators. */
	public static GroupTree getGroups() {
		return groupTree;
	}

	/** Reload all icons, e.g. after a look and feel change. */
	public static void reloadIcons() {
		for (String name : getOperatorNames()) {
			OperatorDescription description = getOperatorDescription(name);
			description.reloadIcon(null);
		}
	}
	
	// ================================================================================
	// Operator Factory Methods
	// ================================================================================

	/**
	 * Returns the operator descriptions for the operators which uses the given
	 * class. Performs a linear seach through all operator descriptions.
	 */
	public static OperatorDescription[] getOperatorDescriptions(Class clazz) {
		List<OperatorDescription> result = new LinkedList<OperatorDescription>();
		Iterator<String> i = names2descriptions.keySet().iterator();
		while (i.hasNext()) {
			OperatorDescription current = getOperatorDescription(i.next());
			if (current.getOperatorClass().equals(clazz))
				result.add(current);
		}
		OperatorDescription[] resultArray = new OperatorDescription[result.size()];
		result.toArray(resultArray);
		return resultArray;
	}

	/**
	 * Returns the operator description for a given class name from the
	 * operators.xml file, e.g. &quot;Process&quot; for a ProcessRootOperator. */
	public static OperatorDescription getOperatorDescription(String completeName) {
		return names2descriptions.get(completeName);
	}

	/**
	 * Use this method to create an operator from the given class name (from
	 * operator description file operators.xml, not from the Java class name).
	 * For most operators, is is recommended to use the method 
	 * {@link #createOperator(Class)} which can be checked during compile time.
	 * This is, however, not possible for some generic operators like the 
	 * Weka operators. In that case, you have to use this method with the 
	 * argument from the operators.xml file, e.g. <tt>createOperator(&quot;J48&quot;)</tt>
	 * for a J48 decision tree learner.
	 */
	public static Operator createOperator(String typeName) throws OperatorCreationException {
		OperatorDescription description = getOperatorDescription(typeName);
		if (description == null)
			throw new OperatorCreationException(OperatorCreationException.NO_DESCRIPTION_ERROR, typeName, null);
		return createOperator(description);
	}

	/** Use this method to create an operator of a given description object. */
	public static Operator createOperator(OperatorDescription description) throws OperatorCreationException {
		return description.createOperatorInstance();
	}

	/**
	 * <p>Use this method to create an operator from an operator class.
	 * This is the only method which ensures operator existence
	 * checks during compile time (and not during runtime) and the usage 
	 * of this method is therefore the recommended way for operator creation.
	 * </p>
	 * 
	 * <p>It is, however, not possible to create some generic operators
	 * with this method (this mainly applies to the Weka operators). Please
	 * use the method {@link #createOperator(String)} for those generic
	 * operators.</p>
	 * 
     * <p>If you try to create a generic operator with this method,
     * the OperatorDescription will not be unique for the given class and
     * an OperatorCreationException is thrown.</p>
     * 
     * <p>Please note that is is not necessary to cast the operator to
     * the desired class.</p>
     * 
     * TODO: can we remove the supress warning here?
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Operator> T createOperator(Class<T> clazz) throws OperatorCreationException {
		OperatorDescription[] descriptions = getOperatorDescriptions(clazz);
		if (descriptions.length == 0)
			throw new OperatorCreationException(OperatorCreationException.NO_DESCRIPTION_ERROR, clazz.getName(), null);
		if (descriptions.length > 1)
			throw new OperatorCreationException(OperatorCreationException.NO_UNIQUE_DESCRIPTION_ERROR, clazz.getName(), null);
		return (T)descriptions[0].createOperatorInstance();
	}
}
