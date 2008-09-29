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
package com.rapidminer.operator.learner.clustering.hierarchical.upgma;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.operator.IOContainer;
import com.rapidminer.operator.ResultObjectAdapter;


/**
 * A tree. Each node can hold user data. Each edge has a length.
 */
public class Tree extends ResultObjectAdapter {

	private static final long serialVersionUID = -3699622592245022307L;

	private static final int NODE_SPACING = 20;

	private static final int LEVEL_HEIGHT = 40;

	// private static final Paint LEAF_PAINT = Color.blue;
	// private static final Paint NODE_PAINT = Color.white;
	private static final Paint HIGHLIGHT_PAINT = Color.red;

	private static final Paint NODE_BORDER = Color.black;

	private static final Font LABEL_FONT = new Font("LucidaSans", Font.PLAIN, 12);

	private static final Font EDGE_FONT = new Font("LucidaSans", Font.PLAIN, 9);

	private Tree parent = null;

	private double edgeLength = 0;

	private List<Tree> children = new ArrayList<Tree>();

	private int leafIndex = -1;

	private int index = -1;

	private String name;

	private boolean isHighlighted = false;

	private double height;

	public Tree(String name) {
		this.name = name;
	}

	public Tree(String name, Tree leftChild, double leftEdgeLength, Tree rightChild, double rightEdgeLength) {
		this(name);
		append(leftChild, leftEdgeLength);
		append(rightChild, rightEdgeLength);
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public double getHeight() {
		return height;
	}

	/**
	 * Returns a unique index if this leaf is a leaf, -1 otherwise. Can be used as an index for a distance matrix.
	 */
	public int getLeafIndex() {
		return leafIndex;
	}

	/** Returns the parent node or null if this node is the root node. */
	public Tree getParent() {
		return parent;
	}

	/** Returns the length of the edge to the parent. */
	public double getEdgeLength() {
		return edgeLength;
	}

	/** Sets the length of the edge to the parent. */
	public void setEdgeLength(double length) {
		this.edgeLength = length;
	}

	/** Returns the number of children. */
	public int getNumberOfChildren() {
		return children.size();
	}

	/** Returns the child node at position index. */
	public Tree getChild(int index_) {
		return children.get(index_);
	}

	/** Returns true if this tree has no children. */
	public boolean isLeaf() {
		return children.size() == 0;
	}

	/** Highlights this node when rendered. */
	public void setHighlighted(boolean highlight) {
		this.isHighlighted = highlight;
	}

	public String getName() {
		return name;
	}

	/** Returns a list of all leaves. Indices are assigned to all leaves. */
	public List<Tree> getLeaves() {
		List<Tree> list = new LinkedList<Tree>();
		getLeaves(list, -1);
		return list;
	}

	/** Returns a list of all nodes. */
	public List<Tree> getAllNodes() {
		List<Tree> list = new LinkedList<Tree>();
		getNodes(list);
		return list;
	}

	private void getNodes(List<Tree> list) {
		list.add(this);
		Iterator i = children.iterator();
		while (i.hasNext())
			((Tree) i.next()).getNodes(list);
	}

	/**
	 * Recursively fills the list with all leaves. Leaves are numbered, the highest used index is returned.
	 */
	private int getLeaves(List<Tree> list, int highestIndex) {
		if (isLeaf()) {
			list.add(this);
			this.leafIndex = ++highestIndex;
			return highestIndex;
		} else {
			this.leafIndex = -1;
			Iterator i = children.iterator();
			while (i.hasNext()) {
				highestIndex = ((Tree) i.next()).getLeaves(list, highestIndex);
			}
		}
		return highestIndex;
	}

	/** Adds a new tree as the rightmost child with given edge length. */
	public void append(Tree tree, double edgeLength_) {
		if (tree.parent != null)
			throw new IllegalArgumentException("Cannot share trees!");
		tree.edgeLength = edgeLength_;
		tree.parent = this;
		children.add(tree);
	}

	/** Removes the given node from the list of children. */
	public void remove(Tree child) {
		children.remove(child);
		child.parent = null;
		child.edgeLength = 0;
	}

	/** Removes this node from its parent. */
	public void removeFromParent() {
		if (parent != null) {
			parent.remove(this);
		}
	}

	/** Returns the root of this tree. */
	public Tree getRoot() {
		if (this.parent == null)
			return this;
		else
			return parent.getRoot();
	}

	/** Returns the sum of edge lengths up to the root. */
	public double getRootDistance() {
		if (this.parent == null)
			return 0.0;
		else
			return this.edgeLength + this.parent.getRootDistance();
	}

	/** Returns the average distance from a leaf to the root node. */
	public double getAverageRootDistance() {
		double sum = 0;
		List leaves = getLeaves();
		if (leaves.isEmpty())
			return 0;
		Iterator i = leaves.iterator();
		while (i.hasNext()) {
			sum += ((Tree) i.next()).getRootDistance();
		}
		return sum / leaves.size();
	}

	/** Calculates and returns a distance matrix for all nodes. */
	public DistanceMatrix getNodeDistanceMatrix() {
		int numberOfNodes = makePrefixIndices(-1) + 1;
		DistanceMatrix nodeDistances = new DistanceMatrix(numberOfNodes);
		calculateNodeDistanceMatrix(nodeDistances, new LinkedList<Tree>());
		return nodeDistances;
	}

	private void calculateNodeDistanceMatrix(DistanceMatrix nodeDistances, List<Tree> nodesSeen) {
		if (parent != null)
			nodeDistances.setDistance(this.index, parent.index, edgeLength);
		Iterator i = nodesSeen.iterator();
		while (i.hasNext()) {
			Tree tree = (Tree) i.next();
			nodeDistances.setDistance(this.index, tree.index, nodeDistances.getDistance(parent.index, tree.index) + edgeLength);
		}
		nodesSeen.add(this);
		i = children.iterator();
		while (i.hasNext()) {
			((Tree) i.next()).calculateNodeDistanceMatrix(nodeDistances, nodesSeen);
		}
	}

	/** Assigns indices to all nodes. */
	private int makePrefixIndices(int highestIndex) {
		this.index = ++highestIndex;
		Iterator i = children.iterator();
		while (i.hasNext()) {
			highestIndex = ((Tree) i.next()).makePrefixIndices(highestIndex);
		}
		return highestIndex;
	}

	/**
	 * Returns a distance matix for all leaf nodes (which is a subset of the node distance matrix).
	 */
	public DistanceMatrix getLeafDistanceMatrix() {
		DistanceMatrix nodeDistances = getNodeDistanceMatrix();
		List leaves = getLeaves();
		DistanceMatrix leafDistances = new DistanceMatrix(leaves.size());
		Iterator i = leaves.iterator();
		while (i.hasNext()) {
			Tree tree1 = (Tree) i.next();
			leafDistances.setName(tree1.leafIndex, tree1.name);
			Iterator j = leaves.iterator();
			while (j.hasNext()) {
				Tree tree2 = (Tree) j.next();
				leafDistances.setDistance(tree1.leafIndex, tree2.leafIndex, nodeDistances.getDistance(tree1.index, tree2.index));
			}
		}
		return leafDistances;
	}

	public DistanceMatrix getUltrametricTransformedLeafDistanceMatrix() {
		DistanceMatrix nodeDistances = getNodeDistanceMatrix();
		List leaves = getLeaves();
		DistanceMatrix e = new DistanceMatrix(leaves.size());
		double dAvg = getAverageRootDistance();
		int rootIndex = getRoot().index;
		Iterator i = leaves.iterator();
		while (i.hasNext()) {
			Tree tree1 = (Tree) i.next();
			e.setName(tree1.leafIndex, tree1.name);
			Iterator j = leaves.iterator();
			while (j.hasNext()) {
				Tree tree2 = (Tree) j.next();
				if (tree1.index == tree2.index)
					continue;
				double dij = nodeDistances.getDistance(tree1.index, tree2.index);
				double dir = nodeDistances.getDistance(tree1.index, rootIndex);
				double djr = nodeDistances.getDistance(tree2.index, rootIndex);
				e.setDistance(tree1.leafIndex, tree2.leafIndex, (dij - dir - djr) / 2 + dAvg);
			}
		}
		return e;
	}

	public String toString() {
		return name;
	}

	public String toPrefixString() {
		StringBuffer s = new StringBuffer(edgeLength + "");
		s.append(name);
		if (children.size() > 0) {
			s.append("(");
			boolean first = true;
			Iterator<Tree> i = children.iterator();
			while (i.hasNext()) {
				if (!first)
					s.append(",");
				first = false;
				s.append((i.next()).toPrefixString());
			}
			s.append(")");
		}
		return s.toString();
	}

	public void render(Graphics2D g) {
		Dimension size = getSize();
		int x = 0;
		Iterator i = children.iterator();
		while (i.hasNext()) {
			Tree child = (Tree) i.next();
			Dimension d = child.getSize();
			g.setPaint(NODE_BORDER);
			int fromX = (int) (size.getWidth() / 2);
			int fromY = LABEL_FONT.getSize() / 2;
			int toX = (int) (x + d.getWidth() / 2);
			int toY = LEVEL_HEIGHT;
			g.drawLine(fromX, fromY, toX, toY);
			g.setFont(EDGE_FONT);
			String length = "" + (double) (Math.round(child.getEdgeLength() * 100)) / 100;
			FontMetrics metrics = g.getFontMetrics();
			Rectangle2D stringSize = metrics.getStringBounds(length, g);
			float textX = (float) ((2.0d * toX + fromX) / 3.0d - stringSize.getWidth() / 2.0d - stringSize.getX());
			float textY = (float) ((2.0d * toY + fromY) / 3.0d - stringSize.getHeight() / 2.0d - stringSize.getY());
			g.clearRect((int)Math.round(textX + stringSize.getX()), (int)Math.round(textY + stringSize.getY()), (int)Math.round(stringSize.getWidth()), (int)Math.round(stringSize.getHeight()));
			g.setPaint(NODE_BORDER);
			g.drawString(length, textX, textY);
			Graphics2D ng = (Graphics2D) g.create(x, LEVEL_HEIGHT, (int) d.getWidth(), (int) d.getHeight() + 1);
			child.render(ng);
			ng.dispose();
			x += d.getWidth();
		}
		renderNode(g, size);
	}

	private void renderNode(Graphics2D g, Dimension size) {
		Paint nodePaint = SwingTools.makeYellowPaint(size.getWidth(), size.getHeight());
		if (isLeaf())
			nodePaint = SwingTools.makeBluePaint(size.getWidth(), size.getHeight());
		if (isHighlighted)
			nodePaint = HIGHLIGHT_PAINT;
		String[] labels = name.split("\n");
		double width = 0.0d;
		double height_ = 0.0d;
		for (int i = 0; i < labels.length; i++) {
			Rectangle2D stringSize = LABEL_FONT.getStringBounds(" " + labels[i] + " ", g.getFontRenderContext());
			height_ += stringSize.getHeight();
			width = Math.max(width, stringSize.getWidth());
		}
		Rectangle2D stringSize = new Rectangle2D.Double(0.0d, 0.0d, width, height_);
		g.setPaint(nodePaint);
		g.fillRoundRect((int) ((size.getWidth() - stringSize.getWidth()) / 2), 0, (int) stringSize.getWidth(), (int) stringSize.getHeight(),
				(int) (stringSize.getWidth() / 4), (int) (stringSize.getHeight() / 4));
		g.setPaint(NODE_BORDER);
		g.drawRoundRect((int) ((size.getWidth() - stringSize.getWidth()) / 2), 0, (int) stringSize.getWidth(), (int) stringSize.getHeight(),
				(int) (stringSize.getWidth() / 4), (int) (stringSize.getHeight() / 4));
		g.setFont(LABEL_FONT);
		height_ = 0.0d;
		for (int i = 0; i < labels.length; i++) {
			Rectangle2D currentSize = LABEL_FONT.getStringBounds(" " + labels[i] + " ", g.getFontRenderContext());
			g.drawString(" " + labels[i] + " ", (int) ((size.getWidth() - currentSize.getWidth()) / 2 - currentSize.getX()), (int) -currentSize
					.getY()
					+ (int) height_);
			height_ += currentSize.getHeight();
		}
	}

	private Dimension getTextSize() {
		String[] labels = name.split("\n");
		double width = 0.0d;
		double height_ = 0.0d;
		for (int i = 0; i < labels.length; i++) {
			Rectangle2D stringSize = LABEL_FONT.getStringBounds(" " + labels[i] + " ", new FontRenderContext(null, false, false));
			height_ += stringSize.getHeight();
			width = Math.max(width, stringSize.getWidth());
		}
		return new Dimension((int) width, (int) height_);
	}

	private Dimension getNodeSize() {
		Dimension d = getTextSize();
		return new Dimension((int) d.getWidth() + NODE_SPACING, (int) d.getHeight());
	}

	public Dimension getSize() {
		Dimension nodeSize = getNodeSize();
		if (isLeaf()) {
			return nodeSize;
		} else {
			int w = 0;
			int h = 0;
			Iterator i = children.iterator();
			while (i.hasNext()) {
				Dimension d = ((Tree) i.next()).getSize();
				w += d.getWidth();
				h = (int) Math.max(h, d.getHeight());
			}
			w = (int) Math.max(w, nodeSize.getWidth());
			return new Dimension(w, h + LEVEL_HEIGHT);
		}
	}

	public Tree getNodeUnder(Graphics g, String userDataKey, int px, int py) {
		Dimension size = getSize();
		if ((py < 0) || (px < 0) || (px > size.getWidth()) || (py > size.getHeight()))
			return null;
		Dimension nodeSize = getNodeSize();
		if (py <= nodeSize.getHeight()) {
			int x = (int) ((size.getWidth() - nodeSize.getWidth()) / 2);
			if ((px >= x) && (px <= x + nodeSize.getWidth()))
				return this;
		} else {
			int x = 0;
			Iterator i = children.iterator();
			while (i.hasNext()) {
				Tree child = (Tree) i.next();
				Tree result = child.getNodeUnder(g, userDataKey, px - x, py - LEVEL_HEIGHT);
				if (result != null)
					return result;
				x += child.getSize().getWidth();
			}
		}
		return null;
	}

	// public String getName() { return "UPGMA tree"; }
	public java.awt.Component getVisualizationComponent(IOContainer container) {
		return new ExtendedJScrollPane(new javax.swing.JPanel() {

			private static final long serialVersionUID = -6444368685617280451L;
			{
				Dimension d = Tree.this.getSize();
				setPreferredSize(d);
			}

			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				render((Graphics2D) g);
			}
		});
	}

	public String getExtension() {
		return "tre";
	}

	public String getFileDescription() {
		return "tree";
	}
}
