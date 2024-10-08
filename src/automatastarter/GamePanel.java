/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package automatastarter;

import java.awt.Color;
import utils.CardSwitcher;
import utils.ImageUtil;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import utils.Main;
import static utils.Main.initializeGrid;

/**
 *
 * @author michael.roy-diclemen
 */
public class GamePanel extends javax.swing.JPanel implements MouseListener {
    
    public static final String CARD_NAME = "game";

    CardSwitcher switcher; // This is the parent panel
    Timer animTimer;
    // Image img1 = Toolkit.getDefaultToolkit().getImage("yourFile.jpg");
    BufferedImage img1;
    //variables to control your animation elements
    int x = 0;
    int y = 0;
    int xdir = 5;
    int lineX = 0;
    
    //initialize variables
    static final int GRID_SIZE = 20;
    static final int PREY_COUNT = 100;
    static final int PREDATOR_COUNT = 10;
    static final int FOOD_COUNT = 50;
    
    static final int MAX_STEPS_WITHOUT_PREY = 20;
    static final double PREY_REPRODUCTION_RATE = 0.10;
    static final double PREDATOR_REPRODUCTION_RATE = 0.05;
    static final int CELL_SIZE = 30;
    //initialize grid
    static int [][] grid = new int[GRID_SIZE][GRID_SIZE];
    static int[][] stepsWithoutFood = new int[GRID_SIZE][GRID_SIZE];
    
    static Random r = new Random();
    
    /**
     * Creates new form GamePanel
     */
    public GamePanel(CardSwitcher p) {
        initComponents();

        img1 = ImageUtil.loadAndResizeImage("yourFile.jpg", 300, 300);//, WIDTH, HEIGHT)//ImageIO.read(new File("yourFile.jpg"));

        this.setFocusable(true);

        // tell the program we want to listen to the mouse
        addMouseListener(this);
        //tells us the panel that controls this one
        switcher = p;
        //create and start a Timer for animation
        animTimer = new Timer(10, new AnimTimerTick());
        animTimer.start();

        //set up the key bindings
        //setupKeys();

    }

//    private void setupKeys() {
//        //these lines map a physical key, to a name, and then a name to an 'action'.  You will change the key, name and action to suit your needs
//        this.getInputMap().put(KeyStroke.getKeyStroke("LEFT"), "leftKey");
//        this.getActionMap().put("leftKey", new Move("LEFT"));
//
//        this.getInputMap().put(KeyStroke.getKeyStroke("W"), "wKey");
//        this.getActionMap().put("wKey", new Move("w"));
//
//        this.getInputMap().put(KeyStroke.getKeyStroke("D"), "dKey");
//        this.getActionMap().put("dKey", new Move("d"));
//
//        this.getInputMap().put(KeyStroke.getKeyStroke("X"), "xKey");
//        this.getActionMap().put("xKey", new Move("x"));
//    }
    
