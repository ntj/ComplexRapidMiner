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
package com.rapidminer.doc;

import java.io.PrintWriter;

import com.rapidminer.operator.Operator;
import com.sun.javadoc.RootDoc;


/**
 * Generates the documentation for operators.
 * 
 * @author Simon Fischer
 * @version $Id: OperatorDocGenerator.java,v 1.1 2007/05/27 21:59:10 ingomierswa Exp $
 */
public interface OperatorDocGenerator {

	/** Generates the documentation for this operator and writes it to the given writer. */
	public void generateDoc(Operator operator, RootDoc rootDoc, PrintWriter out);

	/** Generates the header for the group with the given name. The name might be null. */
	public void beginGroup(String groupName, PrintWriter out);

	/** Generates the footer for the group with the given name. The name might be null. */
	public void endGroup(String groupName, PrintWriter out);
}
