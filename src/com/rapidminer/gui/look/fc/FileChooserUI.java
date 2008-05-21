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
package com.rapidminer.gui.look.fc;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.ActionMapUIResource;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicFileChooserUI;

import sun.awt.shell.ShellFolder;

import com.rapidminer.gui.look.borders.Borders;

/**
 * The UI for the extended file chooser.
 *
 * @author Ingo Mierswa
 * @version $Id: FileChooserUI.java,v 1.5 2008/05/09 20:57:26 ingomierswa Exp $
 */
public class FileChooserUI extends BasicFileChooserUI {
	
	public static final String FILECHOOSER_VIEW_TYPE = "FILECHOOSER_VIEW_TYPE";

	public static final String FILECHOOSER_VIEW_THUMBNAIL = "Thumbnails";

	public static final String FILECHOOSER_VIEW_ICON = "Icons";

	public static final String FILECHOOSER_VIEW_LIST = "List";

	public static final String FILECHOOSER_VIEW_DETAILS = "Details";
	
	
	private static class ButtonAreaLayout implements LayoutManager {
		
		private int horizontalGap = 5;

		private int topMargin = 17;

		public void addLayoutComponent(String string, Component comp) {}

		public void layoutContainer(Container container) {
			Component[] children = container.getComponents();

			if ((children != null) && (children.length > 0)) {
				int numChildren = children.length;
				Dimension[] sizes = new Dimension[numChildren];
				Insets insets = container.getInsets();
				int yLocation = insets.top + this.topMargin;
				int maxWidth = 0;

				for (int counter = 0; counter < numChildren; counter++) {
					sizes[counter] = children[counter].getPreferredSize();
					maxWidth = Math.max(maxWidth, sizes[counter].width);
				}
				int xLocation, xOffset;
				if (container.getComponentOrientation().isLeftToRight()) {
					xLocation = container.getSize().width - insets.left - maxWidth;
					xOffset = this.horizontalGap + maxWidth;
				} else {
					xLocation = insets.left;
					xOffset = -(this.horizontalGap + maxWidth);
				}
				for (int counter = numChildren - 1; counter >= 0; counter--) {
					children[counter].setBounds(xLocation, yLocation, maxWidth, sizes[counter].height);
					xLocation -= xOffset;
				}
			}
		}

		public Dimension minimumLayoutSize(Container c) {
			if (c != null) {
				Component[] children = c.getComponents();

				if ((children != null) && (children.length > 0)) {
					int numChildren = children.length;
					int height = 0;
					Insets cInsets = c.getInsets();
					int extraHeight = this.topMargin + cInsets.top + cInsets.bottom;
					int extraWidth = cInsets.left + cInsets.right;
					int maxWidth = 0;

					for (int counter = 0; counter < numChildren; counter++) {
						Dimension aSize = children[counter].getPreferredSize();
						height = Math.max(height, aSize.height);
						maxWidth = Math.max(maxWidth, aSize.width);
					}
					return new Dimension(extraWidth + numChildren * maxWidth + (numChildren - 1) * this.horizontalGap, extraHeight + height);
				}
			}
			return new Dimension(0, 0);
		}

		public Dimension preferredLayoutSize(Container c) {
			return minimumLayoutSize(c);
		}

		public void removeLayoutComponent(Component c) {}
	}

	private class AlignedLabel extends JLabel {
		
		private static final long serialVersionUID = 4912090609095372381L;

		private AlignedLabel[] group;

		private int maxWidth = 0;

		AlignedLabel(String text) {
			super(text);
			setAlignmentX(Component.LEFT_ALIGNMENT);
		}

		@Override
		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			// Align the width with all other labels in group.
			return new Dimension(getMaxWidth(), d.height);
		}

		private int getMaxWidth() {
			if ((this.maxWidth == 0) && (this.group != null)) {
				int max = 0;
				for (AlignedLabel element : this.group) {
					max = Math.max(element.getSuperPreferredWidth(), max);
				}
				for (AlignedLabel element : this.group) {
					element.maxWidth = max;
				}
			}
			return this.maxWidth;
		}

