package com.squarezhong;

import java.util.ArrayList;
import java.util.HashMap;

import frame.Controller.Game;
import frame.board.BaseGrid;

/**
 * check the condition of the board
 */
public class Inspection {
    private static int[][] directions = new int[][] {
            { 0, 1 }, { 1, 0 }, { 0, -1 }, { -1, 0 },
            { 1, 1 }, { 1, -1 }, { -1, 1 }, { -1, -1 }
    };

    // last flipped piece
    private static ArrayList<int[]> lastFlipped = new ArrayList<>();

  
    /**
     * try to set the piece
     * return the number of pieces that are flipped
     * if count == 0, the move is invalid
     * @param x     x coordinate
     * @param y     y coordinate
     * @param color color of the piece
     * @param flip  whether to record the last flipped pieces,
     * when truly setting the piece, record the last flipped pieces,
     * when checking the possible moves, do not record the last flipped pieces
     * @return the number of pieces that are flipped
     */
    public static int trySetPiece(int x, int y, Color color, boolean flip) {
        int countFlipped = 0;
        // clear the last flipped pieces
        if (flip) {
            lastFlipped.clear();
        }

        if (Game.getBoard().getGrid(x, y).hasPiece()) {
            return 0;
        }
        
        for (int[] direction : directions) {
            int count = 0;
            int dx = direction[0];
            int dy = direction[1];
            int i = x + dx;
            int j = y + dy;
            while (i >= 0 && i < Game.getWidth() && j >= 0 && j < Game.getHeight()) {
                BaseGrid grid = Game.getBoard().getGrid(i, j);
                if (!grid.hasPiece()) {
                    break;
                }
                if (((Piece) grid.getOwnedPiece()).getColor() == color) {
                    if (count > 0) {
                        if (flip) {
                            for (int k = 1; k <= count; k++) {
                                lastFlipped.add(new int[] { x + k * dx, y + k * dy });
                            }
                        }
                        countFlipped += count;
                        count = 0;
                        break;
                    } else {
                        break;
                    }
                } else {
                    count++;
                    i += dx;
                    j += dy;
                }
            }
        }

        return countFlipped;
    }

    /**
     * turn over the pieces based on the last flipped pieces
     * @param x x coordinate
     * @param y y coordinate
     * @param color color of the piece
     * @return true if the pieces are turned over successfully
     */
    public static boolean turnPieces(int x, int y, Color color) {
        for (int[] last : lastFlipped) {
            BaseGrid lastGrid = Game.getBoard().getGrid(last[0], last[1]);
            lastGrid.removeOwnedPiece();
            lastGrid.setOwnedPiece(new Piece(last[0], last[1], color));
        }

        return true;
    }

    /**
     * get all the possible moves for the player
     * @param color color of the player
     * @return a hashmap of possible moves and the number of pieces that are flipped
     */
    public static HashMap<int[], Integer> getPossibleMoves(Color color) {
        HashMap<int[], Integer> possibleMoves = new HashMap<>();
        for (int i = 0; i < Game.getWidth(); i++) {
            for (int j = 0; j < Game.getHeight(); j++) {
                int count = trySetPiece(i, j, color, false);
                if (count > 0) {
                    possibleMoves.put(new int[] { i, j }, count);
                }
            }
        }
        return possibleMoves;
    }

    /**
     * judge the winner
     * @return 0 if player 1 wins, 1 if player 2 wins, -1 if it is a tie
     */
    public static int judgeWinner() {
        int[] count = new int[2];
        for (int i = 0; i < Game.getWidth(); i++) {
            for (int j = 0; j < Game.getHeight(); j++) {
                BaseGrid grid = Game.getBoard().getGrid(i, j);
                if (grid.hasPiece()) {
                    count[((Piece) grid.getOwnedPiece()).getColor().ordinal()]++;
                }
            }
        }
        if (count[0] > count[1]) {
            return 0;
        } else if (count[0] < count[1]) {
            return 1;
        } else {
            return -1;
        }
    }

    public static ArrayList<int[]> getLastFlipped() {
        return lastFlipped;
    }
}
