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
package com.rapidminer.gui.look;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.plaf.UIResource;

/**
 * The UI for image icons.
 *
 * @author Ingo Mierswa
 * @version $Id: ImageIconUIResource.java,v 1.3 2008/05/09 20:57:26 ingomierswa Exp $
 */
public class ImageIconUIResource extends ImageIcon implements UIResource {

	private static final long serialVersionUID = 705603654836477091L;

	public ImageIconUIResource() {}

	public ImageIconUIResource(byte imageData[]) {
		super(imageData);
	}

	public ImageIconUIResource(Image image) {
		super(image);
	}

	public ImageIconUIResource(URL location) {
		super(location, location.toExternalForm());
	}

	public ImageIconUIResource(String filename) {
		super(filename, filename);
	}
}