		private int getSuperPreferredWidth() {
			if (getText() == null) {
				return super.getPreferredSize().width;
			} else {
				return super.getPreferredSize().width + 11;				
			}
		}
	}
	
	private class DirectoryComboBoxAction extends AbstractAction {
		
		private static final long serialVersionUID = -6851838331146924117L;

		protected DirectoryComboBoxAction() {
			super("DirectoryComboBoxAction");
		}

		public void actionPerformed(ActionEvent e) {
			File f = (File) FileChooserUI.this.directoryComboBox.getSelectedItem();
			setCurrentDirectoryOfFileChooser(f);
			FileChooserUI.this.fileNameTextField.requestFocus();
		}
	}
	
	private class FilterComboBoxModel extends AbstractListModel implements ComboBoxModel, PropertyChangeListener {

		private static final long serialVersionUID = -7578988904254755349L;
		
		private FileFilter[] filters;

		protected FilterComboBoxModel() {
			super();
			this.filters = getFileChooser().getChoosableFileFilters();
		}

		public void propertyChange(PropertyChangeEvent e) {
			String prop = e.getPropertyName();
			if (prop == JFileChooser.CHOOSABLE_FILE_FILTER_CHANGED_PROPERTY) {
				this.filters = (FileFilter[]) e.getNewValue();
				fireContentsChanged(this, -1, -1);
			} else if (prop == JFileChooser.FILE_FILTER_CHANGED_PROPERTY) {
				fireContentsChanged(this, -1, -1);
			}
		}

		public void setSelectedItem(Object filter) {
			if (filter != null) {
				getFileChooser().setFileFilter((FileFilter) filter);
				setFileName(null);
				fireContentsChanged(this, -1, -1);
			}
		}

		public Object getSelectedItem() {
			FileFilter currentFilter = getFileChooser().getFileFilter();
			boolean found = false;
			if (currentFilter != null) {
				for (FileFilter element : this.filters) {
					if (element == currentFilter) {
						found = true;
					}
				}
				if (found == false) {
					getFileChooser().addChoosableFileFilter(currentFilter);
				}
			}
			return getFileChooser().getFileFilter();
		}

		public int getSize() {
			if (this.filters != null) {
				return this.filters.length;
			} else {
				return 0;
			}
		}

		public Object getElementAt(int index) {
			if (index > getSize() - 1) {
				// This shouldn't happen. Try to recover gracefully.
				return getFileChooser().getFileFilter();
			}
			if (this.filters != null) {
				return this.filters[index];
			} else {
				return null;
			}
		}
	}
	
	private class FilterComboBoxRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = 7024419790190737084L;
		
		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			if ((value != null) && (value instanceof FileFilter)) {
				setText(((FileFilter) value).getDescription());
			}
			if (isSelected && (index > -1)) {
				setBorder(FileChooserUI.this.roundComboboxListRendererBorder);
			}
			return this;
		}
	}
	
	private class DirectoryComboBoxModel extends AbstractListModel implements ComboBoxModel {
		
		private static final long serialVersionUID = -7566898679781533334L;

		private Vector<File> directories = new Vector<File>();

		private int[] depths = null;

		private File selectedDirectory = null;

		private JFileChooser chooser = getFileChooser();

		private FileSystemView fileSystemView = this.chooser.getFileSystemView();

		public DirectoryComboBoxModel() {
			File dir = getFileChooser().getCurrentDirectory();
			if (dir != null) {
				addItem(dir);
			}
		}

		private void addItem(File directory) {
			if (directory == null) {
				return;
			}

			this.directories.clear();

			File[] baseFolders;
			if (FileChooserUI.this.useShellFolder) {
				baseFolders = (File[]) ShellFolder.get("fileChooserComboBoxFolders");
			} else {
				baseFolders = this.fileSystemView.getRoots();
			}
			this.directories.addAll(Arrays.asList(baseFolders));

			File canonical = null;
			try {
				canonical = directory.getCanonicalFile();
			} catch (IOException e) {
				canonical = directory;
			}

			try {
				File sf = ShellFolder.getShellFolder(canonical);
				File f = sf;
				Vector<File> path = new Vector<File>(10);
				do {
					path.addElement(f);
				} while ((f = f.getParentFile()) != null);

				int pathCount = path.size();
				for (int i = 0; i < pathCount; i++) {
					f = path.get(i);
					if (this.directories.contains(f)) {
						int topIndex = this.directories.indexOf(f);
						for (int j = i - 1; j >= 0; j--) {
							this.directories.insertElementAt(path.get(j), topIndex + i - j);
						}
						break;
					}
				}
				calculateDepths();
				setSelectedItem(sf);
			} catch (FileNotFoundException ex) {
				calculateDepths();
			}
		}

		private void calculateDepths() {
			this.depths = new int[this.directories.size()];
			for (int i = 0; i < this.depths.length; i++) {
				File dir = this.directories.get(i);
				File parent = dir.getParentFile();
				this.depths[i] = 0;
				if (parent != null) {
					for (int j = i - 1; j >= 0; j--) {
						if (parent.equals(this.directories.get(j))) {
							this.depths[i] = this.depths[j] + 1;
							break;
						}
					}
				}
			}
		}

		public int getDepth(int i) {
			return ((this.depths != null) && (i >= 0) && (i < this.depths.length)) ? this.depths[i] : 0;
		}

		public void setSelectedItem(Object selectedDirectory) {
			this.selectedDirectory = (File) selectedDirectory;
			fireContentsChanged(this, -1, -1);
		}

		public Object getSelectedItem() {
			return this.selectedDirectory;
		}

		public int getSize() {
			return this.directories.size();
		}

		public Object getElementAt(int index) {
			return this.directories.elementAt(index);
		}
	}
	
	private static class IndentIcon implements Icon {

		private Icon icon;

		private int depth = 0;

		public void paintIcon(Component c, Graphics g, int x, int y) {
			if (icon != null) {
				if (c.getComponentOrientation().isLeftToRight()) {
					this.icon.paintIcon(c, g, x + this.depth * INDENT_SPACE, y);
				} else {
					this.icon.paintIcon(c, g, x, y);
				}
			} 
		}

		public int getIconWidth() {
			if (icon == null) {
				return depth * INDENT_SPACE;
			} else {
				return this.icon.getIconWidth() + this.depth * INDENT_SPACE;
			}
		}

		public int getIconHeight() {
			if (icon == null) {
				return 0;
			} else {
				return this.icon.getIconHeight();
			}
		}
	}
	
	private class DirectoryComboBoxRenderer extends DefaultListCellRenderer {
		
		private static final long serialVersionUID = 4597909127976297943L;
		
		private IndentIcon indentIcon = new IndentIcon();

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (value == null) {
				setText("");
				return this;
			}
			File directory = (File) value;
			setText(getFileChooser().getName(directory));
			Icon icon = getFileChooser().getIcon(directory);
			if (icon == null) {
				icon = UIManager.getIcon("FileChooser.defaultDirectoryIcon");
			}

			this.indentIcon.icon = icon;
			this.indentIcon.depth = FileChooserUI.this.directoryComboBoxModel.getDepth(index);
			setIcon(this.indentIcon);

			if (isSelected && (index > -1)) {
				setBorder(FileChooserUI.this.roundComboboxListRendererBorder);
			}

			return this;
		}
	}
	
	private class RapidLookFileView extends BasicFileView {
		@Override
		public Icon getIcon(File f) {
			Icon icon = getCachedIcon(f);
			if (icon != null) {
				return icon;
			}
			if (f != null) {
				icon = getFileChooser().getFileSystemView().getSystemIcon(f);
			}
			if (icon == null) {
				icon = super.getIcon(f);
			}
			cacheIcon(f, icon);
			return icon;
		}
	}

	private class BookmarkAction extends AbstractAction {
		
		private static final long serialVersionUID = -654304868192207741L;

		public void actionPerformed(ActionEvent e) {
			FileChooserUI.this.fileList.addToBookmarks();
		}
	}
	
	private class GoBackAction extends AbstractAction {
		
		private static final long serialVersionUID = 5132122622014626886L;

		protected GoBackAction() {
			super("Go Back");
		}

		public void actionPerformed(ActionEvent e) {
			goBack();
		}
	}
	
	private class CancelSelectionAction extends AbstractAction {
		
		private static final long serialVersionUID = 2080395201063859907L;

		public void actionPerformed(ActionEvent e) {
			FileChooserUI.this.fileList.stopTumbnailGeneration();
			getFileChooser().cancelSelection();
		}
	}
	
	private class ExtendedApproveSelectionAction extends ApproveSelectionAction {
		
		private static final long serialVersionUID = 4061933557078579689L;

		@Override
		public void actionPerformed(ActionEvent e) {
			FileChooserUI.this.fileList.stopTumbnailGeneration();
			super.actionPerformed(e);
		}
	}
	
	private class ChangeToParentDirectoryAction extends AbstractAction {
		
		private static final long serialVersionUID = -3805300411336163058L;

		protected ChangeToParentDirectoryAction() {
			super("Go Up");
		}

		public void actionPerformed(ActionEvent e) {
			getFileChooser().changeToParentDirectory();
		}
	}
	
	private class changeViewActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			updateView(e.getActionCommand());
		}
	}
	
	private class ChangeViewAction extends AbstractAction {
		
		private static final long serialVersionUID = 6720057807081456009L;

		public void actionPerformed(ActionEvent e) {
			FileChooserUI.this.changeViewPopup.show((JToggleButton) e.getSource(), 0, ((JToggleButton) e.getSource()).getHeight() - 3);
		}
	}
	
	
	
	private static Border buttonsEmptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	
	private static File userHomeDirectory;

	
	private final static int INDENT_SPACE = 10;
	
	
	private final static Dimension HORIZONTAL_STRUT_5 = new Dimension(5, 5);

	private final static Dimension HORIZONTAL_STRUT_15 = new Dimension(15, 15);

	private final static Dimension VERTICAL_STRUT_5 = new Dimension(1, 5);

	private final static Insets NO_MARGIN = new Insets(0, 0, 0, 0);
	
	
	private ButtonGroup changeViewButtonGroup;

	public String viewType = FILECHOOSER_VIEW_DETAILS;

	private Action goToParentDirectoryAction = new ChangeToParentDirectoryAction();

	private Action goBackAction = new GoBackAction();

	protected Vector<String> backPathVector = new Vector<String>();

	protected JButton bookmarksButton;

	private JPopupMenu changeViewPopup;

	private Icon changeViewNormal = null;

	private Icon changeViewHighlighted = null;

	private Icon bookmarksAddNormal = null;

	private Icon bookmarksAddHighlighted = null;

	private Icon homeNormal = null;

	private Icon homeHighlighted = null;

	private Icon homeDisabled = null;

	private Icon newFolderNormal = null;

	private Icon newFolderHighlighted = null;

	private Icon newFolderDisabled = null;

	private Icon upFolderNormal = null;

	private Icon upFolderHighlighted = null;

	private Icon upFolderDisabled = null;

	private Icon backNormal = null;

	private Icon backHighlighted = null;

	private Icon backDisabled = null;

	private final Border roundComboboxListRendererBorder = Borders.getComboBoxListCellRendererFocusBorder();

	private JToggleButton changeViewButton;

	protected FileList fileList;

	private JLabel lookInLabel;

	private JComboBox directoryComboBox;

	private DirectoryComboBoxModel directoryComboBoxModel;

	private Action directoryComboBoxAction = new DirectoryComboBoxAction();

	private FilterComboBoxModel filterComboBoxModel;

	private JTextField fileNameTextField;

	private boolean useShellFolder;

	private JButton approveButton;

	private JButton cancelButton;

	private JPanel buttonPanel;

	private JPanel bottomPanel;

	private JComboBox filterComboBox;

	private int lookInLabelMnemonic = 0;

	private String lookInLabelText = null;

	private String saveInLabelText = null;

	private int fileNameLabelMnemonic = 0;

	private String fileNameLabelText = null;

	private int filesOfTypeLabelMnemonic = 0;

	private String filesOfTypeLabelText = null;

	private String upFolderToolTipText = null;

	private String upFolderAccessibleName = null;

	private String homeFolderToolTipText = null;

	private String homeFolderAccessibleName = null;

	private String newFolderToolTipText = null;

	private String newFolderAccessibleName = null;

	private String backButtonToolTipText = "Go to Previous Folder Visited";

	private String backButtonAccessibleName = " ";

	private BasicFileView fileView = new RapidLookFileView();

	private Action cancelSelectionAction = new CancelSelectionAction();

	private Action approveSelectionAction = new ExtendedApproveSelectionAction();
	
	
	
	protected ActionMap createActions() {
		final AbstractAction escAction = new AbstractAction() {
			
			private static final long serialVersionUID = -3976059968191425942L;

			public void actionPerformed(ActionEvent e) {
				FileChooserUI.this.fileList.stopTumbnailGeneration();
				getFileChooser().cancelSelection();
			}

			@Override
			public boolean isEnabled() {
				return getFileChooser().isEnabled();
			}
		};
		final ActionMap map = new ActionMapUIResource();
		map.put("approveSelection", getApproveSelectionAction());
		map.put("cancelSelection", escAction);
		return map;
	}
	
	@Override
	public Action getCancelSelectionAction() {
		return this.cancelSelectionAction;
	}

	@Override
	public Action getApproveSelectionAction() {
		return this.approveSelectionAction;
	}

	public String getView() {
		return this.viewType;
	}

	public static ComponentUI createUI(JComponent c) {
		return new FileChooserUI((JFileChooser) c);
	}

	public FileChooserUI(JFileChooser filechooser) {
		super(filechooser);
	}

	@Override
	public void installUI(JComponent c) {
		super.installUI(c);
	}

	@Override
	public void uninstallComponents(JFileChooser fc) {
		fc.removeAll();
		this.bottomPanel = null;
		this.buttonPanel = null;
		this.fileList = null;
		super.uninstallComponents(fc);
	}

	@Override
	public void installIcons(JFileChooser fc) {
		super.installIcons(fc);

		this.changeViewNormal = UIManager.getIcon("FileChooser.ChangeViewNormalIcon");
		this.changeViewHighlighted = UIManager.getIcon("FileChooser.ChangeViewHighlightedIcon");
		this.bookmarksAddNormal = UIManager.getIcon("FileChooser.BookmarksAddNormalIcon");
		this.bookmarksAddHighlighted = UIManager.getIcon("FileChooser.BookmarksAddHighlightedIcon");
		this.homeNormal = UIManager.getIcon("FileChooser.HomeNormalIcon");
		this.homeHighlighted = UIManager.getIcon("FileChooser.HomeHighlightedIcon");
		this.homeDisabled = UIManager.getIcon("FileChooser.HomeDisabledIcon");
		this.newFolderNormal = UIManager.getIcon("FileChooser.NewFolderNormalIcon");
		this.newFolderHighlighted = UIManager.getIcon("FileChooser.NewFolderHighlightedIcon");
		this.newFolderDisabled = UIManager.getIcon("FileChooser.NewFolderDisabledIcon");
		this.upFolderNormal = UIManager.getIcon("FileChooser.UpFolderNormalIcon");
		this.upFolderHighlighted = UIManager.getIcon("FileChooser.UpFolderHighlightedIcon");
		this.upFolderDisabled = UIManager.getIcon("FileChooser.UpFolderDisabledIcon");
		this.backNormal = UIManager.getIcon("FileChooser.BackFolderNormalIcon");
		this.backHighlighted = UIManager.getIcon("FileChooser.BackFolderHighlightedIcon");
		this.backDisabled = UIManager.getIcon("FileChooser.BackFolderDisabledIcon");
	}

	@Override
	protected void uninstallIcons(JFileChooser fc) {
		super.uninstallIcons(fc);
		this.changeViewNormal = null;
		this.changeViewHighlighted = null;
		this.bookmarksAddNormal = null;
		this.bookmarksAddHighlighted = null;
		this.homeNormal = null;
		this.homeHighlighted = null;
		this.homeDisabled = null;
		this.newFolderNormal = null;
		this.newFolderHighlighted = null;
		this.newFolderDisabled = null;
		this.upFolderNormal = null;
		this.upFolderHighlighted = null;
		this.upFolderDisabled = null;
		this.backNormal = null;
		this.backHighlighted = null;
		this.backDisabled = null;
	}

	@Override
	public void installComponents(JFileChooser fc) {
		FileSystemView fsv = fc.getFileSystemView();
		userHomeDirectory = fsv.getHomeDirectory();

		this.changeViewPopup = createViewPopupMenu();

		fc.setBorder(new EmptyBorder(12, 12, 11, 11));
		fc.setLayout(new BorderLayout(0, 11));

		// ********************************* //
		// **** Construct the top panel **** //
		// ********************************* //

		// Directory manipulation buttons
		JToolBar topPanel = new JToolBar();
		topPanel.setFloatable(false);
		topPanel.setBorder(null);
		topPanel.setOpaque(false);

		// Add the top panel to the fileChooser
		fc.add(topPanel, BorderLayout.NORTH);

		// ComboBox Label
		this.lookInLabel = new JLabel(this.lookInLabelText);
		this.lookInLabel.setDisplayedMnemonic(this.lookInLabelMnemonic);
		topPanel.add(this.lookInLabel, BorderLayout.BEFORE_LINE_BEGINS);

		// CurrentDir ComboBox
		this.directoryComboBox = new JComboBox();
		this.directoryComboBox.setOpaque(false);
		this.directoryComboBox.getAccessibleContext().setAccessibleDescription(this.lookInLabelText);
		this.directoryComboBox.putClientProperty("JComboBox.lightweightKeyboardNavigation", "Lightweight");
		this.lookInLabel.setLabelFor(this.directoryComboBox);
		this.directoryComboBoxModel = createDirectoryComboBoxModel(fc);
		this.directoryComboBox.setModel(this.directoryComboBoxModel);
		this.directoryComboBox.addActionListener(this.directoryComboBoxAction);
		this.directoryComboBox.setRenderer(createDirectoryComboBoxRenderer(fc));
		this.directoryComboBox.setMaximumRowCount(9);
		this.directoryComboBox.setMaximumSize(new Dimension((int) this.directoryComboBox.getMaximumSize().getWidth(), 27));
		topPanel.add(this.directoryComboBox, BorderLayout.CENTER);

		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_15));

		//back button
		JButton backButton = new JButton(getGoBackAction());
		backButton.setText(null);
		backButton.setRolloverEnabled(true);
		backButton.setIcon(this.backNormal);
		backButton.setPressedIcon(this.backHighlighted);
		backButton.setRolloverIcon(this.backHighlighted);
		backButton.setDisabledIcon(this.backDisabled);
		backButton.setToolTipText(this.backButtonToolTipText);
		backButton.getAccessibleContext().setAccessibleName(this.backButtonAccessibleName);
		backButton.setBorder(buttonsEmptyBorder);
		backButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		backButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		backButton.setMargin(NO_MARGIN);
		backButton.setBackground((Color) UIManager.get("control"));
		backButton.setOpaque(false);
		backButton.setFocusable(false);

		topPanel.add(backButton);
		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_5));

		// Up Button
		JButton upFolderButton = new JButton(getChangeToParentDirectoryAction());
		upFolderButton.setText(null);
		upFolderButton.setRolloverEnabled(true);
		upFolderButton.setIcon(this.upFolderNormal);
		upFolderButton.setPressedIcon(this.upFolderHighlighted);
		upFolderButton.setRolloverIcon(this.upFolderHighlighted);
		upFolderButton.setDisabledIcon(this.upFolderDisabled);
		upFolderButton.setToolTipText(this.upFolderToolTipText);
		upFolderButton.getAccessibleContext().setAccessibleName(this.upFolderAccessibleName);
		upFolderButton.setBorder(buttonsEmptyBorder);
		upFolderButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		upFolderButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		upFolderButton.setMargin(NO_MARGIN);
		upFolderButton.setBackground((Color) UIManager.get("control"));
		upFolderButton.setFocusable(false);
		upFolderButton.setOpaque(false);

		topPanel.add(upFolderButton);
		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_5));

		this.bookmarksButton = new JButton(new BookmarkAction());
		this.bookmarksButton.setRolloverEnabled(true);
		this.bookmarksButton.setOpaque(false);
		this.bookmarksButton.setIcon(this.bookmarksAddNormal);
		this.bookmarksButton.setPressedIcon(this.bookmarksAddHighlighted);
		this.bookmarksButton.setRolloverIcon(this.bookmarksAddHighlighted);
		this.bookmarksButton.setBorder(buttonsEmptyBorder);
		this.bookmarksButton.setFocusable(false);

		this.bookmarksButton.setToolTipText("Add to Bookmarks");
		this.bookmarksButton.setVisible(true);
		this.bookmarksButton.getAccessibleContext().setAccessibleName(this.homeFolderAccessibleName);
		this.bookmarksButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.bookmarksButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		this.bookmarksButton.setMargin(NO_MARGIN);
		this.bookmarksButton.setBackground((Color) UIManager.get("control"));

		topPanel.add(this.bookmarksButton);
		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_5));

		// Home Button
		File homeDir = fsv.getHomeDirectory();
		String toolTipText = this.homeFolderToolTipText;
		if (fsv.isRoot(homeDir)) {
			toolTipText = getFileView(fc).getName(homeDir); // Probably "Desktop".
		}

		JButton b = new JButton(getGoHomeAction());
		b.setRolloverEnabled(true);
		b.setIcon(this.homeNormal);
		b.setPressedIcon(this.homeHighlighted);
		b.setRolloverIcon(this.homeHighlighted);
		b.setDisabledIcon(this.homeDisabled);
		b.setText("");
		b.setToolTipText(toolTipText);
		b.getAccessibleContext().setAccessibleName(this.homeFolderAccessibleName);
		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		b.setAlignmentY(Component.CENTER_ALIGNMENT);
		b.setMargin(NO_MARGIN);
		b.setBorder(buttonsEmptyBorder);
		b.setFocusable(false);
		b.setOpaque(false);

		topPanel.add(b);
		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_5));

		// New Directory Button
		b = new JButton(getNewFolderAction());
		b.setText(null);
		b.setRolloverEnabled(true);
		b.setIcon(this.newFolderNormal);
		b.setPressedIcon(this.newFolderHighlighted);
		b.setRolloverIcon(this.newFolderHighlighted);
		b.setDisabledIcon(this.newFolderDisabled);
		b.setBorder(buttonsEmptyBorder);
		b.setOpaque(false);
		b.setFocusable(false);

		b.setToolTipText(this.newFolderToolTipText);
		b.getAccessibleContext().setAccessibleName(this.newFolderAccessibleName);
		b.setAlignmentX(Component.LEFT_ALIGNMENT);
		b.setAlignmentY(Component.CENTER_ALIGNMENT);
		b.setMargin(NO_MARGIN);

		topPanel.add(b);
		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_5));

		// views button
		this.changeViewButton = new JToggleButton(new ChangeViewAction());
		this.changeViewButton.setText(null);
		this.changeViewButton.setRolloverEnabled(true);
		this.changeViewButton.setIcon(this.changeViewNormal);
		this.changeViewButton.setPressedIcon(this.changeViewHighlighted);
		this.changeViewButton.setRolloverIcon(this.changeViewHighlighted);
		this.changeViewButton.setSelectedIcon(this.changeViewHighlighted);
		this.changeViewButton.setToolTipText("Change view");
		this.changeViewButton.getAccessibleContext().setAccessibleName(this.newFolderAccessibleName);
		this.changeViewButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.changeViewButton.setAlignmentY(Component.CENTER_ALIGNMENT);
		this.changeViewButton.setMargin(NO_MARGIN);
		this.changeViewButton.setBackground((Color) UIManager.get("control"));
		this.changeViewButton.setBorder(buttonsEmptyBorder);
		this.changeViewButton.setOpaque(false);
		this.changeViewButton.setFocusable(false);

		topPanel.add(this.changeViewButton);
		topPanel.add(Box.createRigidArea(HORIZONTAL_STRUT_5));

		topPanel.setBackground((Color) UIManager.get("control"));

		// Use ShellFolder class to populate combobox only if
		// FileSystemView.getRoots() returns one folder and that is
		// the same as the first item in the ShellFolder combobox list.
		{
			this.useShellFolder = false;
			File[] roots = fsv.getRoots();
			if ((roots != null) && (roots.length == 1)) {
				File[] cbFolders = (File[]) ShellFolder.get("fileChooserComboBoxFolders");
				if ((cbFolders != null) && (cbFolders.length > 0) && (roots[0] == cbFolders[0])) {
					this.useShellFolder = true;
				}
			}
		}

		// ************************************** //
		// ******* Add the directory pane ******* //
		// ************************************** //
		this.fileList = new FileList(this, fc);
		fc.addPropertyChangeListener(this.fileList);
		this.fileList.add(getAccessoryPanel(), BorderLayout.AFTER_LINE_ENDS);
		JComponent accessory = fc.getAccessory();
		if (accessory != null) {
			getAccessoryPanel().add(accessory);
		}
		fc.add(this.fileList, BorderLayout.CENTER);

		// ********************************** //
		// **** Construct the bottom panel ** //
		// ********************************** //
		JPanel bottomPanel = getBottomPanel();
		bottomPanel.setOpaque(false);
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		fc.add(bottomPanel, BorderLayout.SOUTH);

		// FileName label and textfield
		JPanel fileNamePanel = new JPanel();
		fileNamePanel.setOpaque(false);
		fileNamePanel.setLayout(new BoxLayout(fileNamePanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(fileNamePanel);
		bottomPanel.add(Box.createRigidArea(VERTICAL_STRUT_5));

		AlignedLabel fileNameLabel = new AlignedLabel(this.fileNameLabelText);
		fileNameLabel.setDisplayedMnemonic(this.fileNameLabelMnemonic);
		fileNamePanel.add(fileNameLabel);

		this.fileNameTextField = new JTextField();
		fileNamePanel.add(this.fileNameTextField);
		
		if (fc.isMultiSelectionEnabled()) {
			setFileName(fileNameString(fc.getSelectedFiles()));
		} else {
			setFileName(fileNameString(fc.getSelectedFile()));
		}

		// Filetype label and combobox
		JPanel filesOfTypePanel = new JPanel();
		filesOfTypePanel.setOpaque(false);
		filesOfTypePanel.setLayout(new BoxLayout(filesOfTypePanel, BoxLayout.LINE_AXIS));
		bottomPanel.add(filesOfTypePanel);

		AlignedLabel filesOfTypeLabel = new AlignedLabel(this.filesOfTypeLabelText);
		filesOfTypeLabel.setDisplayedMnemonic(this.filesOfTypeLabelMnemonic);
		filesOfTypePanel.add(filesOfTypeLabel);

		this.filterComboBoxModel = createFilterComboBoxModel();
		fc.addPropertyChangeListener(this.filterComboBoxModel);
		this.filterComboBox = new JComboBox(this.filterComboBoxModel);
		this.filterComboBox.setOpaque(false);
		this.filterComboBox.getAccessibleContext().setAccessibleDescription(this.filesOfTypeLabelText);
		filesOfTypeLabel.setLabelFor(this.filterComboBox);
		this.filterComboBox.setRenderer(createFilterComboBoxRenderer());
		filesOfTypePanel.add(this.filterComboBox);

		// buttons
		getButtonPanel().setLayout(new ButtonAreaLayout());

		this.approveButton = new JButton(getApproveButtonText(fc));
		this.approveButton.setOpaque(false);
		this.approveButton.addActionListener(getApproveSelectionAction());
		this.approveButton.setToolTipText(getApproveButtonToolTipText(fc));
		getButtonPanel().add(this.approveButton);

		this.cancelButton = new JButton(this.cancelButtonText);
		this.cancelButton.setOpaque(false);
		this.cancelButton.setToolTipText(this.cancelButtonToolTipText);
		this.cancelButton.addActionListener(getCancelSelectionAction());
		getButtonPanel().add(this.cancelButton);

		if (fc.getControlButtonsAreShown()) {
			addControlButtons();
		}

		groupLabels(new AlignedLabel[] { fileNameLabel, filesOfTypeLabel });
	}

	protected JPanel getButtonPanel() {
		if (this.buttonPanel == null) {
			this.buttonPanel = new JPanel();
			this.buttonPanel.setOpaque(false);
		}
		return this.buttonPanel;
	}

	protected JPanel getBottomPanel() {
		if (this.bottomPanel == null) {
			this.bottomPanel = new JPanel();
		}
		return this.bottomPanel;
	}

	@Override
	protected void installStrings(JFileChooser fc) {
		super.installStrings(fc);

		Locale l = fc.getLocale();

		this.lookInLabelMnemonic = UIManager.getInt("FileChooser.lookInLabelMnemonic");
		this.lookInLabelText = UIManager.getString("FileChooser.lookInLabelText", l);
		this.saveInLabelText = UIManager.getString("FileChooser.saveInLabelText", l);

		this.fileNameLabelMnemonic = UIManager.getInt("FileChooser.fileNameLabelMnemonic");
		this.fileNameLabelText = UIManager.getString("FileChooser.fileNameLabelText", l);

		this.filesOfTypeLabelMnemonic = UIManager.getInt("FileChooser.filesOfTypeLabelMnemonic");
		this.filesOfTypeLabelText = UIManager.getString("FileChooser.filesOfTypeLabelText", l);

		this.upFolderToolTipText = UIManager.getString("FileChooser.upFolderToolTipText", l);
		this.upFolderAccessibleName = UIManager.getString("FileChooser.upFolderAccessibleName", l);

		this.homeFolderToolTipText = UIManager.getString("FileChooser.homeFolderToolTipText", l);
		this.homeFolderAccessibleName = UIManager.getString("FileChooser.homeFolderAccessibleName", l);

		this.newFolderToolTipText = UIManager.getString("FileChooser.newFolderToolTipText", l);
		this.newFolderAccessibleName = UIManager.getString("FileChooser.newFolderAccessibleName", l);
	}

	@Override
	protected void installListeners(JFileChooser fc) {
		super.installListeners(fc);

		this.changeViewPopup.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent e) {
				if (e.getPropertyName().toLowerCase().equals("visible")) {
					FileChooserUI.this.changeViewButton.setSelected(Boolean.parseBoolean(e.getNewValue().toString()));
				}
			}
		});

		ActionMap actionMap = getActions();
		SwingUtilities.replaceUIActionMap(fc, actionMap);
	}

	protected ActionMap getActions() {
		return createActions();
	}

	@Override
	public void uninstallUI(JComponent c) {
		c.removePropertyChangeListener(this.filterComboBoxModel);
		this.cancelButton.removeActionListener(getCancelSelectionAction());
		this.approveButton.removeActionListener(getApproveSelectionAction());
		this.fileNameTextField.removeActionListener(getApproveSelectionAction());
		super.uninstallUI(c);
	}

	@Override
	public Dimension getMaximumSize(JComponent c) {
		return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
	}

	private void setFileSelected() {}

	private String fileNameString(File file) {
		if (file == null) {
			return null;
		} else {
			JFileChooser fc = getFileChooser();
			if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
				return file.getPath();
			} else {
				return file.getName();
			}
		}
	}

	private String fileNameString(File[] files) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; (files != null) && (i < files.length); i++) {
			if (i > 0) {
				buf.append(" ");
			}
			if (files.length > 1) {
				buf.append("\"");
			}
			buf.append(fileNameString(files[i]));
			if (files.length > 1) {
				buf.append("\"");
			}
		}
		return buf.toString();
	}

	/* The following methods are used by the PropertyChange Listener */

	private void doSelectedFileChanged(PropertyChangeEvent e) {
		File f = (File) e.getNewValue();
		JFileChooser fc = getFileChooser();
		if ((f != null) && ((fc.isFileSelectionEnabled() && !f.isDirectory()) || (f.isDirectory() && fc.isDirectorySelectionEnabled()))) {

			setFileName(fileNameString(f));
			setFileSelected();
		}
	}

	private void doSelectedFilesChanged(PropertyChangeEvent e) {
		File[] files = (File[]) e.getNewValue();
		JFileChooser fc = getFileChooser();
		if ((files != null) && (files.length > 0) && ((files.length > 1) || fc.isDirectorySelectionEnabled() || !files[0].isDirectory())) {
			setFileName(fileNameString(files));
		} else {
			setFileName("");
		}
	}

	private void doDirectoryChanged(PropertyChangeEvent e) {
		JFileChooser fc = getFileChooser();
		FileSystemView fsv = fc.getFileSystemView();

		clearIconCache();

		File currentDirectory = fc.getCurrentDirectory();
		this.fileList.updatePath(currentDirectory);

		if (currentDirectory != null) {
			this.directoryComboBoxModel.addItem(currentDirectory);
			getNewFolderAction().setEnabled(currentDirectory.canWrite());
			getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
			getChangeToParentDirectoryAction().setEnabled(!fsv.isRoot(currentDirectory));
			getGoHomeAction().setEnabled(!userHomeDirectory.equals(currentDirectory));

			if (fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled()) {
				if (fsv.isFileSystem(currentDirectory)) {
					setFileName(currentDirectory.getPath());
				} else {
					setFileName(null);
				}
			}
		}
	}

	private void doFilterChanged(PropertyChangeEvent e) {
		this.fileList.doFilterChanged();
	}

	private void doFileSelectionModeChanged(PropertyChangeEvent e) {
		doFilterChanged(e);

		JFileChooser fc = getFileChooser();
		File currentDirectory = fc.getCurrentDirectory();
		if ((currentDirectory != null) && fc.isDirectorySelectionEnabled() && !fc.isFileSelectionEnabled() && fc.getFileSystemView().isFileSystem(currentDirectory)) {

			setFileName(currentDirectory.getPath());
		} else {
			setFileName(null);
		}
	}

	private void doMultiSelectionChanged(PropertyChangeEvent e) {
		if (getFileChooser().isMultiSelectionEnabled()) {
		} else {
			getFileChooser().setSelectedFiles(null);
		}
	}

	private void doAccessoryChanged(PropertyChangeEvent e) {
		if (getAccessoryPanel() != null) {
			if (e.getOldValue() != null) {
				getAccessoryPanel().remove((JComponent) e.getOldValue());
			}
			JComponent accessory = (JComponent) e.getNewValue();
			if (accessory != null) {
				getAccessoryPanel().add(accessory, BorderLayout.CENTER);
			}
		}
	}

	private void doApproveButtonTextChanged(PropertyChangeEvent e) {
		JFileChooser chooser = getFileChooser();
		this.approveButton.setText(getApproveButtonText(chooser));
		this.approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
	}

	private void doDialogTypeChanged(PropertyChangeEvent e) {
		JFileChooser chooser = getFileChooser();
		this.approveButton.setText(getApproveButtonText(chooser));
		this.approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
		if (chooser.getDialogType() == JFileChooser.SAVE_DIALOG) {
			this.lookInLabel.setText(this.saveInLabelText);
		} else {
			this.lookInLabel.setText(this.lookInLabelText);
		}
	}

	private void doApproveButtonMnemonicChanged(PropertyChangeEvent e) {}

	private void doControlButtonsChanged(PropertyChangeEvent e) {
		if (getFileChooser().getControlButtonsAreShown()) {
			addControlButtons();
		} else {
			removeControlButtons();
		}
	}

	@Override
	public PropertyChangeListener createPropertyChangeListener(JFileChooser fc) {
		return new PropertyChangeListener() {

			public void propertyChange(PropertyChangeEvent e) {
				String s = e.getPropertyName();

				if (s.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
					doSelectedFileChanged(e);
				} else if (s.equals(JFileChooser.SELECTED_FILES_CHANGED_PROPERTY)) {
					doSelectedFilesChanged(e);
				} else if (s.equals(JFileChooser.DIRECTORY_CHANGED_PROPERTY)) {
					doDirectoryChanged(e);
				} else if (s.equals(JFileChooser.FILE_FILTER_CHANGED_PROPERTY)) {
					doFilterChanged(e);
				} else if (s.equals(JFileChooser.FILE_SELECTION_MODE_CHANGED_PROPERTY)) {
					doFileSelectionModeChanged(e);
				} else if (s.equals(JFileChooser.MULTI_SELECTION_ENABLED_CHANGED_PROPERTY)) {
					doMultiSelectionChanged(e);
				} else if (s.equals(JFileChooser.ACCESSORY_CHANGED_PROPERTY)) {
					doAccessoryChanged(e);
				} else if (s.equals(JFileChooser.APPROVE_BUTTON_TEXT_CHANGED_PROPERTY) || s.equals(JFileChooser.APPROVE_BUTTON_TOOL_TIP_TEXT_CHANGED_PROPERTY)) {
					doApproveButtonTextChanged(e);
				} else if (s.equals(JFileChooser.DIALOG_TYPE_CHANGED_PROPERTY)) {
					doDialogTypeChanged(e);
				} else if (s.equals(JFileChooser.APPROVE_BUTTON_MNEMONIC_CHANGED_PROPERTY)) {
					doApproveButtonMnemonicChanged(e);
				} else if (s.equals(JFileChooser.CONTROL_BUTTONS_ARE_SHOWN_CHANGED_PROPERTY)) {
					doControlButtonsChanged(e);
				} else if (s.equals(JFileChooser.FILE_HIDING_CHANGED_PROPERTY)) {
					FileChooserUI.this.fileList.doFilterChanged();
				} else if (s.equals("componentOrientation")) {
					ComponentOrientation o = (ComponentOrientation) e.getNewValue();
					JFileChooser cc = (JFileChooser) e.getSource();
					if (o != (ComponentOrientation) e.getOldValue()) {
						cc.applyComponentOrientation(o);
					}
				} else if (s.equals("ancestor")) {
					if ((e.getOldValue() == null) && (e.getNewValue() != null)) {
						// Ancestor was added, set initial focus
						FileChooserUI.this.fileNameTextField.selectAll();
						FileChooserUI.this.fileList.itemPanel.requestFocus();
					}
				}
			}
		};
	}

	protected void removeControlButtons() {
		getBottomPanel().remove(getButtonPanel());
	}

	protected void addControlButtons() {
		getBottomPanel().add(getButtonPanel());
	}

	@Override
	public void ensureFileIsVisible(JFileChooser fc, File f) {}

	@Override
	public void rescanCurrentDirectory(JFileChooser fc) {
		this.fileList.rescanDirectory();
	}

	@Override
	public String getFileName() {
		if (this.fileNameTextField != null) {
			return this.fileNameTextField.getText();
		} else {
			return null;
		}
	}

	@Override
	public void setFileName(String filename) {
		if (this.fileNameTextField != null) {
			this.fileNameTextField.setText(filename);
		}
	}

	@Override
	protected void setDirectorySelected(boolean directorySelected) {
		super.setDirectorySelected(directorySelected);
		JFileChooser chooser = getFileChooser();
		if (directorySelected) {
			this.approveButton.setText(this.directoryOpenButtonText);
			this.approveButton.setToolTipText(this.directoryOpenButtonToolTipText);
		} else {
			this.approveButton.setText(getApproveButtonText(chooser));
			this.approveButton.setToolTipText(getApproveButtonToolTipText(chooser));
		}
	}

	@Override
	public String getDirectoryName() {
		return null;
	}

	@Override
	public void setDirectoryName(String dirname) {}

	protected DirectoryComboBoxRenderer createDirectoryComboBoxRenderer(JFileChooser fc) {
		return new DirectoryComboBoxRenderer();
	}

	protected DirectoryComboBoxModel createDirectoryComboBoxModel(JFileChooser fc) {
		return new DirectoryComboBoxModel();
	}


	protected FilterComboBoxRenderer createFilterComboBoxRenderer() {
		return new FilterComboBoxRenderer();
	}

	protected FilterComboBoxModel createFilterComboBoxModel() {
		return new FilterComboBoxModel();
	}

	public void valueChanged(ListSelectionEvent e) {
		JFileChooser fc = getFileChooser();
		File f = fc.getSelectedFile();
		if (!e.getValueIsAdjusting() && (f != null) && !getFileChooser().isTraversable(f)) {
			setFileName(fileNameString(f));
		}
	}

	protected void setCurrentDirectoryOfFileChooser(File f) {
		getFileChooser().setCurrentDirectory(f);
	}

	@Override
	protected JButton getApproveButton(JFileChooser fc) {
		return this.approveButton;
	}

	private static void groupLabels(AlignedLabel[] group) {
		for (AlignedLabel element : group) {
			element.group = group;
		}
	}

	@Override
	public FileView getFileView(JFileChooser fc) {
		return this.fileView;
	}

	public void goBack() {
		if (this.backPathVector.size() > 0) {
			setCurrentDirectoryOfFileChooser(new File(this.backPathVector.elementAt(this.backPathVector.size() - 1)));

			if (this.backPathVector.size() > 1) {
				this.backPathVector.setSize(this.backPathVector.size() - 2);
			} else {
				this.backPathVector.setSize(this.backPathVector.size() - 1);
			}

			if (this.backPathVector.size() <= 0) {
				getGoBackAction().setEnabled(false);
			}
		}
	}

	public Action getGoBackAction() {
		return this.goBackAction;
	}

	@Override
	public Action getChangeToParentDirectoryAction() {
		return this.goToParentDirectoryAction;
	}

	public JPopupMenu createViewPopupMenu() {
		JMenuItem menuItem;
		changeViewActionListener mal = new changeViewActionListener();

		this.changeViewPopup = new JPopupMenu();
		this.changeViewButtonGroup = new ButtonGroup();

		menuItem = new JRadioButtonMenuItem(FILECHOOSER_VIEW_THUMBNAIL);
		this.changeViewButtonGroup.add(menuItem);
		menuItem.setActionCommand(FILECHOOSER_VIEW_THUMBNAIL);
		menuItem.addActionListener(mal);
		this.changeViewPopup.add(menuItem);

		menuItem = new JRadioButtonMenuItem(FILECHOOSER_VIEW_ICON);
		menuItem.setActionCommand(FILECHOOSER_VIEW_ICON);
		this.changeViewButtonGroup.add(menuItem);
		menuItem.addActionListener(mal);
		this.changeViewPopup.add(menuItem);

		menuItem = new JRadioButtonMenuItem(FILECHOOSER_VIEW_LIST);
		menuItem.setActionCommand(FILECHOOSER_VIEW_LIST);
		this.changeViewButtonGroup.add(menuItem);
		menuItem.addActionListener(mal);
		menuItem.setSelected(true);
		this.changeViewPopup.add(menuItem);

		menuItem = new JRadioButtonMenuItem(FILECHOOSER_VIEW_DETAILS);
		menuItem.setActionCommand(FILECHOOSER_VIEW_DETAILS);
		this.changeViewButtonGroup.add(menuItem);
		menuItem.addActionListener(mal);
		this.changeViewPopup.add(menuItem);

		return this.changeViewPopup;
	}

	protected void updateView(String s) {
		if (!s.equals(FILECHOOSER_VIEW_DETAILS) && !s.equals(FILECHOOSER_VIEW_ICON) && !s.equals(FILECHOOSER_VIEW_LIST) && !s.equals(FILECHOOSER_VIEW_THUMBNAIL)) {
			return;
		}

		this.viewType = s;
		this.fileList.changeCardForView();

		//synchronizing menu's
		JRadioButtonMenuItem rbm;
		Enumeration en = this.changeViewButtonGroup.getElements();
		while (en.hasMoreElements()) {
			rbm = (JRadioButtonMenuItem) en.nextElement();

			if (rbm.getActionCommand().equals(this.getView())) {
				this.changeViewButtonGroup.setSelected(rbm.getModel(), true);
			}
		}
	}
}
