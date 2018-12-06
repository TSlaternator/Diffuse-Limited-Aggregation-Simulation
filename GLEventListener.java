/**
 * @author Tomos Slater
 * version 1.0
 * date 27 / 12 / 2017
 */

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.glu.GLU;
import java.util.Random;

public class GLEventListener implements com.jogamp.opengl.GLEventListener{

    private Random random; //random function
    private JOGLFrame frame; //the JOGLframe containing this GLEventListener
    private GLAutoDrawable GLAD; //the autodrawable being used#
    private int[][] grid; //grid to hold the 'stuck' positions
    private int[] currentPos; //coordinates of the current pixels position
    private boolean stuck, outOfBounds, isPaused, inverse;
    private int width, height, halfWidth, halfHeight; //width / height / half vals of the frame
    private int colour, seeds, xOffset, yOffset, connectivity, shape, startPixels;
    private int unit = 1; //width of one pixel / shape
    private int spawnRadius = 25; //current spawning radius
    private int minSpawnRadius = 25; //minimum spawning radius
    private int minCullRadius = 50; //minimum culling radius
    private int cullRadius = 50; //current culling radius
    private int totalPixels = 12500; //how many pixels to aggregate
    private int pixelsPerRefresh = 10; //how many pixels to stick before repainting
    private float zoom = 1; //how zoomed in the output should be
    private float sizeModifier = 0.995f; //controls size of the pixels if variableSize method is being used
    
    /**
     * Constructor to take parameters from the user control frame
     * @param colour colour scheme to be used
     * @param seeds seed scheme to be used
     * @param zoom how zoomed in the output will be
     * @param xOffset xOffset of the output
     * @param yOffset yOffset of the output
     * @param connectivity either 4 or 8 connectivity
     * @param shape shape scheme being used (squares, rhombus, circle, variable size)
     * @param GLAD the autodrawable being used
     * @param frame the frame to contain the output
     */
    public GLEventListener(int colour, int seeds, float zoom, int xOffset, int yOffset, int connectivity, int shape, GLAutoDrawable GLAD, JOGLFrame frame){
        this.colour = colour;
        this.seeds = seeds;
        this.zoom = zoom;
        this.connectivity = connectivity;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.shape = shape;
        this.GLAD = GLAD;
        this.frame = frame;
    }
    
    /**
     * sets up the graphics settings
     * @param gl2 graphics object being used
     * @param width width of the frame
     * @param height height of the frame
     */
    protected void setup(GL2 gl2, int width, int height) {
        gl2.glMatrixMode(GL2.GL_PROJECTION);
        gl2.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluOrtho2D(0.0f, width, 0.0f, height);

        gl2.glMatrixMode(GL2.GL_MODELVIEW);
        gl2.glLoadIdentity();

        gl2.glViewport(0, 0, width, height);
    }

