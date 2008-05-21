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
package com.rapidminer.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;

import org.freehep.util.export.ExportDialog;

import com.rapidminer.BreakpointListener;
import com.rapidminer.Process;
import com.rapidminer.RapidMiner;
import com.rapidminer.gui.actions.AnovaCalculatorAction;
import com.rapidminer.gui.actions.AttributeDescriptionFileWizardAction;
import com.rapidminer.gui.actions.AttributeEditorAction;
import com.rapidminer.gui.actions.BoxViewerAction;
import com.rapidminer.gui.actions.CheckForJDBCDriversAction;
import com.rapidminer.gui.actions.CheckForUpdatesAction;
import com.rapidminer.gui.actions.EditModeAction;
import com.rapidminer.gui.actions.ExitAction;
import com.rapidminer.gui.actions.ExportAction;
import com.rapidminer.gui.actions.ManageBuildingBlocksAction;
import com.rapidminer.gui.actions.ManageTemplatesAction;
import com.rapidminer.gui.actions.NewAction;
import com.rapidminer.gui.actions.OpenAction;
import com.rapidminer.gui.actions.PageSetupAction;
import com.rapidminer.gui.actions.PrintAction;
import com.rapidminer.gui.actions.PrintPreviewAction;
import com.rapidminer.gui.actions.RedoAction;
import com.rapidminer.gui.actions.ResultHistoryAction;
import com.rapidminer.gui.actions.ResultsModeAction;
import com.rapidminer.gui.actions.RunResumeAction;
import com.rapidminer.gui.actions.SaveAction;
import com.rapidminer.gui.actions.SaveAsAction;
import com.rapidminer.gui.actions.SaveAsTemplateAction;
import com.rapidminer.gui.actions.SettingsAction;
import com.rapidminer.gui.actions.StopAction;
import com.rapidminer.gui.actions.SwitchWorkspaceAction;
import com.rapidminer.gui.actions.ToggleExpertModeAction;
import com.rapidminer.gui.actions.ToggleLoggingViewerItem;
import com.rapidminer.gui.actions.ToggleSystemMonitorItem;
import com.rapidminer.gui.actions.TutorialAction;
import com.rapidminer.gui.actions.UndoAction;
import com.rapidminer.gui.actions.ValidateProcessAction;
import com.rapidminer.gui.actions.WizardAction;
import com.rapidminer.gui.dialog.Browser;
import com.rapidminer.gui.dialog.ProcessInfoScreen;
import com.rapidminer.gui.dialog.RequestSaveDialog;
import com.rapidminer.gui.dialog.Tutorial;
import com.rapidminer.gui.operatortree.OperatorTree;
import com.rapidminer.gui.plotter.PlotterPanel;
import com.rapidminer.gui.processeditor.MainProcessEditor;
import com.rapidminer.gui.processeditor.ResultDisplay;
import com.rapidminer.gui.properties.OperatorPropertyTable;
import com.rapidminer.gui.templates.SaveAsTemplateDialog;
import com.rapidminer.gui.templates.Template;
import com.rapidminer.gui.tools.AboutBox;
import com.rapidminer.gui.tools.ComponentPrinter;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJToolBar;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.LoggingViewer;
import com.rapidminer.gui.tools.StatusBar;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.gui.tools.SystemMonitor;
import com.rapidminer.gui.tools.WelcomeScreen;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.Operator;
import com.rapidminer.operator.OperatorChain;
import com.rapidminer.parameter.ParameterTypeBoolean;
import com.rapidminer.parameter.ParameterTypeColor;
import com.rapidminer.parameter.ParameterTypeInt;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.ParameterService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.XMLException;
import com.rapidminer.tools.plugin.Plugin;

import de.java.print.PreviewDialog;

/**
 * The main component class of the RapidMiner GUI. The class holds a lot of Actions
 * that can be used for the tool bar and for the menu bar. MainFrame has methods
 * for handling the process (saving, opening, creating new). It keeps track
 * of the state of the process and enables/disables buttons. It must be
 * notified whenever the process changes and propagates this event to its
 * children. Most of the code is enclosed within the Actions.
 * 
 * @author Ingo Mierswa
 * @version $Id: MainFrame.java,v 1.30 2008/05/09 22:13:12 ingomierswa Exp $
 */
public class MainFrame extends JFrame implements WindowListener, BreakpointListener {

	/** The property name for &quot;The pixel size of each plot in matrix plots.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE = "rapidminer.gui.plotter.matrixplot.size";

	/** The property name for &quot;The maximum number of rows used for a plotter, using only a sample of this size if more rows are available.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM = "rapidminer.gui.plotter.rows.maximum";

	/** The property name for &quot;Limit number of displayed classes plotter legends. -1 for no limit.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT = "rapidminer.gui.plotter.legend.classlimit";

	/** The property name for &quot;The color for minimum values of the plotter legend.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MINCOLOR = "rapidminer.gui.plotter.legend.mincolor";

	/** The property name for &quot;The color for maximum values of the plotter legend.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MAXCOLOR = "rapidminer.gui.plotter.legend.maxcolor";

	/** The property name for &quot;Limit number of displayed classes for colorized plots. -1 for no limit.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT = "rapidminer.gui.plotter.colors.classlimit";

	/** The property name for &quot;Maximum number of states in the undo list.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE = "rapidminer.gui.undolist.size";

	/** The property name for &quot;Maximum number of examples to use for the attribute editor. -1 for no limit.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_ATTRIBUTEEDITOR_ROWLIMIT = "rapidminer.gui.attributeeditor.rowlimit";

	/** The property name for &quot;Beep on process success?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_BEEP_SUCCESS = "rapidminer.gui.beep.success";

	/** The property name for &quot;Beep on error?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_BEEP_ERROR = "rapidminer.gui.beep.error";

	/** The property name for &quot;Beep when breakpoint reached?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_BEEP_BREAKPOINT = "rapidminer.gui.beep.breakpoint";

	/** The property name for &quot;Limit number of displayed rows in the message viewer. -1 for no limit.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT = "rapidminer.gui.messageviewer.rowlimit";

	/** The property name for &quot;The color for notes in the message viewer.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_NOTES = "rapidminer.gui.messageviewer.highlight.notes";

	/** The property name for &quot;The color for warnings in the message viewer.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_WARNINGS = "rapidminer.gui.messageviewer.highlight.warnings";

	/** The property name for &quot;The color for errors in the message viewer.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_ERRORS = "rapidminer.gui.messageviewer.highlight.errors";

	/** The property name for &quot;The color for the logging service indicator in the message viewer.&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_LOGSERVICE = "rapidminer.gui.messageviewer.highlight.logservice";

	/** The property name for &quot;Shows process info screen after loading?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW = "rapidminer.gui.processinfo.show";

	/** The property name for &quot;Shows process info screen after loading?&quot; */
	public static final String PROPERTY_RAPIDMINER_GUI_SAVEDIALOG = "rapidminer.gui.savedialog";
	
	private static final long serialVersionUID = -1602076945350148969L;

	private static final String NEWS_TEXT =
		"Toggle between the Edit Mode (edit process setup)" + Tools.getLineSeparator() + 
		"and the Results Mode via the upper right icons (or F9)" + Tools.getLineSeparator() +
		"" + Tools.getLineSeparator() + 
		"Arrange and add operators in the tree via drag and drop" + Tools.getLineSeparator() +
		"" + Tools.getLineSeparator() + 
		"Clustering is now part of the RapidMiner core (former plugin)" + Tools.getLineSeparator() + 
		"" + Tools.getLineSeparator() + 
		"Are you interested in support, consulting, or other" + Tools.getLineSeparator() +
		"professional services?   Visit: http://rapid-i.com/";
	
