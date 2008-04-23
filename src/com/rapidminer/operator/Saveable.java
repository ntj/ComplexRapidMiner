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

import java.io.File;
import java.io.IOException;

/**
 * Interface for resultobjects that can be saved to disc. The
 * {@link ResultObjectAdapter} automatically adds a save button for these
 * objects.
 * 
 * @author Ingo Mierswa
 * @version $Id: Saveable.java,v 1.1 2007/05/27 21:59:03 ingomierswa Exp $
 */
public interface Saveable {

    /** Indicates if this object is actually savable. Objects which return false here
     *  will not provide savability options, e.g. no Save button will be shown in the GUI. */
    public boolean isSavable();
    
	/** Writes the object into the given file. */
	public void save(File file) throws IOException;
	
	/** Returns the default extension in file choosers. */
	public String getExtension();
	
	/** Returns the file description used in file choosers. */
	public String getFileDescription();
}
