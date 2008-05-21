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
package com.rapidminer.gui.wizards;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.rapidminer.RapidMiner;
import com.rapidminer.example.Attributes;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.JDBCDriverTable;
import com.rapidminer.gui.tools.ProgressMonitor;
import com.rapidminer.gui.tools.ProgressUtils;
import com.rapidminer.gui.tools.SQLEditor;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.io.DatabaseExampleSource;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.jdbc.ColumnIdentifier;
import com.rapidminer.tools.jdbc.DatabaseHandler;
import com.rapidminer.tools.jdbc.DatabaseService;
import com.rapidminer.tools.jdbc.DriverInfo;
import com.rapidminer.tools.jdbc.JDBCProperties;


/**
 * This class is the creator for wizard dialogs defining the configuration for 
 * {@link DatabaseExampleSource} operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: DBExampleSourceConfigurationWizard.java,v 1.8 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class DBExampleSourceConfigurationWizard extends AbstractConfigurationWizard {
    
    private static final long serialVersionUID = 5127262335077061590L;

	private static final String USER_DEFINED_STRING = "User Defined URL (next step)";
    
    private static final int STEP_USER_DATA    = 2;
    private static final int STEP_TABLE_SELECTION = 3;
    private static final int STEP_TYPE_DEFINITION = 4;

    /** The database handler. */
    private transient DatabaseHandler handler = null;
    
    /** The latest JDBC properties. */
    private transient JDBCProperties properties = null;
    
    /** Indicates if the handler is currently connected. */
    private boolean isConnected = false;
    
    /** All attribute names for the available tables. */
    Map<String, List<ColumnIdentifier>> attributeNameMap = new LinkedHashMap<String, List<ColumnIdentifier>>();
    
    /** This combo box contains all available database drivers. */
    private JComboBox systemComboBox;

    /** This field contains the server name or IP. */
    private JTextField serverField = new JTextField(40);
    
    /** This field contains the complete url string for the defined driver. */
    private JTextField urlField = new JTextField(40);
    
    /** This field contains the database name. */
    private JTextField databaseNameField = new JTextField(40);

    /** This field contains the user name. */
    private JTextField userNameField = new JTextField(40);
    
    /** This field contains the password. */
    private JPasswordField passwordField = new JPasswordField(40);

    /** Remembers the password during connections. */
    private String password = null;
    
    /** Indicates if the password was defined in the text field (and hence should be set as operator parameter). */
    private boolean passwordFromTextField = false;
    
    /** The list with all tables. */
    private JList tableList = new JList();
    
    /** The list with all attribute names. */
    private JList attributeList = new JList();
    
    /** The text area with the where clause. */
    private JTextArea whereTextArea = new JTextArea(4, 20);

    /** The text area with the where clause. */
    private SQLEditor sqlQueryTextArea = new SQLEditor();
    
    /** This map contains all information for the attribute types. */
    private Map<ColumnIdentifier, String> attributeTypeMap = new HashMap<ColumnIdentifier, String>(); 
    
    /** The definition of all attribute types. */
    private DBExampleSourceConfigurationWizardDataTable dataView = new DBExampleSourceConfigurationWizardDataTable();

    
    /** Creates a new wizard. */
    public DBExampleSourceConfigurationWizard(ConfigurationListener listener) {
        super("Database Example Source Wizard", listener);
        
        // add all steps
        addTitleStep();
        addDBSystemSelectionStep();
        addUserDataStep();
        addTableSelectionStep();
        addSpecialAttributesStep();
        
        updateSystemSelection();
    }

    private void addTitleStep() {
        StringBuffer titleString = new StringBuffer();
        titleString.append("This wizard will guide you through the process of data loading from databases. Using this wizard will involve the following steps:" + 
                "<ul>" + 
                "<li>Selection of a database</li>" +
                "<li>Definition of the username and password</li>" + 
                "<li>Selection of tables and attributes (SQL query)</li>" + 
                "<li>Definition of special attributes like labels or IDs</li>" +
                "</ul>");
        titleString.append("<br>The currently available JDBC drivers are listed below. Please make sure to copy missing drivers into the directory lib/jdbc and restart RapidMiner in order to make additional drivers available.");
        
        JPanel panel = SwingTools.createTextPanel("Welcome to the Database Example Source Wizard", titleString.toString());
        DriverInfo[] drivers = DatabaseService.getAllDriverInfos();
        JDBCDriverTable driverTable = new JDBCDriverTable(drivers);
        panel.add(new JScrollPane(driverTable), BorderLayout.CENTER);
        addStep(panel);
    }

    private void addDBSystemSelectionStep() {
        JPanel panel = SwingTools.createTextPanel("Please specify your database system...", "Please specify your database system. If your system is not available, you can select \"" + USER_DEFINED_STRING + "\" and define an appropriate connection URL for your system in the next step. If the connection fails because no suitable driver is available, you might copy a driver library into the directory lib/jdbc and it will be available after the next start of RapidMiner.");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);        

        // database system
        JLabel label = new JLabel("Database System:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        String[] knownNames = DatabaseService.getDBSystemNames();
        String[] names = new String[knownNames.length + 1];
        System.arraycopy(knownNames, 0, names, 0, knownNames.length);
        names[names.length - 1] = USER_DEFINED_STRING;
        systemComboBox = new JComboBox(names);
        systemComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                updateSystemSelection();
            }
        });
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(systemComboBox, c);
        content.add(systemComboBox);

        // database server or IP
        label = new JLabel("Server Name or IP:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(serverField, c);
        content.add(serverField);

        // database name
        label = new JLabel("Database Name:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(databaseNameField, c);
        content.add(databaseNameField);
                
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }

    private void addUserDataStep() {
        JPanel panel = SwingTools.createTextPanel("Please specify the connection data...", "Please check the connection URL and adapt it if necessary (or define it in cases where your database system was not available in the dialog before). Please specify the user name and the password. If the password field remains empty you will be prompted for it during connections. You can validate the connection to the database by pressing the \"Test Connection\" button. Please note that after pressing \"Next\" a connection to your database will be made in order to retrieve table and attribute names (this may take some minutes).");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);        
        
        // url
        JLabel label = new JLabel("URL:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(urlField, c);
        content.add(urlField);

        // fill panel
        JPanel fillPanel = new JPanel();
        layout.setConstraints(fillPanel, c);
        content.add(fillPanel);
        
        // user
        label = new JLabel("User:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(userNameField, c);
        content.add(userNameField);

        // password
        label = new JLabel("Password:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(passwordField, c);
        content.add(passwordField);
        
        // test connection
        JPanel testButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton testButton = new JButton("Test Connection");
        testButton.setToolTipText("Tests the connection to the database based on the current settings.");
        testButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                testConnection();
            }
        });
        testButtonPanel.add(testButton);
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(testButtonPanel, c);
        content.add(testButtonPanel);
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }

    private void addTableSelectionStep() {
        JPanel panel = SwingTools.createTextPanel("Please select tables and attributes...", "Please specify the tables, the attributes and an optional where clause which will be used to create a query statement to retrieve the data. You can modify the statement in the text field below.");

            
        JPanel gridPanel = new JPanel(new GridLayout(1, 3));
        // table and attribute lists, where clause text area
        tableList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateAttributeNames();
                updateSQLQuery();
            }
        });
        JScrollPane tablePane = new ExtendedJScrollPane(tableList);
        tablePane.setBorder(BorderFactory.createTitledBorder("Tables"));
        gridPanel.add(tablePane);

        attributeList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                updateSQLQuery();
            }
        });
        JScrollPane attributePane = new ExtendedJScrollPane(attributeList);
        attributePane.setBorder(BorderFactory.createTitledBorder("Attributes"));
        gridPanel.add(attributePane);
        
        whereTextArea.addKeyListener(new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent e) {
                updateSQLQuery();
            }
        });
        JScrollPane whereTextPane = new ExtendedJScrollPane(whereTextArea);
        whereTextPane.setBorder(BorderFactory.createTitledBorder("Where Clause"));
        gridPanel.add(whereTextPane);
        
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        JPanel content = new JPanel(layout); 
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1;
        c.weighty = 0.5;
        c.insets = new Insets(7,7,7,7);
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(gridPanel, c);
        content.add(gridPanel);
        
        
        // SQL statement field
        c.weighty = 1.0d;
        sqlQueryTextArea.setBorder(BorderFactory.createTitledBorder("SQL Query"));
        layout.setConstraints(sqlQueryTextArea, c);
        content.add(sqlQueryTextArea);
                
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }

    private void addSpecialAttributesStep() {
        JPanel panel = SwingTools.createTextPanel("Please define special attributes...", "Please define which columns should be used as special attributes like labels or Ids (if any). After pressing finish the necessary settings will be made for the operator.");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0d;
        c.weighty = 1.0d;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);        
        
        JScrollPane typeViewPane = new ExtendedJScrollPane(dataView);
        typeViewPane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(7,7,7,7), BorderFactory.createTitledBorder("Data and Attribute Types View (Sample)")));
        layout.setConstraints(typeViewPane, c);
        content.add(typeViewPane);   
        
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }
    
    protected void performStepAction(int currentStep, int oldStep) {
        if (currentStep == STEP_USER_DATA) {
            this.password = null;
            if (systemComboBox.getSelectedIndex() < DatabaseService.getJDBCProperties().size()) {
                urlField.setText(createPredefinedDatabaseURL());
            }
        } else if (currentStep == STEP_TABLE_SELECTION) {
        	if (oldStep < currentStep) {
        		try {
        			retrieveTableAndAttributeNames();
        		} catch (SQLException e) {
        			showConnectionError("Cannot retrieve table and attribute names", e);
        		}
        	}
        } else if (currentStep == STEP_TYPE_DEFINITION) {
            try {
                updateDataView();
            } catch (SQLException e) {
                showConnectionError("Cannot retrieve sample data", e);                
            }
        }
    }
    
    private void updateSystemSelection() {
        if (systemComboBox.getSelectedIndex() >= DatabaseService.getJDBCProperties().size()) {
            serverField.setEnabled(false);
            databaseNameField.setEnabled(false);
            JDBCProperties defaultProps = JDBCProperties.createDefaultJDBCProperties();
            String defaultString = defaultProps.getUrlPrefix() + "server" + ":port" + defaultProps.getDbNameSeperator() + "database_name";
            urlField.setText(defaultString);
        } else {
            serverField.setEnabled(true);
            databaseNameField.setEnabled(true);
        }
    }
    
    private String getDatabaseURL() {
        return urlField.getText().trim();
    }

    private String createPredefinedDatabaseURL() {
        int index = systemComboBox.getSelectedIndex(); 
        String serverName = serverField.getText().trim();
        if ((serverName == null) || (serverName.length() == 0)) {
            SwingTools.showVerySimpleErrorMessage("Please specify a database server name or IP!");
            return "";
        }
        String databaseName = databaseNameField.getText().trim();
        if ((databaseName == null) || (databaseName.length() == 0)) {
            SwingTools.showVerySimpleErrorMessage("Please specify a database name!");
            return "";
        }
        String portString = "";
        String defaultPort = DatabaseService.getJDBCProperties().get(index).getDefaultPort();
        if (defaultPort.length() > 0) {
            portString = ":" + defaultPort;
        }
        
        return DatabaseService.getJDBCProperties().get(index).getUrlPrefix() + serverName + portString + DatabaseService.getJDBCProperties().get(index).getDbNameSeperator() + databaseName;        
    }
    
    private void testConnection() {
        try {
            if (!connect())
            	throw new SQLException("Connection was not possible!");
            disconnect();
            JOptionPane.showMessageDialog(this, "Connection to database is possible.", "Connection OK", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            showConnectionError(null, e);
        }
    }
    
    /** Returns true if the connection was possible and false otherwise. */
    private boolean connect() throws SQLException {
        String urlString = getDatabaseURL();
        if ((urlString != null) && (urlString.length() > 0)) {
            if (isConnected) {
                this.handler.disconnect();
                this.handler = null;
            }
            int selectedSystem = systemComboBox.getSelectedIndex();
            JDBCProperties jdbcProperties = JDBCProperties.createDefaultJDBCProperties();
            if ((selectedSystem >= 0) && (selectedSystem < DatabaseService.getJDBCProperties().size() - 1)) {
            	jdbcProperties = DatabaseService.getJDBCProperties().get(systemComboBox.getSelectedIndex());
            }
            this.handler = new DatabaseHandler(urlString, jdbcProperties);
            this.properties = this.handler.getProperties();
            String userName = null;
            String passwd = null;
            // "hack" to allow convenient MS SQL authentication
            if (urlString.indexOf("AuthenticationMethod") < 0) {
            	userName = userNameField.getText().trim();
            	if ((userName == null) || (userName.length() == 0)) {
            		SwingTools.showVerySimpleErrorMessage("Please specify a user name!");
            		return false;
            	}

            	passwd = this.password;
            	if (passwd == null) {
            		passwd = new String(passwordField.getPassword());
            		if ((passwd == null) || (passwd.length() == 0)) {
            			passwd = RapidMiner.getInputHandler().inputPassword("Password for user '" + userName + "' required:");
            			this.passwordFromTextField = false;
            		} else {
            			this.passwordFromTextField = true;
            		}
            	}
            }
            this.handler.connect(userName, passwd, true);
            this.password = passwd;
            this.isConnected = true;
            return true;
        } else {
            SwingTools.showVerySimpleErrorMessage("Please specify the necessary connection data!");
            return false;
        }
    }
    
    private void disconnect() throws SQLException {
        if (isConnected) {
            this.handler.disconnect();
            this.handler = null;
            this.isConnected = false;
        }
    }
    
    private void retrieveTableAndAttributeNames() throws SQLException {
    	// first connect (password) then retrieval with (modal!) progress dialog...        
        connect();
        
        Thread retrieveTablesThread = new Thread() { 
            public void run() { 
                ProgressMonitor monitor = ProgressUtils.createModalProgressMonitor(DBExampleSourceConfigurationWizard.this, 100, true, 50, true); 
                monitor.start("Fetching tables and attributes from database..."); 
                try {
                	// retrieve data
                    attributeNameMap.clear();
                    if (handler != null) {
                        Map<String, List<ColumnIdentifier>> newAttributeMap;
						try {
							newAttributeMap = handler.getAllTableMetaData();
	                        attributeNameMap.putAll(newAttributeMap);
						} catch (SQLException e) {
							showSQLError("Retrieval of table and attribute names failed", e);
						}
                    }
                    
                    // set table name list data
                    String[] allNames = new String[attributeNameMap.size()];
                    attributeNameMap.keySet().toArray(allNames);
                    tableList.removeAll();
                    tableList.setListData(allNames);
                } finally { 
                    // to ensure that progress monitor is closed in case of any exception 
                    if(monitor.getCurrent() != monitor.getTotal()) 
                        monitor.setCurrent(null, monitor.getTotal()); 
                    
                    // disconnect
                    try {
						disconnect();
					} catch (SQLException e) {
						showSQLError("Disconnecting from the database failed", e);
					}
                }
            } 
        };
        retrieveTablesThread.start();
    }
    
    private void updateAttributeNames() {
        //boolean singleTable = tableList.getSelectedValues().length == 1;
        List<ColumnIdentifier> allColumnIdentifiers = new LinkedList<ColumnIdentifier>();
        Object[] selection = tableList.getSelectedValues();
        for (Object o : selection) {
            String tableName = (String)o;
            List<ColumnIdentifier> attributeNames = this.attributeNameMap.get(tableName);
            if (attributeNames != null) {
                Iterator<ColumnIdentifier> i = attributeNames.iterator();
                while (i.hasNext()) {
                    ColumnIdentifier currentIdentifier = i.next();
                    allColumnIdentifiers.add(currentIdentifier);
                }
            }
        }
        attributeList.removeAll();
        ColumnIdentifier[] identifierArray = new ColumnIdentifier[allColumnIdentifiers.size()];
        allColumnIdentifiers.toArray(identifierArray);
        attributeList.setListData(identifierArray);
    }
    
    private void updateDataView() throws SQLException {
        // update attribute types
        attributeTypeMap.clear();
        Object[] attributeSelection = attributeList.getSelectedValues();
        ColumnIdentifier[] usedAttributes = null;
        if (attributeSelection.length == 0) {
            // use all
            usedAttributes = new ColumnIdentifier[attributeList.getModel().getSize()];
            for (int i = 0; i < attributeList.getModel().getSize(); i++) {
                ColumnIdentifier currentColumn = (ColumnIdentifier)attributeList.getModel().getElementAt(i);
                usedAttributes[i] = currentColumn;
                attributeTypeMap.put(currentColumn, Attributes.ATTRIBUTE_NAME);
            }
        } else {
            // use only selected
            usedAttributes = new ColumnIdentifier[attributeSelection.length];
            for (int i = 0; i < attributeSelection.length; i++) {
                ColumnIdentifier currentColumn = (ColumnIdentifier)attributeSelection[i];
                usedAttributes[i] = currentColumn;
                attributeTypeMap.put(currentColumn, Attributes.ATTRIBUTE_NAME);
            }
        }
        // update data sample
        List<String[]> data = new LinkedList<String[]>();
        connect();
        if (this.handler != null) {
        	Statement statement = this.handler.createStatement();
            ResultSet resultSet = statement.executeQuery(getQueryString());
            int counter = 0;
            while ((resultSet.next()) && (counter < 10)) {
                String[] row = new String[usedAttributes.length];
                for (int c = 0; c < row.length; c++)
                    row[c] = resultSet.getString(c+1);
                data.add(row);
                counter++;
            }
            statement.close();
        }
        disconnect();
        dataView.update(usedAttributes, data, attributeTypeMap);
    }
    
    private void appendAttributeName(StringBuffer result, ColumnIdentifier identifier, boolean first, boolean singleTable) {
        if (!first) {
            result.append(", ");
        }
        if (singleTable) {
            result.append(identifier.getFullName(properties, singleTable));
        } else {
            result.append(identifier.getFullName(properties, singleTable) + " AS " + identifier.getAliasName(properties, singleTable));
        }        
    }
    
    private void updateSQLQuery() {
        Object[] tableSelection = tableList.getSelectedValues();
        if (tableSelection.length == 0) {
            sqlQueryTextArea.setText("");
            return;
        }
        
        boolean singleTable = tableSelection.length == 1;
        
        // SELECT
        StringBuffer result = new StringBuffer("SELECT ");
        Object[] attributeSelection = attributeList.getSelectedValues();
        if (singleTable && (((attributeSelection.length == 0) || (attributeSelection.length == attributeList.getModel().getSize())))) {
            result.append("*");
        } else {
            if ((attributeSelection.length == 0) || (attributeSelection.length == attributeList.getModel().getSize())) {
                boolean first = true;
                for (int i = 0; i < attributeList.getModel().getSize(); i++) {
                    ColumnIdentifier identifier = (ColumnIdentifier)attributeList.getModel().getElementAt(i);
                    appendAttributeName(result, identifier, first, singleTable);
                    first = false;
                }                
            } else {
                boolean first = true;
                for (Object o : attributeSelection) {
                	ColumnIdentifier identifier = (ColumnIdentifier)o;
                    appendAttributeName(result, identifier, first, singleTable);
                    first = false;
                }
            }
        }
        
        // FROM
        result.append(Tools.getLineSeparator() + "FROM ");
        boolean first = true;
        for (Object o : tableSelection) {
            if (first) {
                first = false;
            } else {
                result.append(", ");
            }
            String tableName = (String)o;
            result.append(properties.getIdentifierQuoteOpen() + tableName + properties.getIdentifierQuoteClose());
        }
        
        // WHERE
        String whereText = whereTextArea.getText().trim();
        if (whereText.length() > 0) {
            result.append(Tools.getLineSeparator() + "WHERE " + whereText);
        }
        sqlQueryTextArea.setText(result.toString());
    }
    
    private String getQueryString() {
        String result = sqlQueryTextArea.getText().trim();
        result = result.replaceAll(Tools.getLineSeparator(), " ");
        return result;
    }
    
    private void showConnectionError(String message, SQLException e) {
        JOptionPane.showMessageDialog(this, (message != null ? (message + ": ") : "") + "Connection to database has failed:" + Tools.getLineSeparator() + e.getMessage().substring(0, Math.min(300, e.getMessage().length())) + "...", "Connection failed", JOptionPane.ERROR_MESSAGE);        
    }

    private void showSQLError(String message, SQLException e) {
        JOptionPane.showMessageDialog(this, (message != null ? (message + ": ") : "") + e.getMessage(), "SQL Error", JOptionPane.ERROR_MESSAGE);        
    }
    
    protected void finish(ConfigurationListener listener) {
        try {
            disconnect();
        } catch (SQLException e) {}

        String sqlQueryString = getQueryString();
        String databaseURL = urlField.getText().trim();
        String userName = userNameField.getText().trim();
        
        // sanity checks
        if ((databaseURL.length() == 0) || (sqlQueryString.length() == 0) || (userName.length() == 0)) {
            SwingTools.showVerySimpleErrorMessage("You must specify a database connection and proper settings - the operator will not work without this." + Tools.getLineSeparator() + "Please select \"Cancel\" if you want to abort this wizard.");
        } else {
            // everything is OK --> database parameters
            Parameters parameters = listener.getParameters();
            parameters.setParameter("work_on_database", "false");
            parameters.setParameter("database_system", systemComboBox.getSelectedIndex() + "");
            parameters.setParameter("database_url", databaseURL);
            parameters.setParameter("username", userName);
            parameters.setParameterWithoutCheck("password", null);
            if (passwordFromTextField) {
                parameters.setParameterWithoutCheck("password", new String(passwordField.getPassword()));
            }

            // query string
            parameters.setParameter("query", sqlQueryString);
            parameters.setParameterWithoutCheck("query_file", null);
            parameters.setParameterWithoutCheck("table_name", null);
            
            // special attributes
            parameters.setParameterWithoutCheck("label_attribute", null);
            parameters.setParameterWithoutCheck("id_attribute", null);
            parameters.setParameterWithoutCheck("weight_attribute", null);
            for (int i = 1; i < Attributes.KNOWN_ATTRIBUTE_TYPES.length; i++)
                ensureAttributeTypeIsUnique(Attributes.KNOWN_ATTRIBUTE_TYPES[i]);

            boolean singleTable = tableList.getSelectedValues().length == 1;
            Iterator<ColumnIdentifier> i = attributeTypeMap.keySet().iterator();
            while (i.hasNext()) {
                ColumnIdentifier attributeIdentifier = i.next();
                String attType = attributeTypeMap.get(attributeIdentifier);
                String maskedAttributeName = attributeIdentifier.getAliasName(properties, singleTable);
                maskedAttributeName = maskedAttributeName.substring(1, maskedAttributeName.length() - 1);
                if (attType.equals(Attributes.LABEL_NAME)) {
                    parameters.setParameter("label_attribute", maskedAttributeName);        
                } else if (attType.equals(Attributes.ID_NAME)) {
                    parameters.setParameter("id_attribute", maskedAttributeName);  
                } else if (attType.equals(Attributes.WEIGHT_NAME)) {
                    parameters.setParameter("weight_attribute", maskedAttributeName);        
                } 
            }
            
            listener.setParameters(parameters);
            dispose();
            RapidMinerGUI.getMainFrame().getPropertyTable().refresh();
        }
    }
    
    private void ensureAttributeTypeIsUnique(String type) {
        List<ColumnIdentifier> columns = new LinkedList<ColumnIdentifier>();
        List<Integer> columnNumbers = new LinkedList<Integer>();
        Iterator<ColumnIdentifier> i = attributeTypeMap.keySet().iterator();
        int j = 0;
        while (i.hasNext()) {
            ColumnIdentifier attributeIdentifier = i.next(); 
            String attType = attributeTypeMap.get(attributeIdentifier);
            if (attType.equals(type)) {
                columns.add(attributeIdentifier);
                columnNumbers.add(j);
            }
            j++;
        }
        if (columns.size() > 1) {
            ColumnIdentifier[] identifiers = new ColumnIdentifier[columns.size()];
            columns.toArray(identifiers);
            javax.swing.JTextArea message = new javax.swing.JTextArea("The special attribute " + type + " is multiply defined. Please select one of the data columns (others will be changed to regular attributes). Press \"Cancel\" to ignore.", 4, 40);
            message.setEditable(false);
            message.setLineWrap(true);
            message.setWrapStyleWord(true);
            message.setBackground(new javax.swing.JLabel("").getBackground());
            ColumnIdentifier selection = (ColumnIdentifier) JOptionPane.showInputDialog(this, message, type + " multiply defined", JOptionPane.WARNING_MESSAGE, null, identifiers, identifiers[0]);
            if (selection != null) {
                i = columns.iterator();
                while (i.hasNext()) {
                    ColumnIdentifier name = i.next();
                    if (!name.equals(selection)) {
                        attributeTypeMap.remove(name);
                        attributeTypeMap.put(name, Attributes.ATTRIBUTE_NAME);
                    }
                }
            }
        }
    }
    
    protected void cancel() {
        try {
            disconnect();
        } catch (SQLException e) {
            LogService.getGlobal().log("Problem during disconnecting: " + e.getMessage(), LogService.WARNING);
        }
        super.cancel();
    }
    
    public void createConfigurationWizard(ConfigurationListener listener) {
        (new DBExampleSourceConfigurationWizard(listener)).setVisible(true);
    }
}
