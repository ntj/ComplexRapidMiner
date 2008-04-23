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
package com.rapidminer.gui.attributeeditor;

import java.awt.BorderLayout;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;

import javax.swing.Action;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JToolBar;

import com.rapidminer.gui.attributeeditor.actions.ClearAction;
import com.rapidminer.gui.attributeeditor.actions.CloseAction;
import com.rapidminer.gui.attributeeditor.actions.LoadDataAction;
import com.rapidminer.gui.attributeeditor.actions.LoadSeriesDataAction;
import com.rapidminer.gui.attributeeditor.actions.OpenAttributeFileAction;
import com.rapidminer.gui.attributeeditor.actions.SaveAttributeFileAction;
import com.rapidminer.gui.attributeeditor.actions.SaveDataAction;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedToolBar;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.operator.Operator;
import com.rapidminer.tools.Tools;


/**
 * The dialog for the attribute editor. This dialog is used to display the data,
 * load data, and create attribute description files. Some actions are provided
 * for these purposes.
 * 
 * @see com.rapidminer.gui.attributeeditor.AttributeEditor
 * @author Ingo Mierswa, Simon Fischer
 * @version $Id: AttributeEditorDialog.java,v 2.14 2006/03/21 15:35:40
 *          ingomierswa Exp $
 */
public class AttributeEditorDialog extends JDialog implements WindowListener {

	private static final long serialVersionUID = 6448298163392765295L;

	private AttributeEditor attributeEditor;

	public Action OPEN_ATTRIBUTE_FILE_ACTION_24;

	public Action OPEN_ATTRIBUTE_FILE_ACTION_32;

	public Action SAVE_ATTRIBUTE_FILE_ACTION_24;

	public Action SAVE_ATTRIBUTE_FILE_ACTION_32;

	public Action LOAD_DATA_ACTION_24;

	public Action LOAD_DATA_ACTION_32;

	public Action LOAD_SERIES_DATA_ACTION_24;

	public Action LOAD_SERIES_DATA_ACTION_32;

	public Action SAVE_DATA_ACTION_24;

	public Action SAVE_DATA_ACTION_32;

	public Action CLEAR_ACTION_24;

	public Action CLEAR_ACTION_32;

	public Action CLOSE_ACTION_24;

	public Action CLOSE_ACTION_32;
    
    
	public AttributeEditorDialog(JFrame owner, Operator exampleSource, File file) {
		this(owner, exampleSource);
		if (file != null)
			attributeEditor.openAttributeFile(file);
	}

	public AttributeEditorDialog(JFrame owner, Operator exampleSource) {
		super(owner, "Attribute Editor", true);
        
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(this);
        
		DataControl control = new DataControl(0, 0, "Example", "Attribute", false);
		attributeEditor = new AttributeEditor(exampleSource, control);
		control.addViewChangeListener(attributeEditor);
		getContentPane().add(control, BorderLayout.WEST);
		getContentPane().add(new ExtendedJScrollPane(attributeEditor), BorderLayout.CENTER);
		control.update();

		// initialize actions
		initActions();
		
		// menu bar
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File");
		fileMenu.add(OPEN_ATTRIBUTE_FILE_ACTION_24);
		fileMenu.add(SAVE_ATTRIBUTE_FILE_ACTION_24);
		fileMenu.add(LOAD_DATA_ACTION_24);
		fileMenu.add(SAVE_DATA_ACTION_24);
        fileMenu.add(LOAD_SERIES_DATA_ACTION_24);
        fileMenu.addSeparator();
        fileMenu.add(CLOSE_ACTION_24);
		menuBar.add(fileMenu);

		JMenu tableMenu = new JMenu("Table");
		tableMenu.add(attributeEditor.GUESS_TYPE_ACTION_24);
        tableMenu.add(attributeEditor.GUESS_ALL_TYPES_ACTION_24);
		tableMenu.add(attributeEditor.REMOVE_COLUMN_ACTION_24);
		tableMenu.add(attributeEditor.REMOVE_ROW_ACTION_24);
        tableMenu.add(attributeEditor.USE_ROW_AS_NAMES_ACTION_24);
		tableMenu.add(CLEAR_ACTION_24);
		menuBar.add(tableMenu);

		setJMenuBar(menuBar);

		// tool bar
		JToolBar toolBar = new ExtendedToolBar();
		toolBar.add(OPEN_ATTRIBUTE_FILE_ACTION_32);
		toolBar.add(SAVE_ATTRIBUTE_FILE_ACTION_32);
		toolBar.add(LOAD_DATA_ACTION_32);
		toolBar.add(SAVE_DATA_ACTION_32);
		toolBar.addSeparator();
		toolBar.add(CLEAR_ACTION_32);
		getContentPane().add(toolBar, BorderLayout.NORTH);

		setSize((int) Math.max(600, owner.getWidth() * 2.0d / 3.0d), (int) Math.max(400, owner.getHeight() * 2.0d / 3.0d));

		setLocationRelativeTo(owner);
	}

