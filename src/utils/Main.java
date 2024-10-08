/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import java.awt.Color;
import java.awt.Graphics;
import java.util.*;
import utils.*;

/**
 *
 * @author sophia
 */
public class Main {
    //initialize variables
    static final int GRID_SIZE = 20;
    static final int PREY_COUNT = 100;
    static final int PREDATOR_COUNT = 10;
    static final int FOOD_COUNT = 50;
    
    static final int MAX_STEPS_WITHOUT_PREY = 20;
    static final double PREY_REPRODUCTION_RATE = 0.10;
    static final double PREDATOR_REPRODUCTION_RATE = 0.05;
    
    //initialize grid
    static int [][] grid = new int[GRID_SIZE][GRID_SIZE];
    static int[][] stepsWithoutFood = new int[GRID_SIZE][GRID_SIZE];
    
    static Random r = new Random();
   
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
            run(50);
    }  
    /**
     * Run simulation
     * @param rounds 
     */
    static void run(int rounds) {
        displayGrid(grid);
        for (int round = 0; round < rounds; round++) {
            movePrey();
            //movePredator(); 
            //killPredator();
            killPrey();
            //reproduce();
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
}
