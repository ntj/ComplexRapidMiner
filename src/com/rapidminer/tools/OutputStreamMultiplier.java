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

import java.io.IOException;
import java.io.OutputStream;

/**
 * A stream that writes all that is written to it to a set of other output
 * streams.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: OutputStreamMultiplier.java,v 2.7 2006/03/21 15:35:53
 *          ingomierswa Exp $
 */
public class OutputStreamMultiplier extends OutputStream {

	private OutputStream[] streams;

	public OutputStreamMultiplier(OutputStream[] streams) {
		this.streams = streams;
	}

	public void write(int b) throws IOException {
		for (int i = 0; i < streams.length; i++) {
			streams[i].write(b);
		}
	}

	public void close() throws IOException {
		for (int i = 0; i < streams.length; i++) {
			streams[i].close();
		}
	}

	public void flush() throws IOException {
		for (int i = 0; i < streams.length; i++) {
			streams[i].flush();
		}
	}

}
