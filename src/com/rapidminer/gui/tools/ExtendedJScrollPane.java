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
package com.rapidminer.gui.tools;

import java.awt.Component;

import javax.swing.JScrollPane;

/**
 * This extended version of the JScrollPane uses increased numbers of unit increments
 * for both scroll bars making it more useful for mouse wheels.
 * 
 * @author Ingo Mierswa
 * @version $Id: ExtendedJScrollPane.java,v 1.1 2007/05/27 21:59:31 ingomierswa Exp $
 */
public class ExtendedJScrollPane extends JScrollPane {

    private static final long serialVersionUID = 218317624316997140L;

    public ExtendedJScrollPane(Component component) {
        super(component);
        getHorizontalScrollBar().setUnitIncrement(10);
        getVerticalScrollBar().setUnitIncrement(10);
    }
}
