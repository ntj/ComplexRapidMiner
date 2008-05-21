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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import com.rapidminer.RapidMiner;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.tools.JDBCDriverTable;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.io.DatabaseExampleSetWriter;
import com.rapidminer.parameter.Parameters;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;
import com.rapidminer.tools.jdbc.DatabaseHandler;
import com.rapidminer.tools.jdbc.DatabaseService;
import com.rapidminer.tools.jdbc.DriverInfo;


/**
 * This class is the creator for wizard dialogs defining the configuration for 
 * {@link DatabaseExampleSetWriter} operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: DBExampleSetWriterConfigurationWizard.java,v 1.6 2008/05/09 19:22:56 ingomierswa Exp $
 */
public class DBExampleSetWriterConfigurationWizard extends AbstractConfigurationWizard {
    
    private static final long serialVersionUID = 5127262335077061590L;

	private static final String USER_DEFINED_STRING = "User Defined URL (next step)";
    
    private static final int STEP_USER_DATA    = 2;

    /** The database handler. */
    private transient DatabaseHandler handler = null;
    
    /** Indicates if the handler is currently connected. */
    private boolean isConnected = false;
    
    /** All attribute names for the available tables. */
    Map<String, List<String>> attributeNameMap = new LinkedHashMap<String, List<String>>();
    
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
    
    /** This field contains the table name. */
    private JTextField tableNameField = new JTextField(40);
    
    /** This field contains the password. */
    private JPasswordField passwordField = new JPasswordField(40);

    /** Remembers the password during connections. */
    private String password = null;
    
    
    /** Creates a new wizard. */
    public DBExampleSetWriterConfigurationWizard(ConfigurationListener listener) {
        super("Database Example Set Writer Wizard", listener);
        
        // add all steps
        addTitleStep();
        addDBSystemSelectionStep();
        addUserDataStep();
        addTableSelectionStep();
        
        updateSystemSelection();
    }

    private void addTitleStep() {
        StringBuffer titleString = new StringBuffer();
        titleString.append("This wizard will guide you through the process of writing data into databases. Using this wizard will involve the following steps:" + 
                "<ul>" + 
                "<li>Selection of a database</li>" +
                "<li>Definition of the username and password</li>" + 
                "<li>Definition of a table name</li>" + 
                "</ul>");
        titleString.append("<br>The currently available JDBC drivers are listed below. Please make sure to copy missing drivers into the directory lib/jdbc and restart RapidMiner in order to make additional drivers available.");

        JPanel panel = SwingTools.createTextPanel("Welcome to the Database Example Set Writer Wizard", titleString.toString());
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
        JPanel panel = SwingTools.createTextPanel("Please specify the connection data...", "Please check the connection URL and adapt it if necessary (or define it in cases where your database system was not available in the dialog before). Please specify the user name and the password. If the password field remains empty you will be prompted for it during connections. You can validate the connection to the database by pressing the \"Test Connection\" button.");

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
        JPanel panel = SwingTools.createTextPanel("Please define a table name.", "Please specify the table into which the data should be written.");

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 0;
        c.weighty = 0;
        c.insets = new Insets(7,7,7,7);
        JPanel content = new JPanel(layout);        

        // table name
        JLabel label = new JLabel("Table Name:");
        c.weightx = 0.0d;
        c.gridwidth = GridBagConstraints.RELATIVE;
        layout.setConstraints(label, c);
        content.add(label);
        
        c.weightx = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(tableNameField, c);
        content.add(tableNameField);
        
        panel.add(content, BorderLayout.CENTER);
        addStep(panel);
    }
    
    protected void performStepAction(int currentStep, int oldStep) {        
        if (currentStep == STEP_USER_DATA) {
            this.password = null;
            if (systemComboBox.getSelectedIndex() < DatabaseService.getJDBCProperties().size()) {
                urlField.setText(createPredefinedDatabaseURL());
            }
        }
    }
    
    private void updateSystemSelection() {
        if (systemComboBox.getSelectedIndex() >= DatabaseService.getJDBCProperties().size()) {
            serverField.setEnabled(false);
            databaseNameField.setEnabled(false);
            urlField.setText("");
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
            this.handler = 
                new DatabaseHandler(urlString, DatabaseService.getJDBCProperties().get(systemComboBox.getSelectedIndex()));
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
    
    private void showConnectionError(String message, SQLException e) {
        JOptionPane.showMessageDialog(this, (message != null ? (message + ": ") : "") + "Connection to database has failed:" + Tools.getLineSeparator() + e.getMessage().substring(0, Math.min(300, e.getMessage().length())) + "...", "Connection failed", JOptionPane.ERROR_MESSAGE);        
    }
    
    protected void finish(ConfigurationListener listener) {
        try {
            disconnect();
        } catch (SQLException e) {}

        String tableName = tableNameField.getText().trim();
        String databaseURL = urlField.getText().trim();
        String userName = userNameField.getText().trim();
        
        // sanity checks
        if ((databaseURL.length() == 0) || (tableName.length() == 0) || (userName.length() == 0)) {
            SwingTools.showVerySimpleErrorMessage("You must specify a database connection and proper settings - the operator will not work without this." + Tools.getLineSeparator() + "Please select \"Cancel\" if you want to abort this wizard.");
        } else {
            // everything is OK --> database parameters
            Parameters parameters = listener.getParameters();
            parameters.setParameter("database_system", systemComboBox.getSelectedIndex() + "");
            parameters.setParameter("database_url", databaseURL);
            parameters.setParameter("username", userName);
            parameters.setParameterWithoutCheck("password", null);
            char[] password = passwordField.getPassword();
            if ((password != null) && (password.length > 0))
                parameters.setParameterWithoutCheck("password", new String(password));

            // query string
            parameters.setParameter("table_name", tableName);
            
            listener.setParameters(parameters);
            dispose();
            RapidMinerGUI.getMainFrame().getPropertyTable().refresh();
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
