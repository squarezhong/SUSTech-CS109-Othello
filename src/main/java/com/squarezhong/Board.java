package com.squarezhong;

import frame.board.BaseBoard;
// import frame.board.BaseGrid;

public class Board extends BaseBoard {
    public Board(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        for (int i = 0; i < getWidth(); i++) {
            for (int j = 0; j < getHeight(); j++) {
                grids[i][j] = new Grid(i, j);
            }
        }

        grids[3][3].setOwnedPiece(new Piece(3, 3, Color.WHITE));
        grids[4][4].setOwnedPiece(new Piece(4, 4, Color.WHITE));
        grids[3][4].setOwnedPiece(new Piece(3, 4, Color.BLACK));
        grids[4][3].setOwnedPiece(new Piece(4, 3, Color.BLACK));
    }
}