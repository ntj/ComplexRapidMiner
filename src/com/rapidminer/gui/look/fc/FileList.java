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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.TreeMap;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileSystemView;

import sun.awt.shell.ShellFolder;

import com.rapidminer.gui.tools.SwingTools;

/**
 * The actual file selection list.
 *
 * @author Ingo Mierswa
 * @version $Id: FileList.java,v 1.7 2008/05/09 20:57:26 ingomierswa Exp $
 */
public class FileList extends JPanel implements PropertyChangeListener {

	private static class MenuListener implements java.awt.event.ActionListener {

		private FileList fileList;
		
		public MenuListener(FileList fileList) {
			this.fileList = fileList;
		}

		public void actionPerformed(ActionEvent e) {
			try {
				if (e.getActionCommand().equals("New Folder")) {
					fileList.filechooserUI.getNewFolderAction().actionPerformed(null);
				} else if (e.getActionCommand().equals("Refresh")) {
					fileList.rescanDirectory();
				} else if (e.getActionCommand().startsWith("ORDER")) {
					fileList.orderBy(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(':') + 1), false);
					fileList.arrangeTheFiles();
				} else if (e.getActionCommand().startsWith("viewType")) {
					fileList.filechooserUI.updateView(e.getActionCommand().substring(e.getActionCommand().lastIndexOf(':') + 1));
				}

				else if (e.getActionCommand().equals("Select All")) {
					fileList.selectAll();
				} else if (e.getActionCommand().equals("Add to Bookmarks")) {
					fileList.addToBookmarks();
				}
			} catch (Exception exp) {
			}
		}
	}

	
	private static final long serialVersionUID = 8893252970970228545L;

	private static final ImageIcon SMALL_FILE_IMAGE = SwingTools.createImage("plaf/unknown_file_small.png");

	private static final ImageIcon SMALL_FOLDER_IMAGE = SwingTools.createImage("plaf/unknown_folder_small.png");

	private static final ImageIcon BIG_FILE_IMAGE = SwingTools.createImage("plaf/unknown_file_big.png");

	private static final ImageIcon BIG_FOLDER_IMAGE = SwingTools.createImage("plaf/unknown_folder_big.png");

	
	private File selectedFile;

	private Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);

	private Cursor normalCursor = Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR);

	private ButtonGroup viewButtonGroup;
	
	private ButtonGroup orderButtonGroup;

	protected ItemPanel itemPanel = new ItemPanel(this);

	private ThumbGeneratorThread thumbGenerator = new ThumbGeneratorThread(this.itemPanel);

	private FileSystemView fsv = FileSystemView.getFileSystemView();

	private BookmarkIO bookmarksIO = new BookmarkIO();
	
	private BookmarkListModel bookmarkListModel = new BookmarkListModel();

	private JList bookmarkList = new BookmarkList(bookmarkListModel, this);
	

	private JSplitPane mainSplitPane = new JSplitPane();

	private JMenuItem menuItem;

	private Window window;

	protected JPanel cardPanel = new JPanel(new CardLayout());

	private JScrollPane tableScrollPane = new JScrollPane();

	protected FileTable tablePanel = new FileTable(this);

	protected JScrollPane browseScrollPane = new JScrollPane();

	private TreeMap<String, Object[]> systemInfoCach = new TreeMap<String, Object[]>();

	protected ItemPanelKeyboardListener keyListener = new ItemPanelKeyboardListener();

	protected Vector<Object> completeItemsList = new Vector<Object>(20);

	protected Vector<Item> visibleItemsList = new Vector<Item>(20);

	protected Vector<Object> tempList = new Vector<Object>(20);

	private TreeMap<Item, Item> tempCompareTree = new TreeMap<Item, Item>();

	protected Item lastSelected;

	protected JPopupMenu panePopup;

	private FileList.MenuListener mal;

	private String tempExtension;

	protected JFileChooser fc;

	protected FileChooserUI filechooserUI;

	protected File tempFile, currentFile = null;

	private Object[] tempArray;

	private File[] selectedFilesArray;

	protected Vector<Object> selectedFilesVector = new Vector<Object>();

	private boolean tempFlag = false;

	public static String ORDER_BY_FILE_NAME = "Name";

	public static String ORDER_BY_FILE_TYPE = "File type";

	public static String ORDER_BY_FILE_MODIFIED = "Last modified";

	public static String ORDER_BY_FILE_SIZE = "File size";

	protected String ORDER_BY = ORDER_BY_FILE_NAME;

	private JMenuItem addToBookmarksMenuItem;

	private JCheckBoxMenuItem autoArrangeCheckBox;

	
	public FileList() {
		bookmarkList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		bookmarkList.setLayoutOrientation(JList.VERTICAL);
		bookmarkList.setVisibleRowCount(-1);
	}
	
	protected ImageIcon getSystemIcon(File file, String filename, boolean isDir, boolean bigIcon) throws Exception {
		if (!isDir) {
			if (filename.indexOf('.') > -1) {
				this.tempExtension = filename.substring((1 + file.getName().indexOf('.')));
			} else {
				this.tempExtension = "Type is : ." + this.fsv.getSystemTypeDescription(file);
			}

			if (this.systemInfoCach.containsKey(this.tempExtension)) {
				cachSystemDetails(file, filename);
			}
			this.tempArray = this.systemInfoCach.get(this.tempExtension);

			if (bigIcon) { //getting big Icon
				if (this.tempArray[3] == null) {
					Image image = getShellFolder(file).getIcon(true);
					if (image == null) {
						this.tempArray[3] = BIG_FILE_IMAGE;
					} else {
						this.tempArray[3] = Tools.getBigSystemIcon(image);
					}
					this.systemInfoCach.put(this.tempExtension, this.tempArray);
				}
				return (ImageIcon) this.tempArray[3];
			} else { //getting small Icon
				if (this.tempArray[2] == null) {
					Image image = null;
					try {
						image = getShellFolder(file).getIcon(false);
					} catch (Exception exp) {
						// do nothing
					}
					if (image == null) {
						this.tempArray[2] = SMALL_FILE_IMAGE;
					} else {
						this.tempArray[2] = Tools.getSmallSystemIcon(image);
					}
					this.systemInfoCach.put(this.tempExtension, this.tempArray);
				}
				return (ImageIcon) this.tempArray[2];
			}
		} else {
			if (bigIcon) {
				Image image = getShellFolder(file).getIcon(true);
				if (image == null) {
					return BIG_FOLDER_IMAGE;
				} else {
					return Tools.getBigSystemIcon(image);
				}
			} else {
				Image tempImage = getShellFolder(file).getIcon(false);
				if (tempImage == null) {
					return SMALL_FOLDER_IMAGE;
				} else {
					return Tools.getSmallSystemIcon(tempImage);
				}
			}
		}
	}

	private void updateViewMenuItemsGroup() {
		JRadioButtonMenuItem rbm;

		Enumeration en = this.viewButtonGroup.getElements();
		while (en.hasMoreElements()) {
			rbm = (JRadioButtonMenuItem)en.nextElement();

			if (rbm.getActionCommand().equals("viewType:" + this.filechooserUI.getView())) {
				this.viewButtonGroup.setSelected(rbm.getModel(), true);
			}
		}
	}

	protected void changeCardForView() {
		stopTumbnailGeneration();

		if (this.filechooserUI.getView().equals(FileChooserUI.FILECHOOSER_VIEW_DETAILS)) {
			CardLayout cl = (CardLayout) (this.cardPanel.getLayout());
			cl.show(this.cardPanel, "tableScrollPane");

			updateTableData();
			this.tableScrollPane.getViewport().setViewPosition(new Point());

			this.tablePanel.requestFocusInWindow();

		} else {
			CardLayout cl = (CardLayout) (this.cardPanel.getLayout());
			cl.show(this.cardPanel, "browseScrollPane");

			this.itemPanel.updateViewType();
			this.browseScrollPane.getViewport().setViewPosition(new Point());

			this.itemPanel.requestFocusInWindow();
		}
		updateViewMenuItemsGroup();
	}

	protected Object[] cachSystemDetails(File file, String filename) {
		if (filename.indexOf('.') > -1) {
			this.tempExtension = filename.substring((1 + file.getName().indexOf('.')));
		} else {
			this.tempExtension = "Type is : ." + this.fsv.getSystemTypeDescription(file);
		}

		if (!this.systemInfoCach.containsKey(this.tempExtension)) {
			try {
				String tempDesc = null;
				try {
					tempDesc = this.fsv.getSystemTypeDescription(file);
				} catch (Exception exp) {
					// do nothing
				}

				if (tempDesc == null) {
					tempDesc = filename;
				}

				this.systemInfoCach.put(this.tempExtension, new Object[] { this.tempExtension, tempDesc, null, null });
			} catch (Exception ex) {
				// do nothing
			}
		}
		return this.systemInfoCach.get(this.tempExtension);
	}

	private ShellFolder getShellFolder(File f) {
		try {
			return ShellFolder.getShellFolder(f);
		} catch (FileNotFoundException e) {
			return null;
		} catch (InternalError e) {
			return null;
		}
	}

	public void addToBookmarks() {
		addToBookmarks(this.fc.getCurrentDirectory());
	}

	public void addToBookmarks(File f) {
		String name = "";
		name = JOptionPane.showInputDialog(this.fc, "Please insert preferred name for selected folder", this.fsv.getSystemDisplayName(f));
		if ((name != null) && !name.trim().equals("")) {
			try {
				this.bookmarksIO.addToList(name, f.getCanonicalFile().getPath());
			} catch (IOException ex) {
			}
			updateBookmarks();
		}
	}

	private void updateBookmarks() {
		this.bookmarkListModel.removeAllBookmarks();
		Collection<Bookmark> bookmarks = this.bookmarksIO.getBookmarks();
		for (Bookmark bookmark : bookmarks) {
			this.bookmarkListModel.addBookmark(bookmark);
		}
	}

	public void deleteBookmark(Bookmark bookmark) {
		this.bookmarksIO.deleteBookmark(bookmark);
		updateBookmarks();
	}

	public void renameBookmark(Bookmark bookmark) {
		this.window = Tools.getWindowForComponent(this);
		BookmarkDialog dialog;
		if (this.window instanceof Frame) {
			dialog = new BookmarkDialog((Frame) this.window, true);
		} else {
			dialog = new BookmarkDialog((Dialog) this.window, true);
		}

		dialog.setLocationRelativeTo(this.window);
		dialog.updateDefaults(bookmark.getName(), bookmark.getPath());

		dialog.setVisible(true);

		if (dialog.isNameChanged()) {
			this.bookmarksIO.renameBookmark(bookmark, dialog.getNewName());
			updateBookmarks();
		}
	}

	public File getFile() {
		return this.selectedFile;
	}

	public FileList(FileChooserUI tfcui, JFileChooser fc) {
		try {
			this.fc = fc;
			this.filechooserUI = tfcui;
			init();
			updateBookmarks();
		} catch (Exception e) {
		}
	}

	private void init() throws Exception {
		this.setLayout(new BorderLayout());
		this.mainSplitPane.setName("");
		this.mainSplitPane.setAutoscrolls(true);
		this.mainSplitPane.setBorder(null);
		this.mainSplitPane.setMinimumSize(new Dimension(40, 25));
		this.mainSplitPane.setOpaque(true);
		this.mainSplitPane.setContinuousLayout(false);

		// the bookmarks split divider location 
		this.mainSplitPane.setDividerLocation(170);
		this.mainSplitPane.setLastDividerLocation(170);

		this.mainSplitPane.setOneTouchExpandable(true);
		this.tableScrollPane.getViewport().setBackground(Color.white);
		this.tableScrollPane.setFocusable(false);
		this.tableScrollPane.getVerticalScrollBar().setUnitIncrement(10);

		this.browseScrollPane.setName("");
		this.browseScrollPane.getVerticalScrollBar().setUnitIncrement(10);
		
		JScrollPane bookmarkPane = new JScrollPane(this.bookmarkList);
		bookmarkPane.setBorder(BorderFactory.createMatteBorder(1, 1, 1, 0, new Color(150, 150, 150)));

		this.add(this.mainSplitPane, BorderLayout.CENTER);

		this.cardPanel.add(this.browseScrollPane, "browseScrollPane");
		this.browseScrollPane.setActionMap(null);
		this.cardPanel.add(this.tableScrollPane, "tableScrollPane");
		this.tableScrollPane.setActionMap(null);
		this.tableScrollPane.getViewport().add(this.tablePanel);
		this.browseScrollPane.getViewport().add(this.itemPanel);

		this.mainSplitPane.add(this.cardPanel, JSplitPane.RIGHT);
		this.mainSplitPane.add(bookmarkPane, JSplitPane.LEFT);

		this.fc.setPreferredSize(new Dimension(780, 510));
		updateTablePanelSize();

		this.cardPanel.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				FileList.this.itemPanel.arrangeTheFiles((int) FileList.this.cardPanel.getSize().getWidth());
			}
		});

		this.tableScrollPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				updateTablePanelSize();
				if (FileList.this.filechooserUI.viewType.equals(FileChooserUI.FILECHOOSER_VIEW_DETAILS)) {
					FileList.this.tablePanel.requestFocusInWindow();
				}
			}
		});
	}

	protected void updateTablePanelSize() {
		if (this.tablePanel.getInitialHeight() < this.tableScrollPane.getSize().getHeight()) {
			this.tablePanel.setPreferredSize(new Dimension(this.tablePanel.getWidth(), this.tableScrollPane.getHeight() - this.tablePanel.getTableHeader().getHeight()));
			this.tablePanel.setMinimumSize(this.tablePanel.getPreferredSize());
			this.tablePanel.setSize(this.tablePanel.getPreferredSize());
		} else {
			this.tablePanel.setPreferredSize(new Dimension(this.tablePanel.getWidth(), this.tablePanel.getInitialHeight()));
			this.tablePanel.setMinimumSize(this.tablePanel.getPreferredSize());
			this.tablePanel.setSize(this.tablePanel.getPreferredSize());
		}
	}

	public void generateThumbs() {
		this.thumbGenerator = new ThumbGeneratorThread(this.itemPanel);
		this.thumbGenerator.start();
	}

	public void setSelectedFile(File f) {
		this.filechooserUI.setCurrentDirectoryOfFileChooser(f);
	}

	public void updatePath(File file) {
		this.fc.setCursor(this.waitCursor);
		clearEveryThing();
		explorFolder(file);
		this.fc.setCursor(this.normalCursor);
	}

	@SuppressWarnings("deprecation")
	protected void updateThumbnail() {
		if (this.itemPanel.getCurrentView().equals(FileChooserUI.FILECHOOSER_VIEW_THUMBNAIL)) {
			this.thumbGenerator.stop();
			this.thumbGenerator = new ThumbGeneratorThread(this.itemPanel);
			this.thumbGenerator.start();
		}
	}

	public void rescanDirectory() {
		this.tempFile = this.fc.getCurrentDirectory();
		try {
			this.tempFile = this.tempFile.getCanonicalFile();
		} catch (IOException ex) {
		}

		File[] files = this.tempFile.listFiles();
		TreeMap<File, File> filesMap = new TreeMap<File, File>();
		Enumeration en = this.completeItemsList.elements();
		while (en.hasMoreElements()) {
			Item item = (Item) en.nextElement();
			filesMap.put(item.getFile(), item.getFile());
		}

		for (File element : files) {
			this.tempFile = element;
			try {
				this.tempFile = this.tempFile.getCanonicalFile();
			} catch (IOException ex) {
			}

			if (!filesMap.containsKey(this.tempFile)) {
				Item item = new Item(this.itemPanel, this.tempFile);
				this.completeItemsList.add(item);
				this.visibleItemsList.add(item);
				item.addKeyListener(this.keyListener);
				this.itemPanel.add(item);
				this.itemPanel.findBestConfig(item);
				this.itemPanel.repaint();
				this.scrollRectToVisible(item.getBounds());
			}
		}

		//deleted items
		Vector<Object> removingItems = new Vector<Object>();
		filesMap.clear();

		for (File element : files) {
			this.tempFile = element;
			try {
				this.tempFile = this.tempFile.getCanonicalFile();
			} catch (Exception exp) {
			}
			filesMap.put(this.tempFile, this.tempFile);
		}

		for (int j = 0; j < this.completeItemsList.size(); j++) {
			Item item = (Item) this.completeItemsList.elementAt(j);
			if (!filesMap.containsKey(item.getFile())) {
				removingItems.add(item);
			}
		}

		Enumeration n = removingItems.elements();
		while (n.hasMoreElements()) {
			Item item = (Item) n.nextElement();
			this.completeItemsList.remove(item);
			this.visibleItemsList.remove(item);
			this.itemPanel.remove(item);
			if (this.selectedFilesVector.contains(item)) {
				this.selectedFilesVector.remove(item);
			}
		}

		files = null;

		if (this.filechooserUI.getView().equals(FileChooserUI.FILECHOOSER_VIEW_DETAILS)) {
			updateTableData();
		} else if (this.filechooserUI.getView().equals(FileChooserUI.FILECHOOSER_VIEW_THUMBNAIL)) {
			stopTumbnailGeneration();
			generateThumbs();
		}

		orderBy(this.ORDER_BY, true);
		arrangeTheFiles();

		repaint();
	}

	private void explorFolder(File file) {
		try {
			file = file.getCanonicalFile();
		} catch (IOException ex) {
		}

		if (this.currentFile != null) {
			this.filechooserUI.backPathVector.add(this.currentFile.getPath());
			this.filechooserUI.getGoBackAction().setEnabled(true);
		} else {
			this.filechooserUI.getGoBackAction().setEnabled(false);
		}
		this.currentFile = file;

		File[] files = file.listFiles();
		if (files != null) {
			for (File element : files) {
				this.tempFile = element;
				try {
					this.tempFile = element.getCanonicalFile();
				} catch (IOException ex1) {
				}

				if ((!this.currentFile.toString().equals("My Computer") && this.tempFile.exists() && this.tempFile.canRead()) || (this.currentFile.toString().equals("My Computer"))) {
					Item item = new Item(this.itemPanel, this.tempFile);
					item.addKeyListener(this.keyListener);
					this.completeItemsList.add(item);
				} else {
				}
			}
		}
		files = null;

		doDefaults();

		requestFocus();

		repaint();
	}

	public FileSystemView getFSV() {
		return this.fsv;
	}

	@SuppressWarnings("deprecation")
	public void stopTumbnailGeneration() {
		this.thumbGenerator.stop();
		this.thumbGenerator = new ThumbGeneratorThread(this.itemPanel);
	}

	////------------------------------------------------------------------------------

	public void clearEveryThing() {
		this.itemPanel.removeAll();
		this.lastSelected = null;

		this.completeItemsList.removeAllElements();
		this.visibleItemsList.removeAllElements();
		this.selectedFilesVector.removeAllElements();

		this.fc.setSelectedFiles(new File[] {});
		this.completeItemsList.removeAllElements();

		Enumeration en = this.completeItemsList.elements();
		Item t;
		while (en.hasMoreElements()) {
			t = ((Item) en.nextElement());
			t.finalizeAll();
			t = null;
		}
	}

	public void clearSelectedItemsList() {
		Enumeration en = this.selectedFilesVector.elements();

		while (en.hasMoreElements()) {
			Item item = ((Item) en.nextElement());
			item.updateSelectionMode(false);
			item.repaint();
		}

		this.selectedFilesVector.clear();

		this.fc.setSelectedFiles(null);
		repaint();
	}

	public boolean isItemSelected(Item item) {
		return this.selectedFilesVector.contains(item);
	}

	protected void updateFilechooserSelectedItems(Item t, boolean ctrl) {
		if (!ctrl || !this.fc.isMultiSelectionEnabled()) {
			Enumeration en = this.completeItemsList.elements();
			while (en.hasMoreElements()) {
				((Item) en.nextElement()).updateSelectionMode(false);
			}
			this.selectedFilesVector.removeAllElements();
		}

		t.updateSelectionMode(!t.getSelectionMode());

		if (t.getSelectionMode()) {
			this.itemPanel.scrollRectToVisible(t.getBounds());
			if (!this.selectedFilesVector.contains(t)) {
				this.selectedFilesVector.add(t);
			}

		} else {
			if (this.selectedFilesVector.contains(t)) {
				this.selectedFilesVector.remove(t);
			}
		}
		this.lastSelected = t;
		synchFilechoserSelction();
	}

	@Override
	public void requestFocus() {
		if (this.filechooserUI.viewType.equals(FileChooserUI.FILECHOOSER_VIEW_DETAILS)) {
			this.tablePanel.requestFocus();
		} else {
			this.itemPanel.requestFocus();
		}
	}

	protected void synchFilechoserSelction() {
		int counter = 0;
		this.selectedFilesArray = new File[this.selectedFilesVector.size()];

		Vector<Object> tempVector = new Vector<Object>();

		Enumeration en = this.selectedFilesVector.elements();
		while (en.hasMoreElements()) {
			Item item = (Item) en.nextElement();
			if (((this.fc.isDirectorySelectionEnabled() && item.isDirectory())) || (this.fc.isFileSelectionEnabled() && !item.isDirectory())) {
				tempVector.add(item);
			}
		}

		this.selectedFilesArray = new File[tempVector.size()];

		en = tempVector.elements();
		counter = 0;
		while (en.hasMoreElements()) {
			this.selectedFilesArray[counter] = ((Item) en.nextElement()).getFile();
			counter++;
		}

		this.fc.setSelectedFiles(this.selectedFilesArray);

	}

	protected JPopupMenu getPanePopup() {
		if (this.panePopup != null) {
			return this.panePopup;
		}

		this.mal = new MenuListener(this);

		this.panePopup = new JPopupMenu();

		this.menuItem = new JMenuItem("New Folder");
		this.menuItem.addActionListener(this.mal);
		this.panePopup.add(this.menuItem);

		this.addToBookmarksMenuItem = new JMenuItem("Add to Bookmarks");
		this.addToBookmarksMenuItem.addActionListener(this.mal);
		this.panePopup.add(this.addToBookmarksMenuItem);

		this.panePopup.addSeparator();

		this.menuItem = new JMenuItem("Refresh");
		this.menuItem.addActionListener(this.mal);
		this.panePopup.add(this.menuItem);

		this.menuItem = new JMenuItem("Select All");
		this.menuItem.addActionListener(this.mal);
		this.panePopup.add(this.menuItem);

		JMenu orderMenuItem = new JMenu("Order By");
		this.panePopup.add(orderMenuItem);
		this.orderButtonGroup = new ButtonGroup();

		this.menuItem = new JRadioButtonMenuItem(FileList.ORDER_BY_FILE_NAME);
		this.menuItem.setSelected(true);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("ORDER:" + FileList.ORDER_BY_FILE_NAME);
		orderMenuItem.add(this.menuItem);
		this.orderButtonGroup.add(this.menuItem);
		this.menuItem = new JRadioButtonMenuItem(FileList.ORDER_BY_FILE_TYPE);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("ORDER:" + FileList.ORDER_BY_FILE_TYPE);
		orderMenuItem.add(this.menuItem);
		this.orderButtonGroup.add(this.menuItem);
		this.menuItem = new JRadioButtonMenuItem(FileList.ORDER_BY_FILE_SIZE);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("ORDER:" + FileList.ORDER_BY_FILE_SIZE);
		orderMenuItem.add(this.menuItem);
		this.orderButtonGroup.add(this.menuItem);
		this.menuItem = new JRadioButtonMenuItem(FileList.ORDER_BY_FILE_MODIFIED);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("ORDER:" + FileList.ORDER_BY_FILE_MODIFIED);
		orderMenuItem.add(this.menuItem);
		this.orderButtonGroup.add(this.menuItem);

		this.panePopup.addSeparator();

		JMenu viewMenuItem = new JMenu("View Type");
		this.panePopup.add(viewMenuItem);
		this.viewButtonGroup = new ButtonGroup();

		this.menuItem = new JRadioButtonMenuItem(FileChooserUI.FILECHOOSER_VIEW_THUMBNAIL);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("viewType:" + FileChooserUI.FILECHOOSER_VIEW_THUMBNAIL);
		viewMenuItem.add(this.menuItem);
		this.viewButtonGroup.add(this.menuItem);
		this.menuItem = new JRadioButtonMenuItem(FileChooserUI.FILECHOOSER_VIEW_ICON);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("viewType:" + FileChooserUI.FILECHOOSER_VIEW_ICON);
		viewMenuItem.add(this.menuItem);
		this.viewButtonGroup.add(this.menuItem);
		this.menuItem = new JRadioButtonMenuItem(FileChooserUI.FILECHOOSER_VIEW_LIST);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setSelected(true);
		this.menuItem.setActionCommand("viewType:" + FileChooserUI.FILECHOOSER_VIEW_LIST);
		viewMenuItem.add(this.menuItem);
		this.viewButtonGroup.add(this.menuItem);
		this.menuItem = new JRadioButtonMenuItem(FileChooserUI.FILECHOOSER_VIEW_DETAILS);
		this.menuItem.addActionListener(this.mal);
		this.menuItem.setActionCommand("viewType:" + FileChooserUI.FILECHOOSER_VIEW_DETAILS);
		viewMenuItem.add(this.menuItem);
		this.viewButtonGroup.add(this.menuItem);

		viewMenuItem.addSeparator();

		this.autoArrangeCheckBox = new JCheckBoxMenuItem("Auto arrange");
		this.autoArrangeCheckBox.setSelected(true);
		viewMenuItem.add(this.autoArrangeCheckBox);
		return this.panePopup;

	}

	public boolean isAutoArrange() {
		return this.autoArrangeCheckBox.isSelected();
	}

	@SuppressWarnings("unchecked")
	public void orderBy(String str, boolean newPath) {
		if (!newPath && this.ORDER_BY.equals(str)) {
			this.tempList.clear();
			for (int i = this.completeItemsList.size() - 1; i >= 0; i--) {
				this.tempList.add(this.completeItemsList.elementAt(i));
			}
			this.completeItemsList.clear();
			this.completeItemsList = (Vector<Object>) this.tempList.clone();
		} else {
			this.tempCompareTree.clear();
			Enumeration en = this.completeItemsList.elements();
			while (en.hasMoreElements()) {
				Item item = (Item) en.nextElement();
				item.setCompare_type(str);
				this.tempCompareTree.put(item, item);
			}

			this.completeItemsList.clear();
			this.completeItemsList = new Vector<Object>(this.tempCompareTree.values());
		}
		this.ORDER_BY = str;

		JRadioButtonMenuItem rbm;
		Enumeration en = this.orderButtonGroup.getElements();
		while (en.hasMoreElements()) {
			rbm = (JRadioButtonMenuItem) en.nextElement();

			if (rbm.getActionCommand().equals("ORDER:" + this.getOrder())) {
				this.orderButtonGroup.setSelected(rbm.getModel(), true);
			}
		}
		findVisibleItems();
	}

	private void arrangeTheFiles() {
		if (this.filechooserUI.viewType.equals(FileChooserUI.FILECHOOSER_VIEW_DETAILS)) {
			changeCardForView();
		} else {
			this.itemPanel.arrangeTheFiles();
		}
	}

	public void doFilterChanged() {
		findVisibleItems();
		changeCardForView();

		if (this.visibleItemsList.size() > 0) {
			this.lastSelected = this.visibleItemsList.get(0);
		}
	}

	public void selectAll() {
		if (this.fc.isMultiSelectionEnabled()) {
			if (this.filechooserUI.getView().equals(FileChooserUI.FILECHOOSER_VIEW_DETAILS)) {
				this.tablePanel.selectAll();
			} else {
				clearSelectedItemsList();
				Enumeration tempEn = this.visibleItemsList.elements();
				while (tempEn.hasMoreElements()) {
					Item item = (Item) tempEn.nextElement();
					item.updateSelectionMode(true);
					this.selectedFilesVector.add(item);
					item.repaint();
				}
				synchFilechoserSelction();
			}
		}
	}

	public void doDefaults() {
		orderBy(this.ORDER_BY, true);
		changeCardForView();
		if (this.visibleItemsList.size() > 0) {
			this.lastSelected = this.visibleItemsList.get(0);
		}
	}

	protected void findVisibleItems() {
		this.visibleItemsList.clear();
		this.itemPanel.removeAll();

		Enumeration en = this.completeItemsList.elements();
		while (en.hasMoreElements()) {
			this.tempFlag = true;
			Item item = (Item) en.nextElement();

			if (item.getFile().isHidden()) {
				if (this.fc.isFileHidingEnabled()) {
					this.tempFlag = false;
				}
			}
			if (this.tempFlag && (this.fc.getFileFilter() != null) && (!this.fc.getFileFilter().accept(item.getFile()))) {
				this.tempFlag = false;
			}
			if (this.tempFlag && !this.fc.isFileSelectionEnabled() && item.getFile().isFile()) {
				this.tempFlag = false;
			}

			if (this.tempFlag) {
				item.setVisible(true);
			} else {
				item.setVisible(false);
			}

			if (this.tempFlag) {
				this.visibleItemsList.add(item);
				this.itemPanel.add(item);
			}
		}
	}

	protected void updateTableData() {
		Enumeration els = this.visibleItemsList.elements();
		Vector<Object> vec2 = new Vector<Object>();

		int ni = 0;
		int si = 1;
		int ti = 2;
		int li = 3;

		try {
			ni = this.tablePanel.getColumnModel().getColumnIndex("File Name");
		} catch (Exception exp) {
			// do nothing
		}
		try {
			si = this.tablePanel.getColumnModel().getColumnIndex("Size");
		} catch (Exception exp) {
			// do nothing
		}
		try {
			ti = this.tablePanel.getColumnModel().getColumnIndex("Type");
		} catch (Exception exp) {
			// do nothing
		}
		try {
			li = this.tablePanel.getColumnModel().getColumnIndex("Last modified");
		} catch (Exception exp) {
			// do nothing
		}

		Object[] arr;

		while (els.hasMoreElements()) {
			Item tc = (Item) els.nextElement();
			arr = new Object[4];
			arr[ni] = new FileTableLabel(tc.getFileName(), tc.getSmallSystemIcon(), SwingConstants.LEFT);
			arr[si] = tc.convertToCorrectFormat(tc.getFileSize());
			arr[ti] = tc.getFileType();
			arr[li] = java.text.DateFormat.getDateInstance().format(new Date(tc.getLastModificationTime()));

			vec2.add(arr.clone());
		}

		Object[][] res = new Object[vec2.size()][];

		Enumeration en = vec2.elements();
		int c = 0;
		while (en.hasMoreElements()) {
			res[c] = (Object[]) en.nextElement();
			c++;
		}

		updateTablePanelSize();
		this.tablePanel.updateData(res);
		updateTablePanelSize();

		en = this.selectedFilesVector.elements();
		int i = 0;
		while (en.hasMoreElements()) {
			Item item = (Item) en.nextElement();
			this.tablePanel.updateSelectionInterval(this.visibleItemsList.indexOf(item), true);
			i++;
		}
	}

	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(FileChooserUI.FILECHOOSER_VIEW_TYPE)) {
			this.filechooserUI.updateView(evt.getNewValue().toString());
		}
	}

	public String getOrder() {
		return this.ORDER_BY;
	}
}