    /**
     * overarching algorithm to control the DLA
     */
    public void DLA(){
        //using a thread to control the DLA so the interface controls can still be used
        new Thread() {
            @Override
            public void run(){
                //setting up the grid, random function and pixels amount
                grid = new int[width * 10][height * 10];
                random = new Random();
                if (seeds == -4) totalPixels *= 2;
                
                //creating the seeds
                if (seeds == 1){
                    grid[250][250] = 1;
                    startPixels = 1;
                }
                else if (seeds == 2){
                    grid[230][250] = 1;
                    grid[270][250] = 1;
                    startPixels = 2;
                }
                else if (seeds == 3){
                    grid[250][270] = 1;
                    grid[235][235] = 1;
                    grid[265][235] = 1;
                    startPixels = 3;
                }
                else if (seeds == 4){
                    grid[235][235] = 1;
                    grid[235][265] = 1;
                    grid[265][235] = 1;
                    grid[265][265] = 1;
                    startPixels = 4;
                }
                else if (seeds == -4){
                    inverse = true;
                    spawnRadius = 200 - minSpawnRadius;
                    cullRadius = 200 - minCullRadius;
                    grid[50][50] = 1;
                    grid[50][450] = 1;
                    grid[450][50] = 1;
                    grid[450][450] = 1;
                    startPixels = 4;
                }
                else if (seeds == 5){
                    for(int i = 0; i < width; i++){
                        grid[i][0] = 1;
                    }
                    startPixels = 500;
                }
                
                //loop containing the main algorithm
                for(int i = 0; i < totalPixels; i++){
                    //generates a new pixel with random spawn position
                    currentPos = spawnNewSquare(width, height);
                    stuck = false;
                    //whilst not paused, aggregate!
                    if (!isPaused){
                        //whilst not stuck, move the pixel
                        while(!stuck){
                            if (!isStuck(currentPos)){
                                move(currentPos);
                                //if a pixel moves outside the culling radius, spawns a new pixel to replace it
                                if (outOfBounds){
                                    outOfBounds = false;
                                    stuck = true;
                                    i--;
                                }
                            }
                            //if a pixel sticks, update the spawn and cull radius', the grid, and (sometimes) repaint
                            else{
                                stuck = true;
                                if (!inverse){
                                    spawnRadius = (int)(((float)i / (float)totalPixels) * (halfWidth - minSpawnRadius) + minSpawnRadius);
                                    cullRadius = (int)(((float)i / (float)totalPixels) * (halfWidth - minSpawnRadius) + minCullRadius);
                                }
                                else if(seeds == 5){
                                    spawnRadius = (int)(((float)i / (float)totalPixels * (width - minSpawnRadius) + minSpawnRadius));
                                    cullRadius = (int)(((float)i / (float)totalPixels * (width - minSpawnRadius) + minCullRadius));
                                }
                                else{
                                    spawnRadius = halfWidth - (int)(((float)i / totalPixels) * (halfWidth - minSpawnRadius) + minSpawnRadius);
                                    cullRadius = halfWidth - (int)(((float)i / totalPixels) * (halfWidth - minSpawnRadius) + minCullRadius);
                                }
                                grid[currentPos[0]][currentPos[1]] = 1;
                                updateUI(i);
                                if(i % pixelsPerRefresh == 0)
                                    GLAD.display(); 
                            }
                        }
                    }
                    else i--;
                }
            }
        }.start();
    }
    
