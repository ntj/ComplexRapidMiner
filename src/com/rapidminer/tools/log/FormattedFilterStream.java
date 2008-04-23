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
package com.rapidminer.tools.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Filters according to the given filter before the characters are given to the parent stream.
 * 
 * @author Ingo Mierswa
 * @version $Id: FormattedFilterStream.java,v 1.1 2007/05/27 21:58:59 ingomierswa Exp $
 */
public class FormattedFilterStream extends OutputStream {

    private OutputStream out;
    
    private LogFormatFilter filter;
    
    private int lastChar = -1;
    
    public FormattedFilterStream(OutputStream out, LogFormatFilter filter) {
        this.out = out;
        this.filter = filter;
    }

    public void write(int b) throws IOException {
        if (filter.accept(lastChar, b))
            out.write(b);     
        lastChar = b;
    }
    
    public void close() throws IOException {
		if ((!System.out.equals(out)) && (!System.err.equals(out)))
			out.close();
    }
    
    public void flush() throws IOException {
        out.flush();
    }
}