	private static final String HISTORY_ICON_NAME = "24/history2.png";
	private static final String ABOUT_ICON_NAME   = "24/about.png";
	private static final String HELP_ICON_NAME    = "24/help2.png";
	private static final String SUPPORT_ICON_NAME = "24/lifebelt.png";
	private static final String PLUGINS_ICON_NAME = "24/plug.png";
	
	private static final String RAPID_MINER_LOGO_NAME = "rapidminer_logo.png";
			
	private static Icon historyIcon = null;
	private static Icon aboutIcon   = null;
	private static Icon helpIcon    = null;
	private static Icon supportIcon = null;
	private static Icon pluginsIcon = null;
	
	private static Image rapidMinerLogo = null;
	
	static {
		historyIcon = SwingTools.createIcon(HISTORY_ICON_NAME);
		aboutIcon   = SwingTools.createIcon(ABOUT_ICON_NAME);
		helpIcon    = SwingTools.createIcon(HELP_ICON_NAME);
		supportIcon = SwingTools.createIcon(SUPPORT_ICON_NAME);
		pluginsIcon = SwingTools.createIcon(PLUGINS_ICON_NAME);
		
		URL url = Tools.getResource(RAPID_MINER_LOGO_NAME);
		if (url != null) {
			try {
				rapidMinerLogo = ImageIO.read(url);
			} catch (IOException e) {
				rapidMinerLogo = null;
			}
		} else {
			rapidMinerLogo = null;
		}
	}

