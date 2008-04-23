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
package com.rapidminer.gui.tools;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import com.rapidminer.gui.MainFrame;
import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * The splash screen is displayed during start up of RapidMiner. It displays the
 * logo and the some start information. The product logo should have a size of 
 * approximately 270 times 70 pixels.
 * 
 * @author Ingo Mierswa
 * @version $Id: SplashScreen.java,v 1.4 2007/06/08 15:43:57 ingomierswa Exp $
 */
public class SplashScreen extends JPanel {

	private static final long serialVersionUID = -1525644776910410809L;

	private static final Paint MAIN_PAINT = Color.RED;
	
	private static Image backgroundImage = null;
	
	
	private static final int MARGIN = 10;
	
	static {
		try {			
			URL url = Tools.getResource("splashscreen_background.png");
			if (url != null)
				backgroundImage = ImageIO.read(url);
		} catch (IOException e) {
			LogService.getGlobal().logWarning("Cannot load images for splash screen. Using empty splash screen...");
		}
	}

	
	private transient Image productLogo;
	
	private JFrame splashScreenFrame = new JFrame();

	private String productName;
	
	private String productVersion;
	
	private String message = "Starting...";


	public SplashScreen(String productName, String productVersion, Image productLogo) {
		super();
		
		this.productName = productName;
		this.productVersion = productVersion;
		this.productLogo = productLogo;

		splashScreenFrame = new JFrame(MainFrame.TITLE);
		splashScreenFrame.getContentPane().add(this);
		SwingTools.setFrameIcon(splashScreenFrame);

		splashScreenFrame.setUndecorated(true);
		if (backgroundImage != null)
			splashScreenFrame.setSize(backgroundImage.getWidth(this), backgroundImage.getHeight(this));
		else
			splashScreenFrame.setSize(450, 350);
		splashScreenFrame.setLocationRelativeTo(null);
	}

	public void showSplashScreen() {
		splashScreenFrame.setVisible(true);
	}

	public void dispose() {
		splashScreenFrame.dispose();
	}

	public void paint(Graphics g) {
		super.paint(g);
		g.setColor(Color.black);
		g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
		
		drawMain((Graphics2D)g);
	}

	public void drawMain(Graphics2D g) {
		g.setPaint(MAIN_PAINT);
		g.fillRect(0, 0, getWidth(), getHeight());

		if (backgroundImage != null)
			g.drawImage(backgroundImage, 0, 0, this);

		if (productLogo != null)
			g.drawImage(productLogo, getWidth() / 2 - productLogo.getWidth(this) / 2, 90, this);
		
		g.setColor(SwingTools.BROWN_FONT_COLOR);
		if (message != null) {
			g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
			drawString(g, message, 270);
		}
		
		g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
		drawString(g, productName + " " + productVersion, 290);
		drawString(g, "Copyright (C) 2001-2007 by Rapid-I and contributors", 305);
		drawString(g, "Use is subject to the GNU General Public License", 320);
		drawString(g, "More information at http://rapid-i.com", 335);
	}

	private void drawString(Graphics2D g, String text, int height) {
		Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, g);
		float xPos = (float)(getWidth() - MARGIN - stringBounds.getWidth());
		float yPos = height;
		g.drawString(text, xPos, yPos);
	}
	
	public void setMessage(String message) {
		this.message = message;
		repaint();
	}
}
