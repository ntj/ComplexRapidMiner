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
package com.rapidminer.gui.tools;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.border.BevelBorder;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.gui.RapidMinerGUI;
import com.rapidminer.gui.actions.WelcomeNewAction;
import com.rapidminer.gui.actions.WelcomeOpenAction;
import com.rapidminer.gui.actions.WelcomeOpenRecentAction;
import com.rapidminer.gui.actions.WelcomeTutorialAction;
import com.rapidminer.gui.actions.WelcomeWizardAction;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;

/**
 * Lets the user select with what he wants to start: blank, existing file,
 * recent file, wizard or tutorial. This panel is shown after RapidMiner was started.
 * 
 * @author Ingo Mierswa
 * @version $Id: WelcomeScreen.java,v 1.8 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class WelcomeScreen extends JPanel {

	private static Image borderTopImage = null;
	
	private static Image borderBottomImage = null;
	
	private static Image bottomImage = null;
	
	private static Image togglePerspectiveImage = null;
	
	static {
		try {
			URL url = Tools.getResource("welcome_border_top.png");
			if (url != null) {
				borderTopImage = ImageIO.read(url);
			}
			
			url = Tools.getResource("welcome_border_bottom.png");
			if (url != null) {
				borderBottomImage = ImageIO.read(url);
			}
			
			url = Tools.getResource("welcome_bottom.png");
			if (url != null) {
				bottomImage = ImageIO.read(url);
			}
			
			url = Tools.getResource("toggle_perspective.png");
			if (url != null) {
				togglePerspectiveImage = ImageIO.read(url);
			}
		} catch (IOException e) {
			LogService.getGlobal().logWarning("Cannot load images for welcome screen. Using empty welcome screen...");
		}
	}
	
	private static final long serialVersionUID = -6916236648023490473L;

	private JList recentFileList;

	private MainFrame mainFrame;

	public WelcomeScreen(MainFrame mainFrame, String newsText) {
		this.mainFrame = mainFrame;
		GridBagLayout layout = new GridBagLayout();
		setLayout(layout);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		
		// welcome message
		JLabel welcomeLabel = new JLabel("Welcome to RapidMiner");
		welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
		welcomeLabel.setForeground(SwingTools.BROWN_FONT_COLOR);
		welcomeLabel.setBackground(Color.WHITE);
		welcomeLabel.setBorder(BorderFactory.createEmptyBorder(11,11,11,11));
		JPanel welcomeTextPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		welcomeTextPanel.add(welcomeLabel);
		welcomeTextPanel.setBackground(Color.WHITE);
		layout.setConstraints(welcomeTextPanel, c);
		add(welcomeTextPanel);
		
		// border top
		JPanel borderTopPanel = new ImagePanel(borderTopImage, ImagePanel.IMAGE_PREFERRED_HEIGHT);
		layout.setConstraints(borderTopPanel, c);
		add(borderTopPanel);
		
		// central actions		
		JToolBar actionBar = new ExtendedJToolBar();
		actionBar.setBorder(null);
		actionBar.setLayout(new FlowLayout(FlowLayout.CENTER));
		actionBar.setBackground(Color.WHITE);
		actionBar.setBorderPainted(false);

		JButton button = new JButton(new WelcomeNewAction(this.mainFrame));
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		actionBar.add(button);
		
		actionBar.addSeparator();
		actionBar.addSeparator();
		actionBar.addSeparator();
		actionBar.addSeparator();
		
		button = new JButton(new WelcomeOpenRecentAction(this.mainFrame, this));
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		if (RapidMinerGUI.getRecentFiles().size() == 0) {
			button.setEnabled(false);
		}
		actionBar.add(button);

		actionBar.addSeparator();
		actionBar.addSeparator();
		actionBar.addSeparator();
		actionBar.addSeparator();
		
		button = new JButton(new WelcomeOpenAction(this.mainFrame));
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		actionBar.add(button);
		
		actionBar.addSeparator();
		actionBar.addSeparator();
		actionBar.addSeparator();
		
		button = new JButton(new WelcomeWizardAction(this.mainFrame));
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		actionBar.add(button);
		
		actionBar.addSeparator();
		actionBar.addSeparator();
		
		button = new JButton(new WelcomeTutorialAction(this.mainFrame));
		button.setHorizontalTextPosition(SwingConstants.CENTER);
		button.setVerticalTextPosition(SwingConstants.BOTTOM);
		actionBar.add(button);
		
	    layout.setConstraints(actionBar, c);
		add(actionBar);

		// recent files
		recentFileList = new JList(RapidMinerGUI.getRecentFiles().toArray(new Object[RapidMinerGUI.getRecentFiles().size()]));
		recentFileList.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		recentFileList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
		recentFileList.setBorder(BorderFactory.createTitledBorder("Recent Files"));
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					WelcomeScreen.this.mainFrame.changeMode(MainFrame.EDIT_MODE);
					openRecentProcess();
				}
			}
		};
		recentFileList.addMouseListener(mouseListener);
		
		JPanel listPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
		listPanel.add(recentFileList);
		listPanel.setBackground(Color.WHITE);
		layout.setConstraints(listPanel, c);
		add(listPanel);

		// border bottom
		JPanel borderBottomPanel = new ImagePanel(borderBottomImage, ImagePanel.IMAGE_PREFERRED_HEIGHT);
		layout.setConstraints(borderBottomPanel, c);
		add(borderBottomPanel);
		
		// bottom text panel
		JPanel bottomTextPanel = new ImagePanel(bottomImage, ImagePanel.CHILDRENS_PREFERRED_SIZE);
		BoxLayout textLayout = new BoxLayout(bottomTextPanel, BoxLayout.X_AXIS);
		bottomTextPanel.setLayout(textLayout);
		
		// tip
		final TipOfTheDayProvider tipProvider = new TipOfTheDayProvider();
		final TextPanel tipTextPanel = new TextPanel("Tip of the Day", nextTip(tipProvider), TextPanel.ALIGNMENT_LEFT, TextPanel.ALIGNMENT_BOTTOM);
		
		JLabel nextTipButton = new JLabel("Next Tip");
		nextTipButton.setFont(TextPanel.TEXT_FONT);
		nextTipButton.setForeground(SwingTools.LIGHT_BROWN_FONT_COLOR);
		nextTipButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		nextTipButton.setAlignmentY(Component.TOP_ALIGNMENT);
		nextTipButton.setOpaque(false);
		nextTipButton.setBorder(BorderFactory.createEmptyBorder(0, 24, 24, 0));
		nextTipButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
		nextTipButton.addMouseListener(new MouseListener() {
			
			public void mouseClicked(MouseEvent e) {}
			public void mousePressed(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}

			public void mouseReleased(MouseEvent e) {
				tipTextPanel.setText(nextTip(tipProvider));
				revalidate();
				repaint();
			}
		});
		
		JPanel tipsPanel = new JPanel();
		tipsPanel.setOpaque(false);
		GridBagLayout tipLayout = new GridBagLayout();
		tipsPanel.setLayout(tipLayout);
		GridBagConstraints tipC = new GridBagConstraints();
		tipC.fill = GridBagConstraints.BOTH;
		tipC.anchor = GridBagConstraints.NORTHWEST;
		tipC.gridwidth = GridBagConstraints.REMAINDER;
		tipC.weightx = 1.0d;
		
		// fill panel
		JPanel fillPanel = new JPanel();
		fillPanel.setOpaque(false);
		tipC.weighty = 1.0d;
		tipLayout.setConstraints(fillPanel, tipC);
		tipsPanel.add(fillPanel);		

		tipC.weighty = 0.0d;
		tipLayout.setConstraints(tipTextPanel, tipC);
		tipsPanel.add(tipTextPanel);
		tipLayout.setConstraints(nextTipButton, tipC);
		tipsPanel.add(nextTipButton);

		// add tips panel to bottom box
		bottomTextPanel.add(tipsPanel);
		
		// news text
		String transformedNewsText = Tools.transformAllLineSeparators(newsText);
		final String[] newsLines = transformedNewsText.split("\n");
		JPanel newsTextPanel = null;
		if (togglePerspectiveImage != null) {
			newsTextPanel = new ImageTextPanel(togglePerspectiveImage, "NEWS", newsLines, TextPanel.ALIGNMENT_RIGHT, TextPanel.ALIGNMENT_BOTTOM, false, 0, ImageTextPanel.TEXT_START_Y);
		} else {
			newsTextPanel = new TextPanel("NEWS", newsLines, TextPanel.ALIGNMENT_RIGHT, TextPanel.ALIGNMENT_BOTTOM);
		}
		bottomTextPanel.add(newsTextPanel);
		
		c.weighty = 1;
		layout.setConstraints(bottomTextPanel, c);
		add(bottomTextPanel);
	}
	
	private String[] nextTip(TipOfTheDayProvider tipProvider) {
		String tipText = tipProvider.nextTip();
		String transformedTipText = tipText.replaceAll("<lb>", "\n");
		transformedTipText = transformedTipText.replaceAll("<indent>", "      ");
		transformedTipText = transformedTipText.replaceAll("<item>",   "    * ");
		String[] tipLines = transformedTipText.split("\n");
		return tipLines;
	}
	
	public void openRecentProcess() {
		int selectedIndex = recentFileList.getSelectedIndex();
		if (selectedIndex < 0)
			selectedIndex = 0;
		if (RapidMinerGUI.getRecentFiles().size() > 0)
			mainFrame.open(RapidMinerGUI.getRecentFiles().get(selectedIndex));
		else
			mainFrame.open();
	}
}
