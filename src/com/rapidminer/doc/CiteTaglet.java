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
package com.rapidminer.doc;

import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * A taglet with name &quot;@rapidminer.cite&quot; can be used in the Javadoc comments of an operator to produce a reference
 * to literature. Example: &quot;@rapidminer.cite Mierswa/etal/2003a&quot;. This will include a LaTeX cite command to your
 * document.
 * 
 * @author Simon Fischer, Ingo Mierswa
 * @version $Id: CiteTaglet.java,v 1.3 2008/05/09 19:23:22 ingomierswa Exp $
 */
public class CiteTaglet implements TexTaglet {

	private static final String NAME = "rapidminer.cite";

	public String getName() {
		return NAME;
	}

	public boolean inField() {
		return true;
	}

	public boolean inConstructor() {
		return true;
	}

	public boolean inMethod() {
		return true;
	}

	public boolean inOverview() {
		return true;
	}

	public boolean inPackage() {
		return true;
	}

	public boolean inType() {
		return true;
	}

	public boolean isInlineTag() {
		return true;
	}

	public static void register(Map<String, Taglet> tagletMap) {
		CiteTaglet tag = new CiteTaglet();
		Taglet t = tagletMap.get(tag.getName());
		if (t != null) {
			tagletMap.remove(tag.getName());
		}
		tagletMap.put(tag.getName(), tag);
	}

	public String toString(Tag tag) {
		return "[" + tag.text() + "]";
	}

	public String toString(Tag[] tags) {
		return null;
	}

	public String toTex(Tag tag) {
		return "\\cite{" + tag.text() + "}";
	}

	public String toTex(Tag[] tag) {
		return null;
	}
}
