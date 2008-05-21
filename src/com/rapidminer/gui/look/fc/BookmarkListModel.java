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
package com.rapidminer.gui.look.fc;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

/**
 * The model for the list containing the bookmarks.
 *
 * @author Ingo Mierswa
 * @version $Id: BookmarkListModel.java,v 1.2 2008/05/09 19:22:53 ingomierswa Exp $
 */
public class BookmarkListModel extends AbstractListModel {

	private static final long serialVersionUID = 9145792845942034849L;

	private List<Bookmark> bookmarks = new ArrayList<Bookmark>();
	
	public void removeAllBookmarks() {
		int oldSize = bookmarks.size();
		bookmarks.clear();
		fireIntervalRemoved(this, 0, oldSize);
	}
	
	public void addBookmark(Bookmark bookmark) {
		bookmarks.add(bookmark);
		fireIntervalAdded(this, bookmarks.size(), bookmarks.size());
	}
	
	public Object getElementAt(int index) {
		return bookmarks.get(index);
	}

	public int getSize() {
		return bookmarks.size();
	}
}