	public void initActions() {
		this.OPEN_ATTRIBUTE_FILE_ACTION_24 = new OpenAttributeFileAction(attributeEditor, IconSize.SMALL);
		this.OPEN_ATTRIBUTE_FILE_ACTION_32 = new OpenAttributeFileAction(attributeEditor, IconSize.MIDDLE);

		this.SAVE_ATTRIBUTE_FILE_ACTION_24 = new SaveAttributeFileAction(attributeEditor, IconSize.SMALL);
		this.SAVE_ATTRIBUTE_FILE_ACTION_32 = new SaveAttributeFileAction(attributeEditor, IconSize.MIDDLE);

		this.LOAD_DATA_ACTION_24 = new LoadDataAction(attributeEditor, IconSize.SMALL);
		this.LOAD_DATA_ACTION_32 = new LoadDataAction(attributeEditor, IconSize.MIDDLE);

		this.LOAD_SERIES_DATA_ACTION_24 = new LoadSeriesDataAction(attributeEditor, IconSize.SMALL);
		this.LOAD_SERIES_DATA_ACTION_32 = new LoadSeriesDataAction(attributeEditor, IconSize.MIDDLE);

		this.SAVE_DATA_ACTION_24 = new SaveDataAction(attributeEditor, IconSize.SMALL);
		this.SAVE_DATA_ACTION_32 = new SaveDataAction(attributeEditor, IconSize.MIDDLE);

		this.CLEAR_ACTION_24 = new ClearAction(attributeEditor, IconSize.SMALL);
		this.CLEAR_ACTION_32 = new ClearAction(attributeEditor, IconSize.MIDDLE);

		this.CLOSE_ACTION_24 = new CloseAction(this, IconSize.SMALL);
		this.CLOSE_ACTION_32 = new CloseAction(this, IconSize.MIDDLE);	
	}
	
	public File getFile() {
		return attributeEditor.getFile();
	}

    public void windowActivated(WindowEvent e) {}
    public void windowClosed(WindowEvent e) {}
    public void windowDeactivated(WindowEvent e) {}
    public void windowDeiconified(WindowEvent e) {}
    public void windowIconified(WindowEvent e) {}
    public void windowOpened(WindowEvent e) {}
    
    public void windowClosing(WindowEvent e) {
        close();
    }
    
    public void close() {
        if (attributeEditor.hasDataChanged()) {
            int selectedOption = JOptionPane.showConfirmDialog(this, "It seems that you have changed the data without saving it afterwards." + Tools.getLineSeparator() + "Do you still want to proceed and close the editor (changes will be lost)?", "Save data file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selectedOption == JOptionPane.YES_OPTION)
                dispose();
        } else if (attributeEditor.hasMetaDataChanged()) {
            int selectedOption = JOptionPane.showConfirmDialog(this, "It seems that you have changed the attribute descriptions without saving an attribute description file (.aml) afterwards." + Tools.getLineSeparator() + "Do you still want to proceed and close the editor (changes will be lost)?", "Save attribute description file?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (selectedOption == JOptionPane.YES_OPTION)
                dispose();
        }  else {
            dispose();
        }
    }
}
