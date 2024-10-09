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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
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
    
    static int speed = 1000;
    
    //initialize grid
    static int [][] grid = new int[GRID_SIZE][GRID_SIZE];
    static int[][] stepsWithoutFood = new int[GRID_SIZE][GRID_SIZE];
    
    static Random r = new Random();
    private JLabel predatorCountLabel;
    private JLabel preyCountLabel;
    
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
        
        initializeGrid();
        
        //create and start a Timer for animation
        animTimer = new Timer(speed, new AnimTimerTick());
        
        //predator & prey label
        predatorCountLabel = new JLabel("Predators: ");
        preyCountLabel = new JLabel("Prey: ");
        
        predatorCountLabel.setBounds(650, 500, 150, 30); // Position of predator label
        preyCountLabel.setBounds(650, 520, 150, 30); // Position of prey label
        
        add(predatorCountLabel);
        add(preyCountLabel);
    }
    
    /**
     * create grid & fill with predator, prey & food
     */
    public static void initializeGrid() {
        //fill empty spaces
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                grid[i][j] = 0;
            }
        }
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
                    //eat food
                    else if (grid[newPos[0]][newPos[1]] == 3) {
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
        else if (dy < 0) moveY = -1;

        //calculate new position with wrap-around
        int newX = (x + moveX + GRID_SIZE) % GRID_SIZE;
        int newY = (y + moveY + GRID_SIZE) % GRID_SIZE;
        
        int [] newPos = {newX, newY};
        return newPos;
    }
    
    /**
     * moves predator towards nearest prey
     */
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
                            grid[newPos[0]][newPos[1]] = 2;  //predator eats prey and moves
                            grid[i][j] = 0;
                            stepsWithoutFood[newPos[0]][newPos[1]] = 0;  //reset steps without food
                        }
                    } else {
                        newPos = getAdjacent(i, j);
                    }
                    if (grid[newPos[0]][newPos[1]] == 0) {
                        grid[newPos[0]][newPos[1]] = 2;
                        stepsWithoutFood[newPos[0]][newPos[1]] = stepsWithoutFood[i][j];
                        grid[i][j] = 0;  //empty the old position
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
    /**
     * reproduce predator & prey
     */
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
            
            //eat food
            if (grid[newX][newY] == 3) {  
                grid[newX][newY] = 1;
                return true;
            }
        }
        return false;
    }

    public void countPredatorsAndPrey() {
        int predatorCount = 0;
        int preyCount = 0;

        //loop through the grid to count
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (grid[i][j] == 1) {
                    preyCount++; 
                }
                else if (grid[i][j] == 2) {
                    predatorCount++;
                }
            }
        }
        //display predator prey count
        predatorCountLabel.setText("Predators: " + predatorCount);
        preyCountLabel.setText("Prey: " + preyCount);

    }
    private void generatePrey(int mouseX, int mouseY) {
        
        //convert the mouse coordinates to grid coordinates
        int gridX = mouseX / CELL_SIZE;
        int gridY = mouseY / CELL_SIZE;

        //set the grid cell to prey
        if (gridX >= 0 && gridX < GRID_SIZE && gridY >= 0 && gridY < GRID_SIZE) {
            grid[gridX][gridY] = 1;
        }

        repaint();
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                //set the color based on cell type
                if (grid[i][j] == 0) {
                    g.setColor(Color.WHITE); //empty cell
                } else if (grid[i][j] == 1) {
                    g.setColor(Color.GREEN); //prey
                } else if (grid[i][j] == 2) {
                    g.setColor(Color.RED); //predator
                } else if (grid[i][j] == 3) {
                    g.setColor(Color.BLACK); //food
                }

                //draw the cell as a rectangle
                g.fillRect(j * CELL_SIZE, i * CELL_SIZE, CELL_SIZE, CELL_SIZE);

                //draw grid lines
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

        startButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        changeSpeed = new javax.swing.JSlider();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });

        startButton.setText("Start");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        changeSpeed.setMaximum(2000);
        changeSpeed.setValue(1000);
        changeSpeed.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                changeSpeedStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap(606, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(resetButton)
                        .addGap(75, 75, 75))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(changeSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, 162, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(startButton)
                                .addGap(18, 18, 18)
                                .addComponent(stopButton)))
                        .addGap(32, 32, 32))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(startButton)
                    .addComponent(stopButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(resetButton)
                .addGap(18, 18, 18)
                .addComponent(changeSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(472, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        lineX = 0;
    }//GEN-LAST:event_formComponentShown

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        animTimer.start();
    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        animTimer.stop();
    }//GEN-LAST:event_stopButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        initializeGrid();
    }//GEN-LAST:event_resetButtonActionPerformed

    private void changeSpeedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_changeSpeedStateChanged
        speed = changeSpeed.getValue();
        animTimer = new Timer(speed, new AnimTimerTick());
    }//GEN-LAST:event_changeSpeedStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSlider changeSpeed;
    private javax.swing.JButton resetButton;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables

    /**
     * This event captures a click which is defined as pressing and releasing in
     * the same area
     *
     * @param me
     */
    public void mouseClicked(MouseEvent me) {
        System.out.println("Click: " + me.getX() + ":" + me.getY());
        generatePrey(me.getX(), me.getY());
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
     * Everything inside this actionPerformed will happen every time the
     * animation timer clicks.
     */
    private class AnimTimerTick implements ActionListener {

        public void actionPerformed(ActionEvent ae) {
            //run simulation 
            movePrey();
            movePredator(); 
            killPredator();
            killPrey();
            reproduce();
            countPredatorsAndPrey();
            
            //force redraw
            repaint();
        }
    }
}
