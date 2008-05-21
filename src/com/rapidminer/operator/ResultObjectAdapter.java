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
package com.rapidminer.operator;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.LoggingHandler;
import com.rapidminer.tools.Tools;


/**
 * An adapter class for the interface {@link ResultObject}. Implements most
 * methods and can be used if the subclass does not need to extend other
 * classes. The method {@link #toResultString()} delivers the return value of
 * {@link #toString()}. The visualization components for the graphical user
 * interface is simply the HTML representation of the result string. If a
 * subclass also implements {@link Saveable} an action for Saving will
 * automatically be added to the actions list.
 * 
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: ResultObjectAdapter.java,v 2.15 2006/03/27 13:21:58 ingomierswa
 *          Exp $
 */
public abstract class ResultObjectAdapter extends AbstractIOObject implements ResultObject, LoggingHandler, Saveable {

	private List<Action> actions;

	public ResultObjectAdapter() {
		initActions();
	}
	
	private void initActions() {
		this.actions = new LinkedList<Action>();
        if (isSavable()) {
            addAction(new AbstractAction("Save...") {

                private static final long serialVersionUID = -5888181920926434464L;
                {
                    putValue(SHORT_DESCRIPTION, "Save this " + Tools.classNameWOPackage(ResultObjectAdapter.this.getClass()) + " to disk.");
                }

                public void actionPerformed(ActionEvent e) {
                    File file = SwingTools.chooseFile(null, null, false, getExtension(), getFileDescription());
                    try {
                        if (file != null) {
                            ((Saveable) ResultObjectAdapter.this).save(file);
                        }
                    } catch (Exception ex) {
                        SwingTools.showSimpleErrorMessage("Cannot write to file '" + file + "'", ex);
                    }
                }
            });
        }
	}

	/** Used for deserialization of the transient actions list. */
	protected Object readResolve() {
		initActions();
		return this;
	}
	
	/** The default implementation returns the classname without package. */
	public String getName() {
		return Tools.classNameWOPackage(this.getClass());
	}

    /** Returns true. */
    public boolean isSavable() {
        return true;
    }
    
    /** Saves the object into the given file by using the {@link #write(OutputStream)} 
     *  method of {@link IOObject} (XML format). */
    public void save(final File file) throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            write(fos);
        } finally {
            if (fos != null)
                fos.close();
        }
    }
    
	/**
	 * Adds an action to the list of Java Swing Actions which will shown in the
	 * visualization component. If the class implements Saveable an action for
	 * saving is already added.
	 */
	protected void addAction(Action a) {
		actions.add(a);
	}

	/**
	 * Returns a list of all actions which can be performed for this result
	 * object.
	 */
	public List<Action> getActions() {
		return actions;
	}

	/**
	 * The default implementation simply returns the result of the method
	 * {@link #toString()}.
	 */
	public String toResultString() {
		return toString();
	}

	/**
	 * Returns a editor pane that displays the {@link #toResultString()} result
	 * encoded as html. Please note that the returned pane is already enclosed
     * by a scroll pane. If you overwrite this method you should again ensure that
     * the returned component is scrollable. The given container is totally ignored.
	 */
	public Component getVisualizationComponent(IOContainer container) {
		String str = toHTML(toResultString());
		JEditorPane resultText = new JEditorPane();
        resultText.setContentType("text/html");
        resultText.setText("<html><h1>" + getName() + "</h1><pre>" + str + "</pre></html>");
		resultText.setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 11, 11, 11));
        resultText.setEditable(false);
        resultText.setBackground((new JLabel()).getBackground());
        return new ExtendedJScrollPane(resultText);
	}

	/** Returns null. Subclasses might want to override this method and returns an appropriate
	 *  icon. */
	public Icon getResultIcon() {
		return null;
	}
	
	/**
	 * Encodes the given String as HTML. Only linebreaks and less then and
	 * greater than will be encoded.
	 */
	public static String toHTML(String string) {
		String str = string;
		str = str.replaceAll(">", "&gt;");
		str = str.replaceAll("<", "&lt;");
		str = str.replaceAll(Tools.getLineSeparator(), "<br>");
		return str;
	}
    
    /** Logs a status message with the correct log service. */
    public void log(String message) {
        getLog().log(getName() + ": " + message);
    }

    /** Logs a note message with the correct log service. */
    public void logNote(String message) {
        getLog().logNote(getName() + ": " + message);
    }
    
    /** Logs a warning message with the correct log service. */
    public void logWarning(String message) {
        getLog().logWarning(getName() + ": " + message);
    }
    
    /** Logs an error message with the correct log service. */
    public void logError(String message) {
        getLog().logError(getName() + ": " + message);
    }
}
