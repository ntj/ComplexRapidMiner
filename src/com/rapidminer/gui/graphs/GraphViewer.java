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
package com.rapidminer.gui.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.border.BevelBorder;

import org.apache.commons.collections15.Transformer;
import org.freehep.util.export.ExportDialog;

import com.rapidminer.gui.graphs.actions.PickingModeAction;
import com.rapidminer.gui.graphs.actions.TransformingModeAction;
import com.rapidminer.gui.graphs.actions.ZoomInAction;
import com.rapidminer.gui.graphs.actions.ZoomOutAction;
import com.rapidminer.gui.tools.ExtendedJScrollPane;
import com.rapidminer.gui.tools.ExtendedJToolBar;
import com.rapidminer.gui.tools.IconSize;
import com.rapidminer.gui.tools.SwingTools;
import com.rapidminer.tools.Renderable;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.EdgeLabelRenderer;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.MultiLayerTransformer;
import edu.uci.ics.jung.visualization.VertexLabelRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingControl;
import edu.uci.ics.jung.visualization.decorators.ConstantDirectionalEdgeValueTransformer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.GradientEdgePaintTransformer;
import edu.uci.ics.jung.visualization.layout.LayoutTransition;
import edu.uci.ics.jung.visualization.renderers.BasicVertexRenderer;
import edu.uci.ics.jung.visualization.renderers.Renderer;
import edu.uci.ics.jung.visualization.util.Animator;

/**
 * The basic graph viewer component for graphs.
 *
 * @author Ingo Mierswa
 * @version $Id: GraphViewer.java,v 1.22 2008/05/09 19:23:24 ingomierswa Exp $
 */
public class GraphViewer<V,E> extends JPanel implements Renderable{
	
    private static final long serialVersionUID = -7501422172633548861L;
    
    public static final int MARGIN = 10;
    
    public static final Font EDGE_FONT = new Font("SansSerif", Font.PLAIN, 10);
    
    public static final Font VERTEX_BOLD_FONT = new Font("SansSerif", Font.BOLD, 11);

    public static final Font VERTEX_PLAIN_FONT = new Font("SansSerif", Font.PLAIN, 11);
    
    private static final String INSTRUCTIONS =
        "<html>"+
        "<h3>General Info:</h3>"+
        "<ul>"+
        "<li>Mousewheel scales the view</li>"+
        "</ul>"+
        "<h3>Transforming Mode:</h3>"+
        "<ul>"+
        "<li>Mouse1+drag pans the graph"+
        "<li>Mouse1+Shift+drag rotates the graph"+
        "<li>Mouse1+CTRL(or Command)+drag shears the graph"+
        "</ul>"+
        "<h3>Picking Mode:</h3>"+
        "<ul>"+
        "<li>Mouse1 on a node selects the node"+
        "<li>Mouse1 elsewhere unselects all nodes"+
        "<li>Mouse1+Shift on a node adds/removes node selection"+
        "<li>Mouse1+drag on a node moves all selected nodes"+
        "<li>Mouse1+drag elsewhere selects modes in a region"+
        "<li>Mouse1+Shift+drag adds selection of modes in a new region"+
        "<li>Mouse1+CTRL on a mode selects the mode and centers the display on it"+
        "</ul>"+
        "</html>";
	
    private final transient Action transformAction = new TransformingModeAction<V,E>(this, IconSize.SMALL);
    private final transient Action pickingAction   = new PickingModeAction<V,E>(this, IconSize.SMALL);
    
    private VisualizationViewer<V,E> vv;
    
    private transient Layout<V,E> layout;
    
    private transient GraphCreator<V, E> graphCreator;
    
    private LayoutSelection<V, E> layoutSelection;
    
    private transient ScalingControl scaler = new CrossoverScalingControl();
    
    private transient DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
    
    private boolean showEdgeLabels = true;
    
    private boolean showVertexLabels = true;
        
    private transient JSplitPane objectViewerSplitPane;
    
    private transient ModalGraphMouse.Mode currentMode = ModalGraphMouse.Mode.TRANSFORMING;
    
