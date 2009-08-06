package de.tud.inf.gui.properties;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListModel;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJList;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedListModel;
import com.rapidminer.gui.tools.SwingTools;

public class FilesPropertyDialog extends JDialog{
	
	
	private ExtendedJList files;
	private Set<File> fileSet;
	private ExtendedListModel model;
	
	private boolean ok;
	
	public FilesPropertyDialog(Set<File> fileSet) {
		super(RapidMinerGUI.getMainFrame(),"Files",true);
		ok = false;
		this.fileSet = fileSet;
		model = new ExtendedListModel();
		files = new ExtendedJList(model);
		initializeModel();
		
		getContentPane().setLayout(new BorderLayout());

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

		JButton addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
//				listPropertyTable.addRow();
				add();
			}
		});
		buttonPanel.add(addButton);

		JButton removeButton = new JButton("Remove");
		removeButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
//				listPropertyTable.removeSelected();
				remove();
			}
		});
		buttonPanel.add(removeButton);

		JButton okButton = new JButton("Ok");
		okButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
//				ok();
				ok = true;
				dispose();
			}
		});
		buttonPanel.add(okButton);

		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JScrollPane scrollpane = new ExtendedJScrollPane(files);
		getContentPane().add(scrollpane, BorderLayout.CENTER);
        
		setSize(RapidMinerGUI.getMainFrame().getWidth() / 2, RapidMinerGUI.getMainFrame().getHeight() / 2);
        setLocationRelativeTo(RapidMinerGUI.getMainFrame());
	}
	
	private void initializeModel() {
		
		for(File f : fileSet)
			model.addElement(f);
	}

	private void add() {
		
		JFileChooser chooser = SwingTools.createFileChooser(null, false, null);
		chooser.setMultiSelectionEnabled(true);
		if(chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			
			// add Files to the model
			ExtendedListModel mod = (ExtendedListModel)files.getModel();
			for(File file : chooser.getSelectedFiles()) {
				if(!fileSet.contains(file)) {
					mod.addElement(file);
					fileSet.add(file);
				}
				
			}
			
		}
	}
	
	private void remove() {
		ExtendedListModel mod = (ExtendedListModel)files.getModel();
		for(Object o : files.getSelectedValues()) {
			fileSet.remove(o);
			mod.removeElement(o);
		}
	}
	
	public boolean isOk() {
		return ok;
	}

}
