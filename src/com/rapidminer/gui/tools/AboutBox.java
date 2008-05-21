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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;

import com.rapidminer.tools.LogService;
import com.rapidminer.tools.Tools;


/**
 * This dialog displays some informations about the product.
 * The product logo should have a size of approximately 270 times 70 pixels.
 * 
 * @author Ingo Mierswa
 * @version $Id: AboutBox.java,v 1.5 2008/05/09 19:22:58 ingomierswa Exp $
 */
public class AboutBox extends JDialog {

	private static final long serialVersionUID = -3889559376722324215L;

	private static final String PROPERTY_FILE = "about_infos.properties";
	
	private static class ContentPanel extends JPanel {
		
		private static final long serialVersionUID = -1763842074674706654L;

		private static final Paint MAIN_PAINT = Color.LIGHT_GRAY;
		
		private static Image backgroundImage = null;
		
		private static final int MARGIN = 10;
		
		static {
			try {				
				URL url = Tools.getResource("splashscreen_background.png");
				if (url != null)
					backgroundImage = ImageIO.read(url);
			} catch (IOException e) {
				LogService.getGlobal().logWarning("Cannot load images for about box. Using empty image...");
			}
		}

		private Properties properties;

		private transient Image productLogo;

		public ContentPanel(Properties properties, Image productLogo) {
			this.properties = properties;
			this.productLogo = productLogo;
			
			int width = 450;
			int height = 350;
			if (backgroundImage != null) {
				width  = backgroundImage.getWidth(this);
				height = backgroundImage.getHeight(this);
			}
			setPreferredSize(new Dimension(width, height));
			setMinimumSize(new Dimension(width, height));
			setMaximumSize(new Dimension(width, height));
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
			g.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 11));
			drawString(g, properties.getProperty("name") + " " + properties.getProperty("version"), 240);
			
			g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
			drawString(g, properties.getProperty("name") + " " + properties.getProperty("version"), 260);
			drawString(g, properties.getProperty("copyright"), 275);
			drawString(g, properties.getProperty("licensor"), 290);
			drawString(g, properties.getProperty("license"), 305);
			drawString(g, properties.getProperty("warranty"), 320);
			drawString(g, properties.getProperty("more"), 335);
		}

		private void drawString(Graphics2D g, String text, int height) {
			if (text == null)
				return;
			Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, g);
			float xPos = (float)(getWidth() - MARGIN - stringBounds.getWidth());
			float yPos = height;
			g.drawString(text, xPos, yPos);
		}	
	}

	public AboutBox(Frame owner, String productName, String productVersion, String licensor, String url, String text, Image productLogo) {
		this(owner, createProperties(productName, productVersion, licensor, url, text), productLogo);
	}
	
	public AboutBox(Frame owner, String productVersion, Image productLogo) {
		this(owner, createProperties(productVersion), productLogo);
	}
	
	public AboutBox(Frame owner, Properties properties, Image productLogo) {
		super(owner, "About", true);
		setResizable(false);
		
		setLayout(new BorderLayout());
		
		String name = properties.getProperty("name");
		if (name != null) {
			setTitle("About " + name);
		}
		ContentPanel contentPanel = new ContentPanel(properties, productLogo);
		add(contentPanel, BorderLayout.CENTER);
		
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton closeButton = new JButton(new AbstractAction("Close") {
			private static final long serialVersionUID = 1407089394491740308L;
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		buttonPanel.add(closeButton);
		add(buttonPanel, BorderLayout.SOUTH);
		
		pack();
		setLocationRelativeTo(owner);
	}
	
	private static Properties createProperties(String productVersion) {
		Properties properties = new Properties();
		try {
			InputStream in = Tools.getResource(PROPERTY_FILE).openStream();
			properties.load(in);
			in.close();
		} catch (Exception e) {
			LogService.getGlobal().logError("Cannot read splash screen infos: " + e.getMessage());
		}
		properties.setProperty("version", productVersion);
		return properties;
	}
	
	private static Properties createProperties(String productName, String productVersion, String licensor, String url, String text) {
		Properties properties = new Properties();
		properties.setProperty("name", productName);
		properties.setProperty("version", productVersion);
		properties.setProperty("licensor", licensor);
		properties.setProperty("license", url);
		properties.setProperty("more", text);
		return properties;
	}
}
