/**
 * @author Tomos Slater
 * version 1.0
 * date 27 / 12 / 2017
 */

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.Robot;
import java.awt.Rectangle;
import java.awt.Point;

public class JOGLFrame extends JFrame {

    private JOGLFrame frame; //this frame
    private GLEventListener listener; //the GLEventListener used
    private JLabel pixels, centerOfMass, spawnRadius, cullRadius; //JLabels to hold statistics 
    private int screenshots = 0; //increments each time the output is saved to BMP, so they won't overwrite
      
    /**
     * Constructor to create the JOGLFrame
     * @param colour colour scheme of the aggregation
     * @param seeds seeding scheme of the aggregation
     * @param zoom zoom of the output
     * @param xOffset xOffset of the output
     * @param yOffset yOffset of the output
     * @param connectivity //4 or 8 connectivity 
     * @param shape //shape scheme of the aggregation
     * @throws HeadlessException 
     */
    public JOGLFrame(int colour, int seeds, float zoom, int xOffset, int yOffset, int connectivity, int shape) throws HeadlessException {
        frame = this;
        createStatisticsPanel();
        setTitle("DLA");
        GLProfile glprofile = GLProfile.getDefault();
        GLCapabilities glcapabilities = new GLCapabilities( glprofile );
        final GLCanvas glcanvas = new GLCanvas( glcapabilities );
        glcanvas.setPreferredSize(new Dimension(500,500));
        glcanvas.addGLEventListener(listener = new GLEventListener(colour, seeds, zoom, xOffset, yOffset, connectivity, shape, glcanvas, frame));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        setResizable(false);
        getContentPane().add(glcanvas);
        pack();
    } 
    
    /**
     * Creates a panel to hold the statistics and adds it to the frame
     */
    private void createStatisticsPanel(){
        JPanel statsPanel = new JPanel();
        
        statsPanel.setBackground(Color.black);
        pixels = new JLabel("Pixels:      "); pixels.setForeground(Color.white); statsPanel.add(pixels);
        centerOfMass = new JLabel("Center of Mass:      "); centerOfMass.setForeground(Color.white); statsPanel.add(centerOfMass);
        spawnRadius = new JLabel("Spawn Radius:      "); spawnRadius.setForeground(Color.white); statsPanel.add(spawnRadius);
        cullRadius = new JLabel("Cull Radius:      "); cullRadius.setForeground(Color.white); statsPanel.add(cullRadius);
        
        frame.add(statsPanel, BorderLayout.NORTH);
    }
    
    /**
     * sets the colour of the aggregation
     * @param colour new colour scheme to use
     */
    public void setColour(int colour){
        listener.switchColour(colour);
    }
    
    /**
     * sets the zoom of the output
     * @param zoom new zoom value to use
     */
    public void setZoom(float zoom){
        listener.switchZoom(zoom);
    }
    
    /**
     * sets the xOffset of the output
     * @param xOffset new xOffset to use
     */
    public void setXOffset(int xOffset){
        listener.switchXOffset(xOffset);
    }
    
    /**
     * sets the yOffset of the output
     * @param yOffset new yOffset to use
     */
    public void setYOffset(int yOffset){
        listener.switchYOffset(yOffset);
    }
    
    public void togglePaused(boolean isPaused){
        listener.switchPaused(isPaused);
    }
    
    /**
     * sets the shape scheme of the aggregation
     * @param shape new shape scheme to use
     */
    public void setShape(int shape){
        listener.switchShape(shape);
    }
    
    /**
     * saves the output as a bitmap image file
     */
    public void saveImage(){
        try{
            Point location = this.getLocationOnScreen();
            BufferedImage screenshot = new Robot().createScreenCapture(new Rectangle((int)location.getX() + 5, (int)location.getY() + 5, getWidth() - 10, getHeight() - 10));
            ImageIO.write(screenshot, "BMP", new File("C:/Users/The Slaternator/Desktop/DLAOutput" + screenshots + ".BMP"));
            screenshots++;
        }
        catch(Exception e){
            System.out.println("ERROR SAVING IMAGE!");
        }
    }
    
    /**
     * updates the statistics values
     * @param pixels how may pixels are currently in the aggregation
     * @param xCenter x point of the center of mass
     * @param yCenter y point of the center of mass
     * @param spawnRadius the spawning radius of the pixels
     * @param cullRadius  the culling radius of the pixels
     */
    public void updateStatistics(int pixels, int xCenter, int yCenter, int spawnRadius, int cullRadius){
        this.pixels.setText("Pixels: " + pixels + "     ");
        this.centerOfMass.setText("Center of Mass: [" + xCenter + "," + yCenter + "]     ");
        this.spawnRadius.setText("Spawn Radius: " + spawnRadius + "     ");
        this.cullRadius.setText("Cull Radius: " + cullRadius + "     ");
    }
}