	/**
	 * Registers all RapidMiner GUI properties. This must often be done centrally in
	 * mainframe to ensure that the properties are set when the GUI is started.
	 */
	static {
		RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_MATRIXPLOT_SIZE, "The pixel size of each plot in matrix plots.", 1, Integer.MAX_VALUE, 200));
        RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_ROWS_MAXIMUM, "The maximum number of rows used for a plotter, using only a sample of this size if more rows are available.", 1, Integer.MAX_VALUE, PlotterPanel.DEFAULT_MAX_NUMBER_OF_DATA_POINTS));
        RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_CLASSLIMIT, "Limit number of displayed classes plotter legends. -1 for no limit.", -1, Integer.MAX_VALUE, 10));
        RapidMiner.registerRapidMinerProperty(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MINCOLOR, "The color for minimum values of the plotter legend.", java.awt.Color.blue));
        RapidMiner.registerRapidMinerProperty(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_PLOTTER_LEGEND_MAXCOLOR, "The color for maximum values of the plotter legend.", java.awt.Color.red));
        RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_PLOTTER_COLORS_CLASSLIMIT, "Limit number of displayed classes for colorized plots. -1 for no limit.", -1, Integer.MAX_VALUE, 10));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE, "Maximum number of states in the undo list.", 1, Integer.MAX_VALUE, 10));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_ATTRIBUTEEDITOR_ROWLIMIT, "Maximum number of examples to use for the attribute editor. -1 for no limit.", -1, Integer.MAX_VALUE, 50));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_BEEP_SUCCESS, "Beep on process success?", false));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_BEEP_ERROR, "Beep on error?", false));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_BEEP_BREAKPOINT, "Beep when breakpoint reached?", false));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeInt(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_ROWLIMIT, "Limit number of displayed rows in the message viewer. -1 for no limit.", -1, Integer.MAX_VALUE, 1000));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_NOTES, "The color for notes in the message viewer.", new java.awt.Color(51,151,51)));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_WARNINGS, "The color for warnings in the message viewer.", new java.awt.Color(51,51,255)));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_ERRORS, "The color for errors in the message viewer.", new java.awt.Color(255,51,204)));
        RapidMiner.registerRapidMinerProperty(new ParameterTypeColor(PROPERTY_RAPIDMINER_GUI_MESSAGEVIEWER_HIGHLIGHT_LOGSERVICE, "The color for the logging service indicator in the message viewer.", new java.awt.Color(184,184,184)));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW, "Shows process info screen after loading?", true));
		RapidMiner.registerRapidMinerProperty(new ParameterTypeBoolean(PROPERTY_RAPIDMINER_GUI_SAVEDIALOG, "Shows a dialog asking for saving the current process before the process is started?", true));
	}

	/** The title of the frame. */
	public static final String TITLE = "RapidMiner";
	
	// --------------------------------------------------------------------------------
	
	public static final int EDIT_MODE    = 0;
	public static final int RESULTS_MODE = 1;
	public static final int WELCOME_MODE = 2;
	
	public static final String EDIT_MODE_NAME = "edit";
	public static final String RESULTS_MODE_NAME = "results";
	public static final String WELCOME_MODE_NAME = "welcome";
	
	public final transient Action NEW_ACTION_24 = new NewAction(this, IconSize.SMALL);
	public final transient Action NEW_ACTION_32 = new NewAction(this, IconSize.MIDDLE);

	public final transient Action OPEN_ACTION_24 = new OpenAction(this, IconSize.SMALL);
	public final transient Action OPEN_ACTION_32 = new OpenAction(this, IconSize.MIDDLE);

	public final transient Action SAVE_ACTION_24 = new SaveAction(this, IconSize.SMALL);
	public final transient Action SAVE_ACTION_32 = new SaveAction(this, IconSize.MIDDLE);

	public final transient Action SAVE_AS_ACTION_24 = new SaveAsAction(this, IconSize.SMALL);
	public final transient Action SAVE_AS_ACTION_32 = new SaveAsAction(this, IconSize.MIDDLE);

	public final transient Action SAVE_AS_TEMPLATE_ACTION_24 = new SaveAsTemplateAction(this, IconSize.SMALL);
	public final transient Action SAVE_AS_TEMPLATE_ACTION_32 = new SaveAsTemplateAction(this, IconSize.MIDDLE);

	public final transient Action MANAGE_TEMPLATES_ACTION_24 = new ManageTemplatesAction(this, IconSize.SMALL);
	public final transient Action MANAGE_TEMPLATES_ACTION_32 = new ManageTemplatesAction(this, IconSize.MIDDLE);

	public final transient Action MANAGE_BUILDING_BLOCKS_ACTION_24 = new ManageBuildingBlocksAction(this, IconSize.SMALL);
	public final transient Action MANAGE_BUILDING_BLOCKS_ACTION_32 = new ManageBuildingBlocksAction(this, IconSize.MIDDLE);

	public final transient Action PRINT_ACTION_24 = new PrintAction(this, IconSize.SMALL);
	public final transient Action PRINT_ACTION_32 = new PrintAction(this, IconSize.MIDDLE);
	
	public final transient Action PRINT_PREVIEW_ACTION_24 = new PrintPreviewAction(this, IconSize.SMALL);
	public final transient Action PRINT_PREVIEW_ACTION_32 = new PrintPreviewAction(this, IconSize.MIDDLE);
	
	public final transient Action PAGE_SETUP_ACTION_24 = new PageSetupAction(this, IconSize.SMALL);
	public final transient Action PAGE_SETUP_ACTION_32 = new PageSetupAction(this, IconSize.MIDDLE);

	public final transient Action EXPORT_ACTION_24 = new ExportAction(this, IconSize.SMALL);
	public final transient Action EXPORT_ACTION_32 = new ExportAction(this, IconSize.MIDDLE);

	public final transient Action EXIT_ACTION_24 = new ExitAction(this, IconSize.SMALL);
	public final transient Action EXIT_ACTION_32 = new ExitAction(this, IconSize.MIDDLE);

	public final transient RunResumeAction RUN_RESUME_ACTION_24 = new RunResumeAction(this, IconSize.SMALL);
	public final transient RunResumeAction RUN_RESUME_ACTION_32 = new RunResumeAction(this, IconSize.MIDDLE);

	public final transient Action STOP_ACTION_24 = new StopAction(this, IconSize.SMALL);
	public final transient Action STOP_ACTION_32 = new StopAction(this, IconSize.MIDDLE);

	public final transient Action VALIDATE_ACTION_24 = new ValidateProcessAction(this, IconSize.SMALL);
	public final transient Action VALIDATE_ACTION_32 = new ValidateProcessAction(this, IconSize.MIDDLE);

	public final transient Action WIZARD_ACTION_24 = new WizardAction(this, IconSize.SMALL);
	public final transient Action WIZARD_ACTION_32 = new WizardAction(this, IconSize.MIDDLE);

	public final transient Action SETTINGS_ACTION_24 = new SettingsAction(this, IconSize.SMALL);
	public final transient Action SETTINGS_ACTION_32 = new SettingsAction(this, IconSize.MIDDLE);

	public final transient Action TOGGLE_EXPERT_MODE_ACTION_24 = new ToggleExpertModeAction(this, IconSize.SMALL);
	public final transient Action TOGGLE_EXPERT_MODE_ACTION_32 = new ToggleExpertModeAction(this, IconSize.MIDDLE);

	public final transient Action TUTORIAL_ACTION_24 = new TutorialAction(this, IconSize.SMALL);
	public final transient Action TUTORIAL_ACTION_32 = new TutorialAction(this, IconSize.MIDDLE);

	public final transient Action UNDO_ACTION_24 = new UndoAction(this, IconSize.SMALL);
	public final transient Action UNDO_ACTION_32 = new UndoAction(this, IconSize.MIDDLE);

	public final transient Action REDO_ACTION_24 = new RedoAction(this, IconSize.SMALL);
	public final transient Action REDO_ACTION_32 = new RedoAction(this, IconSize.MIDDLE);

	public final transient Action ATTRIBUTE_EDITOR_ACTION_24 = new AttributeEditorAction(this, IconSize.SMALL);
	public final transient Action ATTRIBUTE_EDITOR_ACTION_32 = new AttributeEditorAction(this, IconSize.MIDDLE);

	public final transient Action ANOVA_CALCULATOR_ACTION_24 = new AnovaCalculatorAction(this, IconSize.SMALL);
	public final transient Action ANOVA_CALCULATOR_ACTION_32 = new AnovaCalculatorAction(this, IconSize.MIDDLE);

	public final transient Action CHECK_FOR_UPDATES_ACTION_24 = new CheckForUpdatesAction(IconSize.SMALL);
	public final transient Action CHECK_FOR_UPDATES_ACTION_32 = new CheckForUpdatesAction(IconSize.MIDDLE);

    public final transient Action CHECK_FOR_JDBC_DRIVERS_ACTION_24 = new CheckForJDBCDriversAction(this, IconSize.SMALL);
    public final transient Action CHECK_FOR_JDBC_DRIVERS_ACTION_32 = new CheckForJDBCDriversAction(this, IconSize.MIDDLE);

    public final transient Action EDIT_MODE_24 = new EditModeAction(this, IconSize.SMALL);
    public final transient Action EDIT_MODE_32 = new EditModeAction(this, IconSize.MIDDLE);

    public final transient Action RESULTS_MODE_24 = new ResultsModeAction(this, IconSize.SMALL);
    public final transient Action RESULTS_MODE_32 = new ResultsModeAction(this, IconSize.MIDDLE);
    
    public final transient Action BOX_VIEW_24 = new BoxViewerAction(this, IconSize.SMALL);
    public final transient Action BOX_VIEW_32 = new BoxViewerAction(this, IconSize.MIDDLE);

    public final transient Action RESULT_HISTORY_24 = new ResultHistoryAction(this, IconSize.SMALL);
    public final transient Action RESULT_HISTORY_32 = new ResultHistoryAction(this, IconSize.MIDDLE);

    public final transient Action ATTRIBUTE_DESCRIPTION_FILE_WIZARD_24 = new AttributeDescriptionFileWizardAction(IconSize.SMALL);
    public final transient Action ATTRIBUTE_DESCRIPTION_FILE_WIZARD_32 = new AttributeDescriptionFileWizardAction(IconSize.MIDDLE);

    public final transient Action SWITCH_WORKSPACE_24 = new SwitchWorkspaceAction(IconSize.SMALL);
    public final transient Action SWITCH_WORKSPACE_32 = new SwitchWorkspaceAction(IconSize.MIDDLE);
    
    public final JCheckBoxMenuItem TOGGLE_LOGGING_VIEWER = new ToggleLoggingViewerItem(this, IconSize.SMALL);
    public final JCheckBoxMenuItem TOGGLE_SYSTEM_MONITOR = new ToggleSystemMonitorItem(this, IconSize.SMALL);
    
	// --------------------------------------------------------------------------------

	private CardLayout mainCardLayout = new CardLayout();
	
	private JPanel mainPanel = new JPanel(mainCardLayout);
	
	private WelcomeScreen welcomeScreen = null;
	
	private ResultDisplay resultDisplay = new ResultDisplay();

	private LoggingViewer messageViewer = new LoggingViewer();

	private SystemMonitor systemMonitor = new SystemMonitor();
	
	private StatusBar statusBar = new StatusBar();
	
	private MainProcessEditor mainEditor = new MainProcessEditor(this);
	
	private JToolBar toolBar = new ExtendedJToolBar();

	private JSplitPane splitPaneV;
	
	private JSplitPane loggingSplitPaneH;
	
	private int lastLoggingDividerLocation = -1;
	
	private int lastMonitorDividerLocation = -1;
	
	private int currentMode = WELCOME_MODE;
	
	private JMenu recentFilesMenu = new JMenu("Recent Files");

	private transient PrinterJob printerJob = PrinterJob.getPrinterJob();

	private transient PageFormat pageFormat = printerJob.defaultPage();
	
	private boolean changed = false;

	private boolean tutorialMode = false;

	private LinkedList<String> undoList = new LinkedList<String>();

	private int undoIndex;
	
	private boolean noActualChange = false;
	
	/** The hostname of the system. Might be empty (no host name will be shown) and will be initialized
	 *  in the first call of {@link #setTitle()}. */
	private String hostname = null;

	private transient Process process = null;
	
	private transient ProcessThread processThread;
	
	// --------------------------------------------------------------------------------
	
	/** Creates a new main frame containing the RapidMiner GUI. */
	public MainFrame() {
		super(TITLE);
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(this);
		
		// set frame icons
		SwingTools.setFrameIcon(this);

		// create and set new process setup
		setProcess(new Process(), true);
		
		// create main editor and result display
		welcomeScreen = new WelcomeScreen(this, NEWS_TEXT);
		mainPanel.setBorder(null);
		mainPanel.add(mainEditor, EDIT_MODE_NAME);
		mainPanel.add(resultDisplay, RESULTS_MODE_NAME);
		mainPanel.add(new ExtendedJScrollPane(welcomeScreen), WELCOME_MODE_NAME);
		mainEditor.setBorder(null);
		resultDisplay.setBorder(null);
		
		// create message viewer and memory monitor
		systemMonitor.setBorder(BorderFactory.createEtchedBorder());
		loggingSplitPaneH = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, messageViewer, systemMonitor);
		loggingSplitPaneH.setBorder(null);
        // will cause the system monitor half to keep fixed size during resizing
		loggingSplitPaneH.setResizeWeight(1.0);
		systemMonitor.startMonitorThread();
		
		// add main panel and message viewer
		splitPaneV = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, loggingSplitPaneH);
        splitPaneV.setBorder(null);
        // will cause the logging message viewer half to keep fixed size during resizing
		splitPaneV.setResizeWeight(1.0);

		splitPaneV.setBorder(null);

		getContentPane().add(splitPaneV, BorderLayout.CENTER);

		// menu bar
		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		// file menu
		JMenu fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(NEW_ACTION_24);
		fileMenu.add(WIZARD_ACTION_24);
		fileMenu.add(OPEN_ACTION_24);
		updateRecentFileList();
		recentFilesMenu.setIcon(historyIcon);
		fileMenu.add(recentFilesMenu);
		fileMenu.addSeparator();
		fileMenu.add(SAVE_ACTION_24);
		fileMenu.add(SAVE_AS_ACTION_24);
		fileMenu.add(SAVE_AS_TEMPLATE_ACTION_24);
		fileMenu.addSeparator();
		fileMenu.add(PRINT_ACTION_24);
		fileMenu.add(PRINT_PREVIEW_ACTION_24);
		fileMenu.add(PAGE_SETUP_ACTION_24);
		fileMenu.add(EXPORT_ACTION_24);
		fileMenu.addSeparator();
		fileMenu.add(SWITCH_WORKSPACE_24);
		fileMenu.addSeparator();
		fileMenu.add(EXIT_ACTION_24);
		menuBar.add(fileMenu);

		// edit menu
		JMenu editMenu = new JMenu("Edit");
		editMenu.setMnemonic(KeyEvent.VK_E);
		editMenu.add(UNDO_ACTION_24);
		editMenu.add(REDO_ACTION_24);
		editMenu.addSeparator();
		editMenu.add(mainEditor.getOperatorTree().NEW_OPERATOR_ACTION_24);
		editMenu.add(mainEditor.getOperatorTree().NEW_BUILDING_BLOCK_ACTION_24);
		editMenu.addSeparator();
		mainEditor.getOperatorTree().addOperatorMenuItems(editMenu);
		menuBar.add(editMenu);

		// view menu
		JMenu viewMenu = new JMenu("View");
		viewMenu.setMnemonic(KeyEvent.VK_V);
		viewMenu.add(EDIT_MODE_24);
		viewMenu.add(RESULTS_MODE_24);
        viewMenu.addSeparator();
        viewMenu.add(TOGGLE_EXPERT_MODE_ACTION_24);
		viewMenu.addSeparator();
		viewMenu.add(BOX_VIEW_24);
		viewMenu.add(RESULT_HISTORY_24);
		viewMenu.addSeparator();
		viewMenu.add(TOGGLE_LOGGING_VIEWER);
		viewMenu.add(TOGGLE_SYSTEM_MONITOR);
		menuBar.add(viewMenu);
		
		// process menu
		JMenu expMenu = new JMenu("Process");
		expMenu.setMnemonic(KeyEvent.VK_P);
		expMenu.add(RUN_RESUME_ACTION_24);
		expMenu.add(STOP_ACTION_24);
		menuBar.add(expMenu);

		// tools menu
		JMenu toolsMenu = new JMenu("Tools");
		toolsMenu.setMnemonic(KeyEvent.VK_T);
		toolsMenu.add(VALIDATE_ACTION_24);
		toolsMenu.add(mainEditor.getOperatorTree().INFO_OPERATOR_ACTION_24);
		toolsMenu.addSeparator();
		toolsMenu.add(ATTRIBUTE_DESCRIPTION_FILE_WIZARD_24);
		toolsMenu.add(ATTRIBUTE_EDITOR_ACTION_24);
		toolsMenu.add(ANOVA_CALCULATOR_ACTION_24);
		toolsMenu.add(messageViewer.CLEAR_MESSAGE_VIEWER_ACTION_24);
		toolsMenu.addSeparator();		
		toolsMenu.add(MANAGE_TEMPLATES_ACTION_24);
		toolsMenu.add(MANAGE_BUILDING_BLOCKS_ACTION_24);
		toolsMenu.addSeparator();
		toolsMenu.add(CHECK_FOR_UPDATES_ACTION_24);
		toolsMenu.addSeparator();
        toolsMenu.add(CHECK_FOR_JDBC_DRIVERS_ACTION_24);
        toolsMenu.add(SETTINGS_ACTION_24);
		menuBar.add(toolsMenu);

		// help menu
		JMenu helpMenu = new JMenu("Help");
		helpMenu.setMnemonic(KeyEvent.VK_H);
		JMenuItem aboutItem = new JMenuItem("About RapidMiner...", aboutIcon);
		aboutItem.setMnemonic(KeyEvent.VK_A);
		aboutItem.setToolTipText("Display information about RapidMiner");
		aboutItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new AboutBox(MainFrame.this, RapidMiner.getVersion(), rapidMinerLogo).setVisible(true);
			}
		});
		helpMenu.add(aboutItem);
		helpMenu.add(TUTORIAL_ACTION_24);
		JMenuItem contents = new JMenuItem("RapidMiner GUI Manual...", helpIcon);
		contents.setMnemonic(KeyEvent.VK_G);
		contents.setToolTipText("Browse the RapidMiner GUI Manual.");
		contents.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				URL manualResource = Tools.getResource("manual/RapidMinerGUIManual.html");
				if (manualResource != null)
					Browser.showDialog(manualResource);
				else
					SwingTools.showVerySimpleErrorMessage("Cannot load GUI manual: file not found.");
			}
		});
		helpMenu.add(contents);

		JMenuItem needSupport = new JMenuItem("Need Support?", supportIcon);
		needSupport.setMnemonic(KeyEvent.VK_S);
		needSupport.setToolTipText("Learn more about the possibilities of getting professional support for RapidMiner.");
		needSupport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String message = 
					"Do you need professional support? Do you want" + Tools.getLineSeparator() + 
					"to optimize the achieved results? Do you need" + Tools.getLineSeparator() + 
					"professional training which enables you to find" + Tools.getLineSeparator() + 
					"better data mining solutions in shorter times?" + Tools.getLineSeparators(2) + 
					"Check out the offers on" + Tools.getLineSeparators(2) + 
					"                      http://rapid-i.com" + Tools.getLineSeparators(2) + 
					" including" + Tools.getLineSeparators(2) +
					"      - improved software versions, e.g. the Enterprise Edition of RapidMiner," + Tools.getLineSeparator() +
					"      - professional support and other services," + Tools.getLineSeparator() + 
					"      - courses for data mining with RapidMiner," + Tools.getLineSeparator() +
					"      - and individual solutions and extensions." + Tools.getLineSeparators(2);
				JOptionPane.showMessageDialog(MainFrame.this, message, "Need Support?", JOptionPane.INFORMATION_MESSAGE);
			}
		});
		helpMenu.add(needSupport);
		
		List allPlugins = Plugin.getAllPlugins();
		if (allPlugins.size() > 0) {
			helpMenu.addSeparator();
			Iterator i = allPlugins.iterator();
			while (i.hasNext()) {
				final Plugin plugin = (Plugin) i.next();
				JMenuItem aboutPluginItem = new JMenuItem("About " + plugin.getName() + "...", pluginsIcon);
				aboutPluginItem.setToolTipText("Display information about " + plugin.getName());
				aboutPluginItem.addActionListener(new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						plugin.createAboutBox(MainFrame.this, rapidMinerLogo).setVisible(true);
					}
				});
				helpMenu.add(aboutPluginItem);
			}
		}

		menuBar.add(helpMenu);

		// Tool Bar
		/*
		toolBar.add(NEW_ACTION_32);
		toolBar.add(OPEN_ACTION_32);
		toolBar.add(SAVE_ACTION_32);
		toolBar.add(SAVE_AS_ACTION_32);
		toolBar.add(PRINT_ACTION_32);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(UNDO_ACTION_32);
		toolBar.add(REDO_ACTION_32);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(mainEditor.getOperatorTree().NEW_OPERATOR_ACTION_32);
		toolBar.add(mainEditor.getOperatorTree().DELETE_OPERATOR_ACTION_32);
		toolBar.add(mainEditor.getOperatorTree().NEW_BUILDING_BLOCK_ACTION_32);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(RUN_RESUME_ACTION_32);
		toolBar.add(STOP_ACTION_32);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(VALIDATE_ACTION_32);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(TOGGLE_EXPERT_MODE_ACTION_32);
		toolBar.addSeparator();
		toolBar.addSeparator();		
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(EDIT_MODE_32);
		toolBar.add(RESULTS_MODE_32);
		*/
		
		toolBar.add(NEW_ACTION_24);
		toolBar.add(OPEN_ACTION_24);
		toolBar.add(SAVE_ACTION_24);
		toolBar.add(SAVE_AS_ACTION_24);
		toolBar.add(PRINT_ACTION_24);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(UNDO_ACTION_24);
		toolBar.add(REDO_ACTION_24);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(mainEditor.getOperatorTree().NEW_OPERATOR_ACTION_24);
		toolBar.add(mainEditor.getOperatorTree().DELETE_OPERATOR_ACTION_24);
		toolBar.add(mainEditor.getOperatorTree().NEW_BUILDING_BLOCK_ACTION_24);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(RUN_RESUME_ACTION_24);
		toolBar.add(STOP_ACTION_24);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(VALIDATE_ACTION_24);
		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.add(TOGGLE_EXPERT_MODE_ACTION_24);
		toolBar.addSeparator();
		toolBar.addSeparator();		
		toolBar.add(Box.createHorizontalGlue());
		toolBar.add(EDIT_MODE_24);
		toolBar.add(RESULTS_MODE_24);
		
		getContentPane().add(toolBar, BorderLayout.NORTH);
		getContentPane().add(statusBar, BorderLayout.SOUTH);
		statusBar.startClockThread();
		
		addToUndoList();

		enableActions();
		
		pack();
		changeMode(WELCOME_MODE);
	}

	protected Object readResolve() {
		this.printerJob = PrinterJob.getPrinterJob();
		this.pageFormat = printerJob.defaultPage();
		return this;
	}
	
	public void changeMode(int mode) {
		this.currentMode = mode;
		switch (this.currentMode) {
		case EDIT_MODE: 
		    mainCardLayout.show(mainPanel, EDIT_MODE_NAME);
		    EDIT_MODE_24.setEnabled(false);
		    EDIT_MODE_32.setEnabled(false);
		    RESULTS_MODE_24.setEnabled(true);
		    RESULTS_MODE_32.setEnabled(true);
		    break;
		case RESULTS_MODE:
			mainCardLayout.show(mainPanel, RESULTS_MODE_NAME);
		    EDIT_MODE_24.setEnabled(true);
		    EDIT_MODE_32.setEnabled(true);
		    RESULTS_MODE_24.setEnabled(false);
		    RESULTS_MODE_32.setEnabled(false);
			break;
		case WELCOME_MODE:
			mainCardLayout.show(mainPanel, WELCOME_MODE_NAME);
		    EDIT_MODE_24.setEnabled(true);
		    EDIT_MODE_32.setEnabled(true);
		    RESULTS_MODE_24.setEnabled(false);
		    RESULTS_MODE_32.setEnabled(false);
		}
	}
	
	public void startTutorial() {
		if (close()) {
			new Tutorial(MainFrame.this).setVisible(true);
		}	
	}
	
	public void setTutorialMode(boolean mode) {
		this.tutorialMode = mode;
		if (tutorialMode) {
			SAVE_ACTION_24.setEnabled(false);
			SAVE_ACTION_32.setEnabled(false);
			SAVE_AS_ACTION_24.setEnabled(false);
			SAVE_AS_ACTION_32.setEnabled(false);
		} else {
			SAVE_ACTION_24.setEnabled(false);
			SAVE_ACTION_32.setEnabled(false);
			SAVE_AS_ACTION_24.setEnabled(true);
			SAVE_AS_ACTION_32.setEnabled(true);
		}
	}

	public boolean isTutorialMode() {
		return this.tutorialMode;
	}
	
	public void setSystemMonitor(SystemMonitor newSystemMonitor) {
		loggingSplitPaneH.remove(this.systemMonitor);
		this.systemMonitor = newSystemMonitor;
		this.systemMonitor.setBorder(BorderFactory.createEtchedBorder());
		loggingSplitPaneH.add(newSystemMonitor);
	}
	
	public void print() {
		switch (this.currentMode) {
		case EDIT_MODE:
			printerJob.setPrintable(new ComponentPrinter(mainEditor));
			break;
		case RESULTS_MODE:
			printerJob.setPrintable(new ComponentPrinter(resultDisplay.getCurrentlyDisplayedComponent()));
			break;
		}
		if (printerJob.printDialog()) {
			try {
				printerJob.print();
			} catch (PrinterException pe) {
				SwingTools.showSimpleErrorMessage("Printer error", pe);
			}
		}	
	}
	
	public void printPreview() {
		Printable printer = null;
		switch (this.currentMode) {
		case EDIT_MODE:
			printer = new ComponentPrinter(mainEditor);
			break;
		case RESULTS_MODE:
			printer = new ComponentPrinter(resultDisplay.getCurrentlyDisplayedComponent());
			break;
		}
        PreviewDialog dialog = new PreviewDialog("Print Preview", this, printer, pageFormat, 1);
        Component[] dialogComponents = dialog.getContentPane().getComponents();
        for (Component c : dialogComponents) {
        	if (c instanceof JToolBar)
        		((JToolBar)c).setFloatable(false);
        }
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
	}
	
	public void pageSetup() {
		this.pageFormat = printerJob.pageDialog(this.pageFormat);
	}
	
	public void export() {
		ExportDialog exportDialog = new ExportDialog("RapidMiner");	
		switch (this.currentMode) {
		case EDIT_MODE:
			exportDialog.showExportDialog(MainFrame.this, "Export", getOperatorTree(), getBaseName());
			break;
		case RESULTS_MODE:
			exportDialog.showExportDialog(MainFrame.this, "Export", resultDisplay, getBaseName());
			break;
		}	
	}

	public int getEditorDividerLocation() {
		return mainEditor.getDividerLocation();
	}

	public int getGroupSelectionDividerLocation() {
		return mainEditor.getGroupSelectionDivider();
	}
	
	public int getMainDividerLocation() {
		if (this.lastLoggingDividerLocation >= 0) 
			return splitPaneV.getHeight() - this.lastLoggingDividerLocation;
		else
			return splitPaneV.getDividerLocation();
	}
	
	public int getLoggingDividerLocation() {
		if (this.lastMonitorDividerLocation >= 0)
			return loggingSplitPaneH.getWidth() - this.lastMonitorDividerLocation;
		else
			return loggingSplitPaneH.getDividerLocation();
	}
	
	/** The first one is for the horizontal main divider, the second for the vertical 
	 *  editor divider, for the logging divider, and the last one for the new operator
	 *  group selection divider. */
	public void setDividerLocations(int mainH, int editorV, int loggingV, int groupSelectionV) {
		mainEditor.setDividerLocation(editorV);
		mainEditor.setGroupSelectionDivider(groupSelectionV);
		splitPaneV.setDividerLocation(mainH);
		loggingSplitPaneH.setDividerLocation(loggingV);
	}

	public void toggleLoggingViewer(boolean show) {
		if (show) {
			splitPaneV.add(loggingSplitPaneH);
			splitPaneV.setDividerLocation(splitPaneV.getHeight() - this.lastLoggingDividerLocation);
			this.lastLoggingDividerLocation = -1;
		} else {
			this.lastLoggingDividerLocation = splitPaneV.getHeight() - splitPaneV.getDividerLocation();
			splitPaneV.remove(loggingSplitPaneH);
		}
	}
	
	public void toggleSystemMonitor(boolean show) {
		if (show) {
			loggingSplitPaneH.add(systemMonitor);
			loggingSplitPaneH.setDividerLocation(loggingSplitPaneH.getWidth() - this.lastMonitorDividerLocation);
			this.lastMonitorDividerLocation = -1;
		} else {
			this.lastMonitorDividerLocation = loggingSplitPaneH.getWidth() - loggingSplitPaneH.getDividerLocation();
			loggingSplitPaneH.remove(systemMonitor);
		}
	}
	
	public void toggleExpertMode() {
		MainFrame.this.getPropertyTable().toggleExpertMode();
		updateToggleExpertModeIcon();
	}
	
	protected void updateToggleExpertModeIcon() {
		((ToggleExpertModeAction) TOGGLE_EXPERT_MODE_ACTION_24).updateIcon();
		((ToggleExpertModeAction) TOGGLE_EXPERT_MODE_ACTION_32).updateIcon();
	}

	public OperatorPropertyTable getPropertyTable() {
		return mainEditor.getPropertyTable();
	}

	public LoggingViewer getMessageViewer() {
		return messageViewer;
	}

	public OperatorTree getOperatorTree() {
		return mainEditor.getOperatorTree();
	}

    public MainProcessEditor getMainProcessEditor() {
        return this.mainEditor;
    }
    
	public ResultDisplay getResultDisplay() {
		return resultDisplay;
	}

	private synchronized void setProcessState(int state) {
		synchronized (process) {
			process.setProcessState(state);	
		}
	}
	
	public synchronized int getProcessState() {
		if (process == null) {
			return Process.PROCESS_STATE_UNKNOWN;
		} else {
			synchronized (process) {
				return process.getProcessState();
			}
		}
	}
	
	/**
	 * @deprecated Use {@link #getProcess()} instead
	 */
	@Deprecated
	public synchronized final Process getExperiment() {
		return getProcess();
	}

	public synchronized final Process getProcess() {
		return this.process;
	}
	
	// ====================================================
	// M A I N   A C T I O N S
    // ===================================================

	/** Creates a new process. */
	public void newProcess() {
		if (close()) {
			stopProcess();
			resultDisplay.clear();
			setProcess(new Process(), true);
			addToUndoList();
			changeMode(EDIT_MODE);
		}	
	}

	/** Runs or resumes the current process. */
	public void runProcess() {
        if (getProcessState() == Process.PROCESS_STATE_STOPPED) {
            // Run
            if ((isChanged() || (getProcess().getProcessFile() == null)) && !isTutorialMode()) {
            	
				String saveDialogProperty = System.getProperty(PROPERTY_RAPIDMINER_GUI_SAVEDIALOG);
				boolean showSaveDialog = true;
				if (saveDialogProperty != null)
					showSaveDialog = Tools.booleanValue(saveDialogProperty, true);
				
				if (showSaveDialog) {
					RequestSaveDialog saveDialog = new RequestSaveDialog("Save Process?", "Save process before start?");
					saveDialog.setVisible(true);
					if (saveDialog.shouldNotAskAgain()) {
						System.setProperty(PROPERTY_RAPIDMINER_GUI_SAVEDIALOG, "false");
						ParameterService.writePropertyIntoMainUserConfigFile(PROPERTY_RAPIDMINER_GUI_SAVEDIALOG, "false");
					}
					if (saveDialog.isOk()) {
	                    save();						
					}
				}
            }
           
        	setProcessState(Process.PROCESS_STATE_RUNNING);
        	enableActions();
        	
            synchronized (processThread) {
            	synchronized (this.process) {
            		MainFrame.this.process.getRootOperator().clearListeners();
            		processThread = new ProcessThread(MainFrame.this.process);
            		MainFrame.this.process.getRootOperator().addProcessListener(statusBar);
            	}
            }
            
            try {
            	resultDisplay.clear();
            	synchronized (processThread) {
            		processThread.start();
				}
            } catch (Throwable t) {
            	SwingTools.showSimpleErrorMessage("Cannot start process!", t);
            } finally {
            	getOperatorTree().refresh();
            }
        } else {
            // Resume
        	// must be synchronized, otherwise this thread is not owner and resume will not work
        	// due to this synchronization all other accesses to the thread must also be synchronized,
        	// hence, the thread must not be null !
            synchronized (processThread) {
                setProcessState(Process.PROCESS_STATE_RUNNING);
                enableActions();
                processThread.notifyAll();
            }
        }
	}

	/** Can be used to stop the currently running process. Please note that
	 *  the ProcessThread will still be running in the background until the current
	 *  operator is finished. */
	public void stopProcess() {
        if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
            getProcess().getLog().log("Process stopped. Completing current operator...", LogService.INIT);
    		statusBar.setSpecialText("Process stopped. Completing current operator...");
    		if (processThread != null) {
    			synchronized (processThread) {
    				if (processThread.isAlive()) {
    					processThread.setPriority(Thread.MIN_PRIORITY);
    					processThread.stopProcess();
    					processThread.notifyAll();
    				}
    	    		setProcess((Process)MainFrame.this.process.clone(), false);
    	            setProcessState(Process.PROCESS_STATE_STOPPED);
    	    		enableActions();
    				statusBar.processEnded();
    			}
    		}
        }
	}
	
	/** Will be invoked from the process thread after the process was successfully ended. */
	void processEnded(IOContainer results) {
		if (results != null) {
			resultDisplay.setData(results, "Process results:");
			changeMode(RESULTS_MODE);
			resultDisplay.showSomething();
			File file = this.process.getProcessFile();
			String resultName = file != null ? file.getName() : "unnamed";
			RapidMinerGUI.getResultHistory().addResults(resultName, this.process.getRootOperator(), results);
		}
		statusBar.processEnded();

        setProcessState(Process.PROCESS_STATE_STOPPED);
		enableActions();
	}

	public void breakpointReached(Operator operator, IOContainer ioContainer, int location) {
		resultDisplay.setData(ioContainer, "Results in application " + operator.getApplyCount() + " of " + operator.getName() + ":");
		changeMode(RESULTS_MODE);
		resultDisplay.showSomething();
		ProcessThread.beep("breakpoint");
        setProcessState(Process.PROCESS_STATE_PAUSED);
        enableActions();
	}

	/** Since the mainframe toggles the resume itself this method does nothing. */
	public void resume() {}

	/** Sets a new process and registers the MainFrame listener. Please note
	 *  that this method does not invoke {@link #processChanged()}. Do so
	 *  if necessary. 
	 * @deprecated Use {@link #setProcess(Process, boolean)} instead*/
	@Deprecated
	public synchronized void setExperiment(Process process) {
		setProcess(process, true);
	}

	/** Sets a new process and registers the MainFrame listener. Please note
     *  that this method only invoke {@link #processChanged()} if the parameter
     *  newProcess is true. */
	public synchronized void setProcess(Process process, boolean newProcess) {
		if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
			if (processThread != null) {
				synchronized (processThread) {
					processThread.stopProcess();	
				}
			}
		}
		synchronized (process) {
			this.process = process;
			this.process.getRootOperator().clearListeners();
			this.processThread = new ProcessThread(this.process);
			this.process.addBreakpointListener(this);
			this.process.addBreakpointListener(statusBar);
			this.process.getRootOperator().addProcessListener(statusBar);
			if (!newProcess)
				noActualChange = true;
			setOperator(this.process.getRootOperator(), newProcess);
			noActualChange = false;
			enableActions();
		}
		setTitle();
	}

	/**
	 * Sets the root operator for all editors. This method should not be used to
	 * change the process. Please use the method
	 * {@link #setProcess(Process)} for this purpose which also sets some
	 * listeners etc.
	 */
	private void setOperator(Operator root, boolean newProcess) {
		mainEditor.processChanged(root);
	}

	/**
	 * Must be called when the process changed (such that is different from 
     * the process before). Enables the correct actions if the process 
     * can be saved to disk.
	 */
	public void processChanged() {
		if (!noActualChange) {
			boolean oldValue = this.changed;
			this.changed = true;
			addToUndoList();
			if (!oldValue) {
				setTitle();
			}
			synchronized (process) {
				if ((this.process.getProcessFile() != null) && !tutorialMode) {
					this.SAVE_ACTION_24.setEnabled(true);
					this.SAVE_ACTION_32.setEnabled(true);
				}

				// changes the XML text after each process change
				mainEditor.getXMLEditor().processChanged(this.process.getRootOperator());
			}
		}
	}
	
	/** Returns true if the process has changed since the last save. */
	public boolean isChanged() {
		return changed;
	}
	
	/** Notifies the main editor of the change of the currently selected operator. */
	public void notifyEditorsOfChange(Operator currentlySelected) {
		mainEditor.setCurrentOperator(currentlySelected);
	}

	private void addToUndoList() {
		String lastStateXML = undoList.size() != 0 ? (String) undoList.get(undoList.size() - 1) : null;
		String currentStateXML = null;
		synchronized (process) {
			currentStateXML = this.process.getRootOperator().getXML("");	
		}
		if (currentStateXML != null) {
			if ((lastStateXML == null) || (!lastStateXML.equals(currentStateXML))) {
				if (undoIndex < (undoList.size() - 1)) {
					while (undoList.size() > (undoIndex + 1))
						undoList.removeLast();
				}
				undoList.add(currentStateXML);
				String maxSizeProperty = System.getProperty(PROPERTY_RAPIDMINER_GUI_UNDOLIST_SIZE);
				int maxSize = 20;
				try {
					if (maxSizeProperty != null)
						maxSize = Integer.parseInt(maxSizeProperty);
				} catch (NumberFormatException e) {
					LogService.getGlobal().log("Bad integer format for property 'rapidminer.gui.undolist.size', using default size of 20.", LogService.WARNING);
				}
				while (undoList.size() > maxSize)
					undoList.removeFirst();
				undoIndex = undoList.size() - 1;
				enableUndoAction();
			}
		}
	}

	public void undo() {
		if (undoIndex > 0) {
			undoIndex--;
			setProcessIntoStateAt(undoIndex);
		}
		enableUndoAction();
	}

	public void redo() {
		if (undoIndex < undoList.size()) {
			undoIndex++;
			setProcessIntoStateAt(undoIndex);
		}
		enableUndoAction();
	}

	private void enableUndoAction() {
		if (undoIndex > 0) {
			UNDO_ACTION_24.setEnabled(true);
			UNDO_ACTION_32.setEnabled(true);
		} else {
			UNDO_ACTION_24.setEnabled(false);
			UNDO_ACTION_32.setEnabled(false);
		}
		if (undoIndex < undoList.size() - 1){
			REDO_ACTION_24.setEnabled(true);
			REDO_ACTION_32.setEnabled(true);
		} else {
			REDO_ACTION_24.setEnabled(false);
			REDO_ACTION_32.setEnabled(false);
		}
	}
	
	private void setProcessIntoStateAt(int undoIndex) {
		String stateXML = undoList.get(undoIndex);
		try {
			synchronized (process) {
				this.process.setupFromXML(stateXML);
				setProcess(this.process, true);
				// cannot use method processChanged() because this would add the
				// old state to the undo stack!
				this.changed = true;
				setTitle();
				if ((this.process.getProcessFile() != null) && !tutorialMode) {
					this.SAVE_ACTION_24.setEnabled(true);
					this.SAVE_ACTION_32.setEnabled(true);
				}
			}
		} catch (Exception e) {
			SwingTools.showSimpleErrorMessage("While changing process:", e);
		}
	}

	/**
	 * Sets the window title (RapidMiner + filename + an asterisk if process was
	 * modified.
	 */
	private void setTitle() {
		if (hostname == null) {
			try {
	            hostname = "@" + InetAddress.getLocalHost().getHostName();
	        } catch (UnknownHostException e) {
	        	hostname = "";
	        }	
		}
		
		File file = null;
		if (this.process != null) {
			synchronized (process) {
				file = this.process.getProcessFile();	
			}
		}
		if (file != null) {
			setTitle(TITLE + hostname + " (" + file.getName() + (changed ? "*" : "") + ")");
		} else {
			setTitle(TITLE + hostname);
		}
	}

	public String getBaseName() {
		if (this.process != null) {
			File file = null;
			synchronized (process) {
				file = this.process.getProcessFile();	
			}
			if (file != null) {
				String base = file.getName();
				int dot = base.lastIndexOf(".");
				if (dot == -1)
					return base;
				else
					return base.substring(0, dot);
			} else {
				return "unnamed";
			}
		} else {
			return "unnamed";
		}
	}

	// //////////////////// File menu actions ////////////////////

	private boolean close() {
		if (changed) {
			File file = null;
			synchronized (process) {
				file = this.process.getProcessFile();
			}
			if (file == null) {
				file = new File("unnamed.xml");
			}
			int choice = JOptionPane.showConfirmDialog(this, "Save changes to '" + file + "'?", "Save changes", JOptionPane.YES_NO_CANCEL_OPTION);
			switch (choice) {
				case JOptionPane.YES_OPTION:
					save();
					return true;
				case JOptionPane.NO_OPTION:
					if (getProcessState() != Process.PROCESS_STATE_STOPPED) {
						synchronized (processThread) {
							processThread.stopProcess();	
						}
					}
					return true;
				default: // cancel
					return false;
			}
		} else {
			return true;
		}
	}

	public void open() {
		if (close()) {
			File file = SwingTools.chooseFile(MainFrame.this, null, true, "xml", "process file");
			if (file == null) {
				return;
			}
			open(file);
		}
	}

	public void open(File file) {
		open(file, true);
	}

	public void open(File file, boolean showInfo) {
		stopProcess();
		try {
			Process process = RapidMiner.readProcessFile(file);
			setProcess(process, true);
			changeMode(EDIT_MODE);
		} catch (XMLException ex) {
			SwingTools.showSimpleErrorMessage("While loading '" + file + "'", ex);
			Process process = new Process();
			process.setProcessFile(file);
			setProcess(process, true);
			changeMode(EDIT_MODE);
			mainEditor.changeToXMLEditor();
			try {
				mainEditor.getXMLEditor().setText(Tools.readOutput(new BufferedReader(new FileReader(file))));
			} catch (FileNotFoundException e) {
				SwingTools.showSimpleErrorMessage("While loading '" + file + "'", e);
				return;
			} catch (IOException e) {
				SwingTools.showSimpleErrorMessage("While loading '" + file + "'", e);
				return;
			}
		} catch (InstantiationException e) {
			SwingTools.showSimpleErrorMessage("While loading '" + file + "'", e);
			return;
		} catch (IllegalAccessException e) {
			SwingTools.showSimpleErrorMessage("While loading '" + file + "'", e);
			return;
		} catch (IOException e) {
			SwingTools.showSimpleErrorMessage("While loading '" + file + "'", e);
			return;
		}

		resultDisplay.clear();
		SAVE_ACTION_24.setEnabled(false);
		SAVE_ACTION_32.setEnabled(false);
		changed = false;
		synchronized (process) {
			RapidMinerGUI.useProcessFile(this.process);	
			updateRecentFileList();
			addToUndoList();
			setTitle();
			if (showInfo && Tools.booleanValue(System.getProperty(PROPERTY_RAPIDMINER_GUI_PROCESSINFO_SHOW), true)) {
				String text = this.process.getRootOperator().getUserDescription();
				if ((text != null) && (text.length() != 0)) {
					ProcessInfoScreen infoScreen = new ProcessInfoScreen(this, file.getName(), text);
					infoScreen.setVisible(true);
				}
			}
		}
	}

	public void save() {
		try {
			synchronized (process) {
				File file = this.process.getProcessFile();
				if (file == null) {
					file = SwingTools.chooseFile(this, new File("."), false, "xml", "process file");
					if (file == null)
						return;
					else
						this.process.setProcessFile(file);
				}
				this.process.save();
				SAVE_ACTION_24.setEnabled(false);
				SAVE_ACTION_32.setEnabled(false);
				changed = false;
				setTitle();
				RapidMinerGUI.useProcessFile(this.process);
				updateRecentFileList();
				//mainEditor.processChanged(this.process.getRootOperator());
			}
		} catch (IOException ex) {
			SwingTools.showSimpleErrorMessage("Cannot save process file!", ex);
		}
	}

	public void saveAs() {
		File file = SwingTools.chooseFile(MainFrame.this, null, false, "xml", "process file");
		if (file != null) {
			getProcess().setProcessFile(file);
			save();
		}	
	}
	
	public void saveAsTemplate() {
		synchronized (process) {
			Operator rootOperator = MainFrame.this.process.getRootOperator();
			SaveAsTemplateDialog dialog = new SaveAsTemplateDialog(MainFrame.this, rootOperator);
			dialog.setVisible(true);
			if (dialog.isOk()) {
				Template template = dialog.getTemplate(rootOperator);
				String name = template.getName();
				try {
					File templateFile = ParameterService.getUserConfigFile(name + ".template");
					template.save(templateFile);
					File templateXmlFile = ParameterService.getUserConfigFile(name + ".xml");
					MainFrame.this.process.save(templateXmlFile);
				} catch (IOException ioe) {
					SwingTools.showSimpleErrorMessage("Cannot write template files:", ioe);
				}
			}
		}
	}
	
	public void exit() {
		if (changed) {
			File file = null;
			synchronized (process) {
				file = this.process.getProcessFile();
			}
			if (file == null) {
				file = new File("unnamed.xml");
			}
			switch (JOptionPane.showConfirmDialog(this, "Save changes to '" + file + "'?", "Save changes", JOptionPane.YES_NO_CANCEL_OPTION)) {
				case JOptionPane.YES_OPTION:
					save();
					if (changed)
						return;
					break;
				case JOptionPane.NO_OPTION:
					break;
				case JOptionPane.CANCEL_OPTION:
				default:
					return;
			}
		} else if (JOptionPane.showConfirmDialog(this, "Really exit?", "Exit RapidMiner", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
			return;
		}
		stopProcess();
		dispose();
		RapidMinerGUI.quit(0);
		//System.exit(0);
	}

	/** Updates the list of recently used files. */
	public void updateRecentFileList() {
		recentFilesMenu.removeAll();
		List recentFiles = RapidMinerGUI.getRecentFiles();
		Iterator i = recentFiles.iterator();
		int j = 1;
		while (i.hasNext()) {
			final File recentFile = (File) i.next();
			JMenuItem menuItem = new JMenuItem(j + " " + recentFile.getPath());
			menuItem.setMnemonic('0' + j);
			menuItem.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					open(recentFile);
				}
			});
			recentFilesMenu.add(menuItem);
			j++;
		}
	}

	public void windowOpened(WindowEvent e) {}

	public void windowClosing(WindowEvent e) {
		exit();
	}

	public void windowClosed(WindowEvent e) {}

	public void windowIconified(WindowEvent e) {}

	public void windowDeiconified(WindowEvent e) {}

	public void windowActivated(WindowEvent e) {}

	public void windowDeactivated(WindowEvent e) {}

	/**
	 * Enables and disables all actions according to the current state
	 * (process running, operator selected...
	 */
	public void enableActions() {
		// must be invoked first since it might be that conditions change
		RUN_RESUME_ACTION_24.updateState();
		RUN_RESUME_ACTION_32.updateState();
		
		boolean[] currentStates = new boolean[ConditionalAction.NUMBER_OF_CONDITIONS];
		Operator op = mainEditor.getOperatorTree().getSelectedOperator();
		if (op != null) {
			currentStates[ConditionalAction.OPERATOR_SELECTED] = true;
			if (op instanceof OperatorChain)
				currentStates[ConditionalAction.OPERATOR_CHAIN_SELECTED] = true;
			if (op.getParent() == null)
				currentStates[ConditionalAction.ROOT_SELECTED] = true;
			else if (op.getParent().getNumberOfOperators() > 1)
				currentStates[ConditionalAction.SIBLINGS_EXIST] = true;
		}
		// important: if whole method is synchronized this will lead to dead locks...
		synchronized (process) {
			currentStates[ConditionalAction.PROCESS_STOPPED] = getProcessState() == Process.PROCESS_STATE_STOPPED;
			currentStates[ConditionalAction.PROCESS_PAUSED] = getProcessState() == Process.PROCESS_STATE_PAUSED;
			currentStates[ConditionalAction.PROCESS_RUNNING] = ((getProcessState() == Process.PROCESS_STATE_RUNNING) || (getProcessState() == Process.PROCESS_STATE_PAUSED));
		}
		currentStates[ConditionalAction.CLIPBOARD_FILLED] = mainEditor.getOperatorTree().getClipBoard() != null;
		currentStates[ConditionalAction.XML_VIEW] = mainEditor.isXMLViewActive();
		currentStates[ConditionalAction.DESCRIPTION_VIEW] = mainEditor.isDescriptionViewActive();
		ConditionalAction.updateAll(currentStates);
	}
}