    public GraphViewer(final GraphCreator<V,E> graphCreator) {
        this.graphCreator = graphCreator;
        
    	setLayout(new BorderLayout());
        
        Graph<V,E> graph = graphCreator.createGraph();
        this.layoutSelection = new LayoutSelection<V,E>(this, graph);
        this.layout = new ISOMLayout<V, E>(graph);
        vv =  new VisualizationViewer<V,E>(layout) {

            private static final long serialVersionUID = 8247229781249216143L;
            
            private boolean initialized = false;

            /** Necessary in order to re-change layout after first painting (starting position and size). */
            public void paint(Graphics g) {
                super.paint(g);
                if (!initialized) {
                    initialized = true;
                    updateLayout();
                    if (objectViewerSplitPane != null) {
                    	objectViewerSplitPane.setDividerLocation(0.9);
                    }
                }
            }
        };
        vv.setBackground(Color.white);
        
        // === design ===

        // ## edge layout ##
        // EDGE SHAPE AND COLOR
        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<V, E>());
        vv.getRenderContext().setEdgeFillPaintTransformer(new GradientEdgePaintTransformer<V, E>(SwingTools.LIGHTEST_BLUE, SwingTools.LIGHT_BLUE, vv));
        vv.getRenderContext().setEdgeDrawPaintTransformer(new GradientEdgePaintTransformer<V, E>(SwingTools.LIGHT_BLUE, SwingTools.DARK_BLUE, vv));
        // EDGE FONT
        vv.getRenderContext().setEdgeFontTransformer(new Transformer<E, Font>() {
			public Font transform(E arg0) {
				return EDGE_FONT;
			}
        });
        // EDGE ARROW FILL PAINT
		vv.getRenderContext().setArrowFillPaintTransformer(new Transformer<E,Paint>() {
			public Paint transform(E arg0) {
				return SwingTools.LIGHT_BLUE;
			}
        });
        // EDGE ARROW DRAW PAINT
		vv.getRenderContext().setArrowDrawPaintTransformer(new Transformer<E,Paint>() {
			public Paint transform(E arg0) {
				return SwingTools.DARK_BLUE;
			}
        });
        // EDGE LABEL POSITION
        vv.getRenderContext().setEdgeLabelClosenessTransformer(new ConstantDirectionalEdgeValueTransformer<V,E>(0.5d, 0.5d));
        int labelOffset = graphCreator.getLabelOffset();
        if (labelOffset >= 0)
            vv.getRenderContext().setLabelOffset(labelOffset);
		
        // EDGE LABELS
        vv.getRenderContext().setEdgeLabelTransformer(new Transformer<E, String>() {
			public String transform(E object) {
				return graphCreator.getEdgeName(object);
			}
        });
        // EDGE LABEL RENDERER
        Renderer.EdgeLabel<V, E> edgeLabelRenderer = graphCreator.getEdgeLabelRenderer();
        if (edgeLabelRenderer != null)
        	vv.getRenderer().setEdgeLabelRenderer(edgeLabelRenderer); // renderer...
        vv.getRenderContext().setEdgeLabelRenderer(new EdgeLabelRenderer() { // ...context!
        	
        	private JLabel renderer = new JLabel();
        	
			public <T> Component getEdgeLabelRendererComponent(JComponent parent, Object value, Font font, boolean isSelected, T edge) {
				this.renderer.setFont(font);
                if (graphCreator.isEdgeLabelDecorating()) {
                    this.renderer.setOpaque(true);
                    renderer.setBackground(Color.WHITE);
                    // use this for a more fancy look and feel
                    //renderer.setBackground(SwingTools.TRANSPARENT_YELLOW);
                    //this.renderer.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(SwingTools.DARK_BLUE), BorderFactory.createEmptyBorder(1,1,1,1)));
                }
        		this.renderer.setText(value.toString());
        		return this.renderer;
			}

            /** Let the graph model decide. */
			public boolean isRotateEdgeLabels() {
				return graphCreator.isRotatingEdgeLabels();
			}

            /** Does nothing. */
			public void setRotateEdgeLabels(boolean rotate) {}
        });
        
        // ## vertex layout ##
        
