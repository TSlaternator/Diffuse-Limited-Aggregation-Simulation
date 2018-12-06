/**
 * @author Tomos Slater
 * version 1.0
 * date 27 / 12 / 2017
 */

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ControlFrame extends JFrame{
    
    private JPanel runPanel; //panel to contain the user controls
    private JButton runButton, pauseButton, saveButton; 
    private JSlider slider;
    private JOGLFrame frame; //the output frame
    private JMenuBar menuBar;
    private JMenu colourMenu, seedMenu, connectivityMenu, shapeMenu;
    private int colour = 0; //int to control colour scheme
    private int seeds = 1; //int to control seed scheme
    private float zoom = 1; //float to control how 'zoomed in' the output is
    private int connectivity = 4; //int to control the connectivity of the aggregation
    private int xCenter = 0; //int to control x offset of the output
    private int yCenter = 0; //int to control y offset of the output
    private boolean started = false; //denotes whether the output has started
    private boolean isPaused = false; //deonotes if the program is paused or not
    private int shape = 0; //int to control which shape to build the aggregation from
    
    /**
     * Constructor to create my control frame
     */
    public ControlFrame(){        
        createRunPanel();
        setSize(265, 240);
    }
    
    /**
     * creates the user control panel, and it's buttons / sliders / menus
     */
    private void createRunPanel(){
        runPanel = new JPanel();
        runPanel.setLayout(new FlowLayout());
        
        runPanel.add(menuBar = new JMenuBar());
        
        menuBar.add(colourMenu = new JMenu("Colours"));
        menuBar.add(seedMenu = new JMenu("Seeds"));
        menuBar.add(connectivityMenu = new JMenu("Connectivity"));
        menuBar.add(shapeMenu = new JMenu("Shapes"));
        
        colourMenu.add(createMenuItem("gray", "colour", 0));
        colourMenu.add(createMenuItem("blue", "colour", 1));
        colourMenu.add(createMenuItem("green", "colour", 2));
        colourMenu.add(createMenuItem("red", "colour", 3));
        colourMenu.add(createMenuItem("blue-green", "colour", 4));
        colourMenu.add(createMenuItem("blue-red", "colour", 5));
        colourMenu.add(createMenuItem("red-yellow", "colour", 6));
        colourMenu.add(createMenuItem("rainbow", "colour", 7));
        
        seedMenu.add(createMenuItem("1 seed", "seeds", 1));
        seedMenu.add(createMenuItem("2 seeds", "seeds", 2));
        seedMenu.add(createMenuItem("3 seeds", "seeds", 3));
        seedMenu.add(createMenuItem("4 seeds", "seeds", 4));
        seedMenu.add(createMenuItem("inverse 4", "seeds", -4));
        seedMenu.add(createMenuItem("line", "seeds", 5));
        
        connectivityMenu.add(createMenuItem("4 connectivity", "connections", 4));
        connectivityMenu.add(createMenuItem("8 connectivity", "connections", 8));
        
        shapeMenu.add(createMenuItem("squares", "shapes", 0));
        shapeMenu.add(createMenuItem("rhombus", "shapes", 1));
        shapeMenu.add(createMenuItem("circles", "shapes", 2));
        shapeMenu.add(createMenuItem("variable size (squares)", "shapes", 3));
        
        runPanel.add(runButton = createJButton("RUN"));
        runPanel.add(pauseButton = createJButton("PAUSE"));
        runPanel.add(saveButton = createJButton("SAVE IMAGE"));
        
        runPanel.add(new JLabel("    Zoom")); slider = createJSlider(0, 25, 5, 1, "zoom"); runPanel.add(slider);
        runPanel.add(new JLabel("X Offset")); slider = createJSlider(-25, 25, 0, 1, "xOffset"); runPanel.add(slider);
        runPanel.add(new JLabel("Y Offset")); slider = createJSlider(-25, 25, 0, 1, "yOffset"); runPanel.add(slider);
                
        add(runPanel);
    }
    
    /**
     * creates a menu item, with an action listener to control it's effect
     * @param label the label of the menu item
     * @param variableType the type of variable the menu item controls
     * @param value the value this item switches the variableType to hold
     * @return the JMenuItem component
     */
    private JMenuItem createMenuItem(String label, String variableType, int value){
        JMenuItem menuItem = new JMenuItem(label);
        class PressListener implements ActionListener{
            public void actionPerformed (ActionEvent event){
                if(variableType.equals("colour")){
                    colour = value; 
                    if(started) frame.setColour(colour);
                }
                else if(variableType.equals("seeds")){
                    seeds = value; 
                }
                else if(variableType.equals("connections")){
                    connectivity = value;
                }
                else if(variableType.equals("shapes")){
                    shape = value;
                    if(started) frame.setShape(shape);
                }
            }
        }     
        ActionListener listener = new PressListener();
        menuItem.addActionListener(listener);
        return menuItem;
    }
    
    /**
     * creates a JButton, with an action listener to control it's effect
     * @param label the label of the button
     * @return the JButton component
     */
    private JButton createJButton(String label){
        JButton button = new JButton(label);
        class PressListener implements ActionListener{
            public void actionPerformed (ActionEvent event){
                if (label.equals("RUN")){
                frame = new JOGLFrame(colour, seeds, zoom, xCenter, yCenter, connectivity, shape);
                started = true;
                }
                else if (label.equals("PAUSE")){
                    if (started){
                        if (!isPaused) isPaused = true;
                        else isPaused = false;
                        frame.togglePaused(isPaused);
                    }
                }
                else if (label.equals("SAVE IMAGE")){
                    if (started){
                        frame.saveImage();
                    }
                }
            }
        }
        ActionListener  listener = new PressListener();
        button.addActionListener(listener);
        return button;
    }
    
    /**
     * Creates a JSlider with a change listener to control it's effect
     * @param min minimum value of the slider
     * @param max maximum value of the slider
     * @param start start value of the slider
     * @param spacing spacing between each minor tick
     * @param type denotes which variable the slider controls (zoom / x offset / y offset)
     * @return the JSlider component
     */
    private JSlider createJSlider(int min, int max, int start, int spacing, String type){
        final float ZOOM_MODIFIER = 0.2f; //modifies zoom slider output 
        final float MIN_ZOOM = 0.2f; //minimum value of zoom (stops zoom being 0)
        final int OFFSET_MODIFIER = 10; //modifies offset slider output
        int majorSpacing = spacing * 5; //major tick every 5 minor ticks
        
        JSlider newSlider = new JSlider(min, max, start);
        newSlider.setPaintTicks(true);
        newSlider.setPaintLabels(true);
        newSlider.setMajorTickSpacing(majorSpacing);
        newSlider.setMinorTickSpacing(spacing);
        
        class sliderListener implements ChangeListener{
            public void stateChanged(ChangeEvent event){
                if (type.equals("zoom")){
                    zoom = (newSlider.getValue() * ZOOM_MODIFIER);
                    if (zoom == 0) zoom = MIN_ZOOM;
                    if (started) frame.setZoom(zoom);
                }
                else if (type.equals("xOffset")){
                    xCenter = newSlider.getValue();
                    if (started) frame.setXOffset(xCenter * OFFSET_MODIFIER);
                }
                else if (type.equals("yOffset")){
                    yCenter = newSlider.getValue();
                    if (started) frame.setYOffset(yCenter * OFFSET_MODIFIER);
                }
            }
        }
        
        ChangeListener listener = new sliderListener();
        newSlider.addChangeListener(listener);
        return newSlider;
    }
}
