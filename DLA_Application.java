/**
 * @author Tomos Slater
 * version 1.0
 * date 27 / 12 / 2017
 */

import javax.swing.JFrame;

public class DLA_Application {
    
    public static void main(String[] args){
        
        //creating my frame
        ControlFrame controlFrame = new ControlFrame();

        //setting up the frames attributes
        controlFrame.setTitle("DLA Application");
        controlFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        controlFrame.setResizable(false);
        controlFrame.setVisible(true);
    }
}
