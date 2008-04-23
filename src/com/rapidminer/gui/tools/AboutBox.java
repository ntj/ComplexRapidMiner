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
import java.net.URL;

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
 * @version $Id: AboutBox.java,v 1.2 2007/05/28 00:29:22 ingomierswa Exp $
 */
public class AboutBox extends JDialog {

	private static final long serialVersionUID = -3889559376722324215L;

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

		private String productName;
		
		private String productVersion;
		
		private String[] aboutTextLines;

		private transient Image productLogo;

		public ContentPanel(String productName, String productVersion, String aboutText, Image productLogo) {
			this.productName = productName;
			this.productVersion = productVersion;
			this.productLogo = productLogo;
			String transformedText = Tools.transformAllLineSeparators(aboutText);
			this.aboutTextLines = transformedText.split("\n");
			
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
			int yPos = getHeight() - MARGIN - aboutTextLines.length * 15 - 5;
			drawString(g, productName + " " + productVersion, yPos);
			yPos += 5;
			g.setFont(new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 10));
			for (String line : aboutTextLines) {
				yPos += 15;
				drawString(g, line, yPos);
			}
		}

		private void drawString(Graphics2D g, String text, int height) {
			Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, g);
			float xPos = (float)(getWidth() - MARGIN - stringBounds.getWidth());
			float yPos = height;
			g.drawString(text, xPos, yPos);
		}	
	}

	public AboutBox(Frame owner, String productName, String productVersion, String aboutText, Image productLogo) {
		super(owner, "About " + productName, true);
		setResizable(false);
		
		setLayout(new BorderLayout());
		ContentPanel contentPanel = new ContentPanel(productName, productVersion, aboutText, productLogo);
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
}
