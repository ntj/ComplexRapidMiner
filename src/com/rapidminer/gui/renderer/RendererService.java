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
package com.rapidminer.gui.renderer;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.rapidminer.operator.IOObject;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.XMLException;

/**
 * The renderer service is the basic provider for all registered 
 * renderers. All {@link IOObject}s which want to provide a 
 * Renderer for visualization and reporting must place an entry
 * in the <code>ioobjects.xml</xml> file in order to allow
 * for renderer retrieval.
 * 
 * @author Ingo Mierswa
 * @version $Id: RendererService.java,v 1.7 2008/07/13 16:39:41 ingomierswa Exp $
 */
public class RendererService {
	
	private static Set<String> objectNames = new TreeSet<String>();
	
	private static Map<String, List<Renderer>> objectRenderers = new HashMap<String, List<Renderer>>();

	private static Map<String, Class<?>> objectClasses = new HashMap<String, Class<?>>();
	
	private static Map<String, Class<IOObject>> objectSuperTypes = new HashMap<String, Class<IOObject>>();

	private static Map<String, Boolean> reportableMap = new HashMap<String, Boolean>();
	
	private static Map<Class<?>, String> class2NameMap = new HashMap<Class<?>, String>();

	
	public static void init() {
		try {
			InputStream in = null;
			try {
				URL url = Tools.getResource("ioobjects.xml");
				if (url != null) {
					in = url.openStream();
					Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(in);
					Element ioObjectsElement = document.getDocumentElement();
					if (ioObjectsElement.getTagName().equals("ioobjects")) {
						NodeList ioObjectNodes = ioObjectsElement.getElementsByTagName("ioobject");
						for (int i = 0; i < ioObjectNodes.getLength(); i++) {
							Node ioObjectNode = ioObjectNodes.item(i);
							if (ioObjectNode instanceof Element) {
								Element ioObjectElement = (Element)ioObjectNode;

								String name = ioObjectElement.getAttribute("name");
								String className = ioObjectElement.getAttribute("class");
								String superTypeName = ioObjectElement.getAttribute("supertype");
								String reportableString = "true";
								if (ioObjectElement.hasAttribute("reportable")) {
									reportableString = ioObjectElement.getAttribute("reportable");
								}
								boolean reportable = Tools.booleanValue(reportableString, true);
								
								NodeList rendererNodes = ioObjectElement.getElementsByTagName("renderer");
								List<String> renderers = new LinkedList<String>();
								for (int k = 0; k < rendererNodes.getLength(); k++) {
									Node rendererNode = rendererNodes.item(k);
									if (rendererNode instanceof Element) {
										Element rendererElement = (Element)rendererNode;
										String rendererName = rendererElement.getTextContent();
										renderers.add(rendererName);
									}
								}

								registerRenderers(name, className, superTypeName, reportable, renderers);
							}
						}
					} else {
						throw new XMLException("Outermost tag of a ioobjects.xml definition must be either <ioobjects>!");
					}
				}
			} catch (javax.xml.parsers.ParserConfigurationException e) {
				throw new XMLException(e.toString(), e);
			} catch (SAXException e) {
				throw new XMLException("Cannot parse document: " + e, e);
			} catch (IOException e) {
				throw new XMLException("Cannot parse document: " + e, e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (IOException e) {
						// do nothing
					}
				}
			}
		} catch (XMLException e) {
			LogService.getGlobal().logError("Cannot initialize io object description: " + e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void registerRenderers(String name, String className, String superTypeName, boolean reportable, List<String> rendererNames) {
		objectNames.add(name);
		
		try {
			Class<?> clazz = Class.forName(className);
			Class<IOObject> superType = (Class<IOObject>)Class.forName(superTypeName);

			List<Renderer> renderers = new LinkedList<Renderer>();
			for (String rendererName : rendererNames) {
				Class<?> rendererClass = Class.forName(rendererName);
				Renderer renderer = (Renderer)rendererClass.newInstance();
				renderers.add(renderer);
			}

			objectRenderers.put(name, renderers);
			objectClasses.put(name, clazz);
			objectSuperTypes.put(name, superType);
			class2NameMap.put(clazz, name);
			reportableMap.put(name, reportable);
		} catch (Throwable e) {
			LogService.getGlobal().logWarning("Cannot register renderer: " + e);
		}
	}
	
	public static Set<String> getAllRenderableObjectNames() {
		return objectNames;
	}

	public static Set<String> getAllReportableObjectNames() {
		Set<String> result = new TreeSet<String>();
		for (String name : objectNames) {
			Boolean reportable = reportableMap.get(name);
			if ((reportable != null) && (reportable)) {
				result.add(name);
			}
		}
		return result;
	}
	
	public static String getName(Class<?> clazz) {
		String result = class2NameMap.get(clazz);
		if (result == null) {
			result = getNameForSuperClass(clazz);
		}
		return result;
	}

	private static String getNameForSuperClass(Class<?> clazz) {
		Class superClazz = clazz.getSuperclass();
		if (superClazz == null) {
			return null;
		} else {
			String result = class2NameMap.get(superClazz);
			if (result == null) {
				return getNameForSuperClass(superClazz);
			} else {
				return result;
			}
		}
	}
	
	public static Class<?> getClass(String name) {
		return objectClasses.get(name);
	}

	public static Class<IOObject> getSuperType(String name) {
		return objectSuperTypes.get(name);
	}
	
	public static List<Renderer> getRenderers(String name) {
		return objectRenderers.get(name);
	}
	
	public static Renderer getRenderer(String reportableName, String rendererName) {
		List<Renderer> renderers = getRenderers(reportableName);
		for (Renderer renderer : renderers) {
			if (renderer.getName().equals(rendererName)) {
				return renderer;
			}
		}
		return null;
	}
}