        // VERTEX FONT
        vv.getRenderContext().setVertexFontTransformer(new Transformer<V, Font>() {
			public Font transform(V vertex) {
				if (graphCreator.isBold(vertex))
					return VERTEX_BOLD_FONT;
				else
					return VERTEX_PLAIN_FONT;
			}
        });
        // VERTEX NAME
        vv.getRenderContext().setVertexLabelTransformer(new Transformer<V, String>() {
			public String transform(V object) {
				return graphCreator.getVertexName(object);
			}
        });
        // VERTEX FILL PAINT
        Transformer<V, Paint> paintTransformer = graphCreator.getVertexPaintTransformer(vv);
        if (paintTransformer == null) {
        	paintTransformer = new Transformer<V, Paint>() {
    			public Paint transform(V vertex) {
    				if (vv.getPickedVertexState().isPicked(vertex))
    					return SwingTools.makeYellowPaint(50, 50);
    				else
    					return SwingTools.makeBluePaint(50, 50);
    			}
            };
        }
        vv.getRenderContext().setVertexFillPaintTransformer(paintTransformer);
        
        // VERTEX DRAW PAINT
        vv.getRenderContext().setVertexDrawPaintTransformer(new Transformer<V, Paint>() {
			public Paint transform(V vertex) {
				if (vv.getPickedVertexState().isPicked(vertex))
					return SwingTools.DARKEST_YELLOW;
				else
					return SwingTools.DARKEST_BLUE;
			}
        });
        // VERTEX TOOL TIP
        this.vv.setVertexToolTipTransformer(new Transformer<V, String>() {
			public String transform(V vertex) {
				return graphCreator.getVertexToolTip(vertex);
			}
        });
        // VERTEX SHAPE
        vv.getRenderContext().setVertexShapeTransformer(new ExtendedVertexShapeTransformer<V>(graphCreator));
        
        // VERTEX RENDERER
        Renderer.Vertex<V, E> vertexRenderer = graphCreator.getVertexRenderer();
        if (vertexRenderer != null)
        	vv.getRenderer().setVertexRenderer(vertexRenderer);
        
        // VERTEX LABEL RENDERER
        setDefaultLabelPosition();
        // custom renderer?
        Renderer.VertexLabel<V, E> customVertexLabelRenderer = graphCreator.getVertexLabelRenderer();
        if (customVertexLabelRenderer != null)
            vv.getRenderer().setVertexLabelRenderer(customVertexLabelRenderer);
        // context
        vv.getRenderContext().setVertexLabelRenderer(new VertexLabelRenderer() {
        	private JLabel label = new JLabel();
			public <T> Component getVertexLabelRendererComponent(JComponent parent, Object object, Font font, boolean isSelection, T vertex) {
				label.setFont(font);
				if (object != null) {
					label.setText(object.toString());
				} else {
					label.setText("");
				}
				return label;
			}
        	
        });
        
        // === end of design ===

        // === main panel ===
        
        if (graphCreator.getObjectViewer() == null) {
            vv.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
            add(vv, BorderLayout.CENTER);
        } else {
            JComponent viewer = graphCreator.getObjectViewer().getViewerComponent();
            if (viewer != null) {
                viewer.setBorder(null);
                objectViewerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
                objectViewerSplitPane.setResizeWeight(1);
                objectViewerSplitPane.add(vv, 0);
                objectViewerSplitPane.add(viewer, 1);
                add(objectViewerSplitPane, BorderLayout.CENTER);
            } else {
                vv.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                add(vv, BorderLayout.CENTER);
            }
        }
        
        // === control panel ===
        
        Component controlPanel = createControlPanel();
        add(new ExtendedJScrollPane(controlPanel), BorderLayout.WEST);
        
        this.showEdgeLabels = !graphCreator.showEdgeLabelsDefault();
        togglePaintEdgeLabels();
        this.showVertexLabels = !graphCreator.showVertexLabelsDefault();
        togglePaintVertexLabels();
        