    public static void initializeGrid() {
        //generate prey in random spaces on the grid
        for (int i = 0; i < PREY_COUNT; i++) {
            int x, y;
            do {
                x = r.nextInt(GRID_SIZE);
                y = r.nextInt(GRID_SIZE);
            } while (grid[x][y] != 0);
            grid[x][y] = 1;
        }
        //generate predators in random spaces on the grid
        for (int i = 0; i < PREDATOR_COUNT; i++) {
            int x, y;
            do {
                x = r.nextInt(GRID_SIZE);
                y = r.nextInt(GRID_SIZE);
            } while (grid[x][y] != 0);
            grid [x][y] = 2;
        }
        //generate food
        for (int i = 0; i < FOOD_COUNT; i++) {
            int x = r.nextInt(GRID_SIZE);
            int y = r.nextInt(GRID_SIZE);
            grid[x][y] = 3; 
        }
        //fill stepsWithoutFood with 0
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                stepsWithoutFood[i][j] = 0;  // Reset food counter
            }
        }
    }
    /**
     * choose a random adjacent cell to move to
     * @param x
     * @param y
     * @return 
     */
    static int[] getAdjacent(int x, int y) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};

        int d = r.nextInt(4); 
        //toroidal grid
        int newX = (x + dx[d] + GRID_SIZE) % GRID_SIZE;
        int newY = (y + dy[d] + GRID_SIZE) % GRID_SIZE;
        
        int[] newPos = {newX, newY};
        return newPos;
    }
    static void movePrey() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for(int j = 0; j < GRID_SIZE; j++) {
                //find prey
                if (grid[i][j] == 1) {
                    //move to an adjacent spot
                    int[] newPos = getAdjacent(i, j);
                    if (grid[newPos[0]][newPos[1]] == 0) {
                        grid[newPos[0]][newPos[1]] = grid[i][j];
                        grid[i][j] = 0;
                    }
                }   
            }
        }
    }
    /**
     * begin moving towards to target position from current position
     * @param x
     * @param y
     * @param target
     * @return 
     */
    static int[] moveTowards(int x, int y, int[] target) {
        //find difference between original and target coordinates
        int dx = target[0] - x;
        int dy = target[1] - y;

        // Adjust for wrap-around on a toroidal grid
        if (dx > GRID_SIZE / 2) dx -= GRID_SIZE;
        if (dx < -GRID_SIZE / 2) dx += GRID_SIZE;
        if (dy > GRID_SIZE / 2) dy -= GRID_SIZE;
        if (dy < -GRID_SIZE / 2) dy += GRID_SIZE;

        // Determine movement direction
        int moveX = 0;
        if (dx > 0) moveX = 1;
        else if (dx < 0) moveX = -1;

        int moveY = 0;
        if (dy > 0) moveY = 1;
//        else if (dy < 0) moveY = -1;

        // Calculate new position with wrap-around
        int newX = (x + moveX + GRID_SIZE) % GRID_SIZE;
        int newY = (y + moveY + GRID_SIZE) % GRID_SIZE;
        
        int [] newPos = {newX, newY};
        return newPos;
    }
    static void movePredator () {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                //if predator is found
                if (grid[i][j] == 2) {
                    int[] preyPos = findClosestPrey(i, j);
                    int[] newPos;
                    if (preyPos != null) {
                        newPos = moveTowards(i, j, preyPos);
                        if (grid[newPos[0]][newPos[1]] == 1) {
                            grid[newPos[0]][newPos[1]] = 2;  // Predator eats prey and moves
                            grid[i][j] = 0;
                            stepsWithoutFood[newPos[0]][newPos[1]] = 0;  // Reset steps without food
                        }
                    } else {
                        newPos = getAdjacent(i, j);
                    }
                    if (grid[newPos[0]][newPos[1]] == 0) {
                        grid[newPos[0]][newPos[1]] = 2;
                        stepsWithoutFood[newPos[0]][newPos[1]] = stepsWithoutFood[i][j];
                        grid[i][j] = 0;  // Empty the old position
                    }
                }
            }
        }
    }
    static void killPrey() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 1) {
                    int[] pos1 = getAdjacent(i,j);
                    int[] pos2 = getAdjacent(i,j);
                    while (pos1 == pos2) {
                        pos2 = getAdjacent(i,j);
                    }
                    //prey dies if not enough adjacent spots
                    if ((grid[pos1[0]][pos1[1]] == 1) && (grid[pos2[0]][pos2[1]] == 1)) {
                        grid[i][j] = 0;
                    }
                }
            }
        }
    }
    static void killPredator() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 2) {
                    stepsWithoutFood[i][j]++;
                    //predator dies if they dont get enough food
                    if (stepsWithoutFood[i][j] > MAX_STEPS_WITHOUT_PREY) {
                        grid[i][j] = 0;
                    }
                }
            }
        }
    }
    
    /**
     * find the closest prey from predator 
     * @param x
     * @param y
     * @return 
     */
    static int[] findClosestPrey (int x, int y) {
        for (int d = 1; d < GRID_SIZE / 2; d++) {
            for (int i = -d; i <= d; i++) {
                for (int j = -d; j <= d; j++) {
                    int newX = (x + i + GRID_SIZE) % GRID_SIZE;
                    int newY = (y + j + GRID_SIZE) % GRID_SIZE;
                    //check if prey is found
                    if (grid[newX][newY] == 1) {
                        int[] newPos = {newX, newY};
                        return newPos;
                    }
                }
            }
        }
        //if no prey found
        return null;
    }
    static void reproduce() {
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                //generate random double to determine whether to reproduce prey
                if (grid[i][j] == 1 && r.nextDouble() < PREY_REPRODUCTION_RATE) {
                    int[] emptyPos = getAdjacent(i, j);
                    if (grid[emptyPos[0]][emptyPos[1]] == 0 && isNextToFood(emptyPos[0], emptyPos[1])) {
                        grid[emptyPos[0]][emptyPos[1]] = 1;
                    }
                }
                //generate random double to determine whether to reproduce predator
                if (grid[i][j] == 2 && stepsWithoutFood[i][j] == 0 && r.nextDouble() < PREDATOR_REPRODUCTION_RATE) {
                    int[] emptyPos = getAdjacent(i, j);
                    if (grid[emptyPos[0]][emptyPos[1]] == 0) {
                        grid[emptyPos[0]][emptyPos[1]] = 2;
                    }
                }
            }
        }
    }
    /**
     * check if there is a food in an adjacent cell
     * @param x
     * @param y
     * @return 
     */
    static boolean isNextToFood(int x, int y) {
        int[] dx = {1, -1, 0, 0};
        int[] dy = {0, 0, 1, -1};
        //iterate through each adjacent
        for (int d = 0; d < 4; d++) {
            int newX = (x + dx[d] + GRID_SIZE) % GRID_SIZE;
            int newY = (y + dy[d] + GRID_SIZE) % GRID_SIZE;
            //check if it's a food cell
            if (grid[newX][newY] == 3) {  
                grid[newX][newY] = 0;
                return true;
            }
        }
        return false;
    }
    public static void main(String[] args) {
            initializeGrid();
            run(100);
    }  
    /**
     * Run simulation
     * @param rounds 
     */
    static void run(int rounds) {
        displayGrid(grid);
        for (int round = 0; round < rounds; round++) {
            movePrey();
            movePredator(); 
            killPredator();
            killPrey();
            reproduce();
            displayGrid(grid);
        }
        
    }
    static void displayGrid(int[][] grid) {
        // Iterate through each row and column of the grid
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                System.out.print(grid[i][j] + " "); 
            }
            System.out.println();
        }
        System.out.println();
    }
    public void draw(int x, int y, Graphics g, Color c) {
        g.setColor(c);
        g.fillRect(x, y, 50, 50);
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
//        if (img1 != null) {
//            g.drawImage(img1, x, y, this);
//        }
        //draw horizontal lines
//        for (int i = 0; i < 600; i=i+50) {
//            g.drawLine(0, i, 800, i);
//        }
//        //draw vertical lines
//        for (int i = 0; i < 800; i=i+50) {
//            g.drawLine(i, 0, i, 600);
//        }
        
//        g.setColor(Color.RED);
//        g.fillRect(x, y, 50, 50);
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                // Set the color based on cell type
                if (grid[i][j] == 0) {
                    g.setColor(Color.WHITE); // Empty cell
                } else if (grid[i][j] == 1) {
                    g.setColor(Color.GREEN); // Prey
                } else if (grid[i][j] == 2) {
                    g.setColor(Color.RED); // Predator
                }

                // Draw the cell as a rectangle
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                // Draw grid lines
                g.setColor(Color.BLACK);
                g.drawRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        lineX = 0;
    }//GEN-LAST:event_formComponentShown


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    /**
     * This event captures a click which is defined as pressing and releasing in
     * the same area
     *
     * @param me
     */
    public void mouseClicked(MouseEvent me) {
        System.out.println("Click: " + me.getX() + ":" + me.getY());
        x = 5;
        y = 5;
    }

    /**
     * When the mountain is pressed
     *
     * @param me
     */
    public void mousePressed(MouseEvent me) {
        System.out.println("Press: " + me.getX() + ":" + me.getY());
    }

    /**
     * When the mouse button is released
     *
     * @param me
     */
    public void mouseReleased(MouseEvent me) {
        System.out.println("Release: " + me.getX() + ":" + me.getY());
    }

    /**
     * When the mouse enters the area
     *
     * @param me
     */
    public void mouseEntered(MouseEvent me) {
        System.out.println("Enter: " + me.getX() + ":" + me.getY());
    }

    /**
     * When the mouse exits the panel
     *
     * @param me
     */
    public void mouseExited(MouseEvent me) {
        System.out.println("Exit: " + me.getX() + ":" + me.getY());
    }

    /**
     * Everything inside here happens when you click on a captured key.
     */
//    private class Move extends AbstractAction {
//
//        String key;
//
//        public Move(String akey) {
//            key = akey;
//        }
//
//        public void actionPerformed(ActionEvent ae) {
//            // here you decide what you want to happen if a particular key is pressed
//            System.out.println("llll" + key);
//            switch(key){
//                case "d": x+=2; break;
//                case "x": animTimer.stop(); switcher.switchToCard(EndPanel.CARD_NAME); break;
//            }
//            if (key.equals("d")) {
//                x = x + 2;
//            }
//            
//        }
//
//    }

    /**
     * Everything inside this actionPerformed will happen every time the
     * animation timer clicks.
     */
    private class AnimTimerTick implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            //the stuff we want to change every clock tick
//            lineX++;
            //force redraw
            repaint();
        }
    }
}
