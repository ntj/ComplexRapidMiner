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
package com.rapidminer.tools.log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Filters according to the given filter before the characters are given to the parent stream.
 * 
 * @author Ingo Mierswa
 * @version $Id: FormattedFilterStream.java,v 1.3 2008/05/09 19:23:25 ingomierswa Exp $
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
