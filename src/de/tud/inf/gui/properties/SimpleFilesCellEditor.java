package de.tud.inf.gui.properties;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.properties.PropertyValueCellEditor;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.Operator;
import com.rapidminer.parameter.ParameterTypeDirectory;
import com.rapidminer.parameter.ParameterTypeFile;

import de.tud.inf.parameters.ParameterTypeFiles;

public class SimpleFilesCellEditor extends AbstractCellEditor implements PropertyValueCellEditor{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5395497787414404444L;
	
	
	private ParameterTypeFile type;
	
	private JButton button;
	
	private Set<File> fileSet = new HashSet<File>(); 
	
	public SimpleFilesCellEditor(ParameterTypeFiles type) {
		this.type = type;
		button = new JButton("Select files...");
		button.setMargin(new Insets(0, 0, 0, 0));
		button.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				buttonPressed();
			}
		});
		
	}
	
	private void buttonPressed() {
//		String value = (String) getCellEditorValue();
//		File file = (value == null || value.length() == 0) ? null : RapidMinerGUI.getMainFrame().getProcess().resolveFileName(value);
//		File selectedFile = SwingTools.chooseFile(RapidMinerGUI.getMainFrame(), file, true, type instanceof ParameterTypeDirectory, type.getExtension(), type.getKey());
//		if ((selectedFile != null)) {
//			setText(selectedFile);
//			fireEditingStopped();
//		} else {
//			fireEditingCanceled();
//		}
		FilesPropertyDialog diag = new FilesPropertyDialog(fileSet);
		diag.setVisible(true);
		if(diag.isOk())
			fireEditingStopped();
		else
			fireEditingCanceled();
		
	}

	@Override
	public Object getCellEditorValue() {
		
		StringBuffer fileList = new StringBuffer();
		for(File f : fileSet)
			fileList.append(f.getPath() + ";");
		return fileList.toString();
	}

	@Override
	public void setOperator(Operator operator) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean useEditorAsRenderer() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value,
			boolean isSelected, int row, int column) {
		String[] files = null;
		Set<File> newFileSet;
		if(value instanceof String) {
			files = ((String)value).split(";");
			newFileSet = new HashSet<File>();
			for(String path : files) {
				if(path.length() > 0)
					newFileSet.add(new File(path));
			}
		} else {
			newFileSet = (Set<File>)value;
			
		}
		
		fileSet = newFileSet;
		return button;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		return getTableCellEditorComponent(table, value, isSelected, row, column);
	}

}