    /**
     * Draws the current output
     * @param gl2 graphics object being used
     * @param unit width of the pixels
     */
    protected void render(GL2 gl2,int unit) {
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
        gl2.glLoadIdentity();
        
        //loops through the grid and paints a shape where the grid contains a 1
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                setColour(gl2, i, j);
                if (grid[i][j] == 1){
                    if(shape == 3){
                        newVariableSizeShape(gl2, i, j, unit);
                    }
                    else{
                        newShape(gl2, i, j, unit);
                    }
                }
            }
        }
    }
    
    /**
     * changes the colour scheme of the aggregation and repaints the output
     * @param colour colour scheme to be used
     */
    public void switchColour(int colour){
        this.colour = colour;
        GLAD.display();
    }
    
    /**
     * changes the zoom of the output and repaints it
     * @param zoom new zoom value to be used
     */
    public void switchZoom(float zoom){
        this.zoom = zoom;
        GLAD.display();
    }
    
    /**
     * changes the xOffset of the output and repaints it
     * @param xOffset new xOffset to be used
     */
    public void switchXOffset(int xOffset){
        this.xOffset = xOffset;
        GLAD.display();
    }
    
    /**
     * changes the yOffset of the output and repaints it
     * @param yOffset new yOffset to be used
     */
    public void switchYOffset(int yOffset){
        this.yOffset = yOffset;
        GLAD.display();
    }
    
    /**
     * pauses (or un-pauses) the aggregation
     * @param isPaused whether to pause (true) or resume (false)
     */
    public void switchPaused(boolean isPaused){
        this.isPaused = isPaused;
    }
    
    /**
     * changes the shape scheme of the aggregation and repaints the output
     * @param shape new shape to use
     */
    public void switchShape(int shape){
        this.shape = shape;
        GLAD.display();
    }
    
    /**
     * draws a single shape to the frame (called for every '1' in the grid)
     * @param gl2 graphics objects
     * @param x x value of the left of the shape
     * @param y y value of the bottom of the shape
     * @param squareWidth width of the shape
     */
    private void newShape(GL2 gl2, float x, float y,  float squareWidth){
        squareWidth *= zoom;
        //changing size / position based on the zoom and offset variables
        x -= (float)halfWidth;
        y -= (float)halfHeight;
        x *= squareWidth;
        y *= squareWidth;
        x += (float)halfWidth + xOffset * zoom;
        y += (float)halfHeight + yOffset * zoom;

        //drawing the shapes
        switch(shape){
            case 0: //a square
                gl2.glBegin(9);
                gl2.glVertex2f(x, y);
                gl2.glVertex2f(x + squareWidth, y);
                gl2.glVertex2f(x + squareWidth, y + squareWidth);
                gl2.glVertex2f(x, y + squareWidth);
                gl2.glEnd();
                break;
        
            case 1:  //a rhombus
                gl2.glBegin(9);
                gl2.glVertex2f(x, y + (squareWidth / 2f));
                gl2.glVertex2f(x + (squareWidth / 2f), y + squareWidth);
                gl2.glVertex2f(x + squareWidth, y + (squareWidth / 2f));
                gl2.glVertex2f(x + (squareWidth / 2f), y);
                gl2.glEnd();  
                break;
        
            case 2: //a circle
                gl2.glBegin(9);
                gl2.glVertex2f(x, y + (squareWidth / 2f));
                gl2.glVertex2f(x + (squareWidth / 5f), y + squareWidth);
                gl2.glVertex2f(x + ((4f * squareWidth) / 5f), y + squareWidth);
                gl2.glVertex2f(x + squareWidth, y + (squareWidth / 2f));
                gl2.glVertex2f(x + ((4 * squareWidth) / 5f), y);
                gl2.glVertex2f(x + squareWidth / 5f, y);
                gl2.glEnd();  
                break;
        }
    }
    
    /**
     * draws a single shape when a '1' is found in the grid
     * @param gl2 graphics object
     * @param x x point of the left side
     * @param y y point of the bottom side
     * @param squareWidth (default)width of the shape 
     */
    private void newVariableSizeShape(GL2 gl2, float x, float y,  float squareWidth){
        float xSquareWidth = squareWidth, ySquareWidth = squareWidth;
        
        //changes the x and y width values based on their distance from the middle (smaller towards the outside)
        xSquareWidth *= (float)Math.pow(sizeModifier, (float)Math.abs(halfWidth - x)) * zoom;
        ySquareWidth *= (float)Math.pow(sizeModifier, (float)Math.abs(halfWidth - y)) * zoom;
      
        //changing size and position based on the zoom / offset variables
        x -= (float)halfWidth;
        y -= (float)halfHeight;
        x *= xSquareWidth;
        y *= ySquareWidth;
        x += (float)halfWidth + xOffset * zoom;
        y += (float)halfHeight + yOffset * zoom;
        
        //draws the square
        gl2.glBegin(9);
        gl2.glVertex2f(x, y);
        gl2.glVertex2f(x + xSquareWidth, y);
        gl2.glVertex2f(x + xSquareWidth, y + ySquareWidth);
        gl2.glVertex2f(x, y + ySquareWidth);
        gl2.glEnd();
    }
    
    /**
     * moves a pixel in a random direction
     * @param currentPos current position of the pixel
     * @return new position of the pixel
     */
    private int[] move(int[] currentPos){
        //generates a random direction (up to 4 or 8 based on connectivity scheme)
        int moveDirection = random.nextInt(connectivity);
        
        if (connectivity == 4){
            switch(moveDirection){
                case 0: currentPos[1] += 1; break; //up
                case 1: currentPos[0] += 1; break; //right
                case 2: currentPos[1] -= 1; break; //down
                case 3: currentPos[0] -= 1; break; //left
                default: System.out.println("INVALID DIRECTION"); //error handling
            }
        }
        else if (connectivity == 8){
            switch(moveDirection){
                case 0: currentPos[1] += 1; break; //up
                case 1: currentPos[0] += 1; break; //right
                case 2: currentPos[1] -= 1; break; //down
                case 3: currentPos[0] -= 1; break; //left
                case 4: currentPos[0] += 1; currentPos[1] += 1; break; //up + right
                case 5: currentPos[0] -= 1; currentPos[1] += 1; break; //up + left
                case 6: currentPos[0] += 1; currentPos[1] -= 1; break; //down + right
                case 7: currentPos[0] -= 1; currentPos[1] -= 1; break; //down + left
                default: System.out.println("INVALID DIRECTION"); //error handling
            }
        }
        
        //signals the pixel to be culled if it's outside the culling radius (or inside, for inverse seeds)
        if (!inverse && seeds != 5 && Math.sqrt(Math.pow(currentPos[0] - halfWidth, 2) + Math.pow(currentPos[1] - halfHeight, 2)) >= cullRadius){
            outOfBounds = true;
        }
        else if (inverse && Math.sqrt(Math.pow(currentPos[0] - halfWidth, 2) + Math.pow(currentPos[1] - halfHeight, 2)) <= cullRadius){
            outOfBounds = true;
        }        
        else if(seeds == 5 && currentPos[1] >= cullRadius){
            outOfBounds = true;
        }
        
        //ensures pixel stays within the grid (only used if the cullRadius is outside of the frame
        if (currentPos[0] < 0) currentPos[0] = 0;
        else if (currentPos[0] > width) currentPos[0] = width;
        if (currentPos[1] < 0) currentPos[1] = 0;
        else if (currentPos[1] > height) currentPos[1] = height; 
        return currentPos;  
    }
    
    /**
     * determines whether a pixel is stuck or not
     * @param currentPos position of the pixel
     * @return boolean to flag (true) if the pixel is stuck
     */
    private boolean isStuck(int[] currentPos){
        boolean isStuck = false;
        
        //need to check up, down, left, right directions for 4 connectivity
        if (connectivity == 4){
            if (checkGrid(currentPos[0] + 1, currentPos[1])) isStuck = true;
            else if (checkGrid(currentPos[0], currentPos[1] + 1)) isStuck = true;
            else if (checkGrid(currentPos[0] - 1, currentPos[1])) isStuck = true;
            else if (checkGrid(currentPos[0], currentPos[1] - 1)) isStuck = true;
        } //need to check diagonals also, for 8 connectivity
        else if (connectivity == 8){
            if (checkGrid(currentPos[0] + 1, currentPos[1])) isStuck = true;
            else if (checkGrid(currentPos[0], currentPos[1] + 1)) isStuck = true;
            else if (checkGrid(currentPos[0] - 1, currentPos[1])) isStuck = true;
            else if (checkGrid(currentPos[0], currentPos[1] - 1)) isStuck = true;
            
            else if (checkGrid(currentPos[0] + 1, currentPos[1] + 1)) isStuck = true;
            else if (checkGrid(currentPos[0] - 1, currentPos[1] + 1)) isStuck = true;
            else if (checkGrid(currentPos[0] + 1, currentPos[1] - 1)) isStuck = true;
            else if (checkGrid(currentPos[0] - 1, currentPos[1] - 1)) isStuck = true;
        }
        return isStuck;
    }
    
    /**
     * spawns a new square (pixel), calls when the previous one is culled, or sticks
     * @param width of the frame
     * @param height of the frame
     * @return position of the new square (pixel)
     */
    private int[] spawnNewSquare(int width, int height){
        int[] spawnPos = new int[2];
        
        /*if the seeds variable is not 5 (line of seeds at the bottom), spawns
        the new pixel on the spawnRadius at a random angle*/
        if (seeds != 5){
            int spawnAngle = random.nextInt(360);
            spawnPos[0] = (int)(Math.cos(spawnAngle) * spawnRadius) + halfWidth;
            spawnPos[1] = (int)(Math.sin(spawnAngle) * spawnRadius) + halfHeight;
        }
        else{ //spawns the pixel with a random x position, with a y position on the 'radius'
            spawnPos[0] = random.nextInt(width);
            spawnPos[1] = spawnRadius;
        }
               
        //ensures the spawnposition is within the bounds of the frame
        if (spawnPos[0] < 0) spawnPos[0] = 0;
        else if (spawnPos[0] > width) spawnPos[0] = width;
        if (spawnPos[1] < 0) spawnPos[1] = 0;
        else if (spawnPos[1] > height) spawnPos[1] = height;
        
        return spawnPos;
    }
    
    /**
     * checks to see if a point in the grid is a 1 or not
     * @param xPos xposition of the point
     * @param yPos yposition of the point
     * @return 
     */
    private boolean checkGrid(int xPos, int yPos){
        boolean check = false;
        //ensures the point being checked is within the grid (preventing arrayoutofbounds error)
        if (xPos < 0) xPos = 0;
        if (xPos > width) xPos = width;
        if (yPos < 0) yPos = 0;
        if (yPos > height) yPos = height;
        //checks the grid position
        if (grid[xPos][yPos] == 1) check = true;
        return check;
    }
    
    /**
     * colours the shape being drawn
     * @param gl2 graphics object
     * @param xPos xposition of the shape
     * @param yPos yposition of the shape
     */
    private void setColour(GL2 gl2,int xPos, int yPos){
        //if any seed method except the line is being used, colour based on distance to frame edge
        if (seeds != 5){
            int closest;
            int xDist = Math.abs(halfWidth - xPos);
            int yDist = Math.abs(halfHeight - yPos);
            if (xDist >= yDist) closest = xDist;
            else closest = yDist;

            switch(colour){
                case 0: gl2.glColor3f(1f / (((float)closest + 1) / 50), 1f / (((float)closest + 1) / 50), 1f / (((float)closest + 1) / 50)); break;
                case 1: gl2.glColor3f(0, 0, 1f / (((float)closest + 1) / 50)); break; //blue
                case 2: gl2.glColor3f(0, 1f / (((float)closest + 1) / 50), 0); break; //green
                case 3: gl2.glColor3f(1f / (((float)closest + 1) / 50), 0, 0); break; //red
                case 4: gl2.glColor3f(0, (0.04f * (((float)closest + 1) / 10)), 1f / (((float)closest + 1) / 40)); break;//blue - green
                case 5: gl2.glColor3f((0.04f * (((float)closest + 1) / 10)), 0, 1f / (((float)closest + 1) / 40)); break;//blue - red
                case 6: gl2.glColor3f(1f, (0.04f * (((float)closest + 1) / 10)), 0f); break; //red - yellow
                case 7: //rainbow
                    if(closest < 10)gl2.glColor3f(1f, 0f, 0f);
                    else if(closest < 20)gl2.glColor3f(1f, 0.2f, 0f);
                    else if(closest < 30)gl2.glColor3f(1f, 0.4f, 0f);
                    else if(closest < 40)gl2.glColor3f(1f, 0.6f, 0f);
                    else if(closest < 50)gl2.glColor3f(1f, 0.8f, 0f);
                    else if(closest < 60)gl2.glColor3f(1f, 1f, 0f);
                    else if(closest < 70)gl2.glColor3f(0.8f, 1f, 0f);
                    else if(closest < 80)gl2.glColor3f(0.6f, 1f, 0f);
                    else if(closest < 90)gl2.glColor3f(0.4f, 1f, 0f);
                    else if(closest < 100)gl2.glColor3f(0.2f, 1f, 0f);
                    else if(closest < 110)gl2.glColor3f(0f, 1f, 0f);
                    else if(closest < 120)gl2.glColor3f(0f, 0.8f, 0.2f);
                    else if(closest < 130)gl2.glColor3f(0f, 0.6f, 0.4f);
                    else if(closest < 140)gl2.glColor3f(0f, 0.4f, 0.6f);
                    else if(closest < 150)gl2.glColor3f(0f, 0.2f, 0.8f);
                    else if(closest < 160)gl2.glColor3f(0f, 0f, 1f);
                    else if(closest < 170)gl2.glColor3f(0.2f, 0f, 0.8f);
                    else if(closest < 180)gl2.glColor3f(0.4f, 0f, 0.6f);
                    else if(closest < 190)gl2.glColor3f(0.6f, 0f, 0.5f);
                    else if(closest < 200)gl2.glColor3f(0.8f, 0f, 0.6f);
                    else gl2.glColor3f(1f, 0f, 0.8f);
            }
        }
        else{ //if the line of seeds is being used, colour based on distance from the bottom
            switch(colour){
                case 0: gl2.glColor3f(1f / (((float)yPos + 1) / 100), 1f / (((float)yPos + 1) / 100), 1f / (((float)yPos + 1) / 100)); break;
                case 1: gl2.glColor3f(0, 0, 1f / (((float)yPos + 1) / 100)); break; //blue
                case 2: gl2.glColor3f(0, 1f / (((float)yPos + 1) / 100), 0); break; //green
                case 3: gl2.glColor3f(1f / (((float)yPos + 1) / 100), 0, 0); break; //red
                case 4: gl2.glColor3f(0, (0.04f * (((float)yPos + 1) / 10)), 1f / (((float)yPos + 1) / 40)); break;//blue - green
                case 5: gl2.glColor3f((0.04f * (((float)yPos + 1) / 10)), 0, 1f / (((float)yPos + 1) / 40)); break;//blue - red
                case 6: gl2.glColor3f(1f, 1f / (((float)yPos + 1) / 40), 0); break; //gren - red
                case 7: //rainbow
                    if(yPos < 11)gl2.glColor3f(1f, 0f, 0f);
                    else if(yPos < 22)gl2.glColor3f(1f, 0.2f, 0f);
                    else if(yPos < 33)gl2.glColor3f(1f, 0.4f, 0f);
                    else if(yPos < 44)gl2.glColor3f(1f, 0.6f, 0f);
                    else if(yPos < 55)gl2.glColor3f(1f, 0.8f, 0f);
                    else if(yPos < 66)gl2.glColor3f(1f, 1f, 0f);
                    else if(yPos < 77)gl2.glColor3f(0.8f, 1f, 0f);
                    else if(yPos < 88)gl2.glColor3f(0.6f, 1f, 0f);
                    else if(yPos < 99)gl2.glColor3f(0.4f, 1f, 0f);
                    else if(yPos < 110)gl2.glColor3f(0.2f, 1f, 0f);
                    else if(yPos < 121)gl2.glColor3f(0f, 1f, 0f);
                    else if(yPos < 132)gl2.glColor3f(0f, 0.8f, 0.2f);
                    else if(yPos < 143)gl2.glColor3f(0f, 0.6f, 0.4f);
                    else if(yPos < 154)gl2.glColor3f(0f, 0.4f, 0.6f);
                    else if(yPos < 165)gl2.glColor3f(0f, 0.2f, 0.8f);
                    else if(yPos < 176)gl2.glColor3f(0f, 0f, 1f);
                    else if(yPos < 187)gl2.glColor3f(0.2f, 0f, 0.8f);
                    else if(yPos < 198)gl2.glColor3f(0.4f, 0f, 0.6f);
                    else if(yPos < 209)gl2.glColor3f(0.6f, 0f, 0.5f);
                    else if(yPos < 220)gl2.glColor3f(0.8f, 0f, 0.6f);
                    else gl2.glColor3f(1f, 0f, 0.8f);
            }
        }
    }
    
    /**
     * updates the UI to show statistics
     * @param pixels how many pixels are in the aggregation
     */
    private void updateUI(int pixels){
        float xCenter = 0, yCenter = 0;
        
        //getting the center of mass
        for(int i = 0; i < width; i++){
            for(int j = 0; j < height; j++){
                if(grid[i][j] == 1){
                    xCenter += (float)i;
                    yCenter += (float)j;
                }
            }
        }
        xCenter /= (pixels + startPixels);
        yCenter /= (pixels + startPixels);
        
        frame.updateStatistics(pixels, (int)xCenter, (int)yCenter, spawnRadius, cullRadius);
    }
    
    @Override
    public void reshape(GLAutoDrawable glautodrawable, int x, int y, int width, int height) {
        setup(glautodrawable.getGL().getGL2(), width, height);
    }

    @Override
    public void init(GLAutoDrawable glautodrawable) {
        width = glautodrawable.getSurfaceWidth();
        height = glautodrawable.getSurfaceHeight();
        halfWidth = width / 2;
        halfHeight = height / 2;
        DLA();
    }

    @Override
    public void dispose(GLAutoDrawable glautodrawable) {
    }

    @Override
    public void display(GLAutoDrawable glautodrawable) {
        render(glautodrawable.getGL().getGL2(), unit);
    }

}
