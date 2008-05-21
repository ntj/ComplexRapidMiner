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
package com.rapidminer.gui.tools.syntax;
/*
 * PatchTokenMarker.java - DIFF patch token marker
 * Copyright (C) 1999 Slava Pestov
 *
 * You may use and modify this package for any purpose. Redistribution is
 * permitted, in both source and binary form, provided that this notice
 * remains intact in all source distributions of this package.
 */

import javax.swing.text.Segment;

/**
 * Patch/diff token marker.
 * 
 * @author Slava Pestov, Ingo Mierswa
 * @version $Id: PatchTokenMarker.java,v 1.3 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class PatchTokenMarker extends TokenMarker {
	public byte markTokensImpl(byte token, Segment line, int lineIndex) {
		if (line.count == 0)
			return Token.NULL;
		switch (line.array[line.offset]) {
		case '+':
		case '>':
			addToken(line.count, Token.KEYWORD1);
			break;
		case '-':
		case '<':
			addToken(line.count, Token.KEYWORD2);
			break;
		case '@':
		case '*':
			addToken(line.count, Token.KEYWORD3);
			break;
		default:
			addToken(line.count, Token.NULL);
			break;
		}
		return Token.NULL;
	}

	public boolean supportsMultilineTokens() {
		return false;
	}
}