        this.layoutSelection.setLayout();
    }
    
    private JComponent createControlPanel() {
        // === mouse behaviour ===        
        vv.setGraphMouse(graphMouse);
        vv.addKeyListener(graphMouse.getModeKeyListener());
        graphMouse.setMode(ModalGraphMouse.Mode.TRANSFORMING);
        transformAction.setEnabled(false);
        pickingAction.setEnabled(true);
     
        vv.addGraphMouseListener(new GraphMouseListener<V>() {
            public void graphClicked(V vertex, MouseEvent arg1) {}
            public void graphPressed(V arg0, MouseEvent arg1) {}
            public void graphReleased(V vertex, MouseEvent arg1) {
            	if (currentMode.equals(ModalGraphMouse.Mode.TRANSFORMING)) {
            		if (graphCreator.getObjectViewer() != null) {
            			vv.getPickedVertexState().clear();
            			vv.getPickedVertexState().pick(vertex, true);
            			graphCreator.getObjectViewer().showObject(graphCreator.getObject(vertex));
            		}
            	}
            }
        });
     
        JPanel controls = new JPanel();
        GridBagLayout gbLayout = new GridBagLayout();
        controls.setLayout(gbLayout);
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.BOTH;
        c.insets = new Insets(4,4,4,4);
        c.weightx = 1;
        c.weighty = 0;
        
        // zooming
        JToolBar zoomBar = new ExtendedJToolBar();
        zoomBar.setLayout(new FlowLayout(FlowLayout.CENTER));
        zoomBar.add(new ZoomInAction(this, IconSize.SMALL));
        zoomBar.add(new ZoomOutAction(this, IconSize.SMALL));
        zoomBar.setBorder(BorderFactory.createTitledBorder("Zoom"));
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(zoomBar, c);
        controls.add(zoomBar);
        
        
        // mode
        JToolBar modeBar = new ExtendedJToolBar();
        modeBar.setLayout(new FlowLayout(FlowLayout.CENTER));
        modeBar.add(transformAction);
        modeBar.add(pickingAction);
        modeBar.setBorder(BorderFactory.createTitledBorder("Mode"));
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(modeBar, c);
        controls.add(modeBar);


        // layout selection
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(layoutSelection, c);
        controls.add(layoutSelection);
        
        // show node labels
        JCheckBox nodeLabels = new JCheckBox("Node Labels", graphCreator.showVertexLabelsDefault());
        nodeLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				togglePaintVertexLabels();
			}
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(nodeLabels, c);
        controls.add(nodeLabels);
        
        // show edge labels
        JCheckBox edgeLabels = new JCheckBox("Edge Labels", graphCreator.showEdgeLabelsDefault());
        edgeLabels.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				togglePaintEdgeLabels();
			}
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(edgeLabels, c);
        controls.add(edgeLabels);
        
        // option components
        for (int i = 0; i < graphCreator.getNumberOfOptionComponents(); i++) {
            JComponent optionComponent = graphCreator.getOptionComponent(this, i);
            if (optionComponent != null) {
                c.gridwidth = GridBagConstraints.REMAINDER;
                gbLayout.setConstraints(optionComponent, c);
                controls.add(optionComponent);
            }
        }
        
        // save image
		JButton imageButton = new JButton("Save Image...");
		imageButton.setToolTipText("Saves an image of the current graph.");
		imageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Component tosave = vv;
				ExportDialog exportDialog = new ExportDialog("RapidMiner");
				exportDialog.showExportDialog(GraphViewer.this, "Save Image...", tosave, "plot");
			}
		});
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(imageButton, c);
        controls.add(imageButton);
        
        // help
        JButton help = new JButton("Help");
        help.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(vv, INSTRUCTIONS);
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(help, c);
        controls.add(help);
        
        JPanel fillPanel = new JPanel();
        c.weighty = 1.0d;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gbLayout.setConstraints(fillPanel, c);
        controls.add(fillPanel);
        
        return controls;
    }
    
    public void updateLayout() {
        changeLayout(this.layout);
    }
    
    public void changeLayout(Layout<V, E> newLayout) {
    	MultiLayerTransformer transformer = vv.getRenderContext().getMultiLayerTransformer();
    	double scale = transformer.getTransformer(Layer.VIEW).getScale();
    	int layoutWidth  = (int)(vv.getWidth() / scale);
    	int layoutHeight = (int)(vv.getHeight() / scale);
        newLayout.setSize(new Dimension(layoutWidth, layoutHeight));
        if (layout == null) {
            // initial layout --> no transition possible!
            vv.setGraphLayout(newLayout);
        } else {
            // No transition possible if no edges in graph!
        	if (newLayout.getGraph().getEdgeCount() > 0 || newLayout.getGraph().getVertexCount() > 0) {	    
            	LayoutTransition<V,E> lt = new LayoutTransition<V,E>(vv, layout, newLayout);
	            Animator animator = new Animator(lt);
	            animator.start();
	        }
        }    
        this.layout = newLayout;
             
        vv.scaleToLayout(this.scaler);
        
    	double viewX = transformer.getTransformer(Layer.VIEW).getTranslateX();
    	double viewY = transformer.getTransformer(Layer.VIEW).getTranslateX();
    	double scaleViewX = viewX * scale;
    	double scaleViewY = viewY * scale;
    	transformer.getTransformer(Layer.VIEW).translate(-scaleViewX, -scaleViewY);
    }
    
    /** VertexLabel is not parameterized in Jung. In order to avoid to make all things
     *  unchecked, the default label position setting is done in this method. */
    @SuppressWarnings("unchecked")
	private void setDefaultLabelPosition() {
        vv.getRenderer().getVertexLabelRenderer().setPosition(Renderer.VertexLabel.Position.CNTR);
    }
    
    public void zoomIn() {
        scaler.scale(vv, 1.1f, vv.getCenter());
    }
    
    public void zoomOut() {
    	scaler.scale(vv, 1/1.1f, vv.getCenter());
    }
    
    public void changeMode(ModalGraphMouse.Mode mode) {
    	graphMouse.setMode(mode);
    	this.currentMode = mode;
    	if (mode.equals(ModalGraphMouse.Mode.PICKING)) {
    		pickingAction.setEnabled(false);
    		transformAction.setEnabled(true);
    	} else {
    		pickingAction.setEnabled(true);
    		transformAction.setEnabled(false);    		
    	}
    }
    
    private void togglePaintEdgeLabels() {
    	this.showEdgeLabels = !this.showEdgeLabels;
    	if (this.showEdgeLabels) {
    		vv.getRenderContext().setEdgeLabelTransformer(new Transformer<E, String>() {
    			public String transform(E object) {
    				return graphCreator.getEdgeName(object);
    			}
    		});
    	} else {
    		vv.getRenderContext().setEdgeLabelTransformer(new Transformer<E, String>() {
    			public String transform(E object) {
    				return null;
    			}
    		});
    	}
    	vv.repaint();
    }
    
    private void togglePaintVertexLabels() {
    	this.showVertexLabels = !this.showVertexLabels;
    	if (this.showVertexLabels) {
            Renderer.Vertex<V, E> vertexRenderer = graphCreator.getVertexRenderer();
            if (vertexRenderer != null)
            	vv.getRenderer().setVertexRenderer(vertexRenderer);
    		vv.getRenderContext().setVertexShapeTransformer(new ExtendedVertexShapeTransformer<V>(graphCreator));
            vv.getRenderContext().setVertexLabelTransformer(new Transformer<V, String>() {
    			public String transform(V object) {
    				return graphCreator.getVertexName(object);
    			}
            });
    	} else {
    		vv.getRenderer().setVertexRenderer(new BasicVertexRenderer<V,E>());
    		vv.getRenderContext().setVertexShapeTransformer(new BasicVertexShapeTransformer<V>());
            vv.getRenderContext().setVertexLabelTransformer(new Transformer<V, String>() {
    			public String transform(V object) {
    				return null;
    			}
            });
    	}
    	vv.repaint();
    }

	public int getRenderHeight(int preferredHeight) {
		return vv.getHeight();
	}

	public int getRenderWidth(int preferredWidth) {
		return vv.getWidth();
	}

	public void render(Graphics graphics, int width, int height) {
		vv.paint(graphics);
	}
}
