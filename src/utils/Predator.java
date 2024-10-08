///*
// * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
// * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
// */
//package utils;
//import java.awt.Graphics;
//import java.awt.Color;
//
///**
// *
// * @author sophia
// */
//public class Predator extends Main {
//    private int x,y;
//    private int sideLength;
//    private int speedX;
//    private int speedY;
//    private int STEP = 50;
//    
//    public Predator(int x, int y, int sideLength) {
//        this.x = x;
//        this.y = y;
//        this.sideLength = sideLength;
//        
//        this.speedX = STEP;
//        this.speedY = STEP;
//    }
//    
//    public void move() {
//        for (int i = 0; i < GRID_SIZE; i++) {
//            for (int j = 0; j < GRID_SIZE; j++) {
//                //if predator is found
//                if (grid[i][j] == 2) {
//                    int[] preyPos = findClosestPrey(i, j);
//                    int[] newPos;
//                    if (preyPos != null) {
//                        newPos = moveTowards(i, j, preyPos);
//                        if (grid[newPos[0]][newPos[1]] == 1) {
//                            grid[newPos[0]][newPos[1]] = 2;  // Predator eats prey and moves
//                            grid[i][j] = 0;
//                            stepsWithoutFood[newPos[0]][newPos[1]] = 0;  // Reset steps without food
//                        }
//                    } else {
//                        newPos = getAdjacent(i, j);
//                    }
//                    if (grid[newPos[0]][newPos[1]] == 0) {
//                        grid[newPos[0]][newPos[1]] = 2;
//                        stepsWithoutFood[newPos[0]][newPos[1]] = stepsWithoutFood[i][j];
//                        grid[i][j] = 0;  // Empty the old position
//                    }
//                }
//            }
//        }
//    }
//    public int[] moveTowards(int x, int y, int[] target) {
//        //find difference between original and target coordinates
//        int dx = target[0] - x;
//        int dy = target[1] - y;
//
//        // Adjust for wrap-around on a toroidal grid
//        if (dx > GRID_SIZE / 2) {
//            dx -= GRID_SIZE;
//        }
//        if (dx < -GRID_SIZE / 2) {
//            dx += GRID_SIZE;
//        }
//        if (dy > GRID_SIZE / 2) {
//            dy -= GRID_SIZE;
//        }
//        if (dy < -GRID_SIZE / 2) {
//            dy += GRID_SIZE;
//        }
//
//        // Determine movement direction
//        int moveX = 0;
//        if (dx > 0) {
//            moveX = 1;
//        } else if (dx < 0) {
//            moveX = -1;
//        }
//
//        int moveY = 0;
//        if (dy > 0) {
//            moveY = 1;
//        }
//        else if (dy < 0) moveY = -1;
//
//        // Calculate new position with wrap-around
//        int newX = (x + moveX + GRID_SIZE) % GRID_SIZE;
//        int newY = (y + moveY + GRID_SIZE) % GRID_SIZE;
//
//        int[] newPos = {newX, newY};
//        return newPos;
//    }
//    public void kill() {
//        for (int i = 0; i < GRID_SIZE; i++) {
//            for (int j = 0; j < GRID_SIZE; j++) {
//                if (grid[i][j] == 2) {
//                    stepsWithoutFood[i][j]++;
//                    //predator dies if they dont get enough food
//                    if (stepsWithoutFood[i][j] > MAX_STEPS_WITHOUT_PREY) {
//                        grid[i][j] = 0;
//                    }
//                }
//            }
//        }
//    }
//    public void reproduce(int i, int j) {
//        if (grid[i][j] == 2 && stepsWithoutFood[i][j] <= 10 && r.nextDouble() < PREDATOR_REPRODUCTION_RATE) {
//            int[] emptyPos = getAdjacent(i, j);
//            if (grid[emptyPos[0]][emptyPos[1]] == 0) {
//                grid[emptyPos[0]][emptyPos[1]] = 2;
//            }
//        }
//    }
//    
//    
//}
