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
package com.rapidminer.gui.wizards;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.operator.io.ExampleSource;


/**
 * This class is the creator for wizard dialogs defining the configuration for 
 * {@link ExampleSource} operators.
 * 
 * @author Ingo Mierswa
 * @version $Id: AbstractConfigurationWizard.java,v 1.1 2007/05/27 22:02:07 ingomierswa Exp $
 */
public abstract class AbstractConfigurationWizard extends JDialog {
    
    private JButton next = new JButton("Next >");

    private JButton previous = new JButton("< Previous");

    private CardLayout cardLayout = new CardLayout();

    private JPanel mainPanel = new JPanel(cardLayout);

    private GridBagLayout layout = new GridBagLayout();
    
    private GridBagConstraints c = new GridBagConstraints();
    
    private JPanel contentPanel = new JPanel(layout);
    
    private int currentStep = 0;

    private int numberOfSteps = 0;

    private ConfigurationListener listener;
        
    /** Creates a new wizard. */
    public AbstractConfigurationWizard(String name, ConfigurationListener listener) {
        super(RapidMinerGUI.getMainFrame(), name, true);

        this.listener = listener;
        
        // button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(next);
        buttonPanel.add(previous);
        previous.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                step(-1);
            }
        });
        buttonPanel.add(next);
        next.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                step(1);
            }
        });
        buttonPanel.add(Box.createHorizontalStrut(11));
        JButton cancel = new JButton("Cancel");
        buttonPanel.add(cancel);
        cancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // main panel
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.weightx = 1;
        c.weighty = 1;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.insets = new Insets(11,11,11,11);
        this.contentPanel = new JPanel(layout);
        layout.setConstraints(mainPanel, c);
        contentPanel.add(mainPanel);
        
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        
        setSize(Math.max(640, (int)(0.66d * getOwner().getWidth())), Math.max(480, (int)(0.66d * getOwner().getHeight())));
        
        setLocationRelativeTo(getOwner());
    }
    
    /** This method is invoked in the method step. Subclasses might perform some additional stuff here. */
    protected abstract void performStepAction(int currentStep, int oldStep);
   
    /** This method is invoked at the end of the configuration process. Subclasses should generate
     *  the parameters object and pass it to the listener. */
    protected abstract void finish(ConfigurationListener listener); 
    
    /** Subclasses might add an additional component here which is seen during all steps, e.g. a 
     *  data view table. */
    protected void addBottomComponent(Component bottomComponent) {
        c.weighty = 2;
        layout.setConstraints(bottomComponent, c);
        contentPanel.add(bottomComponent);
    }
    
    protected int getNumberOfSteps() {
        return numberOfSteps;
    }
    
    protected void addStep(Component c) {
        mainPanel.add(c, numberOfSteps + "");
        numberOfSteps++;
    }

    private void step(int dir) {
    	int oldStep = currentStep;
        currentStep += dir;
        
        if (currentStep < 0)
            currentStep = 0;
        if (currentStep == 0)
            previous.setEnabled(false);
        else
            previous.setEnabled(true);

        if (currentStep >= numberOfSteps) {
            currentStep = numberOfSteps - 1;
            finish(listener);
        }
        if (currentStep == numberOfSteps - 1) {
            next.setText("Finish");
        } else {
            next.setText("Next >");
        }
    
        performStepAction(currentStep, oldStep);
        
        cardLayout.show(mainPanel, currentStep + "");
    }
    
    protected void cancel() {
        dispose();
    }
}
